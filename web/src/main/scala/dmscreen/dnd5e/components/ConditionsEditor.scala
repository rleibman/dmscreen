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
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticWIDTHS
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

object ConditionsEditor {

  case class State(
    conditions: Set[Condition]
  )

  case class Props(
    conditions: Set[Condition],
    onChange:   Set[Condition] => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      def toggle(condition: Condition) = {
        Checkbox
          .toggle(true)
          .checked(state.conditions.contains(condition))
          .onChange {
            (
              _,
              changedData
            ) =>
              $.modState(
                s =>
                  s.copy(conditions =
                    if (changedData.checked.getOrElse(false))
                      s.conditions + condition
                    else {
                      s.conditions - condition
                    }
                  ),
                $.state.flatMap(s => props.onChange(s.conditions))
              )
          }
          .label(condition.toString.capitalize)
      }

      Table
        .inverted(DND5eUI.tableInverted)
        .color(DND5eUI.tableColor)
        .compact(true)(
          Table.Header(
            Table.Row(
              Table.HeaderCell.colSpan(2)(
                Button("Clear all conditions").onClick(
                  (
                    _,
                    _
                  ) =>
                    $.modState(
                      s => s.copy(conditions = Set.empty),
                      $.state.flatMap(s => props.onChange(s.conditions))
                    )
                )
              )
            )
          ),
          Table.Body(
            Table.Row(
              Table.Cell.width(SemanticWIDTHS.`8`)(toggle(Condition.blinded)),
              Table.Cell.width(SemanticWIDTHS.`8`)(toggle(Condition.charmed))
            ),
            Table.Row(
              Table.Cell(toggle(Condition.deafened)),
              Table.Cell(toggle(Condition.frightened))
            ),
            Table.Row(
              Table.Cell(toggle(Condition.grappled)),
              Table.Cell(toggle(Condition.incapacitated))
            ),
            Table.Row(
              Table.Cell(toggle(Condition.invisible)),
              Table.Cell(toggle(Condition.paralyzed))
            ),
            Table.Row(
              Table.Cell(toggle(Condition.petrified)),
              Table.Cell(toggle(Condition.poisoned))
            ),
            Table.Row(
              Table.Cell(toggle(Condition.prone)),
              Table.Cell(toggle(Condition.restrained))
            ),
            Table.Row(
              Table.Cell(toggle(Condition.stunned)),
              Table.Cell(toggle(Condition.unconscious))
            )
          )
        )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props) => "") // make sure the props are ignored for re-rendering the component

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("ConditionsEditor")
    .initialStateFromProps(p => State(p.conditions))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    conditions: Set[Condition],
    onChange:   Set[Condition] => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(conditions = conditions, onChange = onChange))

}
