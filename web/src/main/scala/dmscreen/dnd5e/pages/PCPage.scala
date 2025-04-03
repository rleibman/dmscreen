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
import dmscreen.dnd5e.components.PCEditComponent
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenPage, DMScreenState}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import zio.json.*

object PCPage extends DMScreenPage {

  case class State(
    pcs:               Seq[PlayerCharacter] = Seq.empty,
    editingMode:       EditingMode = EditingMode.view,
    dndBeyondImportId: Option[String] = None
  )

  class Backend($ : BackendScope[CampaignId, State]) {

    def loadState(campaignId: CampaignId): Callback = {
      DND5eGraphQLRepository.live
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
            Modal
              .open(state.dndBeyondImportId.isDefined)(
                Modal.Header("Import from dndbeyond.com"),
                Modal.Content(
                  <.p("Make sure that your character is public!"),
                  Input
                    .label("Character ID").onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String => s
                          case _ => state.dndBeyondImportId.getOrElse("")
                        }
                        $.modState(_.copy(dndBeyondImportId = Some(newVal)))
                    }
                ),
                Modal.Actions(
                  Button
                    .secondary(true).onClick(
                      (
                        _,
                        _
                      ) => $.modState(_.copy(dndBeyondImportId = None))
                    )("Cancel"),
                  Button
                    .primary(true).onClick {
                      (
                        _,
                        _
                      ) =>
                        val maybeId = DndBeyondId(state.dndBeyondImportId.get)
                        maybeId.fold(
                          s => Callback.alert(s),
                          good =>
                            DND5eGraphQLRepository.live
                              .importDndBeyondCharacter(campaign.id, good)
                              .map(newPC =>
                                $.modState(
                                  s =>
                                    s.copy(
                                      s.pcs.filter(_.header.id != newPC.header.id) :+ newPC,
                                      dndBeyondImportId = None
                                    ),
                                  Callback.alert(s"Successfully imported '${newPC.header.name}'!")
                                )
                              )
                              .completeWith(_.get)
                        )
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
                    val newPC = PlayerCharacter(
                      header = PlayerCharacterHeader(
                        id = PlayerCharacterId.empty,
                        campaignId = campaign.header.id,
                        source = DMScreenSource,
                        name = "New Character"
                      ),
                      jsonInfo = PlayerCharacterInfo(
                        health = Health(deathSave = DeathSave.empty, currentHitPoints = 1, maxHitPoints = 1),
                        armorClass = 10,
                        classes = List.empty
                      ).toJsonAST.toOption.get
                    )

                    DND5eGraphQLRepository.live
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
                ) => $.modState(_.copy(dndBeyondImportId = Some("")))
              )("Import from dndbeyond.com"),
              Button("Import from 5th Edition Tools").when(false), // Enhanceement
              Button("Long Rest (Reset resources)").onClick(
                (
                  _,
                  _
                ) =>
                  $.modState(
                    s =>
                      s.copy(
                        pcs = s.pcs.map(pc =>
                          pc.copy(
                            jsonInfo = pc.info
                              .copy(
                                health = pc.info.health.copy(
                                  currentHitPoints = pc.info.health.maxHitPoints,
                                  deathSave = DeathSave.empty
                                ),
                                spellSlots = pc.info.spellSlots.map(s => s.copy(used = 0))
                                // There's of course many other things that are reset on long (or short) rests,
                                // channel divinity, bardic inspiration, ki points, etc, but we're currently not tracking those
                              ).toJsonAST.toOption.get
                          )
                        )
                      ),
                    $.state.flatMap(s =>
                      dmScreenState.onModifyCampaignState(
                        campaignState.copy(
                          changeStack = campaignState.changeStack.logPCChanges(s.pcs*)
                        ),
                        "Long Rest, all pc resources are reset"
                      )
                    )
                  ) >> $.forceUpdate
              )
            ),
            <.div(
              ^.className := "pageContainer",
              ^.key       := "pageContainer",
              state.pcs
                .map(pc =>
                  PCEditComponent( // Internally, make sure each item has a key!
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
                      DND5eGraphQLRepository.live
                        .deleteEntity(entityType = DND5eEntityType.playerCharacter, id = pc.header.id)
                        .map(_ =>
                          $.modState(s =>
                            s.copy(pcs = s.pcs.filter(_.header.id != deleteMe.header.id))
                          ) >> dmScreenState.log(
                            s"Deleted player character ${pc.header.name}"
                          )
                        )
                        .completeWith(_.get),
                    onComponentClose = _ => dmScreenState.onForceSave,
                    onSync = pc => {

                      pc.header.source match {
                        case source: DNDBeyondImportSource =>
                          DND5eGraphQLRepository.live
                            .importDndBeyondCharacter(campaignId = campaign.id, source.dndBeyondId, fresh = true)
                            .map(newPC =>
                              $.modState(
                                s =>
                                  s.copy(
                                    s.pcs.filter(_.header.id != newPC.header.id) :+ newPC
                                  ),
                                Callback.alert(s"Successfully synched '${newPC.header.name}'!")
                              )
                            )
                            .completeWith(_.get)
                        case s => Callback.alert(s"Sorry, I can't synch this character, I just don't know how. ($s)")
                      }

                    }
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
