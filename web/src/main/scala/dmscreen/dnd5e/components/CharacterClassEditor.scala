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

import dmscreen.DMScreenState
import dmscreen.dnd5e.*
import dmscreen.dnd5e.CharacterClassId.paladin
import dmscreen.util.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.react.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

import scala.scalajs.js.JSConverters.*

object CharacterClassEditor {

  case class State(
    classes:    List[PlayerCharacterClass],
    subClasses: Map[CharacterClassId, Set[SubClass]] = Map.empty
  )

  case class Props(
    classes:  List[PlayerCharacterClass],
    onChange: List[PlayerCharacterClass] => Callback = _ => Callback.empty
  )

  case class Backend($ : BackendScope[Props, State]) {

    def loadSubclasses(
      state: State
    ): Callback = {
      Callback.traverse(state.classes.map(_.characterClass)) { characterClass =>
        if (!state.subClasses.contains(characterClass)) {
          DND5eGraphQLRepository.live
            .subClasses(characterClass)
            .map(s => $.modState(state => state.copy(subClasses = state.subClasses + (characterClass -> s.toSet))))
            .completeWith(_.get)

        } else {
          Callback.log(s"$characterClass is already there, not loading it again!")
        }
      }
    }

    def render(
      props: Props,
      state: State
    ): VdomNode = {

      DMScreenState.ctx.consume { dmScreenState =>
        val campaignState = dmScreenState.campaignState
          .map(_.asInstanceOf[DND5eCampaignState]).getOrElse(throw RuntimeException("No campaign"))

        def allClasses = campaignState.classes
        def totalLevel = state.classes.map(_.level).sum

        Table(
          Table.Header(
            Table.Row(
              Table.HeaderCell.colSpan(4).textAlign(semanticUiReactStrings.right)(s"Total Level = $totalLevel")
            ),
            Table.Row(
              Table.HeaderCell("Class"),
              Table.HeaderCell("Subclass"),
              Table.HeaderCell("Level"),
              Table.HeaderCell()
            )
          ),
          Table.Body(
            state.classes.zipWithIndex.map {
              (
                playerCharacterClass,
                i
              ) =>
                Table.Row.withKey(s"key$i")(
                  Table.Cell(
                    Dropdown
                      .placeholder("Class")
                      .clearable(true)
                      .compact(true)
                      .allowAdditions(true)
                      .selection(true)
                      .search(true)
                      .onChange(
                        (
                          _,
                          changedData
                        ) =>
                          changedData.value match {
                            case str: String if str.isEmpty => Callback.empty // Nothing to do
                            case str: String =>
                              $.modState(
                                s =>
                                  s.copy(
                                    classes = s.classes.patch(
                                      i,
                                      Seq(
                                        playerCharacterClass.copy(
                                          characterClass = CharacterClassId.values
                                            .find(_.name == str).getOrElse(
                                              throw RuntimeException(
                                                s"Bad data trying to find a characterClassId with ${changedData.value}"
                                              )
                                            ),
                                          subclass = None
                                        )
                                      ),
                                      1
                                    )
                                  ),
                                $.state.flatMap(s => loadSubclasses(s) >> props.onChange(s.classes))
                              )
                            case a =>
                              Callback.log(s"Bad data trying to find a characterClassId with ${changedData.value}")
                          }
                      )
                      .options(
                        allClasses
                          .map(clazz =>
                            DropdownItemProps()
                              .setValue(clazz.id.name)
                              .setText(clazz.id.name)
                          ).toJSArray
                      )
                      .value(playerCharacterClass.characterClass.name)
                  ),
                  Table.Cell(
                    Dropdown
                      .placeholder("Subclass")
                      .clearable(true)
                      .compact(true)
                      .allowAdditions(true)
                      .selection(true)
                      .search(true)
                      .onChange {
                        (
                          _,
                          changedData
                        ) =>
                          val subclass: Option[SubClass] = changedData.value match {
                            case s: String if s.isEmpty => None
                            case s: String              => Some(SubClass(s.toLowerCase))
                            case _ => throw RuntimeException("Unexpected value")
                          }

                          $.modState(
                            s =>
                              s.copy(
                                classes = s.classes.patch(i, Seq(playerCharacterClass.copy(subclass = subclass)), 1),
                                subClasses = s.subClasses + (playerCharacterClass.characterClass -> (s
                                  .subClasses(playerCharacterClass.characterClass) ++ subclass.toSet))
                              ),
                            $.state.flatMap(s => props.onChange(s.classes))
                          )

                      }
                      .options(
                        state.subClasses
                          .get(playerCharacterClass.characterClass)
                          .toSeq
                          .flatten
                          .map(subclass =>
                            DropdownItemProps()
                              .setValue(subclass.name.toLowerCase)
                              .setText(subclass.name)
                          ).toJSArray
                      )
                      .value(playerCharacterClass.subclass.map(_.name.toLowerCase).getOrElse(""))
                  ),
                  Table.Cell(
                    Input
                      .`type`("number")
                      .min(1)
                      .max(20)
                      .size(SemanticSIZES.mini)
                      .onChange(
                        (
                          _,
                          changedData
                        ) =>
                          $.modState(
                            s => {
                              val newNum = changedData.value.asInt(0)
                              s.copy(
                                classes = s.classes.patch(
                                  i,
                                  Seq(playerCharacterClass.copy(level = newNum)),
                                  1
                                )
                              )
                            },
                            $.state.flatMap(s => props.onChange(s.classes))
                          )
                      )
                      .value(playerCharacterClass.level)
                  ),
                  Table.Cell(
                    Button
                      .title("Delete this class")
                      .icon(true).onClick {
                        (
                          _,
                          _
                        ) =>
                          _root_.components.Confirm.confirm(
                            question = s"Are you sure you want to delete this class (${playerCharacterClass.characterClass.name})?",
                            onConfirm = $.modState(
                              s => s.copy(classes = s.classes.patch(i, Nil, 1)),
                              $.state.flatMap(s => props.onChange(s.classes))
                            )
                          )
                      }(Icon.name(SemanticICONS.delete))
                  )
                )
            }*
          ),
          Table.Footer(
            Table.Row(
              Table.Cell.colSpan(4)(
                Button
                  .title("Add another class")
                  .icon(true).onClick(
                    (
                      _,
                      _
                    ) =>
                      $.modState(
                        s => s.copy(classes = s.classes :+ PlayerCharacterClass(CharacterClassId.barbarian)),
                        $.state.flatMap(s => props.onChange(s.classes))
                      )
                  )(Icon.name(SemanticICONS.add))
              )
            )
          )
        )
      }
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] =
    Reusability.by((s: State) =>
      (
        s.classes,
        s.subClasses
          .map(
            (
              a,
              b
            ) => a.name + b.map(_.name).mkString
          ).toList
      )
    )
  given Reusability[Props] = Reusability.by((_: Props).classes)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("PlayerCharacterClassDialog")
    .initialStateFromProps(p => State(p.classes))
    .renderBackend[Backend]
    .componentDidMount($ =>
      Callback.traverse($.state.classes.map(_.characterClass)) { clazz =>
        $.backend.loadSubclasses($.state)
      }
    )
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    classes:  List[PlayerCharacterClass],
    onChange: List[PlayerCharacterClass] => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(classes, onChange))

}
