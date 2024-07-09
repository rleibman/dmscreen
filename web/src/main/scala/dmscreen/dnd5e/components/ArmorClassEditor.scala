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

import dmscreen.dnd5e.*
import dmscreen.dnd5e.components.HealthEditor.State
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticSIZES, SemanticWIDTHS}
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

object ArmorClassEditor {

  case class State(
    armorClass: Int
  )

  case class Props(
    armorClass: Int,
    onChange:   Int => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      <.div(
        Input
          .`type`("number").min(-10).max(40).onChange(
            (
              _,
              changedData
            ) =>
              $.modState(
                s => {
                  val newNum = changedData.value match {
                    case s: String => s.toIntOption.getOrElse(10)
                    case x: Double => x.toInt
                  }

                  s.copy(armorClass = newNum)
                },
                $.state.flatMap(s => props.onChange(s.armorClass))
              )
          )
          .value(state.armorClass)
          // ENHANCEMENT add explanation of Armor class here, and maybe split the ac into multiple inputs, but frankly it's too complicated to
          // calculate all of that here for now
      )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props) => "") // make sure the props are ignored for re-rendering the component

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("ArmorClassEditor")
    .initialStateFromProps(p => State(p.armorClass))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    armorClass: Int,
    onChange:   Int => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(armorClass = armorClass, onChange = onChange))

}
