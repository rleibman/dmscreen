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

import caliban.*
import dmscreen.sta.STAAPI
import auth.*
import dmscreen.*
import zio.ZIO
import zio.http.*

object STARoutes extends AppRoutes[DMScreenServerEnvironment, Session[User], DMScreenError] {

  lazy private val interpreter = STAAPI.api.interpreter

  override def api: ZIO[
    DMScreenServerEnvironment,
    DMScreenError,
    Routes[DMScreenServerEnvironment & Session[User], DMScreenError]
  ] =
    (for {
      interpreter <- interpreter
    } yield {
      Routes(
        Method.ANY / "api" / "sta" ->
          QuickAdapter(interpreter).handlers.api,
        Method.ANY / "api" / "sta" / "graphiql" ->
          GraphiQLHandler.handler(apiPath = "/api/sta"),
        Method.POST / "api" / "sta" / "upload" ->
          QuickAdapter(interpreter).handlers.upload
      )
    }).mapError(DMScreenError(_))

  /** These do not require a session
    */
  override def unauth: ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, DMScreenError]] =
    (for {
      interpreter <- interpreter
    } yield {
      Routes(
        Method.GET / "unauth" / "sta" / "schema" ->
          Handler.fromBody(Body.fromCharSequence(STAAPI.api.render))
      )
    }).mapError(DMScreenError(_))

}
