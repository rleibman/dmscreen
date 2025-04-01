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
import dmscreen.dnd5e.components.{NPCEditComponent, NPCWizard}
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenPage, DMScreenState}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object NPCPage extends DMScreenPage {

  case class State(
    npcs:              Seq[NonPlayerCharacter] = Seq.empty,
    npcWizard:         Boolean = false,
    editingMode:       EditingMode = EditingMode.view,
    dndBeyondImportId: Option[String] = None,
    scenes:            Seq[Scene] = Seq.empty,
    sceneNpcMap:       Map[SceneId, Seq[NonPlayerCharacterId]] = Map.empty,
    encounters:        Seq[Encounter] = Seq.empty,
    filterScene:       Option[Scene] = None,
    filterDead:        Boolean = true,
    selectedNPCId:     Option[NonPlayerCharacterId] = None
  ) {

    def ncpsInScene: Seq[NonPlayerCharacter] =
      filterScene.fold(npcs) { scene =>
        val npcsIds: Seq[NonPlayerCharacterId] = encounters
          .filter(_.header.sceneId.fold(-1)(_.value) == scene.id.value)
          .flatMap(encounter => encounter.info.npcs.map(_.nonPlayerCharacterId))
          .distinct
        npcs.filter(npc =>
          npcsIds.contains(npc.header.id) || sceneNpcMap.getOrElse(scene.id, Seq.empty).contains(npc.header.id)
        )
      }

  }

  class Backend($ : BackendScope[CampaignId, State]) {

    def loadState(campaignId: CampaignId): Callback = {
      (for {
        npcs        <- DND5eGraphQLRepository.live.nonPlayerCharacters(campaignId)
        scenes      <- DND5eGraphQLRepository.live.scenes(campaignId)
        encounters  <- DND5eGraphQLRepository.live.encounters(campaignId)
        sceneNpcMap <- DND5eGraphQLRepository.live.npcsForScene(campaignId)
      } yield $.modState(_.copy(npcs = npcs, scenes = scenes, encounters = encounters, sceneNpcMap = sceneNpcMap)))
        .completeWith(_.get)
    }

    def render(state: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } {
          case campaignState: DND5eCampaignState if state.npcWizard =>
            NPCWizard(
              campaignState.campaign,
              selectedScenes = state.filterScene.map(_.id).toSet,
              onCancel = $.modState(_.copy(npcWizard = false, selectedNPCId = None)),
              onSaved = updatedNPC =>
                $.modState(s =>
                  s.copy(
                    npcWizard = false,
                    selectedNPCId = None,
                    npcs = s.npcs.filter(_.header.id != updatedNPC.header.id) :+ updatedNPC
                  )
                ),
              npcId = state.selectedNPCId
            )
          case campaignState: DND5eCampaignState if !state.npcWizard =>
            val campaign = campaignState.campaign
            VdomArray(
              <.div(
                ^.className := "pageActions",
                ^.key       := "pageActions",
                Button
                  .className("customSmallIconButton")
                  .title("NPC Wizard").onClick(
                    (
                      _,
                      _
                    ) => $.modState(s => s.copy(npcWizard = true))
                  ).icon(true)(Icon.className("wizardButton")),
                Dropdown
                  .placeholder("Class")
                  .clearable(true)
                  .compact(true)
                  .allowAdditions(false)
                  .selection(true)
                  .search(true)
                  .onChange {
                    (
                      _,
                      data
                    ) =>
                      val newId: Long = data.value match {
                        case s: String => s.toLong
                        case d: Double => d.toLong
                        case _ => -1
                      }

                      $.modState(_.copy(filterScene = state.scenes.find(_.id.value == newId)))
                  }
                  .options(
                    (state.scenes
                      .map(scene =>
                        DropdownItemProps()
                          .setValue(scene.id.value.toDouble)
                          .setText(scene.header.name)
                      ) :+ DropdownItemProps().setValue(-1).setText("All Scenes")).toJSArray
                  )
                  .value(state.filterScene.fold(-1.0)(_.id.value.toDouble)),
                Checkbox
                  .toggle(true)
                  .label("Remove Dead")
                  .checked(state.filterDead)
                  .onChange(
                    (
                      _,
                      data
                    ) => $.modState(_.copy(filterDead = data.checked.getOrElse(false)))
                  )
              ),
              <.div(
                ^.className := "pageContainer",
                ^.key       := "pageContainer",
                state.ncpsInScene
                  .filter(npc => if (state.filterDead) !npc.info.health.isDead else true)
                  .sortBy(_.header.id.value) // order by order of creation
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
                        Callback.log("About to delete NPC") >>
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
                      onComponentClose = _ => dmScreenState.onForceSave,
                      onEdit = npc => $.modState(_.copy(npcWizard = true, selectedNPCId = Some(npc.header.id)))
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
