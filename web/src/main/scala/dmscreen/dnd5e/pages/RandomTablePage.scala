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

package dmscreen.dnd5e.pages

import _root_.components.Toast
import dmscreen.components.DiceRoller
import dmscreen.components.EditableComponent.EditingMode
import dmscreen.dnd5e.components.PCEditComponent
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, Segment, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticWIDTHS
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.vertically
import zio.json.*

import scala.scalajs.js
import scala.util.Random

object RandomTablePage extends DMScreenTab {

  case class State(
    randomTables:  List[RandomTable] = List.empty,
    selectedTable: Option[RandomTable] = None,
    resultLog:     List[RandomTableEntry] = List.empty,
    tableType:     Option[RandomTableType] = None
  )

  class Backend($ : BackendScope[Unit, State]) {

    def loadState(): Callback = {
      for {
        randomTables <- DND5eGraphQLRepository.live.randomTables(None)
      } yield $.modState(_.copy(randomTables = randomTables.toList))
    }.completeWith(_.get)

    def selectTableType(tableType: Option[RandomTableType]): Callback = {
      for {
        randomTables <- DND5eGraphQLRepository.live.randomTables(tableType)
      } yield $.modState(_.copy(randomTables = randomTables.toList))
    }.completeWith(_.get)

    def randomTableSelect(id: RandomTableId): Callback =
      (
        for {
          table <- DND5eGraphQLRepository.live.randomTable(id)
        } yield $.modState(_.copy(selectedTable = table))
      ).completeWith(_.get)

    def render(state: State): VdomNode = {
      Grid.divided(vertically)(
        Grid.Row(
          Label("Table Type"),
          Form.Dropdown
            .search(false)
            .clearable(true)
            .fluid(true)
            .placeholder("Select a Random Table")
            .value(state.tableType.map(_.toString).getOrElse("All"))
            .options(
              js.Array(
                DropdownItemProps().setValue("All").setText("All") +:
                  RandomTableType.values
                    .map(tableType =>
                      DropdownItemProps()
                        .setValue(tableType.toString)
                        .setText(tableType.toString)
                    )*
              )
            )
            .onChange {
              (
                _,
                changedData
              ) =>
                val newVal: Option[RandomTableType] = changedData.value match {
                  case s: String if s == "All" => None
                  case s: String               => Some(RandomTableType.valueOf(s))
                  case _ => None
                }
                selectTableType(newVal)
            },
          Label("Random Table"),
          Form.Dropdown
            .search(false)
            .clearable(true)
            .fluid(true)
            .placeholder("Select a Random Table")
            .value(state.selectedTable.map(_.id.value.toDouble).getOrElse(RandomTableId.empty.value.toDouble))
            .options(
              js.Array(
                DropdownItemProps().setValue(EncounterId.empty.value.toDouble).setText("") +:
                  state.randomTables
                    .map(table =>
                      DropdownItemProps()
                        .setValue(table.id.value.toDouble)
                        .setText(table.name)
                    )*
              )
            )
            .onChange {
              (
                _,
                changedData
              ) =>
                val newVal: RandomTableId = changedData.value match {
                  case s: String => RandomTableId(s.toLong)
                  case d: Double => RandomTableId(d.toLong)
                  case _ => RandomTableId.empty
                }
                randomTableSelect(newVal)
            },
          Button
            .onClick(
              (
                _,
                _
              ) =>
                state.selectedTable.fold(Callback.empty)(table =>
                  DiceRoller
                    .roll(table.diceRoll)
                    .map { result =>
                      state.selectedTable.flatMap(_.findEntry(result.head.value)) match {
                        case Some(entry) =>
                          Toast.toast(s"Rolled a ${result.head.value}") >>
                            $.modState(s => s.copy(resultLog = entry +: s.resultLog))
                        case None =>
                          Toast.toast("No matching result in the table")
                      }

                    }
                    .completeWith(_.get)
                )
            )(s"Roll ${state.selectedTable.map(_.diceRoll.roll).getOrElse("")}").when(state.selectedTable.nonEmpty)
        ),
        Grid.Row(
          Grid.Column.width(SemanticWIDTHS.`8`)(
            Table
              .inverted(DND5eUI.tableInverted)
              .color(DND5eUI.tableColor)(
                Table.Header(
                  Table.Row(
                    Table.HeaderCell("Range"),
                    Table.HeaderCell("Name"),
                    Table.HeaderCell("Description")
                  )
                ),
                Table.Body(
                  state.selectedTable.map(_.entries).toSeq.flatten.map { entry =>
                    Table.Row(
                      Table.Cell(s"${entry.rangeLow} - ${entry.rangeHigh}"),
                      Table.Cell(entry.name),
                      Table.Cell(entry.description)
                    )
                  }*
                )
              ).when(state.selectedTable.flatMap(_.entries.headOption).nonEmpty)
          ),
          Grid.Column.width(SemanticWIDTHS.`8`)(
            Header.as("h2")("Roll log"),
            <.div(
              state.resultLog.map { entry =>
                <.div(s"${entry.rangeLow} - ${entry.rangeHigh} - ${entry.name} - ${entry.description}")
              }*
            )
          )
        )
      )
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("RandomTablePage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState())
    .build

  def apply() = component()

}
