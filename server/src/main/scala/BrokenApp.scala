import zio.*
import zio.http.*

object BrokenApp extends ZIOAppDefault {

  lazy private val unauthRoute = Routes(
    Method.ANY / trailing -> handler {
      (
        path: Path,
        _:    Request
      ) =>
        Handler.html(s"<html>unauthRoute: unauth path: ${path.toString}</html>")
    }.flatten
  )

  lazy private val authRoute = Routes(
    Method.GET / trailing -> handler {
      (
        path: Path,
        _:    Request
      ) =>
        val str = path.toString.trim
        if (str.startsWith("/unauth")) {
          Handler.html(s"<html>authRoute: unauth path: $str</html>")
        } else {
          Handler.html(s"<html>authRoute: auth path: $str</html>")
        }
    }.flatten
  )

  override def run =
    Server
      .serve(unauthRoute ++ authRoute)
      .provide(
        ZLayer.succeed(Server.Config.default.binding("localhost", 8888)),
        Server.live
      )

}
