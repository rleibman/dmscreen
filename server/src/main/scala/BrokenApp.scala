import zio.*
import zio.http.*

object BrokenApp extends ZIOAppDefault {

  case class MySession(user: String)

  private def seeOther(location: String): ZIO[Any, Throwable, Response] =
    for {
      url <- ZIO.fromEither(URL.decode(location))
    } yield Response(Status.SeeOther, Headers(Header.Location(url)))

  def bearerAuthWithContext: HandlerAspect[Any, MySession] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] { request =>
      request.header(Header.Authorization) match {
        case Some(Header.Authorization.Bearer(_ @token)) => // We got a bearer token, let's decode it
          ZIO.succeed((request, MySession("something"))) // Normally we would decode the token here
        case _ =>
          for {
            loginFormRedirect <- seeOther("/loginForm").mapError(e => Response.internalServerError(e.getMessage))
            session = Option(MySession("pretend I got it from request")) // getSession(request)
            r <-
              session match {
                case _ if request.path.startsWith(Path.decode("/loginForm")) =>
                  // I think we should never get here, given that I never really logged in in this test
                  ZIO.fail(
                    Response.unauthorized.addHeaders(Headers(Header.WWWAuthenticate.Bearer(realm = "Access")))
                  )
                case Some(s) => ZIO.succeed((request, s))
                case None    => ZIO.fail(loginFormRedirect)
              }
          } yield r
      }
    })

  lazy private val unauthRoute: Routes[Any, Nothing] = Routes(
    Method.GET / "loginForm" -> handler { (request: Request) =>
      Handler.html("<html>login form</html>")
    }.flatten,
    Method.ANY / "unauth" / trailing -> handler {
      (
        path: Path,
        _:    Request
      ) =>
        Handler.html(s"<html>unauthRoute: unauth path: ${path.toString}</html>")
    }.flatten
  ) @@ Middleware.debug

  lazy private val authRoute: Routes[Any, Nothing] = Routes(
    Method.GET / "" -> handler { (_: Request) =>
      Handler.fromZIO(
        for {
          session <- ZIO.service[MySession]
        } yield Response.html(s"<html>index: ${session.user}</html>")
      )
    }.flatten,
    Method.GET / trailing -> handler {
      (
        path: Path,
        _:    Request
      ) =>
        val str = path.toString.trim.nn
        if (str.startsWith("/unauth")) {
          Handler.html(s"<html>authRoute: unauth path: $str</html>")
        } else {
          Handler.html(s"<html>authRoute: auth path: $str</html>")
        }
    }.flatten
  ) @@ bearerAuthWithContext @@ Middleware.debug

  lazy val app = unauthRoute ++ authRoute

  override def run: ZIO[Environment & ZIOAppArgs & Scope, Throwable, ExitCode] = {
    // Configure thread count using CLI
    for {
      server <- Server
        .serve(app)
        .provideSome[Environment](
          ZLayer.succeed(Server.Config.default.binding("localhost", 8888)),
          Server.live
        )
    } yield server
  }

}
