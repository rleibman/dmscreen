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

import auth.AuthClient.backend
import caliban.client.CalibanClientError
import japgolly.scalajs.react.{AsyncCallback, Callback}
import org.scalajs.dom.window
import sttp.capabilities
import sttp.client3.*
import sttp.model.*
import zio.json.*

import scala.concurrent.{Future, Promise}
import scala.scalajs.js.Object.keys

object ApiClientSttp3 {
  // All of this goes away once caliban supports sttp4

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  private def asyncJwtToken: AsyncCallback[Option[String]] =
    AsyncCallback.pure(Option(window.localStorage.getItem("jwtToken")))

  val backend: SttpBackend[Future, capabilities.WebSockets] = FetchBackend()

  case class ZioJsonException(error: String) extends Exception

  object JsonInput {

    def sanitize[T: IsOption]: String => String = { s =>
      if (implicitly[IsOption[T]].isOption && s.trim.isEmpty) "null" else s
    }

  }

  def asJson[B: {JsonDecoder, IsOption}]: ResponseAs[Either[ResponseException[String, Exception], B], Any] =
    asString.mapWithMetadata(ResponseAs.deserializeRightWithError(deserializeJson))

  def deserializeJson[B: {JsonDecoder, IsOption}]: String => Either[Exception, B] =
    JsonInput.sanitize[B].andThen(_.fromJson[B].left.map(ZioJsonException(_)))

  def withAuth[A](
    request: Request[Either[CalibanClientError, A], Any],
    onAuthError: String => AsyncCallback[Any] = msg =>
      AsyncCallback.pure {
        window.alert(msg)
        window.location.reload()
      }
  ): AsyncCallback[Either[CalibanClientError, A]] = {
    def doCall(tok: String): AsyncCallback[Response[Either[CalibanClientError, A]]] = {
      AsyncCallback.fromFuture(
        request.auth
          .bearer(tok)
          .send(backend)
      )
    }

    for {
      tokOpt      <- asyncJwtToken
      responseOpt <- AsyncCallback.traverseOption(tokOpt)(doCall)
      withRefresh <- responseOpt match {
        case Some(response)
            if response.code == StatusCode.Unauthorized && response.body.left.exists(
              _.getMessage.contains("token_expired")
            ) =>
          Callback.log("Refreshing token").asAsyncCallback >>
            // Call refresh endpoint
            (for {
              refreshResponse <- AsyncCallback.fromFuture(
                basicRequest
                  .get(uri"/refresh")
                  .response(asString)
                  .send(backend)
              )
              retried <- refreshResponse.code match {
                case c if c.isSuccess =>
                  refreshResponse.header(HeaderNames.Authorization) match {
                    case Some(authHeader) =>
                      val newToken = authHeader.stripPrefix("Bearer ")
                      window.localStorage.setItem("jwtToken", newToken)
                      doCall(newToken).map(_.body)
                    case None =>
                      val msg = "Server said refresh was ok, but didn't return a token"
                      onAuthError(msg) >> AsyncCallback.pure(
                        Left(CalibanClientError.CommunicationError(msg): CalibanClientError)
                      )
                  }
                case c =>
                  val msg = s"Trying to get Refresh token got $c"
                  onAuthError(msg) >> AsyncCallback.pure(Left(CalibanClientError.CommunicationError(msg)))
              }
            } yield retried)
        case None =>
          val msg = "No token set, please log in"
          onAuthError(msg) >> AsyncCallback.pure(Left(CalibanClientError.CommunicationError(msg)))
        case Some(other) if !other.code.isSuccess =>
          AsyncCallback.pure {
            // Clear the token
            window.localStorage.removeItem("jwtToken")
            window.localStorage.clear()
          } >>
            AsyncCallback.pure(other.body) // Success, or some other "normal" error, pass it along
        case Some(other) =>
          AsyncCallback.pure(other.body) // Success, or some other "normal" error, pass it along
      }
    } yield withRefresh
  }

}
