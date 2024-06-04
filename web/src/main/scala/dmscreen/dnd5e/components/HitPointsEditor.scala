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

object HitPointsEditor {

  case class State(
    hitPoints: HitPoints
  )

  case class Props(
    hitPoints: HitPoints,
    onChange:  HitPoints => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      <.div(
        ^.backgroundColor := state.hitPoints.lifeColor,
        <.table(
          <.tbody(
            state.hitPoints.currentHitPoints match {
              case ds: DeathSave =>
                <.tr(
                  <.th("Death Save Status"),
                  <.td(s"${ds.fails} fails, ${ds.successes} successes ${if (ds.isStabilized) "Stabilized" else ""}")
                )
              case _: Int => EmptyVdom
            },
            <.tr(
              <.th("Current Hit Points"),
              <.td(
                Input
                  .`type`("number")
                  .max(state.hitPoints.currentMax)
                  .onChange(
                    (
                      _,
                      changedData
                    ) =>
                      $.modState(
                        (
                          s,
                          p
                        ) => {
                          val newNum = changedData.value match {
                            case s: String => s.toIntOption.getOrElse(0)
                            case x: Double => x.toInt
                          }
                          s.copy(hitPoints =
                            s.hitPoints.copy(currentHitPoints = if (newNum <= 0) DeathSave(0, 0) else newNum)
                          )
                        },
                        $.state.flatMap(s => props.onChange(s.hitPoints))
                      )
                  )
                  .value(state.hitPoints.currentHitPoints match {
                    case _: DeathSave => 0
                    case i: Int       => i
                  })
              )
            ),
            <.tr(
              <.th("Max Hit Points"),
              <.td(
                Input
                  .`type`("number")
                  .min(0)
                  .onChange(
                    (
                      _,
                      changedData
                    ) =>
                      $.modState(
                        (
                          s,
                          p
                        ) => {
                          val newNum = changedData.value match {
                            case s: String => s.toIntOption.getOrElse(0)
                            case x: Double => x.toInt
                          }
                          s.copy(hitPoints = s.hitPoints.copy(maxHitPoints = newNum))
                        },
                        $.state.flatMap(s => props.onChange(s.hitPoints))
                      )
                  )
                  .value(state.hitPoints.maxHitPoints)
              )
            ),
            <.tr(
              <.td(
                Checkbox
                  .toggle(true)
                  .label("Override Max Hit Points")
                  .checked(state.hitPoints.overrideMaxHitPoints.isDefined)
                  .onChange(
                    (
                      _,
                      changedData
                    ) =>
                      $.modState(
                        (
                          s,
                          p
                        ) => {
                          val newVal = if (changedData.checked.getOrElse(false)) Some(0) else None
                          s.copy(hitPoints = s.hitPoints.copy(overrideMaxHitPoints = newVal))
                        },
                        $.state.flatMap(s => props.onChange(s.hitPoints))
                      )
                  )
              ),
              <.td(
                Input
                  .`type`("number")
                  .min(0)
                  .disabled(state.hitPoints.overrideMaxHitPoints.isEmpty)
                  .onChange(
                    (
                      _,
                      changedData
                    ) =>
                      $.modState(
                        (
                          s,
                          p
                        ) => {
                          val newNum = changedData.value match {
                            case s: String => s.toIntOption.getOrElse(0)
                            case x: Double => x.toInt
                          }
                          s.copy(hitPoints =
                            s.hitPoints.copy(overrideMaxHitPoints = if (newNum <= 0) None else Some(newNum))
                          )
                        },
                        $.state.flatMap(s => props.onChange(s.hitPoints))
                      )
                  )
                  .value(state.hitPoints.overrideMaxHitPoints.getOrElse(0))
              )
            ),
            <.tr(
              <.th("Temporary Hit Points"),
              <.td(
                Input
                  .`type`("number")
                  .min(0)
                  .onChange(
                    (
                      _,
                      changedData
                    ) =>
                      $.modState(
                        (
                          s,
                          p
                        ) => {
                          val newNum = changedData.value match {
                            case s: String => s.toIntOption.getOrElse(0)
                            case x: Double => x.toInt
                          }
                          s.copy(hitPoints = s.hitPoints.copy(temporaryHitPoints = newNum))
                        },
                        $.state.flatMap(s => props.onChange(s.hitPoints))
                      )
                  )
                  .value(state.hitPoints.temporaryHitPoints)
              )
            )
          )
        )
      )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props).hitPoints)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("HitPointsDialog")
    .initialStateFromProps(p => State(p.hitPoints))
    .renderBackend[Backend]
    .componentDidMount($ => Callback.empty)
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    hitPoints: HitPoints,
    onChange:  HitPoints => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(hitPoints, onChange))

}
