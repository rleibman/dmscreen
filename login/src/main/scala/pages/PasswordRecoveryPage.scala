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
import japgolly.scalajs.react.*
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom.HTMLInputElement
import react.Toast
import net.leibman.dmscreen.semanticUiReact.components.{Button, Form, FormField, Input, Label}
import net.leibman.dmscreen.semanticUiReact.distCommonjsElementsInputInputMod.InputOnChangeData

import scala.util.{Failure, Success}
import zio.json.*
object PasswordRecoveryPage {

  case class State(
    submitted: Boolean = false,
    email:     String = ""
  )

  class Backend($ : BackendScope[Unit, State]) {

    def onSubmitEmailAddress(email: String) =
      Ajax("POST", s"/unauth/passwordRecoveryRequest")
        .and(_.withCredentials = true)
        .setRequestContentTypeJson
        .send(email.toJson)
        .asAsyncCallback
        .completeWith {
          case Success(xhr) if xhr.status < 300 =>
            $.modState(_.copy(submitted = true))
          case Success(xhr) =>
            Toast.error(
              s"There was an error with your email: ${xhr.statusText}, please try again."
            )
          case Failure(e: Throwable) =>
            Toast.error(
              s"There was an error with your email: ${e.getLocalizedMessage}, please try again."
            )
            e.printStackTrace()
            Callback(e.printStackTrace())
        }

    def render(state: State) =
      LoginControllerState.ctx.consume { _ =>
        <.div(
          <.div(<.img(^.src := "/resources/images/logo.png", ^.width := 350.px)),
          <.h1("Recover password!"),
          if (state.submitted)
            <.div(
              <.p(
                "We have sent you instructions by email to change your password, you have limited time, if you take too long you'll have to try again"
              ),
              <.p(
                Button()
                  .compact(true)
                  .basic(true)
                  .onClick {
                    (
                      _,
                      _
                    ) =>
                      $.modState(_.copy(submitted = false))
                  }("Try again")
              )
            )
          else {
            <.div(
              "Please enter your email, we'll send you instructions to change your password",
              Form()(
                FormField()(
                  Label()("Email"),
                  Input()
                    .required(true)
                    .name("email")
                    .`type`("email")
                    .value(state.email)
                    .onChange {
                      (
                        _:    ReactEventFrom[HTMLInputElement],
                        data: InputOnChangeData
                      ) =>
                        $.modState(_.copy(email = data.value.get.asInstanceOf[String]))
                    }()
                ),
                Button()
                  .compact(true)
                  .basic(true)
                  .onClick {
                    (
                      _,
                      _
                    ) =>
                      onSubmitEmailAddress(state.email)
                  }("Ok")
              )
            )
          }
        )
      }

  }

  val component = ScalaComponent
    .builder[Unit]("PasswordRecoveryPage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply() = component()

}
