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

package dmscreen.dnd5e.components

import dmscreen.CampaignId
import dmscreen.components.{EditableNumber, EditableText}
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{CtorType, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.*
import org.scalablytyped.runtime.StObject
import org.scalajs.dom.*
import zio.json.*
import zio.json.ast.Json

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

def createMonsterCombatant(
  encounter: Encounter,
  header:    MonsterHeader
): MonsterCombatant = {
  val number =
    encounter.info.combatants.collect {
      case monsterCombatant: MonsterCombatant if monsterCombatant.monsterHeader.id == header.id => monsterCombatant
    }.size + 1

  MonsterCombatant(
    id = CombatantId.create,
    monsterHeader = header,
    health = Health(
      deathSave = DeathSave.empty,
      currentHitPoints = header.maximumHitPoints,
      maxHitPoints = header.maximumHitPoints
    ),
    armorClass = header.armorClass,
    name = s"${header.name} #$number",
    initiativeBonus = header.initiativeBonus
  )
}

object EncounterEditor {

  extension (orderCol: MonsterSearchOrder) {

    def toReactSortDirection(
      compareMe: MonsterSearchOrder,
      direction: OrderDirection
    ): ascending | descending = {
      import scala.language.unsafeNulls
      if (orderCol == compareMe) {
        direction match {
          case OrderDirection.asc  => ascending
          case OrderDirection.desc => descending
        }

      } else {
        null
      }
    }

  }

  enum CloneMonster {

    case yes, no

  }

  case class State(
    npcs:           List[NonPlayerCharacter] = List.empty,
    monsters:       List[MonsterHeader] = List.empty,
    monsterSearch:  MonsterSearch = MonsterSearch(),
    monsterCount:   Long = 0,
    editingMonster: Option[(MonsterId, CloneMonster)] = None,
    viewMonsterId:  Option[MonsterId] = None,
    viewNPCId:      Option[NonPlayerCharacterId] = None,
    isAddingNPC:    Boolean = false,
    npcToAdd:       Option[NonPlayerCharacter] = None
  )

  case class Props(
    encounter:  Encounter,
    difficulty: EncounterDifficultyLevel,
    onDelete:   Encounter => Callback,
    onChange:   Encounter => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def loadState: Callback =
      (
        for {
          oldState <- $.state.asAsyncCallback
          props    <- $.props.asAsyncCallback
          monsters <- DND5eGraphQLRepository.live.bestiary(oldState.monsterSearch)
          npcs     <- DND5eGraphQLRepository.live.nonPlayerCharacters(props.encounter.header.campaignId)
        } yield $.modState(_.copy(monsters = monsters.results, monsterCount = monsters.total, npcs = npcs.toList))
      ).completeWith(_.get)

    def monsterSearch: Callback =
      (
        for {
          oldState      <- $.state.asAsyncCallback
          searchResults <- DND5eGraphQLRepository.live.bestiary(oldState.monsterSearch)
        } yield $.modState(_.copy(monsters = searchResults.results, monsterCount = searchResults.total))
      ).completeWith(_.get)

    def modMonsterSearch(f: MonsterSearch => MonsterSearch): Callback =
      $.modState(s => s.copy(monsterSearch = f(s.monsterSearch)), monsterSearch)

    def changeSort(orderCol: MonsterSearchOrder): Callback = {
      modMonsterSearch { currentSearch =>
        currentSearch.copy(
          orderCol = orderCol,
          orderDir = if (currentSearch.orderCol == orderCol) {
            currentSearch.orderDir.toggle
          } else OrderDirection.asc
        )
      }
    }

    def render(
      props: Props,
      state: State
    ): VdomNode = {

      def modEncounter(encounter: Encounter): Callback = {
        props.onChange(encounter)
      }

      val encounter = props.encounter

      Container(
        state.viewMonsterId.fold(EmptyVdom: VdomNode)(monsterId =>
          Modal
            .withKey("monsterStackBlockModal")
            .open(true)
            .size(semanticUiReactStrings.tiny)
            .closeIcon(true)
            .onClose(
              (
                _,
                _
              ) => $.modState(_.copy(viewMonsterId = None))
            )(
              Modal.Content(^.padding := 5.px, MonsterStatBlock(monsterId))
            ): VdomNode
        ),
        state.viewNPCId.fold(EmptyVdom: VdomNode)(npcId =>
          Modal
            .withKey("npcBlockModal")
            .open(true)
            .size(semanticUiReactStrings.tiny)
            .closeIcon(true)
            .onClose(
              (
                _,
                _
              ) => $.modState(_.copy(viewNPCId = None))
            )(
              Modal.Content(NPCStatBlock(npcId))
            ): VdomNode
        ),
        Modal
          .open(state.isAddingNPC)
          .size(semanticUiReactStrings.tiny)
          .closeIcon(false)(
            Modal.Header("Select NPC to add"),
            Modal.Content(
              Dropdown
                .fluid(true)
                .search(false)
                .clearable(true)
                .placeholder("Click to Select NPC")
                .options(
                  state.npcs
                    .filter(npc =>
                      !encounter.info.combatants.exists(c =>
                        c.isInstanceOf[NonPlayerCharacterCombatant] && c
                          .asInstanceOf[NonPlayerCharacterCombatant].nonPlayerCharacterId == npc.id
                      )
                    )
                    .map(npc => DropdownItemProps().setValue(npc.header.id.value.toDouble).setText(npc.header.name))
                    .toJSArray
                )
                .onChange {
                  (
                    _,
                    data
                  ) =>
                    val newVal = data.value match {
                      case s: String if s.trim.isEmpty => None
                      case s: String                   => state.npcs.find(_.id.value == s.toInt)
                      case s: Double                   => state.npcs.find(_.id.value == s.toInt)
                      case s => None
                    }

                    $.modState(_.copy(npcToAdd = newVal))
                }
                .value(state.npcToAdd.fold(0.0)(_.header.id.value.toDouble))
            ),
            Modal.Actions(
              Button
                .secondary(true)("Cancel").onClick(
                  (
                    _,
                    _
                  ) => $.modState(_.copy(isAddingNPC = false, npcToAdd = None))
                ),
              Button.primary(true)("Ok").onClick {
                (
                  _,
                  _
                ) =>
                  {

                    val newCombatant = state.npcToAdd.map { npc =>
                      NonPlayerCharacterCombatant(
                        id = CombatantId.create,
                        nonPlayerCharacterId = npc.id,
                        name = npc.header.name,
                        notes = "",
                        initiative = 10,
                        initiativeBonus = npc.info.initiativeBonus
                      )
                    }
                    modEncounter(
                      encounter.copy(
                        jsonInfo = encounter.info
                          .copy(combatants = encounter.info.combatants ++ newCombatant)
                          .toJsonAST.toOption.get
                      )
                    ) >> $.modState(_.copy(isAddingNPC = false, npcToAdd = None))
                  }
              }
            )
          )
          .when(state.isAddingNPC),
        Table
          .inverted(DND5eUI.tableInverted)
          .color(DND5eUI.tableColor)(
            Table.Header(
              Table.Row(
                Table.HeaderCell.colSpan(3)(
                  <.h2(
                    EditableText(
                      value = encounter.header.name,
                      allowEditing = encounter.header.status != EncounterStatus.archived,
                      onChange = name =>
                        modEncounter(
                          encounter.copy(header = encounter.header.copy(name = name))
                        )
                    )
                  )
                ),
                Table.HeaderCell
                  .colSpan(3).textAlign(semanticUiReactStrings.center)(
                    s"Difficulty: ${props.difficulty}, XP: ${encounter.info.enemyXP(state.npcs)}"
                  ),
                Table.HeaderCell
                  .colSpan(4)
                  .singleLine(true)
                  .textAlign(semanticUiReactStrings.right)(
                    Button
                      .compact(true)
                      .icon(true)
                      .title("Add NPC")
                      .onClick(
                        (
                          _,
                          _
                        ) =>
                          if (
                            state.npcs.exists(npc =>
                              !encounter.info.combatants.exists(c =>
                                c.isInstanceOf[NonPlayerCharacterCombatant] && c
                                  .asInstanceOf[NonPlayerCharacterCombatant].nonPlayerCharacterId == npc.id
                              )
                            )
                          ) {
                            $.modState(_.copy(isAddingNPC = true))
                          } else {
                            Callback.alert("There are no NPCs you can add, create one in the NPC page first!")
                          }
                      )(Icon.name(SemanticICONS.`add user`)),
                    Button
                      .compact(true)
                      .icon(true)
                      .title("Archive this encounter")
                      .onClick(
                        (
                          _,
                          _
                        ) =>
                          modEncounter(
                            encounter.copy(header = encounter.header.copy(status = EncounterStatus.archived))
                          )
                      )(Icon.name(SemanticICONS.`archive`)).when(
                        encounter.header.status != EncounterStatus.archived
                      ),
                    Button
                      .compact(true)
                      .icon(true)
                      .title("Delete this encounter")
                      .onClick(
                        (
                          _,
                          _
                        ) =>
                          _root_.components.Confirm.confirm(
                            question = "Are you 100% sure you want to delete this encounter?",
                            onConfirm = props.onDelete(encounter)
                          )
                      )(Icon.name(SemanticICONS.`trash`))
                  )
              ),
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
            Table.Body(
              encounter.info.combatants.sortBy(_.name).collect {
                case combatant: NonPlayerCharacterCombatant =>
                  state.npcs
                    .find(_.id == combatant.nonPlayerCharacterId)
                    .fold(EmptyVdom) { npc =>
                      Table.Row
                        .withKey(s"combatant ${combatant.id.value}")(
                          Table.Cell(combatant.name),
                          Table.Cell(
                            s"NPC (${npc.info.race.name},${npc.info.classes.map(_.characterClass.name).mkString(",")})"
                          ),
                          Table.Cell("-"),
                          Table.Cell(npc.info.alignment.name),
                          Table.Cell("-"),
                          Table.Cell("-"),
                          Table.Cell(npc.info.armorClass),
                          Table.Cell(npc.info.health.maxHitPoints),
                          Table.Cell(npc.info.size.toString.capitalize),
                          Table.Cell.singleLine(true)(
                            Button
                              .compact(true)
                              .size(SemanticSIZES.mini)
                              .title("Delete this combatant from the encounter")
                              .icon(true)
                              .onClick {
                                (
                                  _,
                                  _
                                ) =>
                                  modEncounter(
                                    encounter
                                      .copy(jsonInfo =
                                        encounter.info
                                          .copy(combatants = encounter.info.combatants.filter(_.id != combatant.id))
                                          .toJsonAST.toOption.get
                                      )
                                  )
                              }(Icon.name(SemanticICONS.`trash`))
                              .when(encounter.header.status != EncounterStatus.archived),
                            Button
                              .title("View NPC stats")
                              .compact(true)
                              .size(SemanticSIZES.mini)
                              .icon(true)(Icon.name(SemanticICONS.`eye`))
                              .onClick(
                                (
                                  _,
                                  _
                                ) => $.modState(_.copy(viewNPCId = Some(npc.header.id)))
                              )
                          )
                        )
                    }
                case combatant: MonsterCombatant =>
                  Table.Row
                    .withKey(s"combatant ${combatant.id.value}")(
                      Table.Cell(
                        EditableText(
                          value = combatant.name,
                          allowEditing = encounter.header.status != EncounterStatus.archived,
                          onChange = name =>
                            modEncounter(
                              encounter
                                .copy(jsonInfo =
                                  encounter.info
                                    .copy(combatants = encounter.info.combatants.map {
                                      case monsterCombatant2: MonsterCombatant
                                          if monsterCombatant2.id == combatant.id =>
                                        monsterCombatant2.copy(name = name)
                                      case c => c
                                    }).toJsonAST.toOption.get
                                )
                            )
                        )
                      ),
                      Table.Cell(combatant.monsterHeader.monsterType.toString.capitalize),
                      Table.Cell(combatant.monsterHeader.biome.fold("")(_.toString.capitalize)),
                      Table.Cell(combatant.monsterHeader.alignment.fold("")(_.name)),
                      Table.Cell(combatant.monsterHeader.cr.name),
                      Table.Cell(combatant.monsterHeader.xp),
                      Table.Cell(
                        EditableNumber(
                          value = combatant.monsterHeader.armorClass,
                          allowEditing = encounter.header.status != EncounterStatus.archived,
                          min = 0,
                          max = 30,
                          onChange = v =>
                            modEncounter {
                              encounter
                                .copy(jsonInfo =
                                  encounter.info
                                    .copy(combatants = encounter.info.combatants.map {
                                      case monsterCombatant2: MonsterCombatant
                                          if monsterCombatant2.id == combatant.id =>
                                        monsterCombatant2.copy(armorClass = v.toInt)
                                      case c => c
                                    }).toJsonAST.toOption.get
                                )
                            }
                        )
                      ),
                      Table.Cell(
                        EditableNumber(
                          value = combatant.health.maxHitPoints,
                          allowEditing = encounter.header.status != EncounterStatus.archived,
                          min = 0,
                          max = 1000,
                          onChange = v =>
                            modEncounter {
                              encounter
                                .copy(jsonInfo =
                                  encounter.info
                                    .copy(combatants = encounter.info.combatants.map {
                                      case monsterCombatant2: MonsterCombatant
                                          if monsterCombatant2.id == combatant.id =>
                                        monsterCombatant2
                                          .copy(health = monsterCombatant2.health.copy(maxHitPoints = v.toInt))
                                      case c => c
                                    }).toJsonAST.toOption.get
                                )
                            }
                        )
                      ),
                      Table.Cell(combatant.monsterHeader.size.toString.capitalize),
                      Table.Cell.singleLine(true)(
                        Button
                          .compact(true)
                          .size(SemanticSIZES.mini)
                          .title("Delete this combatant from the encounter")
                          .icon(true)
                          .onClick {
                            (
                              _,
                              _
                            ) =>
                              modEncounter(
                                encounter
                                  .copy(jsonInfo =
                                    encounter.info
                                      .copy(combatants = encounter.info.combatants.filter(_.id != combatant.id))
                                      .toJsonAST.toOption.get
                                  )
                              )
                          }(Icon.name(SemanticICONS.`trash`))
                          .when(encounter.header.status != EncounterStatus.archived),
                        Button
                          .compact(true)
                          .size(SemanticSIZES.mini)
                          .title("More of this combatant!")
                          .onClick {
                            (
                              _,
                              _
                            ) =>
                              modEncounter(
                                encounter
                                  .copy(jsonInfo =
                                    encounter.info
                                      .copy(combatants =
                                        encounter.info.combatants :+
                                          createMonsterCombatant(encounter, combatant.monsterHeader)
                                      )
                                      .toJsonAST.toOption.get
                                  )
                              )
                          }
                          .icon(true)(Icon.name(SemanticICONS.`clone outline`))
                          .when(encounter.header.status != EncounterStatus.archived),
                        Button
                          .title("View Monster Stat Block")
                          .compact(true)
                          .size(SemanticSIZES.mini)
                          .icon(true)(Icon.name(SemanticICONS.`eye`))
                          .onClick(
                            (
                              _,
                              _
                            ) => $.modState(_.copy(viewMonsterId = Some(combatant.monsterHeader.id)))
                          )
                      )
                    )
              }*
            )
          ),
        Divider.section(true),
        MonsterTable(
          campaignId = props.encounter.header.campaignId,
          extraActions = header =>
            Button
              .size(SemanticSIZES.mini)
              .compact(true)
              .title("Add this combatant to the encounter")
              .onClick(
                (
                  _,
                  _
                ) =>
                  modEncounter {
                    encounter
                      .copy(jsonInfo =
                        encounter.info
                          .copy(combatants = encounter.info.combatants :+ createMonsterCombatant(encounter, header))
                          .toJsonAST.toOption.get
                      )
                  }
              )
              .icon(true)(Icon.name(SemanticICONS.`add`))
        )
          .when(encounter.header.status != EncounterStatus.archived)
      )
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("EncounterEditor")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState)
    .build

  def apply(
    encounter:  Encounter,
    difficulty: EncounterDifficultyLevel,
    onDelete:   Encounter => Callback = _ => Callback.empty,
    onChange:   Encounter => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] =
    component.withKey(encounter.header.id.value.toString)(Props(encounter, difficulty, onDelete, onChange))

}
