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

import zio.*
import zio.http.*

object BrokenApp extends ZIOAppDefault {

  lazy private val unauthRoute = Routes(
    Method.ANY / Root / "unauth" / trailing -> handler {
      (
        path: Path,
        _:    Request
      ) =>
        Handler.html(s"<html>unauthRoute: unauth path: ${path.toString}</html>")
    }.flatten
  )

  lazy private val authRoute = Routes(
    Method.GET / Root / "blah" / trailing -> handler {
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
