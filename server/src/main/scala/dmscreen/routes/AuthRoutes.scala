/*
 * Copyright (c) 2024 Roberto Leibman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dmscreen.routes

import auth.{UserRepository, *, given}
import dmscreen.db.RepositoryError
import dmscreen.mail.{EmailGenerator, Postman}
import dmscreen.{DMScreenError, DMScreenServerEnvironment, DMScreenSession, DMScreenTask}
import dmscreen.util.*
import zio.http.*
import zio.json.*
import zio.json.ast.Json
import zio.logging.*
import zio.prelude.NonEmptyList
import zio.{Clock, IO, ZIO}

import java.time.Instant
import java.util.Locale
import scala.language.unsafeNulls

object AuthRoutes {

  private def seeOther(location: String): IO[DMScreenError, Response] =
    for {
      url <- ZIO.fromEither(URL.decode(location)).mapError(e => DMScreenError(e))
    } yield Response(Status.SeeOther, Headers(Header.Location(url)))

  def json(value: Json): Response = Response.json(value.toString)

  def json[A: JsonEncoder](value: A): Response = Response.json(value.toJson)

  given localeEncoder: JsonEncoder[Locale] = JsonEncoder.string.contramap(_.toString)

  // TODO get from config
  lazy val availableLocales: NonEmptyList[Locale] =
    NonEmptyList("es_MX", "es", "en-US", "en", "eo").map(t => Locale.forLanguageTag(t).nn)

  val url: String = "auth"

  def getPK(obj: User): UserId = obj.id

  def authOther: Routes[DMScreenServerEnvironment & DMScreenSession, Throwable] =
    Routes(
      Method.GET / "api" / "auth" / "isFirstLoginToday" -> handler { (_: Request) =>
        for {
          userOps       <- ZIO.service[UserRepository[DMScreenTask]]
          now           <- Clock.localDateTime
          firstLoginOpt <- userOps.firstLogin
        } yield json(firstLoginOpt.fold(true)(_.isAfter(now.minus(java.time.Duration.ofDays(1)))))
      },
      Method.GET / "api" / "auth" / "whoami" -> handler { (_: Request) =>
        ZIO.serviceWith[DMScreenSession](session => json(session.user))
      },
      Method.POST / "api" / "auth" / "changePassword" -> handler { (req: Request) =>
        for {
          userOps <- ZIO.service[UserRepository[DMScreenTask]]
          session <- ZIO.service[DMScreenSession]
          newPass <- req.body.asString
          ret     <- userOps.changePassword(session.user, newPass)
        } yield json(ret)
      },
      Method.GET / "api" / "auth" / "refreshToken" -> handler { (_: Request) =>
        for {
          session          <- ZIO.service[DMScreenSession]
          sessionTransport <- ZIO.service[SessionTransport[DMScreenSession]]
          response         <- sessionTransport.refreshSession(session, json(true))
        } yield response
      },
      Method.GET / "api" / "auth" / "doLogout" -> handler { (_: Request) =>
        for {
          session          <- ZIO.service[DMScreenSession]
          sessionTransport <- ZIO.service[SessionTransport[DMScreenSession]]
          loginFormURL     <- ZIO.fromEither(URL.decode("/loginForm")).mapError(DMScreenError(_))
          response <- sessionTransport
            .invalidateSession(session, Response(Status.SeeOther, Headers(Header.Location(loginFormURL))))
        } yield response
      }
    )

  def unauthRoute: Routes[DMScreenServerEnvironment, Throwable] =
    Routes(
      Method.GET / "serverVersion" -> handler {
        Response.json(BuildInfo.version)
      },
      Method.POST / "passwordReset" -> handler { (req: Request) =>
        (for {
          formData <- req.formData
          token = formData.get("token").map(TokenString.apply)
          password = formData.get("password")
          _ <- ZIO.fail(SessionError("You need to pass a token and password")).when(token.isEmpty || password.isEmpty)
          userOps     <- ZIO.service[UserRepository[DMScreenTask]]
          tokenHolder <- ZIO.service[TokenHolder[DMScreenTask]]
          userOpt <- ZIO
            .foreach(token)(tok => tokenHolder.validateToken(token.get, TokenPurpose.LostPassword)).map(_.flatten)
          passwordChanged            <- ZIO.foreach(userOpt)(user => userOps.changePassword(user, password.get))
          passwordChangeSucceededUrl <- seeOther("/loginForm?passwordChangeSucceeded")
          passwordChangeFailedUrl    <- seeOther("/loginForm?passwordChangeFailed")
        } yield passwordChanged.fold(passwordChangeFailedUrl)(isChanged =>
          if (isChanged) passwordChangeSucceededUrl else passwordChangeFailedUrl
        ))
          .provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)
      },
      Method.POST / "passwordRecoveryRequest" -> handler { (req: Request) =>
        (for {
          emailJson <- req.body.asString
          email     <- ZIO.fromEither(emailJson.fromJson[String]).mapError(DMScreenError(_))
          userOps   <- ZIO.service[UserRepository[DMScreenTask]]
          postman   <- ZIO.service[Postman]
          userOpt   <- userOps.userByEmail(email)
          _ <-
            ZIO
              .foreach(userOpt)(EmailGenerator.lostPasswordEmail(_, "")).flatMap(envelope =>
                ZIO.foreach(envelope)(postman.deliver)
              ).forkDaemon
        } yield Response.ok)
          .provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)
      },
      Method.PUT / "userCreation" -> handler { (req: Request) =>
        (for {
          postman             <- ZIO.service[Postman]
          userOps             <- ZIO.service[UserRepository[DMScreenTask]]
          userCreationRequest <- req.bodyAs[UserCreationRequest]
          validate <- ZIO.succeed( // TODO Use cats validate instead of option
            if (userCreationRequest.user.email.trim.nn.isEmpty)
              Option("User Email cannot be empty")
            else if (userCreationRequest.user.name.trim.nn.isEmpty)
              Option("User Name cannot be empty")
            else if (userCreationRequest.password.trim.nn.isEmpty || userCreationRequest.password.trim.nn.length < 3)
              Option("Password is invalid")
            else if (userCreationRequest.user.id.nonEmpty)
              Option("You can't register an existing user")
            else
              None
          )
          exists <- userOps.userByEmail(userCreationRequest.user.email).map(_.nonEmpty)
          saved <-
            if (validate.nonEmpty || exists)
              ZIO.none
            else
              userOps.upsert(userCreationRequest.user.copy(active = false)).map(Option(_))
          _ <- ZIO.foreachDiscard(saved)(userOps.changePassword(_, userCreationRequest.password))
          _ <- ZIO.logInfo("About to send")
          _ <- ZIO
            .foreach(saved)(EmailGenerator.registrationEmail(_, ""))
            .flatMap(envelope => ZIO.foreach(envelope)(postman.deliver))
            .forkDaemon
          _ <- ZIO.logInfo("Maybe sent")
        } yield {
          if (exists)
            json(UserCreationResponse(Option("A user with that email already exists")))
          else
            json(UserCreationResponse(validate))
        }).provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)
      },
      Method.GET / "confirmRegistration" -> handler { (req: Request) =>
        (for {
          formData <- req.formData
          token <- ZIO
            .fromOption(formData.get("token")).mapBoth(_ => SessionError("Token not found"), TokenString.apply)
          userOps                  <- ZIO.service[UserRepository[DMScreenTask]]
          tokenHolder              <- ZIO.service[TokenHolder[DMScreenTask]]
          user                     <- tokenHolder.validateToken(token, TokenPurpose.NewUser)
          activate                 <- ZIO.foreach(user)(user => userOps.upsert(user.copy(active = true)))
          registrationFailedUrl    <- seeOther("/loginForm?registrationFailed")
          registrationSucceededUrl <- seeOther("/loginForm?registrationSucceeded")
        } yield activate.fold(registrationFailedUrl)(_ => registrationSucceededUrl))
          .provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)
      },
      Method.POST / "doLogin" -> handler { (req: Request) =>
        for {
          formData <- req.formData
          email <-
            ZIO.fromOption(formData.get("email")).orElseFail(SessionError("Missing email"))
          password <- ZIO.fromOption(formData.get("password")).orElseFail(SessionError("Missing Password"))

          userOps <- ZIO.service[UserRepository[DMScreenTask]]
          login   <- userOps.login(email, password).provideLayer(DMScreenSession.adminSession.toLayer)
          _       <- login.fold(ZIO.logDebug(s"Bad login for $email"))(_ => ZIO.logDebug(s"Good Login for $email"))
          sessionTransport <- ZIO.service[SessionTransport[DMScreenSession]]
          rootURL          <- ZIO.fromEither(URL.decode("/")).mapError(DMScreenError(_))
          loginFormBadUrl  <- ZIO.fromEither(URL.decode("/loginForm?bad=true")).mapError(DMScreenError(_))
          res <- login.fold(ZIO.succeed(Response(Status.SeeOther, Headers(Header.Location(loginFormBadUrl))))) { user =>
            sessionTransport.refreshSession(
              DMScreenSession(user),
              Response(
                Status.SeeOther,
                Headers(Header.Location(rootURL), Header.ContentType(MediaType.text.plain))
              )
            )
          }
        } yield res
      }
    )

  lazy private val authCRUD: Routes[DMScreenSession & DMScreenServerEnvironment, Throwable] =
    Routes(
      Method.POST / "api" / `url` -> handler { (req: Request) =>
        for {
          obj <- req.bodyAs[User]
          _   <- ZIO.logInfo(s"Upserting $url with $obj")
          res <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.upsert(obj))
        } yield Response.json(res.toJson)

      },
      Method.PUT / "api" / `url` -> handler { (req: Request) =>
        for {
          obj <- req.bodyAs[User]
          _   <- ZIO.logInfo(s"Upserting $url with $obj")
          res <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.upsert(obj))
        } yield Response.json(res.toJson)

      },
      Method.POST / "api" / `url` / "search" -> handler { (req: Request) =>
        for {
          search <- req.bodyAs[UserSearch]
          res    <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.search(Some(search)))
        } yield Response.json(res.toJson)
      },
      Method.POST / s"api" / `url` / "count" -> handler { (req: Request) =>
        for {
          search <- req.bodyAs[UserSearch]
          res    <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.count(Some(search)))
        } yield Response.json(res.toJson)
      },
      Method.GET / "api" / `url` / int("pk") -> handler {
        (
          pk:  Int,
          req: Request
        ) =>
          for {
            res <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.get(UserId(pk)))
          } yield Response.json(res.toJson)
      },
      Method.DELETE / "api" / `url` / int("pk") -> handler {
        (
          pk:  Int,
          req: Request
        ) =>
          for {
            res <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.delete(UserId(pk)))
            _   <- ZIO.logInfo(s"Deleted ${pk.toString}")
          } yield Response.json(res.toJson)
      }
    )

  lazy val authRoute: Routes[DMScreenSession & DMScreenServerEnvironment, Throwable] =
    authOther ++ authCRUD

}
