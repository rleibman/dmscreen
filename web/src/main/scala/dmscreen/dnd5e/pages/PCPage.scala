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
import dmscreen.dnd5e.components.PlayerCharacterComponent
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import zio.json.*

object PCPage extends DMScreenTab {

  case class State(
    pcs:         Seq[PlayerCharacter] = Seq.empty,
    editingMode: EditingMode = EditingMode.view
  )

  class Backend($ : BackendScope[CampaignId, State]) {

    def loadState(campaignId: CampaignId): Callback = {
      GraphQLRepository.live
        .playerCharacters(campaignId).map { pcs =>
          $.modState(_.copy(pcs = pcs))
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
                .title("Add new player character").onClick(
                  (
                    _,
                    _
                  ) => {
                    // new player
                    val newPC = PlayerCharacter(
                      header = PlayerCharacterHeader(
                        id = PlayerCharacterId.empty,
                        campaignId = campaign.header.id,
                        name = "New Character"
                      ),
                      jsonInfo = PlayerCharacterInfo(
                        health = Health(deathSave = DeathSave.empty, currentHitPoints = 1, maxHitPoints = 1),
                        armorClass = 10,
                        classes = List.empty
                      ).toJsonAST.toOption.get
                    )

                    GraphQLRepository.live
                      .upsert(header = newPC.header, info = newPC.jsonInfo)
                      .map(id =>
                        $.modState(s => s.copy(pcs = s.pcs :+ newPC.copy(header = newPC.header.copy(id = id)))) >>
                          dmScreenState.log("Added new PC")
                      )
                      .completeWith(_.get)
                  }
                )("Add Character"),
              Button("Import from dndbeyond.com"), // TODO
              Button("Import from 5th Edition Tools"), // TODO
              Button("Long Rest (Reset resources)") // TODO long rest
            ),
            <.div(
              ^.className := "pageContainer",
              ^.key       := "pageContainer",
              state.pcs
                .map(pc =>
                  PlayerCharacterComponent( // Internally, make sure each item has a key!
                    playerCharacter = pc,
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
                      GraphQLRepository.live
                        .deleteEntity(entityType = DND5eEntityType.playerCharacter, id = pc.header.id)
                        .map(_ =>
                          $.modState(s =>
                            s.copy(pcs = s.pcs.filter(_.header.id != deleteMe.header.id))
                          ) >> dmScreenState.log(
                            s"Deleted player character ${pc.header.name}"
                          )
                        )
                        .completeWith(_.get)
                  )
                ).toVdomArray
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
