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

package dmscreen

import dmscreen.routes.*
import zio.*
import zio.http.*

import java.util.concurrent.TimeUnit

object DMScreen extends ZIOApp {

  override type Environment = DMScreenServerEnvironment & ConfigurationService
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[DMScreenServerEnvironment & ConfigurationService] = 
//    EnvironmentBuilder.live
    EnvironmentBuilder.withContainer

  private val defaultRouteZio: ZIO[ConfigurationService, ConfigurationError, Routes[ConfigurationService, Throwable]] =
    for {
      config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)

    } yield Routes(Method.ANY / trailing -> handler {
      (
        path: Path,
        _:    Request
      ) =>
        ZIO.logWarning(s"Path $path not found") *>
          ZIO.attempt(Response.notFound(path.toString))
    })

  def mapError(e: Cause[Throwable]): UIO[Response] = {
    lazy val contentTypeJson: Headers = Headers(Header.ContentType(MediaType.application.json).untyped)
    e.squash match {
      case e: DMScreenError =>
        val body =
          s"""{
            "exceptionMessage": ${e.getMessage},
            "stackTrace": [${e.getStackTrace.nn.map(s => s"\"${s.toString}\"").mkString(",")}]
          }"""

        ZIO.logError(body) *>
          ZIO.succeed(
            Response.apply(body = Body.fromString(body), status = Status.BadGateway, headers = contentTypeJson)
          )
      case e =>
        val body =
          s"""{
            "exceptionMessage": ${e.getMessage},
            "stackTrace": [${e.getStackTrace.nn.map(s => s"\"${s.toString}\"").mkString(",")}]
          }"""
        ZIO.logError(body) *>
          ZIO.succeed(
            Response.apply(body = Body.fromString(body), status = Status.InternalServerError, headers = contentTypeJson)
          )

    }
  }

  val zapp: ZIO[Environment, Throwable, Routes[Environment, Nothing]] = for {
    _             <- ZIO.log("Initializing Routes")
    defaultRoutes <- defaultRouteZio
    dmScreenRoute <- DMScreenRoutes.route
    dnd5eRoute    <- DND5eRoutes.route
    staRoute      <- STARoutes.route
    start         <- Clock.currentTime(TimeUnit.MILLISECONDS)
  } yield (dmScreenRoute ++
    dnd5eRoute ++
    staRoute ++
    StaticRoutes.unauthRoute /*Move this to after there's a user*/ ++
    StaticRoutes.authRoute ++
    defaultRoutes)
    .handleErrorCauseZIO(mapError)

  override def run: ZIO[Environment & ZIOAppArgs & Scope, Throwable, ExitCode] = {
    // Configure thread count using CLI
    for {
      config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
      app    <- zapp
      _      <- ZIO.logInfo(s"Starting application with config $config")
      server <- {
        val serverConfig = ZLayer.succeed(
          Server.Config.default
            .binding(config.dmscreen.host, config.dmscreen.port)
        )

        Server
          .serve(app)
          .zipLeft(ZIO.logDebug(s"Server Started on ${config.dmscreen.port}"))
          .tapErrorCause(ZIO.logErrorCause(s"Server on port ${config.dmscreen.port} has unexpectedly stopped", _))
          .provideSome[Environment](serverConfig, Server.live)
          .foldCauseZIO(
            cause => ZIO.logErrorCause("err when booting server", cause).exitCode,
            _ => ZIO.logError("app quit unexpectedly...").exitCode
          )
      }
    } yield server
  }

}
