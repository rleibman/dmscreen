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

import dmscreen.components.EditableComponent.EditingMode
import dmscreen.dnd5e.components.NPCEditComponent
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import zio.json.*

object NPCPage extends DMScreenTab {

  case class State(
    npcs:              Seq[NonPlayerCharacter] = Seq.empty,
    editingMode:       EditingMode = EditingMode.view,
    dndBeyondImportId: Option[String] = None
  )

  class Backend($ : BackendScope[CampaignId, State]) {

    def loadState(campaignId: CampaignId): Callback = {
      DND5eGraphQLRepository.live
        .nonPlayerCharacters(campaignId).map { npcs =>
          $.modState(_.copy(npcs = npcs))
        }.completeWith(_.get)
    }

    def render(state: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          val campaign = campaignState.campaign

          VdomArray(
            <.div(
              ^.className := "pageActions",
              ^.key       := "pageActions",
              Button
                .title("Add new NPC").onClick(
                  (
                    _,
                    _
                  ) => {
                    // new npc
                    val newNPC = NonPlayerCharacter(
                      header = NonPlayerCharacterHeader(
                        id = NonPlayerCharacterId.empty,
                        campaignId = campaign.header.id,
                        name = "New NPC"
                      ),
                      jsonInfo = NonPlayerCharacterInfo(
                        health = Health(deathSave = DeathSave.empty, currentHitPoints = 1, maxHitPoints = 1),
                        armorClass = 10,
                        classes = List.empty
                      ).toJsonAST.toOption.get
                    )

                    DND5eGraphQLRepository.live
                      .upsert(header = newNPC.header, info = newNPC.jsonInfo)
                      .map(id =>
                        $.modState(s => s.copy(npcs = s.npcs :+ newNPC.copy(header = newNPC.header.copy(id = id)))) >>
                          dmScreenState.log("Added new NPC")
                      )
                      .completeWith(_.get)
                  }
                )("Add NPC")
            ),
            <.div(
              ^.className := "pageContainer",
              ^.key       := "pageContainer",
              state.npcs
                .map(npc =>
                  NPCEditComponent( // Internally, make sure each item has a key!
                    npc = npc,
                    onEditingModeChange = newMode => $.modState(s => s.copy(editingMode = newMode)),
                    onChange = updatedNPC =>
                      $.modState(
                        s =>
                          s.copy(npcs = s.npcs.map {
                            case npc if npc.header.id == updatedNPC.header.id => updatedNPC
                            case other                                        => other
                          }),
                        dmScreenState.onModifyCampaignState(
                          campaignState.copy(
                            changeStack = campaignState.changeStack.logNPCChanges(updatedNPC)
                          ),
                          ""
                        )
                      ),
                    onDelete = deleteMe =>
                      DND5eGraphQLRepository.live
                        .deleteEntity(entityType = DND5eEntityType.nonPlayerCharacter, id = npc.header.id)
                        .map(_ =>
                          $.modState(s =>
                            s.copy(npcs = s.npcs.filter(_.header.id != deleteMe.header.id))
                          ) >> dmScreenState.log(
                            s"Deleted non player character ${npc.header.name}"
                          )
                        )
                        .completeWith(_.get),
                    onComponentClose = _ => dmScreenState.onForceSave
                  )
                ).toVdomArray
            )
          )
        }
      }
    }

  }

  private val component = ScalaComponent
    .builder[CampaignId]("npcPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props))
    .shouldComponentUpdatePure($ => $.nextState.editingMode != EditingMode.edit) // Don't update while we have a dialog open
    .build

  def apply(campaignId: CampaignId): Unmounted[CampaignId, State, Backend] = component(campaignId)

}
