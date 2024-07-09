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
import zio.json.*
import zio.json.ast.*

object PlayerPage extends DMScreenTab {

  case class State()

  class Backend($ : BackendScope[Unit, State]) {

    def render(state: State) = {
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
                        dmScreenState
                          .onModifyCampaignState(
                            campaignState
                              .copy(pcs = campaignState.pcs :+ newPC.copy(header = newPC.header.copy(id = id))),
                            "Added new PC"
                          )
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
              campaignState.pcs
                .map(pc =>
                  PlayerCharacterComponent( // Internally, make sure each item has a key!
                    playerCharacter = pc,
                    onDelete = deleteMe =>
                      GraphQLRepository.live
                        .deleteEntity(entityType = DND5eEntityType.playerCharacter, id = pc.header.id)
                        .map(_ =>
                          dmScreenState.onModifyCampaignState(
                            campaignState.copy(pcs = campaignState.pcs.filter(_.header.id != deleteMe.header.id)),
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
    .builder[Unit]("router")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
