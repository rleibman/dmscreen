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

import auth.{AuthServer, Session}
import dmscreen.db.RepositoryError
import dmscreen.db.dnd5e.DND5eZIORepository
import dmscreen.dnd5e.EncounterDifficultyLevel
import dmscreen.routes.*
import zio.*
import zio.http.*

import java.io.{PrintWriter, StringWriter}

object DMScreen extends ZIOApp {

  override type Environment = DMScreenServerEnvironment
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[DMScreenServerEnvironment] = EnvironmentBuilder.live
//    EnvironmentBuilder.withContainer

  object TestRoutes extends AppRoutes[DMScreenServerEnvironment, Session[User], DMScreenError] {

    /** These routes represent the api, the are intended to be used thorough ajax-type calls they require a session
      */
    override def api: ZIO[
      DMScreenServerEnvironment,
      DMScreenError,
      Routes[DMScreenServerEnvironment & Session[User], DMScreenError]
    ] =
      ZIO.succeed(
        Routes(
          Method.GET / "api" / "test" -> handler((_: Request) => Handler.html(s"<html>Test API Ok!</html>")).flatten
        )
      )

    override def unauth
      : ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, DMScreenError]] =
      ZIO.succeed(
        Routes(
          Method.GET / "unauth" / "unauthtest.html" -> handler((_: Request) =>
            Handler.html(s"<html>Test Unauth Ok!</html>")
          ).flatten
        )
      )

  }

  object AllTogether extends AppRoutes[DMScreenServerEnvironment, Session[User], DMScreenError] {

    private val routes: Seq[AppRoutes[DMScreenServerEnvironment, Session[User], DMScreenError]] =
      Seq(
        DMScreenRoutes,
        DND5eRoutes,
        STARoutes,
        TestRoutes,
        StaticRoutes
      )

    override def api: ZIO[
      DMScreenServerEnvironment,
      DMScreenError,
      Routes[DMScreenServerEnvironment & Session[User], DMScreenError]
    ] = ZIO.foreach(routes)(_.api).map(_.reduce(_ ++ _) @@ Middleware.debug)

    override def unauth
      : ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, DMScreenError]] =
      ZIO.foreach(routes)(_.unauth).map(_.reduce(_ ++ _) @@ Middleware.debug)

  }

  def mapError(original: Cause[Throwable]): UIO[Response] = {
    lazy val contentTypeJson: Headers = Headers(Header.ContentType(MediaType.application.json).untyped)

    val squashed = original.squash
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    squashed.printStackTrace(pw)

    val body = "Error in DMScreen"
    // We really don't want details

//    {
//      s"""{
//      "exceptionMessage": ${squashed.getMessage},
//      "stackTrace": ${sw.toString}
//    }"""
//    }
    val status = squashed match {
      case e: RepositoryError if e.isTransient => Status.BadGateway
      case _: DMScreenError                    => Status.InternalServerError
      case _ => Status.InternalServerError
    }
    ZIO
      .logErrorCause("Error in DMScreen", original).as(
        Response.apply(body = Body.fromString(body), status = status, headers = contentTypeJson)
      )
  }

  def testEncounter: ZIO[DMScreenServerEnvironment, DMScreenError, Seq[EncounterDifficultyLevel]] = {
    val campaignId = CampaignId(3)
    for {
      repo         <- ZIO.service[DND5eZIORepository]
      encounterIds <- repo.encounters(campaignId)
      pcs          <- repo.playerCharacters(campaignId)
      npcs         <- repo.nonPlayerCharacters(campaignId)
    } yield encounterIds.map(_.info.calculateDifficulty(pcs, npcs))
  }.provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)

  lazy val zapp: ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, Nothing]] = for {
    _                <- ZIO.log("Initializing Routes")
    authServer       <- ZIO.service[AuthServer[User, UserId]]
    authServerApi    <- authServer.authRoutes
    authServerUnauth <- authServer.unauthRoutes
    _                <- testEncounter
    unauth           <- AllTogether.unauth
    api              <- AllTogether.api
  } yield (
    ((api ++ authServerApi) @@ authServer.bearerSessionProvider) ++
      authServerUnauth ++ unauth
  )
    .handleErrorCauseZIO(mapError)

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
