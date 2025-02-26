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

import app.LoginControllerState
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import org.scalajs.dom.window
import react.Toast
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.submit
import net.leibman.dmscreen.semanticUiReact.components.{Button, Form, FormField, Input, Label}
import org.scalajs.dom.HTMLFormElement

object PasswordRecoveryAfterTokenPage {

  case class State(
    password:      String = "",
    passwordAgain: String = ""
  )

  class Backend($ : BackendScope[Props, State]) {

    def handleSubmit(
      state: State,
      event: ReactEventFrom[HTMLFormElement]
    ): Callback =
      if (state.password.isEmpty)
        Callback(event.preventDefault()) >> Toast.error("Password can't be empty")
      else if (state.password != state.passwordAgain)
        Callback(event.preventDefault()) >> Toast.error("Passwords must match")
      else
        Callback.empty

    def render(
      props: Props,
      state: State
    ): VdomElement =
      LoginControllerState.ctx.consume { _ =>
        <.div(
          ^.width := 800.px,
          <.div(<.img(^.src := "/unauth/images/logo.png", ^.width := 350.px)),
          <.h1("Recover Password"),
          Form()
            .action("/unauth/passwordReset")
            .method("post")
            .onSubmit {
              (
                event,
                _
              ) =>
                handleSubmit(state, event)
            }(
              FormField()(
                Label()("Password"),
                Input()
                  .required(true)
                  .name("password")
                  .`type`("password")
                  .value(state.password)
                  .onChange {
                    (
                      _,
                      data
                    ) =>
                      $.modState(_.copy(password = data.value.get.asInstanceOf[String]))
                  }()
              ),
              FormField()(
                Label()("Repeat password"),
                Input()
                  .required(true)
                  .name("passwordAgain")
                  .`type`("password")
                  .value(state.passwordAgain)
                  .onChange {
                    (
                      _,
                      data
                    ) =>
                      $.modState(_.copy(passwordAgain = data.value.get.asInstanceOf[String]))
                  }()
              ),
              <.input(
                ^.`type` := "hidden",
                ^.id     := "token",
                ^.name   := "token",
                ^.value  := props.token.getOrElse("")
              ),
              Button()
                .compact(true)
                .basic(true)
                .`type`(submit)("Change password"),
              Button()
                .compact(true)
                .basic(true)
                .onClick {
                  (
                    _,
                    _
                  ) =>
                    Callback(window.location.replace("/"))
                }("Cancel")
            )
        )
      }

  }

  case class Props(token: Option[String])

  private val component = ScalaComponent
    .builder[Props]("PasswordRecoveryAfterTokenPage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(token: Option[String]): Unmounted[Props, State, Backend] = component(Props(token))

}
