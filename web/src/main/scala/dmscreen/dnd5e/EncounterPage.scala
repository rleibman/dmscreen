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

package dmscreen.dnd5e

import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, Table, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticWIDTHS
import zio.json.*

object EncounterPage extends DMScreenTab {

  case class State(
    encounters:    List[Encounter] = List.empty,
    monsterSearch: MonsterSearch = MonsterSearch(),
    monsters:      List[Monster] = List.empty
  ) {

    def currentEncounter: Option[Encounter] = encounters.find(_.header.status == EncounterStatus.active)

  }

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          val campaign = campaignState.campaign

          <.div(
            ^.className := "pageContainer",
            Grid(
              Grid
                .Column()
                .width(SemanticWIDTHS.`14`)(
                  s.currentEncounter.fold(<.div("There's no current encounter")) { encounter =>
                    Table(
                      Table.Header(
                        Table.Row(
                          Table.HeaderCell.colSpan(8)(
                            s"${encounter.header.name}",
                            Button("Roll NPC Initiative"),
                            Button("Clear NPCs")
                          )
                        ),
                        Table.Row(
                          Table.HeaderCell("Initiative"),
                          Table.HeaderCell("Name"),
                          Table.HeaderCell("Concentration"),
                          Table.HeaderCell("HP"),
                          Table.HeaderCell("AC"),
                          Table.HeaderCell("Conditions"),
                          Table.HeaderCell("Other"),
                          Table.HeaderCell( /*For actions*/ )
                        )
                      ),
                      Table.Body()
                    )
                  },
                  Table(
                    Table.Header(
                      Table.Row(Table.Cell.colSpan(9)("Search stuff goes here")),
                      Table.Row(
                        Table.HeaderCell("Name"),
                        Table.HeaderCell("Type"),
                        Table.HeaderCell("Biome"),
                        Table.HeaderCell("Alignment"),
                        Table.HeaderCell("CR"),
                        Table.HeaderCell("XP"),
                        Table.HeaderCell("AC"),
                        Table.HeaderCell("HP"),
                        Table.HeaderCell("Size"),
                        Table.HeaderCell( /*For actions*/ )
                      )
                    ),
                    Table.Body(),
                    Table.Footer(
                      Table.Row(
                        Table.Cell.colSpan(10)(
                          Pagination(10)
                        )
                      )
                    )
                  )
                ),
              Grid
                .Column()
                .width(SemanticWIDTHS.`4`)(
                  <.div(<.h2("Encounter Log"))
                )
            )
          )

        }
      }
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("router")
    .initialState {
      State()
    }
    .renderBackend[Backend]
    .componentDidMount(
      // _.backend.refresh(initial = true)()
      $ => Callback.empty
    )
    .componentWillUnmount($ =>
      // TODO close down streams here
      Callback.empty
    )
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
