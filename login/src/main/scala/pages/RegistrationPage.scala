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

import java.time.{Instant, LocalDateTime}
import app.{LoginControllerState, Mode}
import auth.{User, UserCreationRequest, UserCreationResponse, UserId, given}
import dmscreen.{*, given}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{ReactMouseEventFrom, *}
import org.scalajs.dom.HTMLButtonElement
import org.scalajs.dom.window
import react.Toast
import net.leibman.dmscreen.semanticUiReact.distCommonjsElementsButtonButtonMod.ButtonProps
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticWIDTHS
import net.leibman.dmscreen.semanticUiReact.distCommonjsElementsInputInputMod.InputOnChangeData
import zio.json.*

object RegistrationPage {

  case class State(
    passwordPair: (String, String) = ("", ""),
    user:         User = User(id = UserId.empty, email = "", name = "", created = LocalDateTime.now)
  )

  class Backend($ : BackendScope[Unit, State]) {

    private def onUserInputChange(fn: (User, String) => User) = {
      (
        _:   ReactEventFromInput,
        obj: InputOnChangeData
      ) =>
        $.modState(state => state.copy(user = fn(state.user, obj.value.get.asInstanceOf[String])))
    }

    private def validate(state: State): Seq[String] =
      Seq.empty[String] ++
        (if (state.passwordPair._1.trim.nn.isEmpty) Seq("Password is required")
         else Nil) ++
        (if (state.passwordPair._1 != state.passwordPair._2)
           Seq("Both passwords need to match")
         else Nil) ++
        (if (state.user.name.trim.nn.isEmpty) Seq("name cannot be emtpy") else Nil) ++
        (if (state.user.email.trim.nn.isEmpty)
           Seq("email can't be empty")
         else Nil)

    private def doCreate(
      state:   State,
      context: LoginControllerState
    ): (ReactMouseEventFrom[HTMLButtonElement], ButtonProps) => Callback = {
      (
        _: ReactMouseEventFrom[HTMLButtonElement],
        _: ButtonProps
      ) =>
        {
          val async: AsyncCallback[Callback] = for {
            saved <-
              Ajax("PUT", "/userCreation").setRequestContentTypeJson
                .send(UserCreationRequest(state.user, state.passwordPair._1).toJson)
                .asAsyncCallback
                .map { xhr =>
                  if (xhr.status < 300) {
                    xhr.responseText
                      .fromJson[UserCreationResponse]
                      .fold(
                        e => Toast.error(e),
                        response =>
                          response.error.fold(
                            context.onModeChanged(Mode.login, response.error) >> Toast.success(
                              "Account created successfully, please await for an email to confirm registration"
                            )
                          )(errorMsg => Toast.error(errorMsg))
                      )
                  } else Toast.error(s"Error Creating Account: ${xhr.statusText}")
                }
          } yield saved
          val valid: Seq[String] = validate(state)
          if (valid.nonEmpty)
            Toast.error(valid.map(s => <.p(s)).toVdomArray)
          else
            async.completeWith(_.get)
        }
    }

    private def renderUserInfo(
      state:   State,
      context: LoginControllerState
    ): VdomElement =
      <.div(
        FormGroup()(
          FormField().width(SemanticWIDTHS.`3`)(
            Label()("Name"),
            Input()
              .onChange(
                onUserInputChange(
                  (
                    user,
                    value
                  ) => user.copy(name = value)
                )
              )
              .value(state.user.name)()
          )
        ),
        FormGroup()(
          FormField().width(SemanticWIDTHS.`6`)(
            Label()("Email"),
            Input()
              .`type`("email")
              .onChange(
                onUserInputChange(
                  (
                    user,
                    value
                  ) => user.copy(email = value)
                )
              )
              .value(state.user.email)()
          )
        ),
        FormGroup()(
          FormField().width(SemanticWIDTHS.`3`)(
            Label()("Password"),
            Input()
              .required(true)
              .name("password")
              .`type`("password")
              .value(state.passwordPair._1)
              .onChange {
                (
                  _,
                  obj
                ) =>
                  $.modState(state =>
                    state.copy(passwordPair = (obj.value.get.asInstanceOf[String], state.passwordPair._2))
                  )
              }()
          ),
          FormField().width(SemanticWIDTHS.`3`)(
            Label()("Repeat password"),
            Input()
              .`type`("password")
              .name("password")
              .value(state.passwordPair._2)
              .onChange {
                (
                  _,
                  obj
                ) =>
                  $.modState(state =>
                    state.copy(passwordPair = (state.passwordPair._1, obj.value.get.asInstanceOf[String]))
                  )
              }()
          )
        ),
        FormGroup()(
          Button()
            .compact(true)
            .basic(true)
            .onClick(doCreate(state, context))("Create account"),
          Button()
            .compact(true)
            .basic(true)
            .onClick {
              (
                _: ReactMouseEventFrom[HTMLButtonElement],
                _: ButtonProps
              ) =>
                Callback(window.location.replace("/"))
            }("Cancelar")
        )
      )

    def render(state: State): VdomElement =
      LoginControllerState.ctx.consume { context =>
        <.div(
          <.div(<.img(^.src := "/unauth/images/logo.png", ^.width := 350.px)),
          <.h1("Account registration"),
          Form()(
            renderUserInfo(state, context)
          )
        )
      }

  }

  private val component = ScalaComponent
    .builder[Unit]("RegistrationPage")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
