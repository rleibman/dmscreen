import zio.*
import zio.http.*

object BrokenApp2 extends ZIOAppDefault {

  lazy private val unauthRoute = Routes(
    Method.ANY / "unauth" -> handler((_: Request) => Handler.html(s"<html>unauthRoute</html>")).flatten
  )

  lazy private val authRoute = Routes(
    Method.GET / Root -> handler((_: Request) => Handler.html(s"<html>unauthRoute</html>")).flatten
  )

  override def run =
    Server
      .serve(unauthRoute ++ authRoute)
      .provide(
        ZLayer.succeed(Server.Config.default.binding("localhost", 8888)),
        Server.live
      )

}
