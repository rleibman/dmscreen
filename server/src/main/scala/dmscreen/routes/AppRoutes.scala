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

  def unauth: ZIO[R, E, Routes[R, E]] = ZIO.succeed(Routes.empty)

}
