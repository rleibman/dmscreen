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

object AbilitiesEditor {

  case class State(
    abilities: Abilities
  )

  case class Props(
    abilities: Abilities,
    onChange:  Abilities => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      val abilities = state.abilities
      Table(
        Table.Header(
          Table.Row(
            Table.HeaderCell(),
            Table.HeaderCell("Str"),
            Table.HeaderCell("Dex"),
            Table.HeaderCell("Con"),
            Table.HeaderCell("Int"),
            Table.HeaderCell("Wis"),
            Table.HeaderCell("Cha")
          )
        ),
        Table.Body(
          Table.Row(
            Table.HeaderCell("Value"),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(1).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.getOrElse(abilities.strength.value)
                          case x: Double => x.toInt
                        }
                        s.copy(abilities = s.abilities.copy(strength = s.abilities.strength.copy(value = newNum)))
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(abilities.strength.value)
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(1).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.getOrElse(abilities.dexterity.value)
                          case x: Double => x.toInt
                        }
                        s.copy(abilities = s.abilities.copy(dexterity = s.abilities.dexterity.copy(value = newNum)))
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(abilities.dexterity.value)
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(1).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.getOrElse(abilities.constitution.value)
                          case x: Double => x.toInt
                        }
                        s.copy(abilities =
                          s.abilities.copy(constitution = s.abilities.constitution.copy(value = newNum))
                        )
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(abilities.constitution.value)
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(1).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.getOrElse(abilities.intelligence.value)
                          case x: Double => x.toInt
                        }
                        s.copy(abilities =
                          s.abilities.copy(intelligence = s.abilities.intelligence.copy(value = newNum))
                        )
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(abilities.intelligence.value)
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(1).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.getOrElse(abilities.wisdom.value)
                          case x: Double => x.toInt
                        }
                        s.copy(abilities = s.abilities.copy(wisdom = s.abilities.wisdom.copy(value = newNum)))
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(abilities.wisdom.value)
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(1).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.getOrElse(abilities.charisma.value)
                          case x: Double => x.toInt
                        }
                        s.copy(abilities = s.abilities.copy(charisma = s.abilities.charisma.copy(value = newNum)))
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(abilities.charisma.value)
            )
          ),
          Table.Row(
            Table.HeaderCell("Overriden Value"),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(0).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.orElse(abilities.strength.overrideValue).getOrElse(0)
                          case x: Double => x.toInt
                        }

                        s.copy(abilities =
                          s.abilities.copy(strength =
                            s.abilities.strength.copy(overrideValue = if (newNum == 0) None else Some(newNum))
                          )
                        )
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(
                  abilities.strength.overrideValue.getOrElse(0)
                )
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(0).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.orElse(abilities.dexterity.overrideValue).getOrElse(0)
                          case x: Double => x.toInt
                        }

                        s.copy(abilities =
                          s.abilities.copy(dexterity =
                            s.abilities.dexterity.copy(overrideValue = if (newNum == 0) None else Some(newNum))
                          )
                        )
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(
                  abilities.dexterity.overrideValue.getOrElse(0)
                )
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(0).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.orElse(abilities.constitution.overrideValue).getOrElse(0)
                          case x: Double => x.toInt
                        }

                        s.copy(abilities =
                          s.abilities.copy(constitution =
                            s.abilities.constitution.copy(overrideValue = if (newNum == 0) None else Some(newNum))
                          )
                        )
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(
                  abilities.constitution.overrideValue.getOrElse(0)
                )
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(0).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.orElse(abilities.intelligence.overrideValue).getOrElse(0)
                          case x: Double => x.toInt
                        }

                        s.copy(abilities =
                          s.abilities.copy(intelligence =
                            s.abilities.intelligence.copy(overrideValue = if (newNum == 0) None else Some(newNum))
                          )
                        )
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(
                  abilities.intelligence.overrideValue.getOrElse(0)
                )
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(0).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.orElse(abilities.wisdom.overrideValue).getOrElse(0)
                          case x: Double => x.toInt
                        }

                        s.copy(abilities =
                          s.abilities.copy(wisdom =
                            s.abilities.wisdom.copy(overrideValue = if (newNum == 0) None else Some(newNum))
                          )
                        )
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(
                  abilities.wisdom.overrideValue.getOrElse(0)
                )
            ),
            Table.Cell(
              Input
                .`type`("number").size(SemanticSIZES.mini).min(0).max(30).onChange(
                  (
                    _,
                    changedData
                  ) =>
                    $.modState(
                      s => {
                        val newNum = changedData.value match {
                          case s: String => s.toIntOption.orElse(abilities.charisma.overrideValue).getOrElse(0)
                          case x: Double => x.toInt
                        }

                        s.copy(abilities =
                          s.abilities.copy(charisma =
                            s.abilities.charisma.copy(overrideValue = if (newNum == 0) None else Some(newNum))
                          )
                        )
                      },
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                ).value(
                  abilities.charisma.overrideValue.getOrElse(0)
                )
            )
          ),
          Table.Row(
            Table.HeaderCell("Proficiency"),
            Table.Cell(
              Checkbox
                .fitted(true).checked(abilities.strength.isProficient).onChange(
                  (
                    _,
                    data
                  ) => {
                    $.modState(
                      s =>
                        s.copy(abilities =
                          s.abilities.copy(strength =
                            s.abilities.strength
                              .copy(isProficient = data.checked.getOrElse(s.abilities.strength.isProficient))
                          )
                        ),
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                  }
                )
            ),
            Table.Cell(
              Checkbox
                .fitted(true).checked(abilities.dexterity.isProficient).onChange(
                  (
                    _,
                    data
                  ) => {
                    $.modState(
                      s =>
                        s.copy(abilities =
                          s.abilities.copy(dexterity =
                            s.abilities.dexterity
                              .copy(isProficient = data.checked.getOrElse(s.abilities.dexterity.isProficient))
                          )
                        ),
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                  }
                )
            ),
            Table.Cell(
              Checkbox
                .fitted(true).checked(abilities.constitution.isProficient).onChange(
                  (
                    _,
                    data
                  ) => {
                    $.modState(
                      s =>
                        s.copy(abilities =
                          s.abilities.copy(constitution =
                            s.abilities.constitution
                              .copy(isProficient = data.checked.getOrElse(s.abilities.constitution.isProficient))
                          )
                        ),
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                  }
                )
            ),
            Table.Cell(
              Checkbox
                .fitted(true).checked(abilities.intelligence.isProficient).onChange(
                  (
                    _,
                    data
                  ) => {
                    $.modState(
                      s =>
                        s.copy(abilities =
                          s.abilities.copy(intelligence =
                            s.abilities.intelligence
                              .copy(isProficient = data.checked.getOrElse(s.abilities.intelligence.isProficient))
                          )
                        ),
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                  }
                )
            ),
            Table.Cell(
              Checkbox
                .fitted(true).checked(abilities.wisdom.isProficient).onChange(
                  (
                    _,
                    data
                  ) => {
                    $.modState(
                      s =>
                        s.copy(abilities =
                          s.abilities.copy(wisdom =
                            s.abilities.wisdom
                              .copy(isProficient = data.checked.getOrElse(s.abilities.wisdom.isProficient))
                          )
                        ),
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                  }
                )
            ),
            Table.Cell(
              Checkbox
                .fitted(true).checked(abilities.charisma.isProficient).onChange(
                  (
                    _,
                    data
                  ) => {
                    $.modState(
                      s =>
                        s.copy(abilities =
                          s.abilities.copy(charisma =
                            s.abilities.charisma
                              .copy(isProficient = data.checked.getOrElse(s.abilities.charisma.isProficient))
                          )
                        ),
                      $.state.flatMap(s => props.onChange(s.abilities))
                    )
                  }
                )
            )
          )
        )
      )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props) => "") // make sure the props are ignored for re-rendering the component

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("AbilitiesEditor")
    .initialStateFromProps(p => State(p.abilities))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    abilities: Abilities,
    onChange:  Abilities => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(abilities = abilities, onChange = onChange))

}
