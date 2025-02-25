package auth

import auth.{AuthError, SecretKey, SessionConfig, key, given}
import dmscreen.{*, given}
import auth.UserId
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import pdi.jwt.exceptions.JwtExpirationException
import zio.*
import zio.http.{Cookie, Handler, HandlerAspect, Header, Request, Response, URL}
import zio.json.*

import java.time.Instant

object CookieAndBearerSessionTransport {

  def live: URLayer[SessionConfig, CookieAndBearerSessionTransport] =
    ZLayer.fromZIO(
      for {
        config          <- ZIO.service[SessionConfig]
        invalidSessions <- Ref.make(Map.empty[UserId, Instant])
        transport = CookieAndBearerSessionTransport(config, invalidSessions)
        _ <- transport.cleanUp.repeat(Schedule.fixed(1.hour)).forkDaemon
      } yield transport
    )

}

case class CookieAndBearerSessionTransport(
  config:          SessionConfig,
  invalidSessions: Ref[Map[UserId, Instant]]
) extends SessionTransport[DMScreenSession] {

  private def jwtEncode(
    session: DMScreenSession,
    ttl:     Duration
  ): UIO[TokenString] =
    for {
      now <- Clock.instant
    } yield {
      val claim = JwtClaim(session.toJson)
        .issuedAt(now.getEpochSecond)
        .expiresAt(now.getEpochSecond + ttl.toSeconds)
      TokenString(Jwt.encode(claim, config.secretKey.key, JwtAlgorithm.HS512))
    }

  private def jwtDecode(token: String): Task[JwtClaim] =
    for {
      tok <- ZIO.fromTry(Jwt.decode(token, config.secretKey.key, Seq(JwtAlgorithm.HS512)))
    } yield tok

  extension (claim: JwtClaim) {

    def session: ZIO[Any, AuthError, DMScreenSession] =
      ZIO.fromEither(claim.content.fromJson[DMScreenSession]).mapError(AuthError(_))

  }

  // Handler aspect that runs on every api call, it checks for the presence of a bearer token
  // and either injects the session into the calls, or redirects as needed
  override def apiSessionProvider: HandlerAspect[Any, DMScreenSession] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] { request =>
      if (request.path.toString.startsWith("/unauth")) {
        ZIO.succeed((request, DMScreenSession.empty)) // No authentication needed
      } else {
        for {
          refreshURL <- ZIO.fromEither(URL.decode("/refresh")).orDie
          sessionOpt <- (request.header(Header.Authorization) match {
            // We got a bearer token, let's decode it
            case Some(Header.Authorization.Bearer(token)) =>
              for {
                claim <- jwtDecode(token.value.asString)
                u     <- claim.session
              } yield Some(u)
            case _ => ZIO.none
          }).catchAll {
            case e: JwtExpirationException =>
              ZIO.logInfo(s"Expired access token: ${e.getMessage}") *> ZIO.fail(Response.seeOther(refreshURL))
            case e: Throwable =>
              ZIO.fail(Response.badRequest(e.getMessage))
          }
          session <- sessionOpt
            .fold(ZIO.logInfo(s"No access token at all") *> ZIO.fail(Response.seeOther(refreshURL)))(ZIO.succeed)
          _ <- ZIO.fail(Response.unauthorized).whenZIO(invalidSessions.get.map(_.contains(session.user.id)))
        } yield (request, session)
      }
    })

  // Handler aspect that runs on every resource call, it checks for the presence of a request cookie
  // and either injects the session into the calls, or redirects as needed
  override def resourceSessionProvider: HandlerAspect[Any, DMScreenSession] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] { request =>
      if (request.path.toString.startsWith("/unauth")) {
        ZIO.succeed((request, DMScreenSession.empty)) // No authentication needed
      } else {
        val sessionCookieOpt = request.cookies.find(_.name == config.accessTokenName).map(_.content)
        val loginURL = URL.decode("/loginForm").toOption.get
        (for {
          claim      <- ZIO.foreach(sessionCookieOpt)(jwtDecode)
          sessionOpt <- ZIO.foreach(claim)(_.session)
          session <- sessionOpt
            .fold(ZIO.logInfo(s"No refresh token at all") *> ZIO.fail(Response.seeOther(loginURL)))(ZIO.succeed)
          _ <- ZIO.fail(Response.unauthorized).whenZIO(invalidSessions.get.map(_.contains(session.user.id)))
        } yield (request, session)).catchAll {
          case e: JwtExpirationException =>
            ZIO.logInfo(s"Expired refresh token: ${e.getMessage}") *> ZIO.fail(Response.seeOther(loginURL))
          case e: Throwable =>
            ZIO.fail(Response.badRequest(e.getMessage))
          case response: Response => ZIO.fail(response)
        }

      }

    })

  override def invalidateSession(
    session:  DMScreenSession,
    response: Response
  ): UIO[Response] = {
    // All invalidation (logout) happens in the client, we don't add the session to the invalidSessions because the user may still have a valid session elsewhere.
    // currently invalidSessions is not used at all, it requires an admin screen to kick people out
    ZIO.succeed(response)
  }

  override def cleanUp: UIO[Unit] =
    for {
      now <- Clock.instant
      _   <- invalidSessions.update(_.filter(_._2.isAfter(now)))
    } yield ()

  override def refreshSession(
    request:  Request,
    response: Response
  ): UIO[Response] = {
    val sessionCookieOpt = request.cookies.find(_.name == config.accessTokenName).map(_.content)
    (for {
      // Check to see if the refresh token exists AND is valid, otherwise you can't refresh
      session <- (for {
        claim      <- ZIO.foreach(sessionCookieOpt)(jwtDecode)
        sessionOpt <- ZIO.foreach(claim)(_.session)
        session <- sessionOpt
          .fold(ZIO.logInfo(s"No refresh token at all") *> ZIO.fail(Response.unauthorized))(ZIO.succeed)
        _ <- ZIO.fail(Response.unauthorized).whenZIO(invalidSessions.get.map(_.contains(session.user.id)))
      } yield session).catchAll {
        case e: JwtExpirationException =>
          ZIO.logInfo(s"Expired refresh token: ${e.getMessage}") *> ZIO.fail(Response.unauthorized)
        case e:        Throwable => ZIO.fail(Response.badRequest(e.getMessage))
        case response: Response  => ZIO.fail(response)
      }
      accessToken  <- jwtEncode(session, config.accessTTL)
      refreshToken <- jwtEncode(session, config.refreshTTL)
    } yield response
      .addHeader(Header.Authorization.Bearer(accessToken.str))
      .addCookie(
        Cookie.Response(
          name = config.refreshTokenName,
          content = refreshToken.str,
          maxAge = Option(config.refreshTTL.plus(1.hour)),
          domain = config.sessionDomain,
          path = config.sessionPath,
          isSecure = config.sessionIsSecure,
          isHttpOnly = config.sessionIsHttpOnly,
          sameSite = config.sessionSameSite
        )
      )).catchAll(ZIO.succeed)
  }

}
