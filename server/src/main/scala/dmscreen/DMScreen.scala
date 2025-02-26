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

import _root_.auth.{SessionConfig, SessionTransport}
import dmscreen.db.RepositoryError
import dmscreen.routes.*
import zio.*
import zio.http.*

import java.io.{PrintWriter, StringWriter}
import java.util.concurrent.TimeUnit

object DMScreen extends ZIOApp {

  override type Environment = DMScreenServerEnvironment
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[DMScreenServerEnvironment] = EnvironmentBuilder.live

  object TestRoutes extends AppRoutes[DMScreenServerEnvironment, DMScreenSession, DMScreenError] {

    /** These routes represent the api, the are intended to be used thorough ajax-type calls they require a session
      */
    override def api: ZIO[
      DMScreenServerEnvironment,
      DMScreenError,
      Routes[DMScreenServerEnvironment & DMScreenSession, DMScreenError]
    ] =
      ZIO.succeed(
        Routes(
          Method.GET / "test" -> handler((_: Request) => Handler.html(s"<html>Test API Ok!</html>")).flatten
        )
      )

    /** These routes that bring up resources that require authentication (an existing session)
      */
    override def auth: ZIO[
      DMScreenServerEnvironment,
      DMScreenError,
      Routes[DMScreenServerEnvironment & DMScreenSession, DMScreenError]
    ] =
      ZIO.succeed(
        Routes(
          Method.GET / "test.html" -> handler((_: Request) => Handler.html(s"<html>Test Auth Ok!</html>")).flatten
        )
      )

    /** These do not require a session
      */
    override def unauth
      : ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, DMScreenError]] =
      ZIO.succeed(
        Routes(
          Method.GET / "unauthtest.html" -> handler((_: Request) =>
            Handler.html(s"<html>Test Unauth Ok!</html>")
          ).flatten
        )
      )

  }

  object AllTogether extends AppRoutes[DMScreenServerEnvironment, DMScreenSession, DMScreenError] {

    private val routes: Seq[AppRoutes[DMScreenServerEnvironment, DMScreenSession, DMScreenError]] =
      Seq(AuthRoutes, StaticRoutes, DMScreenRoutes, DND5eRoutes, STARoutes, TestRoutes)

    override def api: ZIO[
      DMScreenServerEnvironment,
      DMScreenError,
      Routes[DMScreenServerEnvironment & DMScreenSession, DMScreenError]
    ] = ZIO.foreach(routes)(_.api).map(_.reduce(_ ++ _))

    override def auth: ZIO[
      DMScreenServerEnvironment,
      DMScreenError,
      Routes[DMScreenServerEnvironment & DMScreenSession, DMScreenError]
    ] = ZIO.foreach(routes)(_.auth).map(_.reduce(_ ++ _))

    override def unauth
      : ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, DMScreenError]] =
      ZIO.foreach(routes)(_.unauth).map(_.reduce(_ ++ _))

  }

  def mapError(original: Cause[Throwable]): UIO[Response] = {
    lazy val contentTypeJson: Headers = Headers(Header.ContentType(MediaType.application.json).untyped)

    val squashed = original.squash
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    squashed.printStackTrace(pw)

    val body =
      s"""{
      "exceptionMessage": ${squashed.getMessage},
      "stackTrace": ${sw.toString}
    }"""
    val status = squashed match {
      case e: RepositoryError if e.isTransient => Status.BadGateway
      case e: DMScreenError                    => Status.InternalServerError
      case e => Status.InternalServerError
    }
    ZIO
      .logErrorCause("Error in DMScreen", original).as(
        Response.apply(body = Body.fromString(body), status = status, headers = contentTypeJson)
      )
  }

  lazy val zapp: ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, Nothing]] = for {
    _                <- ZIO.log("Initializing Routes")
    sessionTransport <- ZIO.service[SessionTransport[DMScreenSession]]
    auth             <- AllTogether.auth
    unauth           <- AllTogether.unauth
    api              <- AllTogether.api
  } yield (unauth.nest("unauth") ++
    ((auth @@ sessionTransport.resourceSessionProvider) ++
      (api.nest("api")) @@ sessionTransport.apiSessionProvider))
    .handleErrorCauseZIO(mapError) @@ Middleware.debug

  override def run: ZIO[Environment & ZIOAppArgs & Scope, DMScreenError, ExitCode] = {
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
