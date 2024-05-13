package dmscreen.routes

import dmscreen.ConfigurationService
import zio.http.*
import zio.*
import zio.http.codec.HttpCodec.NotFound

import java.nio.file.{Files, Paths as JPaths}

object StaticRoutes {

  lazy private val authNotRequired: Set[String] = Set(
    "login.html",
    "css/dmscreen.css",
    "css/app-sui-theme.css",
    "dmscreen-login-opt-bundle.js",
    "dmscreen-login-opt-bundle.js.map",
    "css/app.css",
    "images/favicon.ico",
    "images/logo.png",
    "favicon.ico",
    "webfonts/fa-solid-900.woff2",
    "webfonts/fa-solid-900.woff",
    "webfonts/fa-solid-900.ttf"
  )

  //  val meHandler: Handler[Any, Nothing, String, Response] = Handler.fromFunction { (request: String) => Response.text(s"Hello $request") }

  private def file(
    fileName: String,
    request:  Request
  ): IO[Exception, java.io.File] = {
    JPaths.get(fileName) match {
      case path: java.nio.file.Path if !Files.exists(path) => ZIO.fail(Exception(s"NotFound(${request.path})"))
      case path: java.nio.file.Path                        => ZIO.succeed(path.toFile.nn)
      case null => ZIO.fail(Exception(s"HttpError.InternalServerError(Could not find file $fileName))"))
    }
  }

  val unauthRoute: Routes[ConfigurationService, Throwable] = Routes(
    Method.GET / "loginForm" -> handler { (request: Request) =>
      Handler.fromFileZIO {
        for {
          config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
          staticContentDir = config.dmscreen.staticContentDir
          file <- file(s"$staticContentDir/login.html", request)
        } yield file
      }
    }.flatten,
    Method.ANY / "unauth" / string("somethingElse") -> handler {
      (
        somethingElse: String,
        request:       Request
      ) =>
        if (authNotRequired(somethingElse)) {
          Handler.fromFileZIO {
            for {
              config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
              staticContentDir = config.dmscreen.staticContentDir
              file <- file(s"$staticContentDir/$somethingElse", request)
            } yield file
          }
        } else {
          Handler.error(Status.Unauthorized)
        }
    }.flatten
  )

  val authRoute: Routes[ConfigurationService, Throwable] =
    Routes(
      Method.GET / "index.html" -> handler { (request: Request) =>
        Handler.fromFileZIO {
          for {
            config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
            staticContentDir = config.dmscreen.staticContentDir
            file <- file(s"$staticContentDir/index.html", request)
          } yield file
        }
      }.flatten,
      Method.GET / string("somethingElse") -> handler {
        (
          somethingElse: String,
          request:       Request
        ) =>
          Handler.fromFileZIO {
            if (somethingElse == "/") {
              for {
                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
                staticContentDir = config.dmscreen.staticContentDir
                file <- file(s"$staticContentDir/index.html", request)
              } yield file
            } else {
              for {
                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
                staticContentDir = config.dmscreen.staticContentDir
                file <- file(s"$staticContentDir/$somethingElse", request)
              } yield file
            }
          }
      }.flatten
    )

}
