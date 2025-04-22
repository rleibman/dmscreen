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

import auth.*
import dmscreen.*
import zio.*
import zio.http.*

import java.nio.file.{Files, Paths as JPaths}

object StaticRoutes extends AppRoutes[DMScreenServerEnvironment, Session[User], DMScreenError] {

  //  def authNotRequired(str: String) = {
//    val set: Set[String] = Set(
//      "login.html",
//      "css/dmscreen.css",
//      "css/sui-dmscreen.css",
//      "dmscreen-login-opt-bundle.js",
//      "dmscreen-login-opt-bundle.js.map",
//      "dmscreen-web-opt-bundle.js",
//      "dmscreen-web-opt-bundle.js.map",
//      "css/app.css",
//      "images/favicon.ico",
//      "images/logo.png",
//      "favicon.ico",
//      "webfonts/fa-solid-900.woff2",
//      "webfonts/fa-solid-900.woff",
//      "webfonts/fa-solid-900.ttf"
//    )
//    val startsWith: List[String] = List(
////      "confirmRegistration"
//    )
//    set(str) || startsWith.exists(str.startsWith)
//  }
//
  private def file(
    fileName: String,
    request:  Request
  ): IO[DMScreenError, java.io.File] = {
    JPaths.get(fileName) match {
      case path: java.nio.file.Path if !Files.exists(path) => ZIO.fail(DMScreenError(s"NotFound(${request.path})"))
      case path: java.nio.file.Path                        => ZIO.succeed(path.toFile.nn)
      case null => ZIO.fail(DMScreenError(s"HttpError.InternalServerError(Could not find file $fileName))"))
    }
  }
  private def file(
    fileName: String
  ): IO[DMScreenError, java.io.File] = {
    JPaths.get(fileName) match {
      case path: java.nio.file.Path if !Files.exists(path) => ZIO.fail(DMScreenError(s"NotFound($fileName)"))
      case path: java.nio.file.Path                        => ZIO.succeed(path.toFile.nn)
      case null => ZIO.fail(DMScreenError(s"HttpError.InternalServerError(Could not find file $fileName))"))
    }
  }
  override def unauth: ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, DMScreenError]] =
    ZIO.succeed(
      Routes(
        Method.GET / Root -> handler {
          (
            _: Request
          ) =>
            Handler
              .fromFileZIO {
                for {
                  config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
                  staticContentDir = config.dmscreen.http.staticContentDir
                  file <- file(s"$staticContentDir/index.html")
                } yield file
              }.mapError(DMScreenError(_))
        }.flatten,
        Method.GET / trailing -> handler {
          (
            path: Path,
            _:    Request
          ) =>

            // You might want to restrict the files that could come back, but then again, you may not
            val somethingElse = path.toString
            Handler
              .fromFileZIO {
                for {
                  config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
                  staticContentDir = config.dmscreen.http.staticContentDir
                  file <- file(s"$staticContentDir/$somethingElse")
                } yield file
              }.mapError(DMScreenError(_))
        }.flatten
      )
    )
//
//  /** These routes that bring up resources that require authentication (an existing session)
//    */
//  def auth: ZIO[
//    DMScreenServerEnvironment,
//    DMScreenError,
//    Routes[DMScreenServerEnvironment & Session[User], DMScreenError]
//  ] =
//    ZIO.succeed(
//      Routes(
//        Method.GET / "index.html" -> handler { (request: Request) =>
//          Handler
//            .fromFileZIO {
//              for {
//                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
//                staticContentDir = config.dmscreen.http.staticContentDir
//                file <- file(s"$staticContentDir/index.html", request)
//              } yield file
//            }.mapError(DMScreenError(_))
//        }.flatten,
//        Method.GET / "" -> handler { (request: Request) =>
//          Handler
//            .fromFileZIO {
//              for {
//                config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
//                staticContentDir = config.dmscreen.http.staticContentDir
//                file <- file(s"$staticContentDir/index.html", request)
//              } yield file
//            }.mapError(DMScreenError(_))
//        }.flatten,
//        Method.GET / trailing -> handler {
//          (
//            path:    Path,
//            request: Request
//          ) =>
//            Handler
//              .fromFileZIO {
//                val somethingElse = path.toString.trim.nn
//
//                if (somethingElse == "/" || somethingElse.isEmpty) {
//                  for {
//                    config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
//                    staticContentDir = config.dmscreen.http.staticContentDir
//                    file <- file(s"$staticContentDir/index.html", request)
//                  } yield file
//                } else {
//                  for {
//                    config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
//                    staticContentDir = config.dmscreen.http.staticContentDir
//                    file <- file(s"$staticContentDir/$somethingElse", request)
//                  } yield file
//                }
//              }.mapError(DMScreenError(_))
//        }.flatten
//      )
//    )

}
