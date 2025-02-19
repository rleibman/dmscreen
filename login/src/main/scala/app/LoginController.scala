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

package app

import japgolly.scalajs.react.React.Context
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, React, ScalaComponent}
import pages.*
import react.Toast
//import react.Toast
import org.scalajs.dom.window

enum Mode {

  case login, registration, passwordRecoveryRequest, passwordRecoveryAfterToken

}
import app.Mode.*
object LoginControllerState {

  val ctx: Context[LoginControllerState] = React.createContext(LoginControllerState())

}

case class LoginControllerState(
  mode:             Mode = login,
  messageForScreen: Option[String] = None,
  onModeChanged: (Mode, Option[String]) => Callback = {
    (
      _,
      _
    ) => Callback.empty
  }
)
object LoginController {

  lazy val queryParams = new org.scalajs.dom.URLSearchParams(window.location.search)

  case class State(
    context: LoginControllerState = LoginControllerState(),
    token:   Option[String] = None
  )

  class Backend($ : BackendScope[Unit, State]) {

    def onModeChanged(
      mode:             Mode,
      messageForScreen: Option[String]
    ): Callback = $.modState(s => s.copy(context = s.context.copy(mode = mode, messageForScreen = messageForScreen)))

    def render(state: State) = {
      LoginControllerState.ctx.provide(state.context) {
        <.div(
          Toast.render(),
          state.context.mode match {
            case Mode.login                      => LoginPage(state.context.messageForScreen)
            case Mode.registration               => RegistrationPage()
            case Mode.passwordRecoveryRequest    => PasswordRecoveryPage()
            case Mode.passwordRecoveryAfterToken => PasswordRecoveryAfterTokenPage(state.token)
          }
        )
      }
    }

  }
  private val component = ScalaComponent
    .builder[Unit]("LoginController")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount { $ =>
      $.modState(s =>
        s.copy(
          token = Option(queryParams.get("token")),
          context = s.context.copy(
            mode =
              if (queryParams.has("passwordReset"))
                Mode.passwordRecoveryAfterToken
              else
                Mode.login,
            onModeChanged = $.backend.onModeChanged,
            messageForScreen =
              if (queryParams.has("registrationFailed"))
                Option(
                  "La confirmación del registro fallo, lo sentimos mucho, tendrás que intentar de nuevo"
                )
              else if (queryParams.has("registrationSucceeded"))
                Option("Estas registrado! ya puedes usar tu correo y contraseña para ingresar!")
              else if (queryParams.has("passwordChangeFailed"))
                Option("El cambio de contraseña fallo, lo siento, tendrás que tratar otra vez.")
              else if (queryParams.has("passwordChangeSucceeded"))
                Option(
                  "El cambio de contraseña fue exitoso, ya puedes usar tu nueva contraseña para ingresar!"
                )
              else
                None
          )
        )
      )
    }
    .build

  def apply() = component()

}
