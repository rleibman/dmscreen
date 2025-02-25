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

import dmscreen.routes.StaticRoutes.file
import dmscreen.{ConfigurationService, DMScreenError, DMScreenServerEnvironment, DMScreenSession}
import zio.*
import zio.http.*

import java.nio.file.{Files, Paths as JPaths}

object StaticRoutes extends AppRoutes[DMScreenServerEnvironment, DMScreenSession, DMScreenError] {

  lazy private val authNotRequired: Set[String] = Set(
    "login.html",
    "css/dmscreen.css",
    "css/sui-dmscreen.css",
    "dmscreen-login-opt-bundle.js",
    "dmscreen-login-opt-bundle.js.map",
    "dmscreen-web-opt-bundle.js",
    "dmscreen-web-opt-bundle.js.map",
    "css/app.css",
    "images/favicon.ico",
    "images/logo.png",
    "favicon.ico",
    "webfonts/fa-solid-900.woff2",
    "webfonts/fa-solid-900.woff",
    "webfonts/fa-solid-900.ttf"
  )

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
  private def file(
    fileName: String
  ): IO[Exception, java.io.File] = {
    JPaths.get(fileName) match {
      case path: java.nio.file.Path if !Files.exists(path) => ZIO.fail(Exception(s"NotFound($fileName)"))
      case path: java.nio.file.Path                        => ZIO.succeed(path.toFile.nn)
      case null => ZIO.fail(Exception(s"HttpError.InternalServerError(Could not find file $fileName))"))
    }
  }

  val unauthRoute: Routes[ConfigurationService, Throwable] = Routes(
    Method.GET / "loginForm" -> handler { (request: Request) =>
      Handler.fromFileZIO {
        for {
          config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
          staticContentDir = config.dmscreen.http.staticContentDir
          file <- file(s"$staticContentDir/login.html", request)
        } yield file
      }
    }.flatten,
    Method.ANY / trailing -> handler {
      (
        path: Path,
        _:    Request
      ) =>
        val somethingElse = path.toString
        println(somethingElse)

        if (authNotRequired(somethingElse)) {
          Handler.fromFileZIO {
            for {
              config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
              staticContentDir = config.dmscreen.http.staticContentDir
              file <- file(s"$staticContentDir/$somethingElse")
            } yield file
          }
        } else {
          Handler.error(Status.Unauthorized)
        }
    }.flatten
  )

  val authRoute: Routes[ConfigurationService & DMScreenSession, Throwable] =
    Routes(
      Method.GET / "index.html" -> handler { (request: Request) =>
        Handler.fromFileZIO {
          for {
            config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
            staticContentDir = config.dmscreen.http.staticContentDir
            file <- file(s"$staticContentDir/index.html", request)
          } yield file
        }
      }.flatten,
      Method.GET / "" -> handler { (request: Request) =>
        Handler.fromFileZIO {
          for {
            config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
            staticContentDir = config.dmscreen.http.staticContentDir
            file <- file(s"$staticContentDir/index.html", request)
          } yield file
        }
      }.flatten,
      Method.GET / trailing -> handler {
        (
          path:    Path,
          request: Request
        ) =>
          Handler.fromFileZIO {
            val somethingElse = path.toString.trim.nn

            if (somethingElse == "/" || somethingElse.isEmpty) {
              for {
                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
                staticContentDir = config.dmscreen.http.staticContentDir
                file <- file(s"$staticContentDir/index.html", request)
              } yield file
            } else {
              for {
                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
                staticContentDir = config.dmscreen.http.staticContentDir
                file <- file(s"$staticContentDir/$somethingElse", request)
              } yield file
            }
          }
      }.flatten
//      Method.GET / trailing -> handler {
//        (
//          path:    Path,
//          request: Request
//        ) =>
//          Handler.fromFileZIO {
//            val somethingElse = path.toString.trim.nn
//
//            if (somethingElse == "/" || somethingElse.isEmpty) {
//              for {
//                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
//                staticContentDir = config.dmscreen.staticContentDir
//                file <- file(s"$staticContentDir/index.html", request)
//              } yield file
//            } else {
//              for {
//                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
//                staticContentDir = config.dmscreen.staticContentDir
//                file <- file(s"$staticContentDir/$somethingElse", request)
//              } yield file
//            }
//          }
//      }.flatten
    )

}
