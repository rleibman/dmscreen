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

package pages

import app.{LoginControllerState, Mode}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.react.mod.CSSProperties
import org.scalajs.dom.window
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.submit
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticCOLORS
import sttp.capabilities
import sttp.client3.*

import scala.concurrent.Future

object LoginPage {

  case class Props(
    message:       Option[String],
    changeMessage: String => Callback
  )

  case class State(
    email:    String = "",
    password: String = ""
  )

  class Backend($ : BackendScope[Props, State]) {

    val query: String = if (window.location.search.isEmpty) "" else window.location.search.substring(1).nn
    val isBad: Boolean = query.contains("bad=true")

    def handleSubmit(e: ReactEventFromHtml): Callback = {
      e.preventDefaultCB >>
        $.state.flatMap { state =>
          window.localStorage.removeItem("accessToken")

          given backend: SttpBackend[Future, capabilities.WebSockets] = FetchBackend()
          import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

          Callback.future {
            basicRequest
              .followRedirects(false)
              .post(uri"/doLogin")
              .body(Map("email" -> state.email, "password" -> state.password))
              .response(asStringAlways)
              .send(backend)
              .map { response =>
                if (!response.code.isSuccess) {
                  val error = response.body.headOption.fold("Login Failed")(msg => s"Login failed: $msg")
                  $.props.flatMap(_.changeMessage(error))
                } else {
                  Callback.log(s"Headers ${response.headers.map(_.name).mkString(",")}") >>
                    (response.headers.find(_.name.equalsIgnoreCase("authorization")) match {
                      case Some(authHeader) =>
                        Callback {
                          val token = authHeader.value.stripPrefix("Bearer ")
                          window.localStorage.setItem("accessToken", token)
                          window.location.href = "/" // Redirect
                        } >>
                          Callback.log("Authentication successful, token received")
                      case None =>
                        $.props.flatMap(_.changeMessage("Login successful, but no token received."))
                    })
                }
              }

          }

        }

    }

    def render(
      props: Props,
      state: State
    ): VdomElement = {
      LoginControllerState.ctx.consume { context =>
        <.div(
          <.div(<.img(^.src := "/unauth/images/logo.png", ^.width := 350.px)),
          <.div(
            "Welcome to DMScreen!"
          ),
          <.span(
            Message().color(SemanticCOLORS.red)(
              "Wrong credentials, it's possible your email and password haven't been activated, try again!"
            )
          ).when(isBad),
          props.message.fold(EmptyVdom: VdomNode)(str => <.span(Message()(str))),
          Form
            .style(CSSProperties().set("width", 800.px))
            .method("post")
            .action("/doLogin")
            .onSubmit(
              (
                e,
                data
              ) => handleSubmit(e)
            )(
              Form.Input
                .label("Email")
                .required(true)
                .name("email")
                .`type`("email")
                .value(state.email)
                .autoComplete("on")
                .onChange(
                  (
                    _,
                    d
                  ) => $.modState(_.copy(email = d.value.get.asInstanceOf[String]))
                ),
              Form.Input
                .label("Password")
                .`type`("password")
                .required(true)
                .name("password")
                .value(state.password)
                .autoComplete("on")
                .onChange(
                  (
                    _,
                    d
                  ) => $.modState(_.copy(password = d.value.get.asInstanceOf[String]))
                ),
              Button.compact(true).basic(true).`type`(submit)("Go!"),
              Button
                .compact(true)
                .basic(true)
                .onClick {
                  (
                    _,
                    _
                  ) =>
                    context.onModeChanged(Mode.registration, None)
                }("Register"),
              Button
                .compact(true)
                .basic(true)
                .onClick {
                  (
                    _,
                    _
                  ) =>
                    context.onModeChanged(Mode.passwordRecoveryRequest, None)
                }("Lost my password")
            )
        )
      }
    }

  }

  val component = ScalaComponent
    .builder[Props]("LoginPage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(
    message:       Option[String],
    changeMessage: String => Callback
  ): Unmounted[Props, State, Backend] = component(Props(message, changeMessage))

}
