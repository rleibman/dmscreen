package dmscreen.routes

import dmscreen.DMScreenError
import zio.{IO, ZIO}
import zio.http.{Header, Headers, Response, Routes, Status, URL}
import zio.json.JsonEncoder
import zio.json.*
import zio.json.ast.Json

trait AppRoutes[-R, -SessionType, +E] {

  final protected def seeOther(location: String): IO[DMScreenError, Response] =
    for {
      url <- ZIO.fromEither(URL.decode(location)).mapError(e => DMScreenError(e))
    } yield Response(Status.SeeOther, Headers(Header.Location(url)))

  final protected def json(value: Json): Response = Response.json(value.toString)

  final protected def json[A: JsonEncoder](value: A): Response = Response.json(value.toJson)

  /** These routes represent the api, the are intended to be used thorough ajax-type calls they require a session
    */
  def api: ZIO[R, E, Routes[R & SessionType, E]] = ZIO.succeed(Routes.empty)

  /** These routes that bring up resources that require authentication (an existing session)
    */
  def auth: ZIO[R, E, Routes[R & SessionType, E]] = ZIO.succeed(Routes.empty)

  /** These do not require a session
    */
  def unauth: ZIO[R, E, Routes[R, E]] = ZIO.succeed(Routes.empty)

}
