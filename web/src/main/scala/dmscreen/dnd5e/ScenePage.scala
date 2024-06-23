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
import dmscreen.dnd5e.NPCPage.State
import dmscreen.dnd5e.components.PlayerCharacterComponent
import japgolly.scalajs.react.{BackendScope, Callback, Reusability, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import japgolly.scalajs.react.vdom.html_<^.*

object ScenePage extends DMScreenTab {

  case class Thing(
    value1: String,
    value2: String
  )

  case class State(things: Seq[Thing] = Seq.empty)

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State) = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          val campaign = campaignState.campaign
          Table(
            Table.Row(
              Table.HeaderCell("Value1"),
              Table.HeaderCell("Value2"),
              Table.HeaderCell("")
            ),
            Table.Body(
              s.things.map(thing => {
                Table.Row(
                  Table.Cell(thing.value1),
                  Table.Cell(thing.value2),
                  Table.Cell(
                    Button
                      .onClick(
                        (
                          _,
                          _
                        ) => $.modState(s => s.copy(things = s.things.filterNot(_ == thing)))
                      )
                      .value("Delete")
                  )
                )
              })*
            )
          )
        }
      }
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("router")
    .initialState {
      State(
        things = Seq(
          Thing("1", "2"),
          Thing("3", "4"),
          Thing("5", "6"),
          Thing("7", "8"),
          Thing("9", "10"),
          Thing("11", "12"),
          Thing("13", "14")
        )
      )
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

  def apply(
//             scenes: Seq[Scene]
  ): Unmounted[Unit, State, Backend] = component()

}
