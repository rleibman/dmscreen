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

object SensesEditor {

  case class State(
    senses: List[SenseRange]
  )

  case class Props(
    senses:   List[SenseRange],
    onChange: List[SenseRange] => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      Table(
        Table.Header(
          Table.Row(
            Table.HeaderCell("Sight"),
            Table.HeaderCell("Blindsight"),
            Table.HeaderCell("Darkvision"),
            Table.HeaderCell("Tremorsense"),
            Table.HeaderCell("Truesight"),
            Table.HeaderCell("Scent"),
            Table.HeaderCell("Other")
          )
        ),
        Table.Body(
          Table.Row(
            Table.Cell(
              Input
                .`type`("number")
                .min(0)
                .step(5)
                .style(CSSProperties().set("width", 70.px))
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
                        s.toIntOption.orElse(state.senses.find(_.sense == Sense.sight).map(_.range)).getOrElse(0)
                      case _ => 0
                    }
                    $.modState(
                      s =>
                        s.copy(senses =
                          s.senses.filter(_.sense != Sense.sight) ++
                            (if (newVal > 0) Seq(SenseRange(Sense.sight, newVal))
                             else Seq.empty)
                        ),
                      $.state.flatMap(s => props.onChange(s.senses))
                    )
                  }
                )
                .value(
                  state.senses.find(_.sense == Sense.sight).map(_.range).getOrElse(0).toString
                )
            ),
            Table.Cell(
              Input
                .`type`("number")
                .min(0)
                .step(5)
                .style(CSSProperties().set("width", 70.px))
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
                        s.toIntOption.orElse(state.senses.find(_.sense == Sense.blindsight).map(_.range)).getOrElse(0)
                      case _ => 0
                    }
                    $.modState(
                      s =>
                        s.copy(senses =
                          s.senses.filter(_.sense != Sense.blindsight) ++
                            (if (newVal > 0) Seq(SenseRange(Sense.blindsight, newVal))
                             else Seq.empty)
                        ),
                      $.state.flatMap(s => props.onChange(s.senses))
                    )
                  }
                )
                .value(
                  state.senses.find(_.sense == Sense.blindsight).map(_.range).getOrElse(0).toString
                )
            ),
            Table.Cell(
              Input
                .`type`("number")
                .min(0)
                .step(5)
                .style(CSSProperties().set("width", 70.px))
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
                        s.toIntOption.orElse(state.senses.find(_.sense == Sense.darkvision).map(_.range)).getOrElse(0)
                      case _ => 0
                    }
                    $.modState(
                      s =>
                        s.copy(senses =
                          s.senses.filter(_.sense != Sense.darkvision) ++
                            (if (newVal > 0) Seq(SenseRange(Sense.darkvision, newVal))
                             else Seq.empty)
                        ),
                      $.state.flatMap(s => props.onChange(s.senses))
                    )
                  }
                )
                .value(
                  state.senses.find(_.sense == Sense.darkvision).map(_.range).getOrElse(0).toString
                )
            ),
            Table.Cell(
              Input
                .`type`("number")
                .min(0)
                .step(5)
                .style(CSSProperties().set("width", 70.px))
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
                          .orElse(state.senses.find(_.sense == Sense.tremorsense).map(_.range)).getOrElse(0)
                      case _ => 0
                    }
                    $.modState(
                      s =>
                        s.copy(senses =
                          s.senses.filter(_.sense != Sense.tremorsense) ++
                            (if (newVal > 0) Seq(SenseRange(Sense.tremorsense, newVal))
                             else Seq.empty)
                        ),
                      $.state.flatMap(s => props.onChange(s.senses))
                    )
                  }
                )
                .value(
                  state.senses.find(_.sense == Sense.tremorsense).map(_.range).getOrElse(0).toString
                )
            ),
            Table.Cell(
              Input
                .`type`("number")
                .min(0)
                .step(5)
                .style(CSSProperties().set("width", 70.px))
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
                          .orElse(state.senses.find(_.sense == Sense.truesight).map(_.range)).getOrElse(0)
                      case _ => 0
                    }
                    $.modState(
                      s =>
                        s.copy(senses =
                          s.senses.filter(_.sense != Sense.truesight) ++
                            (if (newVal > 0) Seq(SenseRange(Sense.truesight, newVal))
                             else Seq.empty)
                        ),
                      $.state.flatMap(s => props.onChange(s.senses))
                    )
                  }
                )
                .value(
                  state.senses.find(_.sense == Sense.truesight).map(_.range).getOrElse(0).toString
                )
            ),
            Table.Cell(
              Input
                .`type`("number")
                .min(0)
                .step(5)
                .style(CSSProperties().set("width", 70.px))
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
                          .orElse(state.senses.find(_.sense == Sense.scent).map(_.range)).getOrElse(0)
                      case _ => 0
                    }
                    $.modState(
                      s =>
                        s.copy(senses =
                          s.senses.filter(_.sense != Sense.scent) ++
                            (if (newVal > 0) Seq(SenseRange(Sense.scent, newVal))
                             else Seq.empty)
                        ),
                      $.state.flatMap(s => props.onChange(s.senses))
                    )
                  }
                )
                .value(
                  state.senses.find(_.sense == Sense.scent).map(_.range).getOrElse(0).toString
                )
            ),
            Table.Cell(
              Input
                .`type`("number")
                .min(0)
                .step(5)
                .style(CSSProperties().set("width", 70.px))
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
                          .orElse(state.senses.find(_.sense == Sense.other).map(_.range)).getOrElse(0)
                      case _ => 0
                    }
                    $.modState(
                      s =>
                        s.copy(senses =
                          s.senses.filter(_.sense != Sense.other) ++
                            (if (newVal > 0) Seq(SenseRange(Sense.other, newVal))
                             else Seq.empty)
                        ),
                      $.state.flatMap(s => props.onChange(s.senses))
                    )
                  }
                )
                .value(
                  state.senses.find(_.sense == Sense.other).map(_.range).getOrElse(0).toString
                )
            )
          )
        )
      )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.by((s: State) => s.senses.mkString) // make sure the state is ignored for re-rendering the component
  given Reusability[Props] = Reusability.by((_: Props) => "") // make sure the props are ignored for re-rendering the component

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("SensesEditor")
    .initialStateFromProps(p => State(p.senses))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    senses:   List[SenseRange],
    onChange: List[SenseRange] => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(senses = senses, onChange = onChange))

}
