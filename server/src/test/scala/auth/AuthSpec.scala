package auth

import dmscreen.{
  DMScreen,
  DMScreenError,
  DMScreenServerEnvironment,
  DMScreenSession,
  DMScreenTask,
  EnvironmentBuilder,
  given
}
import pdi.jwt.*
import zio.*
import zio.http.{Cookie, Header, *}
import zio.test.*
import zio.json.*

object AuthSpec extends ZIOSpec[DMScreenServerEnvironment] {

  override def bootstrap: ULayer[DMScreenServerEnvironment] = EnvironmentBuilder.withContainer

  val goodEmail = "validuser@example.com"
  val goodPassword = "correctpassword"

  extension (r: Response) {

    def setCookie(name: String): Option[String] = {
      r
        .headers(Header.SetCookie)
        .find(_.value.name == name)
        .map(_.value.content)
    }

  }

  def validToken: ZIO[DMScreenServerEnvironment, DMScreenError, TokenString] = {
    for {
      config <- ZIO.service[SessionConfig]
      user <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.userByEmail(goodEmail)).map(_.get) // This is a test, so we can use .get
      now       <- Clock.instant
      javaClock <- Clock.javaClock
    } yield {
      val claim = JwtClaim(DMScreenSession(user).toJson)
        .issuedNow(javaClock)
        .expiresIn(config.accessTTL.toSeconds)(javaClock)
      TokenString(Jwt.encode(claim, config.secretKey.key, JwtAlgorithm.HS512))
    }
  }.provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)

  def expiredToken: ZIO[DMScreenServerEnvironment, DMScreenError, TokenString] = {
    for {
      config <- ZIO.service[SessionConfig]
      user <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.userByEmail(goodEmail)).map(_.get) // This is a test, so we can use .get
      now       <- Clock.instant
      javaClock <- Clock.javaClock
    } yield {
      val claim = JwtClaim(DMScreenSession(user).toJson)
        .expiresNow(javaClock)
        .issuedNow(javaClock)
      TokenString(Jwt.encode(claim, config.secretKey.key, JwtAlgorithm.HS512))
    }
  }.provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)

  def doLogin(
    app:      Routes[DMScreenServerEnvironment, Nothing],
    email:    String,
    password: String
  ): URIO[DMScreenServerEnvironment, Response] =
    app.runZIO(
      Request
        .post(
          URL(Path("/unauth/doLogin")),
          Body.fromURLEncodedForm(
            Form(
              FormField.simpleField("email", email),
              FormField.simpleField("password", password)
            )
          )
        ).addHeader(Header.ContentType(MediaType.application.`x-www-form-urlencoded`))
    )

  def goodLogin(
    app: Routes[DMScreenServerEnvironment, Nothing]
  ): URIO[DMScreenServerEnvironment, (TokenString, TokenString)] = {
    for {
      response <- doLogin(app, goodEmail, goodPassword)
    } yield (
      TokenString(response.header(Header.Authorization).get.renderedValue.replace("Bearer ", "")),
      TokenString(response.setCookie("_refreshToken").get)
    )
  }

  override def spec: Spec[DMScreenServerEnvironment & TestEnvironment & Scope, Any] =
    suite("Auth")(
      test("simple user does not exist") {
        for {
          app      <- DMScreen.zapp
          response <- doLogin(app, "yoyo@meem.com", "blah")
          str      <- response.body.asString
        } yield assertTrue(
          response.status == Status.SeeOther,
          response.header(Header.Location).exists(_.renderedValue.contains("/loginForm?bad=true"))
        )
      },
      test("bad password") {
        for {
          app      <- DMScreen.zapp
          response <- doLogin(app, goodEmail, "badpassword")
          str      <- response.body.asString
        } yield assertTrue(
          response.status == Status.SeeOther,
          response.header(Header.Location).exists(_.renderedValue.contains("/loginForm?bad=true"))
        )
      },
      test("good login") {
        for {
          app      <- DMScreen.zapp
          response <- doLogin(app, goodEmail, goodPassword)
          authHeader = response.header(Header.Authorization)
          refreshCookie = response.setCookie("_refreshToken")
        } yield assertTrue(
          response.status == Status.Ok,
          authHeader.exists(_.renderedValue.startsWith("Bearer ")),
          refreshCookie.isDefined
        )
      },
      test("refresh token (valid access token)") {
        for {
          config     <- ZIO.service[SessionConfig]
          app        <- DMScreen.zapp
          validToken <- validToken
          response <- app.runZIO(
            Request
              .get(URL(Path("/unauth/refresh")))
              .addHeader(Header.Cookie(NonEmptyChunk(Cookie.Request(config.accessTokenName, validToken.str))))
          )
          authHeader = response.header(Header.Authorization)
          refreshCookie = response.setCookie("_refreshToken")
        } yield assertTrue(
          response.status == Status.Ok,
          authHeader.exists(_.renderedValue.startsWith("Bearer ")),
          refreshCookie.isDefined
        )
      },
      test("refresh token (expired access token)") {
        for {
          config       <- ZIO.service[SessionConfig]
          app          <- DMScreen.zapp
          expiredToken <- expiredToken
          response <- app.runZIO(
            Request
              .get(URL(Path("/unauth/refresh")))
              .addHeader(Header.Cookie(NonEmptyChunk(Cookie.Request(config.accessTokenName, expiredToken.str))))
          )
        } yield assertTrue(response.status == Status.Unauthorized)
      },
      test("refresh token (invalid access token)") {
        for {
          config <- ZIO.service[SessionConfig]
          app    <- DMScreen.zapp
          response <- app.runZIO(
            Request
              .get(URL(Path("/unauth/refresh")))
              .addHeader(Header.Cookie(NonEmptyChunk(Cookie.Request(config.accessTokenName, "invalidToken"))))
          )
        } yield assertTrue(response.status == Status.BadRequest)
      },
      test("Calling '/api/test' with no access token") {
        for {
          app      <- DMScreen.zapp
          response <- app.runZIO(Request.get(URL(Path("/api/test"))))
        } yield assertTrue(
          response.status == Status.SeeOther,
          response.header(Header.Location).exists(_.renderedValue.contains("/refresh"))
        )
      },
      test("Calling '/api/test' with an expired access token") {
        for {
          app          <- DMScreen.zapp
          expiredToken <- expiredToken
          response <- app.runZIO(
            Request.get(URL(Path("/api/test"))).addHeader(Header.Authorization.Bearer(expiredToken.str))
          )
        } yield assertTrue(
          response.status == Status.SeeOther,
          response.header(Header.Location).exists(_.renderedValue.contains("/refresh"))
        )
      },
      test("Calling '/api/test' with a valid access token") {
        for {
          app        <- DMScreen.zapp
          validToken <- validToken
          response <- app.runZIO(
            Request.get(URL(Path("/api/test"))).addHeader(Header.Authorization.Bearer(validToken.str))
          )
        } yield assertTrue(response.status == Status.Ok)
      },
      test("Calling '/unauth/unauthtest.html' with no access token") {
        for {
          app      <- DMScreen.zapp
          response <- app.runZIO(Request.get(URL(Path("/unauth/unauthtest.html"))))
        } yield assertTrue(response.status == Status.Ok)
      },
      test("Calling '/unauth/unauthtest.html' with an expired access token") {
        for {
          config       <- ZIO.service[SessionConfig]
          app          <- DMScreen.zapp
          expiredToken <- expiredToken
          response <- app.runZIO(
            Request
              .get(URL(Path("/unauth/unauthtest.html"))).addHeader(
                Header.Cookie(NonEmptyChunk(Cookie.Request(config.accessTokenName, expiredToken.str)))
              )
          )
        } yield assertTrue(response.status == Status.Ok)
      },
      test("Calling '/test.html' (restricted resource) with no access token") {
        for {
          config   <- ZIO.service[SessionConfig]
          app      <- DMScreen.zapp
          response <- app.runZIO(Request.get(URL(Path("/test.html"))))
        } yield assertTrue(
          response.status == Status.SeeOther,
          response.header(Header.Location).exists(_.renderedValue.contains("/loginForm"))
        )
      },
      test("Calling '/test.html' (restricted resource) with an expired access token") {
        for {
          config       <- ZIO.service[SessionConfig]
          app          <- DMScreen.zapp
          expiredToken <- expiredToken
          response <- app.runZIO(
            Request
              .get(URL(Path("/test.html"))).addHeader(
                Header.Cookie(NonEmptyChunk(Cookie.Request(config.accessTokenName, expiredToken.str)))
              )
          )
        } yield assertTrue(
          response.status == Status.SeeOther,
          response.header(Header.Location).exists(_.renderedValue.contains("/loginForm"))
        )
      },
      test("Calling '/test.html' (restricted resource) with a valid access token") {
        for {
          config     <- ZIO.service[SessionConfig]
          app        <- DMScreen.zapp
          validToken <- validToken
          response <- app.runZIO(
            Request
              .get(URL(Path("/test.html"))).addHeader(
                Header.Cookie(NonEmptyChunk(Cookie.Request(config.accessTokenName, validToken.str)))
              )
          )
        } yield assertTrue(response.status == Status.Ok)
      }
    )

}

/** Other tests Login with missing email field → Should return 400 Bad Request Login with missing password field →
  * Should return 400 Bad Request Login with malformed form data → Should return 400 Bad Request Login with a
  * deactivated user → Should redirect to /loginForm?bad=true Login with a user who requires a password reset → Should
  * redirect to /resetPassword Access token with invalid signature → Should return 401 Unauthorized Access token with
  * incorrect issuer (iss claim) → Should return 401 Unauthorized Access token with incorrect audience (aud claim) →
  * Should return 401 Unauthorized Refresh token with incorrect signature → Should return 401 Unauthorized Refresh token
  * with incorrect issuer (iss claim) → Should return 401 Unauthorized Refresh token with incorrect audience (aud claim)
  * → Should return 401 Unauthorized Expired refresh token → Should return 401 Unauthorized Refresh token used twice
  * (refresh token rotation test) → First use succeeds, second use fails with 401 Unauthorized Refresh token revoked by
  * admin before use → Should return 401 Unauthorized Logging in generates a refresh token that cannot be used
  * immediately → Ensure refresh token rotation is enforced.
  */
