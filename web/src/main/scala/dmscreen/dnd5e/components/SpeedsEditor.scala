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
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticSIZES
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

object SpeedsEditor {

  case class State(
    speeds: List[Speed]
  )

  case class Props(
    speeds:   List[Speed],
    onChange: List[Speed] => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      Table
        .inverted(DND5eUI.tableInverted)
        .color(DND5eUI.tableColor)(
          Table.Header(
            Table.Row(
              Table.HeaderCell("walk"),
              Table.HeaderCell("fly"),
              Table.HeaderCell("swim"),
              Table.HeaderCell("climb"),
              Table.HeaderCell("burrow")
            )
          ),
          Table.Body(
            Table.Row(
              Table.Cell(
                Input
                  .`type`("number")
                  .min(0)
                  .step(5)
                  .style(CSSProperties().set("width", 100.px))
                  .maxLength(4)
                  .size(SemanticSIZES.mini)
                  .onChange(
                    (
                      _,
                      data
                    ) => {
                      val newVal = data.value match {
                        case x: Double => x.toInt
                        case s: String =>
                          s.toIntOption
                            .orElse(state.speeds.find(_.speedType == SpeedType.walk).map(_.value)).getOrElse(0)
                        case _ => 0
                      }
                      $.modState(
                        s =>
                          s.copy(speeds =
                            s.speeds.filter(_.speedType != SpeedType.walk) ++
                              (if (newVal > 0) Seq(Speed(SpeedType.walk, newVal))
                               else Seq.empty)
                          ),
                        $.state.flatMap(s => props.onChange(s.speeds))
                      )
                    }
                  )
                  .value(
                    state.speeds.find(_.speedType == SpeedType.walk).map(_.value).getOrElse(0).toString
                  )
              ),
              Table.Cell(
                Input
                  .`type`("number")
                  .min(0)
                  .step(5)
                  .style(CSSProperties().set("width", 100.px))
                  .maxLength(4)
                  .size(SemanticSIZES.mini)
                  .onChange(
                    (
                      _,
                      data
                    ) => {
                      val newVal = data.value match {
                        case x: Double => x.toInt
                        case s: String =>
                          s.toIntOption
                            .orElse(state.speeds.find(_.speedType == SpeedType.fly).map(_.value)).getOrElse(0)
                        case _ => 0
                      }
                      $.modState(
                        s =>
                          s.copy(speeds =
                            s.speeds.filter(_.speedType != SpeedType.fly) ++
                              (if (newVal > 0) Seq(Speed(SpeedType.fly, newVal))
                               else Seq.empty)
                          ),
                        $.state.flatMap(s => props.onChange(s.speeds))
                      )
                    }
                  )
                  .value(
                    state.speeds.find(_.speedType == SpeedType.fly).map(_.value).getOrElse(0).toString
                  )
              ),
              Table.Cell(
                Input
                  .`type`("number")
                  .min(0)
                  .step(5)
                  .style(CSSProperties().set("width", 100.px))
                  .maxLength(4)
                  .size(SemanticSIZES.mini)
                  .onChange(
                    (
                      _,
                      data
                    ) => {
                      val newVal = data.value match {
                        case x: Double => x.toInt
                        case s: String =>
                          s.toIntOption
                            .orElse(state.speeds.find(_.speedType == SpeedType.swim).map(_.value)).getOrElse(0)
                        case _ => 0
                      }
                      $.modState(
                        s =>
                          s.copy(speeds =
                            s.speeds.filter(_.speedType != SpeedType.swim) ++
                              (if (newVal > 0) Seq(Speed(SpeedType.swim, newVal))
                               else Seq.empty)
                          ),
                        $.state.flatMap(s => props.onChange(s.speeds))
                      )
                    }
                  )
                  .value(
                    state.speeds.find(_.speedType == SpeedType.swim).map(_.value).getOrElse(0).toString
                  )
              ),
              Table.Cell(
                Input
                  .`type`("number")
                  .min(0)
                  .step(5)
                  .style(CSSProperties().set("width", 100.px))
                  .maxLength(4)
                  .size(SemanticSIZES.mini)
                  .onChange(
                    (
                      _,
                      data
                    ) => {
                      val newVal = data.value match {
                        case x: Double => x.toInt
                        case s: String =>
                          s.toIntOption
                            .orElse(state.speeds.find(_.speedType == SpeedType.climb).map(_.value)).getOrElse(0)
                        case _ => 0
                      }
                      $.modState(
                        s =>
                          s.copy(speeds =
                            s.speeds.filter(_.speedType != SpeedType.climb) ++
                              (if (newVal > 0) Seq(Speed(SpeedType.climb, newVal))
                               else Seq.empty)
                          ),
                        $.state.flatMap(s => props.onChange(s.speeds))
                      )
                    }
                  )
                  .value(
                    state.speeds.find(_.speedType == SpeedType.climb).map(_.value).getOrElse(0).toString
                  )
              ),
              Table.Cell(
                Input
                  .`type`("number")
                  .min(0)
                  .step(5)
                  .style(CSSProperties().set("width", 100.px))
                  .maxLength(4)
                  .size(SemanticSIZES.mini)
                  .onChange(
                    (
                      _,
                      data
                    ) => {
                      val newVal = data.value match {
                        case x: Double => x.toInt
                        case s: String =>
                          s.toIntOption
                            .orElse(state.speeds.find(_.speedType == SpeedType.burrow).map(_.value)).getOrElse(0)
                        case _ => 0
                      }
                      $.modState(
                        s =>
                          s.copy(speeds =
                            s.speeds.filter(_.speedType != SpeedType.burrow) ++
                              (if (newVal > 0) Seq(Speed(SpeedType.burrow, newVal))
                               else Seq.empty)
                          ),
                        $.state.flatMap(s => props.onChange(s.speeds))
                      )
                    }
                  )
                  .value(
                    state.speeds.find(_.speedType == SpeedType.burrow).map(_.value).getOrElse(0).toString
                  )
              )
            )
          )
        )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.by((s: State) => s.speeds.mkString) // make sure the state is ignored for re-rendering the component
  given Reusability[Props] = Reusability.by((_: Props) => "") // make sure the props are ignored for re-rendering the component

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("SpeedsEditor")
    .initialStateFromProps(p => State(p.speeds))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    speeds:   List[Speed],
    onChange: List[Speed] => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(speeds = speeds, onChange = onChange))

}
