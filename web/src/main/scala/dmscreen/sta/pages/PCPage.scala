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

package dmscreen.sta.pages

import dmscreen.components.EditableComponent.EditingMode
import dmscreen.sta.components.*
import dmscreen.sta.{*, given}
import dmscreen.{CampaignId, DMScreenPage, DMScreenState}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import zio.json.*

object PCPage extends DMScreenPage {

  case class State(
    pcs:         Seq[Character] = Seq.empty,
    editingMode: EditingMode = EditingMode.view,
    fileName:    Option[String] = None
  )

  class Backend($ : BackendScope[CampaignId, State]) {

    def loadState(campaignId: CampaignId): Callback = {
      STAGraphQLRepository.live
        .characters(campaignId).map { pcs =>
          $.modState(_.copy(pcs = pcs))
        }.completeWith(_.get)
    }

    def render(state: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: STACampaignState =>
          val campaign = campaignState.campaign

          VdomArray(
            Modal
              .open(false)(
                Modal.Header("Import from foundry export"),
                Modal.Content(
                  Input
                    .label("File").onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String => s
                          case _ => state.fileName.getOrElse("")
                        }
                        $.modState(_.copy(fileName = Some(newVal)))
                    }
                ),
                Modal.Actions(
                  Button.onClick(
                    (
                      _,
                      _
                    ) => $.modState(_.copy(fileName = None))
                  )("Cancel"),
                  Button.onClick {
                    (
                      _,
                      _
                    ) =>
                      Callback.alert("uploading file")
                  }("Import!")
                )
              ),
            <.div(
              ^.className := "pageActions",
              ^.key       := "pageActions",
              Button
                .title("Add new player character").onClick(
                  (
                    _,
                    _
                  ) => {
                    // new player
                    val newPC = Character(
                      header = CharacterHeader(
                        id = CharacterId.empty,
                        campaignId = campaign.header.id,
                        name = None
                      ),
                      jsonInfo = CharacterInfo(
                      ).toJsonAST.toOption.get
                    )

                    STAGraphQLRepository.live
                      .upsert(header = newPC.header, info = newPC.jsonInfo)
                      .map(id =>
                        $.modState(s => s.copy(pcs = s.pcs :+ newPC.copy(header = newPC.header.copy(id = id)))) >>
                          dmScreenState.log("Added new PC")
                      )
                      .completeWith(_.get)
                  }
                )("Add Character"),
              Button.onClick(
                (
                  _,
                  _
                ) => $.modState(_.copy(fileName = Some("")))
              )("Import from Foundry"),
              <.div(
                ^.className := "pageContainer",
                ^.key       := "pageContainer",
                state.pcs
                  .map(pc =>
                    PCEditComponent( // Internally, make sure each item has a key!
                      character = pc,
                      onEditingModeChange = newMode => $.modState(s => s.copy(editingMode = newMode)),
                      onChange = updatedPC =>
                        $.modState(
                          s =>
                            s.copy(pcs = s.pcs.map {
                              case pc if pc.header.id == updatedPC.header.id => updatedPC
                              case other                                     => other
                            }),
                          dmScreenState.onModifyCampaignState(
                            campaignState.copy(
                              changeStack = campaignState.changeStack.logPCChanges(updatedPC)
                            ),
                            ""
                          )
                        ),
                      onDelete = deleteMe =>
                        STAGraphQLRepository.live
                          .deleteEntity(entityType = STAEntityType.character, id = pc.header.id)
                          .map(_ =>
                            $.modState(s =>
                              s.copy(pcs = s.pcs.filter(_.header.id != deleteMe.header.id))
                            ) >> dmScreenState.log(
                              s"Deleted player character ${pc.header.name}"
                            )
                          )
                          .completeWith(_.get),
                      onComponentClose = _ => dmScreenState.onForceSave
                    )
                  ).toVdomArray
              )
            )
          )
        }
      }
    }

  }
  private val component = ScalaComponent
    .builder[CampaignId]("pcPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props))
    .shouldComponentUpdatePure($ => $.nextState.editingMode != EditingMode.edit) // Don't update while we have a dialog open
    .build

  def apply(campaignId: CampaignId): Unmounted[CampaignId, State, Backend] = component(campaignId)

}
