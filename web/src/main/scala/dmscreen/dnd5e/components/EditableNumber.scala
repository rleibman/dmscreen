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

package dmscreen.dnd5e.components

import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticSIZES
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

import scala.scalajs.js
import scala.scalajs.js.UndefOr

object EditableNumber {

  case class State(
    value:     Double,
    isEditing: Boolean = false
  )

  case class Props(
    value:        Double,
    allowEditing: Boolean,
    min:          UndefOr[Double] = js.undefined,
    max:          UndefOr[Double] = js.undefined,
    onChange:     Double => Callback = _ => Callback.empty
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      p: Props,
      s: State
    ): VdomNode = {
      def doBlur = $.modState(_.copy(isEditing = false)) >> $.props.flatMap(_.onChange(s.value))

      if (s.isEditing) {
        Input
          .`type`("Number")
          .value(s.value)
          .set("min", p.min)
          .set("max", p.max)
          .maxLength(5)
          .style(CSSProperties().set("width", 60.px))
          .autoFocus(true)
          .size(semanticUiReactStrings.mini)
          .onChange {
            (
              _,
              d
            ) =>
              val newVal = d.value match {
                case s: String => s.toDouble
                case d: Double => d
              }

              $.modState(_.copy(value = newVal))
          }
          .onKeyUp { e =>
            if (e.key == "Enter" || e.keyCode == 13) doBlur else Callback.empty
          }
          .onBlur(_ => doBlur)
      } else {
        if (p.allowEditing)
          <.span(
            ^.onClick --> $.modState(_.copy(isEditing = true)),
            s.value
          )
        else
          <.span(s.value)
      }
    }

  }

  import scala.language.unsafeNulls
  given Reusability[State] = Reusability.by(s => (s.value.toString, s.isEditing))
  given Reusability[Props] = Reusability.by((_: Props).value.toString)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("EditableNumber")
    .initialStateFromProps(p => State(p.value))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    value:        Double,
    allowEditing: Boolean = true,
    min:          UndefOr[Double] = js.undefined,
    max:          UndefOr[Double] = js.undefined,
    onChange:     Double => Callback = _ => Callback.empty
  ): VdomElement = component(Props(value, allowEditing, min, max, onChange))

}
