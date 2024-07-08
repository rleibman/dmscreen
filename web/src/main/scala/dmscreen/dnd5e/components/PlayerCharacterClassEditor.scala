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

import caliban.ScalaJSClientAdapter.asyncCalibanCall
import caliban.client.scalajs.DND5eClient.{Queries, SubClass as CalibanSubclass}
import dmscreen.DMScreenState
import dmscreen.dnd5e.*
import dmscreen.dnd5e.CharacterClassId.paladin
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

object PlayerCharacterClassEditor {

  case class State(
    classes:    List[PlayerCharacterClass],
    subClasses: Map[CharacterClassId, List[SubClass]] = Map.empty
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
          val sb = Queries.subclasses(characterClass.name)(CalibanSubclass.name.map(SubClass.apply))
          asyncCalibanCall(sb)
            .map(_.toSeq.flatten)
            .map(s => $.modState(state => state.copy(subClasses = state.subClasses + (characterClass -> s.toList))))
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

        def allClasses = dmScreenState.dnd5e.classes
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
                      .allowAdditions(false)
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
                              .setText(clazz.id.name),
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
                      .onChange(
                        (
                          _,
                          changedData
                        ) =>
                          $.modState(
                            s =>
                              s.copy(
                                classes = s.classes.patch(
                                  i,
                                  Seq(playerCharacterClass.copy(subclass = changedData.value match {
                                    case s: String if s.isEmpty => None
                                    case s: String              => Some(SubClass(s))
                                    case _ => throw RuntimeException("Unexpected value")
                                  })),
                                  1
                                )
                              ),
                            $.state.flatMap(s => props.onChange(s.classes))
                          )
                      )
                      .options(
                        state.subClasses
                          .get(playerCharacterClass.characterClass)
                          .toSeq
                          .flatten
                          .map(subclass =>
                            DropdownItemProps()
                              .setValue(subclass.name)
                              .setText(subclass.name),
                          ).toJSArray
                      )
                      .value(playerCharacterClass.subclass.map(_.name).getOrElse(""))
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
                              val newNum = changedData.value match {
                                case s: String => s.toIntOption.getOrElse(0)
                                case x: Double => x.toInt
                              }
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
                          // TODO confirm deletion
                          $.modState(
                            s => s.copy(classes = s.classes.patch(i, Nil, 1)),
                            $.state.flatMap(s => props.onChange(s.classes))
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
