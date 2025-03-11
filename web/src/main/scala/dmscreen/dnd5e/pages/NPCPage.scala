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
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticSIZES
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import zio.json.*
import org.scalablytyped.runtime.StringDictionary

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object NPCPage extends DMScreenTab {

  case class State(
    npcs:              Seq[NonPlayerCharacter] = Seq.empty,
    editingMode:       EditingMode = EditingMode.view,
    dndBeyondImportId: Option[String] = None,
    scenes:            Seq[Scene] = Seq.empty,
    sceneNpcMap:       Map[SceneId, Seq[NonPlayerCharacterId]] = Map.empty,
    encounters:        Seq[Encounter] = Seq.empty,
    filterScene:       Option[Scene] = None,
    // Used to select a monster to create a new NPC
    monsterSearch:   MonsterSearch = MonsterSearch(name = Some("")),
    monsterSelected: Option[Monster] = None,
    monsterList:     Seq[MonsterHeader] = Seq.empty,
    selectMonster:   Boolean = false,
    filterDead:      Boolean = true
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
        } { case campaignState: DND5eCampaignState =>
          val campaign = campaignState.campaign

          VdomArray(
            Modal
              .open(state.selectMonster)
              .closeIcon(true)
              .onClose(
                (
                  _,
                  _
                ) => $.modState(_.copy(selectMonster = false))
              )(
                Modal.Header(<.h2("Select Monster")),
                Modal.Content(
                  Form(
                    Search()
                      .onSearchChange(
                        (
                          _,
                          data
                        ) =>
                          DND5eGraphQLRepository.live
                            .bestiary(MonsterSearch(name = Some(data.value.getOrElse(""))))
                            .map(monsters => $.modState(_.copy(monsterList = monsters.results)))
                            .completeWith(_.get)
                      )
                      .onResultSelect {
                        (
                          _,
                          data
                        ) =>
                          val monsterName = data.result.asInstanceOf[js.Dynamic].title.asInstanceOf[String]
                          val header = state.monsterList.find(_.name == monsterName)
                          DND5eGraphQLRepository.live
                            .monster(header.get.id)
                            .map(monster =>
                              Callback.log(s"monster selected: ${monster.get.header.name}") >> $.modState(
                                _.copy(monsterSelected = monster)
                              )
                            )
                            .completeWith(_.get)
                      }
                      .results(
                        state.monsterList
                          .map(monster =>
                            js.Dynamic.literal(
                              "title"       -> monster.name,
                              "description" -> ""
                            )
                          ).toJSArray
                      )
                  )
                ),
                Modal.Actions(
                  Button
                    .disabled(state.monsterSelected.isEmpty)
                    .onClick {
                      (
                        _,
                        _
                      ) =>
                        val monster = state.monsterSelected.get
                        val monsterNPC = NonPlayerCharacter(
                          header = NonPlayerCharacterHeader(
                            id = NonPlayerCharacterId.empty,
                            campaignId = campaign.header.id,
                            name = s"New NPC (${monster.header.name})"
                          ),
                          jsonInfo = NonPlayerCharacterInfo(
                            race = Race(monster.header.name),
                            size = monster.header.size,
                            health = Health(
                              deathSave = DeathSave.empty,
                              currentHitPoints = monster.header.maximumHitPoints,
                              maxHitPoints = monster.header.maximumHitPoints
                            ),
                            armorClass = monster.header.armorClass,
                            abilities = monster.info.abilities,
                            senses = monster.info.senses,
                            speeds = monster.info.speeds,
                            languages = monster.info.languages,
                            alignment = monster.header.alignment.getOrElse(Alignment.trueNeutral),
                            actions = monster.info.actions ++ monster.info.reactions ++ monster.info.legendaryActions,
                            conditionImmunities = monster.info.conditionImmunities,
                            damageVulnerabilities = monster.info.damageVulnerabilities,
                            damageResistances = monster.info.damageResistances,
                            damageImmunities = monster.info.damageImmunities,
                            monster = Some(monster.header.id),
                            challengeRating = Some(monster.header.cr),
                            classes = List.empty
                          ).toJsonAST.toOption.get
                        )

                        Callback.log(s"Saving NPC: $monsterNPC") >>
                          DND5eGraphQLRepository.live
                            .upsert(header = monsterNPC.header, info = monsterNPC.jsonInfo)
                            .map { id =>
                              Callback.log(s"Saved NPC with id: $id") >>
                                $.modState(s =>
                                  s.copy(
                                    npcs = s.npcs :+ monsterNPC.copy(header = monsterNPC.header.copy(id = id)),
                                    monsterSelected = None,
                                    selectMonster = false
                                  )
                                ) >>
                                dmScreenState.log("Added new NPC")
                            }
                            .completeWith(_.get)
                    }("Go!")
                )
              ),
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
                )("Add NPC"),
              Button
                .title("Add new NPC From Monster").onClick(
                  (
                    _,
                    _
                  ) => $.modState(_.copy(selectMonster = true))
                )("Add NPC From Monster"),
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
