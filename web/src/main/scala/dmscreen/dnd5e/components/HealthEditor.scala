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
import dmscreen.util.*

object HealthEditor {

  case class State(
    health: Health
  )

  case class Props(
    health:   Health,
    onChange: Health => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      <.div(
        ^.backgroundColor := state.health.lifeColor,
        <.table(
          <.tbody(
            <.tr(
              <.th("Death Save Status"),
              <.td(s"${state.health.deathSave.fails} fails, ${state.health.deathSave.successes} successes ${
                  if (state.health.deathSave.isStabilized) "Stabilized" else ""
                }")
            ).when(state.health.currentHitPoints <= 0),
            <.tr(
              <.th("Current Hit Points"),
              <.td(
                Input
                  .`type`("number")
                  .max(state.health.currentMax)
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
                          val newNum = changedData.value.asInt()
                          s.copy(health =
                            s.health.copy(
                              deathSave =
                                if (newNum <= 0) DeathSave(fails = 0, successes = 0, isStabilized = false)
                                else s.health.deathSave,
                              currentHitPoints = newNum
                            )
                          )
                        },
                        $.state.flatMap(s => props.onChange(s.health))
                      )
                  )
                  .value(state.health.currentHitPoints)
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
                          val newNum = changedData.value.asInt()
                          s.copy(health = s.health.copy(maxHitPoints = newNum))
                        },
                        $.state.flatMap(s => props.onChange(s.health))
                      )
                  )
                  .value(state.health.maxHitPoints)
              )
            ),
            <.tr(
              <.td(
                Checkbox
                  .toggle(true)
                  .label("Override Max Hit Points")
                  .checked(state.health.overrideMaxHitPoints.isDefined)
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
                          s.copy(health = s.health.copy(overrideMaxHitPoints = newVal))
                        },
                        $.state.flatMap(s => props.onChange(s.health))
                      )
                  )
              ),
              <.td(
                Input
                  .`type`("number")
                  .min(0)
                  .disabled(state.health.overrideMaxHitPoints.isEmpty)
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
                          val newNum = changedData.value.asInt()
                          s.copy(health = s.health.copy(overrideMaxHitPoints = if (newNum <= 0) None else Some(newNum)))
                        },
                        $.state.flatMap(s => props.onChange(s.health))
                      )
                  )
                  .value(state.health.overrideMaxHitPoints.getOrElse(0))
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
                          val newNum = changedData.value.asInt()
                          s.copy(health = s.health.copy(temporaryHitPoints = newNum))
                        },
                        $.state.flatMap(s => props.onChange(s.health))
                      )
                  )
                  .value(state.health.temporaryHitPoints)
              )
            )
          )
        )
      )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props).health)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("HitPointsDialog")
    .initialStateFromProps(p => State(p.health))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    health:   Health,
    onChange: Health => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(health, onChange))

}
