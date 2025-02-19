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
import org.scalajs.dom.window
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.submit
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticCOLORS

object LoginPage {

  class Backend($ : BackendScope[Props, Unit]) {

    val query: String = if (window.location.search.isEmpty) "" else window.location.search.substring(1).nn
    val isBad: Boolean = query.contains("bad=true")
    def render(
      props: Props
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
          props.messageForScreen.fold(EmptyVdom: VdomNode)(str => <.span(Message()(str))),
          <.form(
            ^.action    := "doLogin",
            ^.method    := "post",
            ^.className := "ui form",
            ^.width     := 800.px,
            FormField()(
              Label()("Email"),
              Input().required(true).name("email").`type`("email")()
            ),
            FormField()(
              Label()("Password"),
              Input().`type`("password").required(true).name("password")()
            ),
            Button().compact(true).basic(true).`type`(submit)("Go!")
          ),
          Button()
            .compact(true)
            .basic(true)
            .onClick {
              (
                _,
                _
              ) =>
                context.onModeChanged(Mode.registration, None)
            }("Register"),
          Button()
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
      }
    }

  }

  val component = ScalaComponent
    .builder[Props]("LoginPage")
    .renderBackend[Backend]
    .build

  case class Props(messageForScreen: Option[String])

  def apply(messageForScreen: Option[String]): Unmounted[Props, Unit, Backend] = component(Props(messageForScreen))

}
