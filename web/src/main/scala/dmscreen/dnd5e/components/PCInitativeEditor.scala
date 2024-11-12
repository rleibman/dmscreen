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

import dmscreen.*
import dmscreen.components.EditableComponent.EditingMode
import dmscreen.components.{EditableComponent, EditableText}
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{CtorType, *}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticSIZES
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import zio.json.*
import dmscreen.util.*

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object PCInitativeEditor {

  enum EditingMode {

    case closed, open

  }

  case class State(
    initiatives: Map[PlayerCharacter, Int],
    editingMode: EditingMode = EditingMode.closed
  )

  case class Props(
    pcs:      Seq[PlayerCharacter],
    open:     Boolean,
    onChange: Map[PlayerCharacter, Int] => Callback,
    onCancel: Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      Modal
        .open(state.editingMode == EditingMode.open)
        .size(semanticUiReactStrings.small)
        .closeIcon(false)(
          ModalHeader("Edit PC Initative"),
          ModalContent(
            Table(
              Table.Header(
                Table.Row(
                  Table.HeaderCell("Character"),
                  Table.HeaderCell("Initiative")
                )
              ),
              Table.Body(
                state.initiatives.toSeq
                  .map { entry =>
                    Table.Row.withKey(entry._1.id.value.toString)(
                      Table.Cell(s"${entry._1.header.name}${entry._1.header.playerName.fold("")(name => s" ($name)")}"),
                      Table.Cell(
                        Input
                          .`type`("number")
                          .size(SemanticSIZES.mini)
                          .min(1)
                          .max(40)
                          .onChange(
                            (
                              _,
                              changedData
                            ) =>
                              $.modState(s => {
                                val newNum = changedData.value.asInt(entry._2)
                                s.copy(initiatives = s.initiatives.updated(entry._1, newNum))
                              })
                          )
                          .value(entry._2)
                      )
                    )
                  }*
              )
            )
          ),
          ModalActions(
            Button("Cancel").onClick {
              (
                _,
                _
              ) => props.onCancel >> $.modState(_.copy(editingMode = EditingMode.closed))
            },
            Button("OK").onClick {
              (
                _,
                _
              ) =>
                $.modState(
                  _.copy(editingMode = EditingMode.closed),
                  $.state.flatMap(s => props.onChange(s.initiatives))
                )
            }
          )
        )
    }

  }

  private def component =
    ScalaComponent
      .builder[Props]("PCInitativeEditor")
      .initialStateFromProps { p =>
        State(
          editingMode = if (p.open) EditingMode.open else EditingMode.closed,
          initiatives = p.pcs
            .sortBy(e => (e.header.playerName, e.header.name))
            .map(_ -> 10)
            .toMap
        )
      }
      .renderBackend[Backend]
      .build

  def apply(
    pcs:      Seq[PlayerCharacter],
    open:     Boolean,
    onChange: Map[PlayerCharacter, Int] => Callback,
    onCancel: Callback
  ) = component(Props(pcs, open, onChange, onCancel))

}
