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

import auth.{SessionConfig, SessionTransport}
import caliban.CalibanError
import dmscreen.routes.*
import zio.*
import zio.http.*

import java.util.concurrent.TimeUnit

object DMScreen extends ZIOApp {

  override type Environment = DMScreenServerEnvironment
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[DMScreenServerEnvironment] = EnvironmentBuilder.live

  lazy private val unauthRoute: Routes[DMScreenServerEnvironment, Nothing] =
    Seq(
      AuthRoutes.unauthRoute,
      StaticRoutes.unauthRoute
    ).reduce(_ ++ _).handleErrorCauseZIO(mapError) @@ Middleware.debug

  val ignoreUnauth: Middleware[Any] = new Middleware[Any] {
    def apply[Env1 <: Any, Err](routes: Routes[Env1, Err]): Routes[Env1, Err] =
      routes.transform[Env1] { h =>
        handler { (req: Request) =>
          if (req.path.toString.startsWith("/unauth")) {
            // What should go here in order to ignore it
            ZIO.fail(Response.notFound)
          } else {
            // What should go here in order to process it?
            h(req)
          }
        }
      }
  }

  def authRoutes: ZIO[SessionTransport[DMScreenSession], Throwable, Routes[DMScreenServerEnvironment, Nothing]] =
    for {
      sessionTransport <- ZIO.service[SessionTransport[DMScreenSession]]
      dmScreenRoute    <- DMScreenRoutes.route
      dnd5eRoute       <- DND5eRoutes.route
      staRoute         <- STARoutes.route
    } yield {
      (Seq(
        AuthRoutes.authRoute,
        dmScreenRoute,
        staRoute,
        dnd5eRoute,
        StaticRoutes.authRoute
      ).reduce(_ ++ _) @@ ignoreUnauth)
        .handleErrorCauseZIO(mapError) @@ sessionTransport.bearerAuthWithContext(DMScreenSession.empty) @@ Middleware.debug
    }

  private val defaultRouteZio: ZIO[ConfigurationService, ConfigurationError, Routes[ConfigurationService, Throwable]] =
    for {
      _ <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)

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

        ZIO
          .logError(body).as(
            Response.apply(body = Body.fromString(body), status = Status.BadGateway, headers = contentTypeJson)
          )
      case e =>
        val body =
          s"""{
            "exceptionMessage": ${e.getMessage},
            "stackTrace": [${e.getStackTrace.nn.map(s => s"\"${s.toString}\"").mkString(",")}]
          }"""
        ZIO
          .logError(body).as(
            Response.apply(body = Body.fromString(body), status = Status.InternalServerError, headers = contentTypeJson)
          )

    }
  }

  lazy val zapp: ZIO[Environment, Throwable, Routes[Environment, Nothing]] = for {
    _         <- ZIO.log("Initializing Routes")
    authRoute <- authRoutes
  } yield unauthRoute ++ authRoute

  override def run: ZIO[Environment & ZIOAppArgs & Scope, Throwable, ExitCode] = {
    // Configure thread count using CLI
    for {
      config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
      app    <- zapp
      _      <- ZIO.logInfo(s"Starting application with config $config")
      server <- {
        val serverConfig = ZLayer.succeed(
          Server.Config.default
            .binding(config.dmscreen.http.hostName, config.dmscreen.http.port)
        )

        Server
          .serve(app)
          .zipLeft(ZIO.logDebug(s"Server Started on ${config.dmscreen.http.port}"))
          .tapErrorCause(ZIO.logErrorCause(s"Server on port ${config.dmscreen.http.port} has unexpectedly stopped", _))
          .provideSome[Environment](serverConfig, Server.live)
          .foldCauseZIO(
            cause => ZIO.logErrorCause("err when booting server", cause).exitCode,
            _ => ZIO.logError("app quit unexpectedly...").exitCode
          )
      }
    } yield server
  }

}
