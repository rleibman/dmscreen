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

package dmscreen.util

import japgolly.scalajs.react.{AsyncCallback, Callback}
import org.scalajs.dom.window
import sttp.capabilities
import sttp.client3.*
import sttp.model.{HeaderNames, Uri}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js.Object.keys

object ApiClient {

  val accessTokenName = "accessToken"

  val backend: SttpBackend[Future, capabilities.WebSockets] = FetchBackend()

  def getAccessToken: Option[String] = {
    Option(window.localStorage.getItem(accessTokenName))
  }

  def clearAccessToken: Callback = {
    Callback(window.localStorage.removeItem(accessTokenName)) >>
      Callback(window.sessionStorage.clear())
  }

  ///////////////////////////////////////////////////////////////////////////////////////////
  // New code that tries to do this with refresh and access tokens
  ///////////////////////////////////////////////////////////////////////////////////////////
  private var refreshingToken: Option[Promise[String]] = None // Shared refresh operation

  def apiCall[E, A](
    request: Request[Either[E, A], Any]
  ): AsyncCallback[Response[Either[E, A]]] = {
    import scala.concurrent.duration.*
    import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

    def attempt = {
      val noTok = request
        .followRedirects(true)
        .readTimeout(2.minute)
      AsyncCallback.fromFuture {
        val tok = getAccessToken.fold(noTok)(noTok.auth.bearer(_))

        tok.send(backend).map { response =>
          val authHeaderOpt = response.headers.find(_.name.equalsIgnoreCase("authorization"))
          authHeaderOpt.foreach { authHeader =>
            val token = authHeader.value.stripPrefix("Bearer ")
            window.localStorage.setItem(accessTokenName, token)
          }
          response
        }

      }
    }

    val r: AsyncCallback[Response[Either[E, A]]] = for {
      t1 <- attempt
      t2 <- t1.body match {
        case Right("authRefreshed") =>
          Callback.log("We refreshed the token, need to call again").asAsyncCallback >>
            attempt // try again
        case _ => AsyncCallback.pure(t1) // this handles success and errors perfectly fine
      }
    } yield t2

    r
  }

}
