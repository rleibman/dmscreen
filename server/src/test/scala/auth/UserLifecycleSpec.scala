package auth

import ai.dnd5e.DND5eAIServer
import courier.*
import dmscreen.db.DMScreenZIORepository
import dmscreen.db.dnd5e.DND5eZIORepository
import dmscreen.dnd5e.Encounter
import dmscreen.dnd5e.dndbeyond.DNDBeyondImporter
import dmscreen.dnd5e.fifthEditionCharacterSheet.FifthEditionCharacterSheetImporter
import dmscreen.dnd5e.srd.SRDImporter
import dmscreen.mail.Postman
import dmscreen.{*, given}
import pdi.jwt.*
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*

import scala.util.matching.Regex

object UserLifecycleSpec extends ZIOSpec[DMScreenServerEnvironment] {

  import EnvironmentBuilder.*

  val regex = "<a[^>]*>(.*?)</a>".r

  def extractLink(
    envelope: Envelope,
    regex:    Regex
  ): String = {
    val str = envelope._content match {
      case Multipart(_parts, _) => _parts.map(_.getContent.toString).mkString("\n")
      case Text(str, _)         => str
      case other                => other.toString
    }

    val res = regex.findFirstMatchIn(str) match {
      case Some(m) => m.group(1)
      case None    => ""
    }
    res
  }

  case class MockPostman(ref: Ref[Map[String, Envelope]]) extends Postman {

    override def deliver(envelope: Envelope): UIO[Unit] = {
      ZIO.logDebug(s"Mock delivering ${envelope.toString}") *> ref.update(map =>
        map + (envelope.to.head.getAddress -> envelope)
      )
    }
    def sentEmails: Task[Map[String, Envelope]] = ref.get

  }

  val mockPostman: ULayer[MockPostman] = ZLayer.fromZIO(for {
    ref <- Ref.make(Map.empty[String, Envelope])
  } yield MockPostman(ref))

  val mockAIServer: ULayer[DND5eAIServer] = ZLayer.succeed(new DND5eAIServer {
    override def generateEncounterDescription(encounter: Encounter)
      : ZIO[DMScreenSession & DND5eZIORepository & DMScreenZIORepository, DMScreenError, String] =
      ZIO.succeed("Fake Encounter Description")

  })

  override def bootstrap: ULayer[DMScreenServerEnvironment] =
    ZLayer
      .make[DMScreenServerEnvironment & InitializingLayer & DMScreenSession](
        DMScreenSession.adminSession.toLayer,
        DMScreenContainer.containerLayer,
        ConfigurationServiceImpl.live >>> DMScreenContainer.configLayer,
        dmscreen.db.QuillRepository.db,
        dmscreen.db.dnd5e.QuillRepository.db,
        dmscreen.db.sta.QuillRepository.db,
        dmscreen.db.QuillAuthService.live,
        DNDBeyondImporter.live,
        FifthEditionCharacterSheetImporter.live,
        SRDImporter.live,
        containerInitializingLayer,
        mockAIServer,
        CookieAndBearerSessionTransport.live,
        mockPostman,
        // We can't use zlayer on these map because _.appConfig is itself a zio.
        ZLayer.fromZIO(ZIO.serviceWithZIO[ConfigurationService](_.appConfig).map(_.dmscreen.session))
//        ZLayer.fromZIO(ZIO.serviceWithZIO[ConfigurationService](_.appConfig).map(_.dmscreen.smtp)) // Specialize the configuration service to pass it to Postman
      ).orDie

  val goodEmail = "validuser@example.com"
  val goodPassword = "correctpassword"

  val anotherGoodEmail = "validuser2@example.com"
  val betterPassword = "betterpassword"

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
    app:      Routes[DMScreenServerEnvironment, Nothing],
    email:    String = goodEmail,
    password: String = goodPassword
  ): ZIO[DMScreenServerEnvironment, DMScreenError, (TokenString, TokenString)] = {
    for {
      response <- doLogin(app, email, password)
      _ <- ZIO
        .fail(
          new DMScreenError(
            s"Login failed with status ${response.status} and body ${response.body.asString}"
          )
        ).when(response.status != Status.Ok)
    } yield (
      TokenString(
        response
          .header(Header.Authorization).map((header: Header.Authorization) =>
            header.renderedValue.replace("Bearer ", "")
          ).getOrElse("")
      ),
      TokenString(
        response
          .headers(Header.SetCookie)
          .find((header: Header.SetCookie) => header.value.name == "_refreshToken")
          .fold("")((header: Header.SetCookie) => header.value.content)
      )
    )
  }

  override def spec: Spec[DMScreenServerEnvironment & TestEnvironment & Scope, Any] =
    suite("User Lifecycle")(
      test("whoami endpoint returns user info when authenticated") {
        for {
          app        <- DMScreen.zapp
          authHeader <- goodLogin(app).map(a => Header.Authorization.Bearer(a._1.str))
          response   <- app.runZIO(Request.get(URL(Path("/api/whoami"))).addHeader(authHeader))
          body       <- response.body.asString
        } yield assertTrue(response.status == Status.Ok, body.contains("email"))
      },
      test(
        "changePassword endpoint updates user password"
      ) { // Note: can't change the password for a user that is being used in other tests
        for {
          app        <- DMScreen.zapp
          authHeader <- goodLogin(app, email = anotherGoodEmail).map(a => Header.Authorization.Bearer(a._1.str))
          response <- app.runZIO(
            Request
              .post(
                URL(Path("/api/changePassword")),
                Body.fromString(betterPassword)
              ).addHeader(authHeader)
          )
        } yield assertTrue(response.status == Status.Ok)
      },
      test("doLogout logs the user out") {
        for {
          app        <- DMScreen.zapp
          authHeader <- goodLogin(app)
          response <- app
            .runZIO(
              Request
                .get(URL(Path("/api/doLogout")))
                .addHeader(Header.Authorization.Bearer(authHeader._1.str))
                .addHeader(Header.Cookie(NonEmptyChunk(Cookie.Request("_refreshToken", authHeader._2.str))))
            )
        } yield assertTrue(
          response.status == Status.SeeOther,
          response.header(Header.Location).get.url.path == Path("/unauth/loginForm")
        )
      },
      test("serverVersion returns application version") {
        for {
          app      <- DMScreen.zapp
          response <- app.runZIO(Request.get(URL(Path("/unauth/serverVersion"))))
          body     <- response.body.asString
        } yield assertTrue(response.status == Status.Ok, body.contains("1.0.0"))
      },
      test("password recovery updates password if token is valid") {
        for {
          _   <- TestRandom.setSeed(1)
          app <- DMScreen.zapp
          // Do password recovery request
          passwordRecoveryResponse <- app.runZIO(
            Request
              .post(
                URL(Path("/unauth/passwordRecoveryRequest")),
                Body.fromString(goodEmail.toJson)
              )
          )
          // Get the token from the email
          postman   <- ZIO.serviceWith[Postman](_.asInstanceOf[MockPostman])
          resetLink <- postman.sentEmails.map(l => extractLink(l(goodEmail), regex))
          resetUrl <- ZIO
            .fromEither(URL.decode(resetLink.replace("http://localhost:8079", ""))).mapError(DMScreenError(_))
          resetResponse <- app.runZIO(Request.get(resetUrl))
        } yield assertTrue(
          passwordRecoveryResponse.status == Status.Ok, // Redirects to login page where you'll geta  password change request with a token.
          resetResponse.status == Status.Ok
        )
      }, // http://localhost:8079/unauth/loginForm?passwordReset=true&token=3jqkdbnm4sn0cmu3nrd29fijb8
      test("user creation and registration confirmation") {
        val email = "roberto1@leibman.net"
        for {
          _   <- TestRandom.setSeed(2)
          app <- DMScreen.zapp
          now <- Clock.localDateTime
          userReq = UserCreationRequest(
            User(
              UserId(0),
              email = email,
              name = "Roberto",
              created = now
            ),
            goodPassword
          )
          createResponse <- app.runZIO(
            Request.put(
              URL(Path("/unauth/userCreation")),
              Body.fromString(userReq.toJson).contentType(MediaType.application.json)
            )
          )
          _           <- ZIO.logDebug(createResponse.status.text)
          postman     <- ZIO.serviceWith[Postman](_.asInstanceOf[MockPostman])
          confirmLink <- postman.sentEmails.map(l => extractLink(l(email), regex))
          confirmUrl = URL(
            Path(confirmLink.replace("http://localhost:8079", "").replaceAll("\\?token=.*", "")),
            queryParams = QueryParams("token" -> confirmLink.replaceAll(".*\\?token=", ""))
          )

          confirmResponse <- app.runZIO(Request.get(confirmUrl))
          user <- ZIO
            .serviceWithZIO[UserRepository[DMScreenTask]](
              _.userByEmail("roberto1@leibman.net").provideLayer(DMScreenSession.adminSession.toLayer)
            )
        } yield assertTrue(
          confirmUrl.toString.nonEmpty,
          confirmLink.contains(s"http://localhost:8079/unauth/confirmRegistration?token="),
          createResponse.status == Status.Ok,
          confirmResponse.status == Status.SeeOther,
          confirmResponse
            .header(Header.Location).exists(loc =>
              loc.url.toString.contains("/unauth/loginForm?registrationSucceeded=true")
            ),
          user.isDefined
        )
      },
      test("refresh token provides new access token") {
        for {
          app <- DMScreen.zapp
          tok <- goodLogin(app)
          response <- app.runZIO(
            Request
              .get(URL(Path("/refresh")))
              .addHeader(Header.Authorization.Bearer(tok._1.str))
              .addHeader(Header.Cookie(NonEmptyChunk(Cookie.Request("_refreshToken", tok._2.str))))
          )
          body <- response.body.asString
        } yield assertTrue(response.status == Status.Ok, body.contains("authRefreshed"))
      }
    )

}
