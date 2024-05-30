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
import caliban.schema.GenericSchema
import dmscreen.DMScreenServerEnvironment
import dmscreen.dnd5e.{DND5eAPI, DND5eRepository}
import zio.{IO, ZIO}
import zio.http.*

object DND5eRoutes {

  lazy private val interpreter = DND5eAPI.api.interpreter

  lazy val route: IO[CalibanError.ValidationError, Routes[DMScreenServerEnvironment, Nothing]] =
    for {
      interpreter <- interpreter
    } yield {
      Routes(
        Method.ANY / "api" / "dnd5e" ->
          QuickAdapter(interpreter).handlers.api,
        Method.ANY / "api" / "dnd5e" / "graphiql" ->
          GraphiQLHandler.handler(apiPath = "/api/dnd5e", graphiqlPath = "/api/dnd5e/graphiql"),
        Method.GET / "api" / "dnd5e" / "schema" ->
          Handler.fromBody(Body.fromCharSequence(DND5eAPI.api.render)),
        Method.POST / "api" / "dnd5e" / "upload" ->
          QuickAdapter(interpreter).handlers.upload
      )
    }

}
