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
import dmscreen.dnd5e.components.{EditableText, PlayerCharacterComponent}
import japgolly.scalajs.react.{BackendScope, Callback, Reusability, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES}

object ScenePage extends DMScreenTab {

  case class State()

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State) = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          val campaign = campaignState.campaign
          Table(
            Table.Header(
              Table.Row(
                Table.HeaderCell.colSpan(2)("Scenes"),
                Table.HeaderCell(Button.title("Add Scene").icon(true)(Icon.name(SemanticICONS.`plus circle`))) // TODO add encounter
              ),
              Table.Row(
                Table.HeaderCell("Active"),
                Table.HeaderCell("Name"),
                Table.HeaderCell("")
              )
            ),
            Table.Body(
              campaignState.scenes.map(scene => {

                def modScene(fn: Scene => Scene): Callback = {
                  dmScreenState.onModifyCampaignState {
                    campaignState.copy(
                      scenes = campaignState.scenes.map(s => if (s.header.id == scene.header.id) fn(s) else s)
                    )
                  } // TODO make it stick
                }

                Table.Row(
                  Table.Cell(Radio.checked(scene.header.isActive).onChange {
                    (
                      _,
                      d
                    ) =>
                      dmScreenState.onModifyCampaignState {
                        campaignState.copy(
                          scenes = campaignState.scenes.map(s =>
                            if (s.header.id == scene.header.id && d.checked.getOrElse(false))
                              s.copy(header = s.header.copy(isActive = true))
                            else
                              s.copy(s.header.copy(isActive = false))
                          )
                        )
                      }
                  }), // TODO make it stick
                  Table.Cell(
                    EditableText(
                      value = scene.header.name,
                      onChange = newValue => modScene(s => s.copy(header = s.header.copy(name = newValue)))
                    )
                  ), // TODO editable name
                  Table.Cell(
                    Button()
                      .compact(true).size(SemanticSIZES.mini).icon(true)(Icon.name(SemanticICONS.`edit`)).title(
                        "Edit Scene"
                      ), // TODO edit
                    Button()
                      .compact(true).size(SemanticSIZES.mini).icon(true)(Icon.name(SemanticICONS.`delete`)).title(
                        "Delete Scene"
                      ), // TODO delete
                    Button()
                      .compact(true).size(SemanticSIZES.mini).icon(true)(Icon.name(SemanticICONS.`arrow up`)).title(
                        "Move Up"
                      ), // TODO move up
                    Button()
                      .compact(true).size(SemanticSIZES.mini).icon(true)(Icon.name(SemanticICONS.`arrow down`)).title(
                        "Move Down"
                      ) // TODO move down
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

  def apply(
//             scenes: Seq[Scene]
  ): Unmounted[Unit, State, Backend] = component()

}
