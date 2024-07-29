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

package dmscreen.dnd5e.pages

import dmscreen.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, Ref, Reusability, ScalaComponent, ScalaFnComponent}
import net.leibman.dmscreen.reactQuill.components.ReactQuill
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHS}

object CampaignLog {

  case class State(logOpen: Boolean = false)

  case class Backend($ : BackendScope[Unit, State]) {

    def render(state: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        Modal
          .open(state.logOpen)
          .closeIcon(true)
          .onClose(
            (
              _,
              _
            ) => $.modState(_.copy(logOpen = false))
          )(
            Modal.Header(<.h2("Campaign Log")),
            Modal.Content(
              Container.className("campaignLog")(
                <.table(
                  ^.className := "campaignLog",
                  <.tbody(
                    dmScreenState.campaignLog.map(logEntry =>
                      <.tr(
                        <.td(logEntry)
                      )
                    )*
                  )
                )
              )
            )
          )
      }
    }

  }
  private val component =
    ScalaComponent
      .builder[Unit]("CampaignLog")
      .initialState(State())
      .renderBackend[Backend]
      .build

  private val ref = Ref.toScalaComponent(component)

  def render(): Unmounted[Unit, State, Backend] = ref.component()

  def showLog: Callback =
    ref.get
      .flatMap(
        _.fold(Callback.empty)(_.backend.$.modState(_.copy(logOpen = true)))
      )

}
