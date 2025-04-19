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

import components.{Confirm, Toast}
import dmscreen.components.DiceRoller
import dmscreen.dnd5e.{*, given}
import dmscreen.util.*
import dmscreen.{Campaign, CampaignId, DMScreenState}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.{<, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.components.{Confirm as SConfirm, List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{
  SemanticCOLORS,
  SemanticICONS,
  SemanticSIZES,
  SemanticWIDTHS
}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.*
import zio.json.*
import zio.json.ast.Json

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object EncounterWizard {

  def toCombatant(npc: NonPlayerCharacter): NonPlayerCharacterCombatant = {
    NonPlayerCharacterCombatant(
      id = CombatantId.create,
      nonPlayerCharacterId = npc.id,
      name = npc.header.name,
      notes = "",
      initiative = 10,
      initiativeBonus = npc.info.initiativeBonus
    )
  }
  def toCombatant(
    encounterInfo: EncounterInfo,
    header:        MonsterHeader
  ): MonsterCombatant = {
    val number =
      encounterInfo.combatants.collect {
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

  enum CloneMonster {

    case yes, no

  }

  case class NPCSearch(
    name:       String = "",
    removeDead: Boolean = true
  )

  case class Props(
    campaign: Campaign,
    onCancel: Callback,
    onSaved:  Encounter => Callback
  )

  case class State(
    currentStep: WizardStepType = WizardStepType.setTheScene,
    encounterHeader: EncounterHeader = EncounterHeader(
      id = EncounterId.empty,
      CampaignId.empty,
      name = "",
      status = EncounterStatus.planned,
      sceneId = None,
      orderCol = 0
    ),
    // General
    encounterInfo: EncounterInfo = EncounterInfo(),
    //
    scenes: List[Scene] = List.empty,
    pcs:    List[PlayerCharacter] = List.empty,

    // NPC Step
    npcs:        List[NonPlayerCharacter] = List.empty,
    npcSearch:   NPCSearch = NPCSearch(),
    viewNPCId:   Option[NonPlayerCharacterId] = None,
    isAddingNPC: Boolean = false,
    npcToAdd:    Option[NonPlayerCharacter] = None,
    // Monster Step
    monsters:       List[MonsterHeader] = List.empty,
    monsterSearch:  MonsterSearch = MonsterSearch(pageSize = 8),
    monsterCount:   Long = 0,
    viewMonsterId:  Option[MonsterId] = None,
    editingMonster: Option[(MonsterId, CloneMonster)] = None,
    // Treasure Step
    treasureTables: List[RandomTable] = List.empty,
    treasureTable:  Option[RandomTable] = None,
    treasureTheme:  Option[TreasureTheme] = None,
    treasureRarity: Option[TreasureRarity] = None,

    // Confirm step
    generating: Boolean = false
  )

  enum WizardStepType(val name: String) {

    case setTheScene extends WizardStepType("Set the Scene")
    case encounterDetails extends WizardStepType("Encounter Details")
    case selectNPCs extends WizardStepType("Select NPCs")
    case selectCreatures extends WizardStepType("Select Creatures")
    case lootGenerator extends WizardStepType("Loot Generator")
    case confirmWizard extends WizardStepType("Confirm")

  }

  case class Backend($ : BackendScope[Props, State]) {

    def monsterSearch: Callback =
      (
        for {
          oldState      <- $.state.asAsyncCallback
          searchResults <- DND5eGraphQLRepository.live.bestiary(oldState.monsterSearch)
        } yield $.modState(_.copy(monsters = searchResults.results, monsterCount = searchResults.total))
      ).completeWith(_.get)

    extension ($ : BackendScope[Props, State]) {

      def modHeader(mod: EncounterHeader => EncounterHeader): Callback =
        $.modState(s => s.copy(encounterHeader = mod(s.encounterHeader)))
      def modInfo(mod: EncounterInfo => EncounterInfo): Callback =
        $.modState(s => s.copy(encounterInfo = mod(s.encounterInfo)))
      def modMonsterSearch(f: MonsterSearch => MonsterSearch): Callback =
        $.modState(s => s.copy(monsterSearch = f(s.monsterSearch)), monsterSearch)

    }

    def changeSort(orderCol: MonsterSearchOrder): Callback = {
      $.modMonsterSearch { currentSearch =>
        currentSearch.copy(
          orderCol = orderCol,
          orderDir = if (currentSearch.orderCol == orderCol) {
            currentSearch.orderDir.toggle
          } else OrderDirection.asc
        )
      }
    }

    def loadState(): Callback =
      (
        for {
          oldState       <- $.state.asAsyncCallback
          props          <- $.props.asAsyncCallback
          monsters       <- DND5eGraphQLRepository.live.bestiary(oldState.monsterSearch)
          scenes         <- DND5eGraphQLRepository.live.scenes(props.campaign.id)
          npcs           <- DND5eGraphQLRepository.live.nonPlayerCharacters(props.campaign.id)
          pcs            <- DND5eGraphQLRepository.live.playerCharacters(props.campaign.id)
          treasureTables <- DND5eGraphQLRepository.live.randomTables(Some(RandomTableType.treasure))
        } yield {
          $.modState(s =>
            s.copy(
              monsters = monsters.results,
              monsterCount = monsters.total,
              npcs = npcs.toList,
              pcs = pcs.toList,
              scenes = scenes.toList,
              treasureTables = treasureTables.toList
            )
          )
        }
      ).completeWith(_.get)

    private def SetTheScene(
      state: State
    ): VdomNode =
      Form(
        Form.Field(
          <.label("Scene"),
          Dropdown
            .labeled(true)
            .search(false)
            .clearable(true)
            .fluid(true)
            .placeholder("Select a scene")
            .value(state.encounterHeader.sceneId.getOrElse(SceneId.empty).value.toDouble)
            .options(
              js.Array(
                DropdownItemProps().setValue(EncounterId.empty.value.toDouble).setText("No Scene") +:
                  state.scenes.map(scene =>
                    DropdownItemProps()
                      .setValue(scene.id.value.toDouble)
                      .setText(scene.header.name)
                  )*
              )
            )
            .onChange {
              (
                _,
                changedData
              ) =>
                val newVal = changedData.value match {
                  case s: String => SceneId(s.toLong)
                  case d: Double => SceneId(d.toLong)
                  case _ => SceneId.empty
                }
                $.modHeader(_.copy(sceneId = Some(newVal)))
            }
        ),
        Form.Input
          .label("Encounter Name")
          .value(state.encounterHeader.name)
          .onChange {
            (
              _,
              changedData
            ) =>
              val newStr: String = changedData.value match {
                case x: String => x
                case _ => ""
              }
              $.modHeader(_.copy(name = newStr))
          },
        Form.Field(
          <.label("Biome"),
          Dropdown
            .labeled(true)
            .search(false)
            .clearable(true)
            .fluid(true)
            .placeholder("Select a biome")
            .value(state.encounterInfo.biome.toString)
            .options(
              Biome.values.map { biome =>
                DropdownItemProps().setValue(biome.toString).setText(biome.toString)
              }.toJSArray
            )
            .onChange {
              (
                _,
                changedData
              ) =>
                val newVal = changedData.value match {
                  case s: String => Biome.valueOf(s)
                  case _ => Biome.Unimportant
                }
                $.modInfo(_.copy(biome = newVal))
            }
        ),
        Form.Field(
          <.label("Time of Day"),
          Dropdown
            .labeled(true)
            .search(false)
            .clearable(true)
            .placeholder("Select a time")
            .fluid(true)
            .value(state.encounterInfo.timeOfDay.toString)
            .options(
              EncounterTimeOfDay.values.map { timeOfDay =>
                DropdownItemProps().setValue(timeOfDay.toString).setText(timeOfDay.toString)
              }.toJSArray
            )
            .onChange {
              (
                _,
                changedData
              ) =>
                val newVal = changedData.value match {
                  case s: String => EncounterTimeOfDay.valueOf(s)
                  case _ => EncounterTimeOfDay.unimportant
                }
                $.modInfo(_.copy(timeOfDay = newVal))
            }
        ),
        Form.Input
          .label("Location Details")
          .value(state.encounterInfo.locationNotes)
          .onChange {
            (
              _,
              changedData
            ) =>
              val newStr: String = changedData.value match {
                case x: String => x
                case _ => ""
              }
              $.modInfo(_.copy(locationNotes = newStr))
          }
      )

    private def EncounterDetails(
      state: State
    ): VdomNode =
      Form(
        Form.Field(
          <.label("Difficulty"),
          Form.Dropdown
            .labeled(true)
            .search(false)
            .clearable(true)
            .placeholder("Select a difficulty")
            .fluid(true)
            .value(state.encounterInfo.desiredDifficulty.toString)
            .options(
              EncounterDifficultyLevel.values.map { difficulty =>
                DropdownItemProps().setValue(difficulty.toString).setText(difficulty.toString)
              }.toJSArray
            )
            .onChange {
              (
                _,
                changedData
              ) =>
                val newVal = changedData.value match {
                  case s: String => EncounterDifficultyLevel.valueOf(s)
                  case _ => EncounterDifficultyLevel.Moderate
                }
                $.modInfo(_.copy(desiredDifficulty = newVal))
            },
          Form.Input
            .label("Initial Description (may be enhanced by AI later)").value(
              state.encounterInfo.initialDescription
            ).onChange {
              (
                _,
                changedData
              ) =>
                val newStr: String = changedData.value match {
                  case x: String => x
                  case _ => ""
                }
                $.modInfo(_.copy(initialDescription = newStr))
            }
        )
      )

    private def SelectNPCs(
      state: State
    ): VdomNode =
      Grid
        .container(true)(
          Grid.Row(
            Form.Group
              .widths(equal)(
                Checkbox
                  .toggle(true)
                  .checked(state.npcSearch.removeDead)
                  .label("Filter out dead NPCs")
                  .onChange {
                    (
                      _,
                      changedData
                    ) =>
                      val newVal = changedData.checked match {
                        case b: Boolean => b
                        case _ => false
                      }
                      $.modState(_.copy(npcSearch = state.npcSearch.copy(removeDead = newVal)))
                  },
                Form.Input
                  .label("Name")
                  .value(state.npcSearch.name)
                  .onChange {
                    (
                      _,
                      changedData
                    ) =>
                      val newVal = changedData.value match {
                        case s: String if s.trim.isEmpty => ""
                        case s: String                   => s
                        case _ => ""
                      }

                      $.modState(_.copy(npcSearch = state.npcSearch.copy(name = newVal)))
                  }
              )
          ),
          Grid.Row(
            Grid.Column
              .width(SemanticWIDTHS.`8`)(
                Table
                  .inverted(DND5eUI.tableInverted)
                  .color(DND5eUI.tableColor)(
                    Table.Header(
                      Table.Row(
                        Table.HeaderCell.colSpan(2)("Available NPCs")
                      )
                    ),
                    Table.Body(
                      state.npcs
                        .filter(npc =>
                          !state.encounterInfo.npcs.exists(_.nonPlayerCharacterId == npc.id) // Filter out already selected NPCs
                            && (state.npcSearch.name.isEmpty || npc.header.name.toLowerCase
                              .contains(state.npcSearch.name.toLowerCase))
                            && (!state.npcSearch.removeDead || !npc.info.health.isDead)
                        )
                        .map { npc =>
                          Table.Row.withKey(npc.id.value.toString)(
                            Table.Cell(s"${npc.header.name} (${(npc.info.race.name +: npc.info.classes
                                .map(_.characterClass.name)).distinct.mkString(", ")})"),
                            Table.Cell(
                              Button
                                .icon(true)
                                .color(SemanticCOLORS.violet)
                                .onClick(
                                  (
                                    _,
                                    _
                                  ) => $.modInfo(info => info.copy(combatants = info.combatants :+ toCombatant(npc)))
                                )(Icon.name(SemanticICONS.`plus`))
                            )
                          )
                        }*
                    )
                  )
              ),
            Grid.Column
              .width(SemanticWIDTHS.`8`)(
                Table
                  .inverted(DND5eUI.tableInverted)
                  .color(DND5eUI.tableColor)(
                    Table.Header(Table.Row(Table.HeaderCell.colSpan(2)("Selected NPCs"))),
                    Table.Body(state.encounterInfo.npcs.map { npcCombatant =>
                      val npc = state.npcs.find(_.id == npcCombatant.nonPlayerCharacterId).get
                      Table.Row.withKey(npc.id.value.toString)(
                        Table.Cell(
                          s"${npc.header.name} (${(npc.info.race.name +: npc.info.classes
                              .map(_.characterClass.name)).distinct.mkString(", ")})"
                        ),
                        Table.Cell(
                          Button
                            .icon(true)
                            .color(SemanticCOLORS.violet)
                            .onClick(
                              (
                                _,
                                _
                              ) =>
                                $.modInfo(info =>
                                  info.copy(combatants = info.combatants.collect {
                                    case c: NonPlayerCharacterCombatant if c.id != npcCombatant.id => c
                                  })
                                )
                            )(Icon.name(SemanticICONS.`minus`))
                        )
                      )
                    }*)
                  )
              )
          )
        )

    private def SelectCreatures(
      state: State
    ): VdomNode =
      Grid
        .container(true)(
          Grid.Row(
            Form(
              Form.Group
                .inline(true)(
                  Form.Field(
                    Input
                      .size(semanticUiReactStrings.mini)
                      .label("Name")
                      .onChange {
                        (
                          _,
                          data
                        ) =>
                          val newVal = data.value match {
                            case s: String if s.trim.isEmpty => None
                            case s: String                   => Some(s)
                            case _ => None

                          }

                          $.modMonsterSearch(_.copy(name = newVal))
                      }
                      .value(state.monsterSearch.name.getOrElse(""))
                  ),
                  <.div(^.width := 100.pct, ""), // This is just a spacer
                  Button
                    .title("Clear the search criteria")
                    .compact(true)
                    .icon(true)
                    .onClick(
                      (
                        _,
                        _
                      ) => $.modMonsterSearch(_ => MonsterSearch())
                    )(
                      Icon.className("clearIcon")
                    ),
                  Button
                    .compact(true)
                    .title("Random search based on currently selected criteria")
                    .icon(true)
                    .onClick(
                      (
                        _,
                        _
                      ) => $.modMonsterSearch(_.copy(orderCol = MonsterSearchOrder.random))
                    )(
                      Icon.name(SemanticICONS.`random`)
                    ),
                  Button
                    .compact(true)
                    .title("Brand new Homebrew monster")
                    .icon(true)
                    .onClick(
                      (
                        _,
                        _
                      ) =>
                        $.modState(_.copy(editingMonster = Some((MonsterId.empty, CloneMonster.no)))) >>
                          $.modMonsterSearch(_ => MonsterSearch()) // Clear the search
                    )(
                      Icon.name(SemanticICONS.add)
                    )
                ),
              Form.Group(
                Form.Field(
                  Label("Type"),
                  Dropdown
                    .fluid(true)
                    .search(false)
                    .clearable(true)
                    .placeholder("All")
                    .options(
                      MonsterType.values
                        .map(s =>
                          DropdownItemProps().setValue(s.toString.capitalize).setText(s.toString.capitalize)
                        ).toJSArray
                    )
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String if s.trim.isEmpty => None
                          case s: String                   => MonsterType.values.find(_.toString.equalsIgnoreCase(s))
                          case _ => None
                        }

                        $.modMonsterSearch(_.copy(monsterType = newVal))
                    }
                    .value(state.monsterSearch.monsterType.fold("")(_.toString.capitalize))
                ),
                Form.Field(
                  Label("Biome"),
                  Form
                    .Dropdown()
                    .fluid(true)
                    .search(false)
                    .clearable(true)
                    .placeholder("All")
                    .options(
                      Biome.values
                        .map(s =>
                          DropdownItemProps().setValue(s.toString.capitalize).setText(s.toString.capitalize)
                        ).toJSArray
                    )
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String if s.trim.isEmpty => None
                          case s: String                   => Biome.values.find(_.toString.equalsIgnoreCase(s))
                          case _ => None
                        }

                        $.modMonsterSearch(_.copy(biome = newVal))
                    }
                    .value(state.monsterSearch.biome.fold("")(_.toString.capitalize))
                ),
                Form.Field(
                  Label("Aligment"),
                  Form
                    .Dropdown()
                    .fluid(true)
                    .search(false)
                    .clearable(true)
                    .placeholder("All")
                    .options(
                      Alignment.values
                        .map(s => DropdownItemProps().setValue(s.name).setText(s.name)).toJSArray
                    )
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String if s.trim.isEmpty => None
                          case s: String                   => Alignment.values.find(_.name.equalsIgnoreCase(s))
                          case _ => None
                        }

                        $.modMonsterSearch(_.copy(alignment = newVal))
                    }
                    .value(state.monsterSearch.alignment.fold("")(_.name))
                ),
                Form.Field(
                  Label("CR"),
                  Form
                    .Dropdown()
                    .fluid(true)
                    .search(false)
                    .clearable(true)
                    .placeholder("All")
                    .options(
                      ChallengeRating.values
                        .map(s => DropdownItemProps().setValue(s.toString).setText(s.name)).toJSArray
                    )
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String if s.trim.isEmpty => None
                          case s: String => ChallengeRating.values.find(_.toString.equalsIgnoreCase(s))
                          case _ => None
                        }

                        $.modMonsterSearch(_.copy(challengeRating = newVal))
                    }
                    .value(state.monsterSearch.challengeRating.fold("")(_.toString))
                ),
                Form.Field(
                  Label("Size"),
                  Form
                    .Dropdown()
                    .fluid(true)
                    .search(false)
                    .clearable(true)
                    .placeholder("All")
                    .options(
                      CreatureSize.values
                        .map(s =>
                          DropdownItemProps().setValue(s.toString.capitalize).setText(s.toString.capitalize)
                        ).toJSArray
                    )
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String if s.trim.isEmpty => None
                          case s: String                   => CreatureSize.values.find(_.toString.equalsIgnoreCase(s))
                          case _ => None
                        }

                        $.modMonsterSearch(_.copy(size = newVal))
                    }
                    .value(state.monsterSearch.size.fold("")(_.toString.capitalize))
                )
              )
            )
          ),
          Grid.Row(
            Grid.Column
              .width(SemanticWIDTHS.`8`)(
                Table
                  .inverted(DND5eUI.tableInverted)
                  .color(DND5eUI.tableColor)(
                    Table.Body(
                      state.monsters.map { header =>
                        Table.Row.withKey(header.id.value.toString)(
                          Table.Cell(
                            s"${header.name} (${header.monsterType.toString.capitalize}, CR: ${header.cr.name})"
                          ),
                          Table.Cell(
                            Button
                              .icon(true)
                              .color(SemanticCOLORS.violet)
                              .onClick(
                                (
                                  _,
                                  _
                                ) =>
                                  $.modInfo(info =>
                                    info
                                      .copy(combatants = info.combatants :+ toCombatant(state.encounterInfo, header))
                                  )
                              )(Icon.name(SemanticICONS.`plus`))
                          )
                        )
                      }*
                    ),
                    Table.Body(
                      Table
                        .Row(
                          Table.Cell.colSpan(2)(
                            Pagination(state.monsterCount / state.monsterSearch.pageSize.toDouble)
                              .set("size", tiny)
                              .onPageChange {
                                (
                                  _,
                                  data
                                ) =>
                                  val newVal = data.activePage match {
                                    case s: String => s.toInt
                                    case d: Double => d.toInt
                                    case _: Unit   => 1
                                  }

                                  $.modMonsterSearch(_.copy(page = newVal - 1))
                              }
                              .activePage(state.monsterSearch.page + 1)
                          )
                        ).when(state.monsters.nonEmpty)
                    )
                  )
              ),
            Grid.Column
              .width(SemanticWIDTHS.`8`)(
                Table
                  .inverted(DND5eUI.tableInverted)
                  .color(DND5eUI.tableColor)(
                    Table.Body(
                      state.encounterInfo.monsters.map { monsterCombatant =>
                        Table.Row
                          .withKey(monsterCombatant.id.value.toString)(
                            Table.Cell(
                              s"${monsterCombatant.name}"
                            ),
                            Table.Cell(
                              Button
                                .icon(true)
                                .color(SemanticCOLORS.violet)
                                .onClick(
                                  (
                                    _,
                                    _
                                  ) =>
                                    $.modInfo(info =>
                                      info.copy(combatants = info.combatants.filter(_.id != monsterCombatant.id))
                                    )
                                )(Icon.name(SemanticICONS.`minus`))
                            )
                          )
                      }*
                    )
                  )
              )
          )
        )

    def rollTreasure(state: State): Callback = {
      val cr: Option[ChallengeRating] = ChallengeRating.fromXP(state.encounterInfo.enemyXP(state.npcs))
      val crNum = cr.fold(0)(_.value.toInt)
      val gp: AsyncCallback[Int] = crNum match {
        case i if i <= 4 && !state.encounterInfo.treasure.isHoard  => DiceRoller.roll("3d6").map(_.head.value)
        case i if i <= 10 && !state.encounterInfo.treasure.isHoard => DiceRoller.roll("2d8").map(_.head.value * 10)
        case i if i <= 16 && !state.encounterInfo.treasure.isHoard => DiceRoller.roll("2d10").map(_.head.value * 10)
        case i if i >= 17 && !state.encounterInfo.treasure.isHoard => AsyncCallback.pure(0) // It's all PPs!

        case i if i <= 4 && state.encounterInfo.treasure.isHoard  => DiceRoller.roll("2d4").map(_.head.value * 100)
        case i if i <= 10 && state.encounterInfo.treasure.isHoard => DiceRoller.roll("8d10").map(_.head.value * 100)
        case i if i <= 16 && state.encounterInfo.treasure.isHoard => DiceRoller.roll("8d8").map(_.head.value * 10000)
        case i if i >= 17 && state.encounterInfo.treasure.isHoard => DiceRoller.roll("6d10").map(_.head.value * 10000)

        case _ => AsyncCallback.pure(0)
      }
      val pp: AsyncCallback[Int] = crNum match {
        case i if i >= 17 && !state.encounterInfo.treasure.isHoard => DiceRoller.roll("2d8").map(_.head.value * 100)
        case _                                                     => AsyncCallback.pure(0)
      }

      val magic = crNum match {
        case i if i <= 4 && state.encounterInfo.treasure.isHoard  => DiceRoller.roll("1d4-1").map(_.head.value)
        case i if i <= 10 && state.encounterInfo.treasure.isHoard => DiceRoller.roll("1d4-1").map(_.head.value)
        case i if i <= 16 && state.encounterInfo.treasure.isHoard => DiceRoller.roll("1d4").map(_.head.value)
        case i if i >= 17 && state.encounterInfo.treasure.isHoard => DiceRoller.roll("1d6").map(_.head.value)
        case _                                                    => AsyncCallback.pure(0)
      }

      val averageLevel = state.pcs.map(_.info.totalLevel).sum / state.pcs.size

      def rarities(magic: Int) = {
        (1 to magic).map { i =>
          Toast.toast(s"Rolling to see what rarity we'll get for item # $i").asAsyncCallback >>
            DiceRoller.roll("1d100").map { rollResult =>
              (rollResult.head.value, averageLevel) match {
                case (r, averageLevel) if averageLevel <= 4 && r <= 54  => TreasureRarity.common
                case (r, averageLevel) if averageLevel <= 4 && r <= 91  => TreasureRarity.uncommon
                case (r, averageLevel) if averageLevel <= 4 && r <= 100 => TreasureRarity.rare

                case (r, averageLevel) if averageLevel <= 10 && r <= 30 => TreasureRarity.common
                case (r, averageLevel) if averageLevel <= 10 && r <= 81 => TreasureRarity.uncommon
                case (r, averageLevel) if averageLevel <= 10 && r <= 98 => TreasureRarity.rare
                case (r, averageLevel) if averageLevel <= 10 && r <= 30 => TreasureRarity.veryRare

                case (r, averageLevel) if averageLevel <= 16 && r <= 11  => TreasureRarity.common
                case (r, averageLevel) if averageLevel <= 16 && r <= 34  => TreasureRarity.uncommon
                case (r, averageLevel) if averageLevel <= 16 && r <= 70  => TreasureRarity.rare
                case (r, averageLevel) if averageLevel <= 16 && r <= 93  => TreasureRarity.veryRare
                case (r, averageLevel) if averageLevel <= 16 && r <= 100 => TreasureRarity.legendary

                case (r, _) if r <= 20  => TreasureRarity.rare
                case (r, _) if r <= 64  => TreasureRarity.veryRare
                case (r, _) if r <= 100 => TreasureRarity.legendary
                case (_, _)             => TreasureRarity.common
              }
            }
        }
      }.foldLeft(AsyncCallback.pure(List.empty[TreasureRarity])) {
        (
          acc,
          cb
        ) =>
          acc.flatMap(results => cb.map(result => results :+ result))
      }

      // Based on Theme and Rarity we get a bunch of treasure tables
      def magicTreasureTables(rarities: Seq[TreasureRarity])
        : AsyncCallback[Map[TreasureRarity, Option[RandomTable]]] = {
        val tables: Seq[(TreasureRarity, RandomTable)] = rarities.distinct.flatMap { rarity =>
          state.treasureTables
            .filter(table =>
              state.treasureTheme
                .fold(true)(theme => table.subType.toLowerCase.startsWith(theme.toString.toLowerCase)) &&
                table.subType.toLowerCase.endsWith(rarity.toString.toLowerCase)
            )
            .map(rarity -> _)
        }

        AsyncCallback
          .traverse(tables) { case (rarity, table) =>
            DND5eGraphQLRepository.live.randomTable(table.id).map(rarity -> _)
          }.map(_.toMap)
      }

      def magicTreasureTableRolls(
        rarities: Seq[TreasureRarity],
        tables:   Map[TreasureRarity, Option[RandomTable]]
      ): AsyncCallback[Seq[String]] = {
        rarities
          .flatMap(tables.apply)
          .map(table =>
            Toast.toast(s"Rolling on the treasure table ${table.name} (${table.diceRoll.roll})").asAsyncCallback >>
              DiceRoller
                .roll(table.diceRoll)
                .map(r => table.findEntry(r.head.value))
          )
          .foldLeft(AsyncCallback.pure(List.empty[String])) {
            (
              acc,
              cb
            ) =>
              acc.flatMap(results => cb.map(result => results ++ result.map(_.name)))
          }
      }

      (for {
        gp    <- gp
        _     <- Toast.toast(s"Rolled for gp=$gp").asAsyncCallback.when(gp > 0)
        pp    <- pp
        _     <- Toast.toast(s"Rolled for pp=$pp").asAsyncCallback.when(pp > 0)
        magic <- magic
        _ <- Toast.toast(s"Rolled for magic, will roll $magic times for magic items").asAsyncCallback.when(magic > 0)
        rarities   <- rarities(magic)
        tables     <- magicTreasureTables(rarities)
        _          <- Toast.toast(s"Loaded the tables=${tables.size}").asAsyncCallback.when(tables.nonEmpty)
        magicItems <- magicTreasureTableRolls(rarities, tables)
      } yield {
        $.modState(s =>
          s.copy(
            encounterInfo = s.encounterInfo.copy(treasure =
              s.encounterInfo.treasure + Treasure(
                pp = pp,
                gp = gp,
                items = magicItems.toList
              )
            )
          )
        )
      }).completeWith(_.get)
    }

    private def LootGenerator(
      state: State
    ): VdomNode = {

      def treasureTableSelect(id: RandomTableId): Callback =
        (
          for {
            table <- DND5eGraphQLRepository.live.randomTable(id)
          } yield $.modState(_.copy(treasureTable = table))
        ).completeWith(_.get)

      Form(
        Grid
          .container(true)(
            Grid.Row(
              Form.Group(
                Label("Treasure Theme"),
                Form.Dropdown
                  .labeled(true)
                  .search(false)
                  .clearable(true)
                  .fluid(true)
                  .placeholder("Select a Theme")
                  .value(state.treasureTheme.fold("All")(_.toString))
                  .options(
                    js.Array(
                      DropdownItemProps().setValue("All").setText("All") +:
                        TreasureTheme.values.map { theme =>
                          DropdownItemProps().setValue(theme.toString).setText(theme.toString)
                        }*
                    )
                  )
                  .onChange {
                    (
                      _,
                      changedData
                    ) =>
                      val newVal = changedData.value match {
                        case s: String if s == "All" => None
                        case s: String               => Some(TreasureTheme.valueOf(s))
                        case _ => None
                      }
                      $.modState(s => s.copy(treasureTheme = newVal, treasureTable = None))
                  },
                Label("Treasure Rarity"),
                Form.Dropdown
                  .labeled(true)
                  .search(false)
                  .clearable(true)
                  .fluid(true)
                  .placeholder("Select a Rarity")
                  .value(state.treasureRarity.fold("All")(_.toString))
                  .options(
                    js.Array(
                      DropdownItemProps().setValue("All").setText("All") +:
                        TreasureRarity.values.map { theme =>
                          DropdownItemProps().setValue(theme.toString).setText(theme.toString)
                        }*
                    )
                  )
                  .onChange {
                    (
                      _,
                      changedData
                    ) =>
                      val newVal = changedData.value match {
                        case s: String if s == "All" => None
                        case s: String               => Some(TreasureRarity.valueOf(s))
                        case _ => None
                      }
                      $.modState(s => s.copy(treasureRarity = newVal, treasureTable = None))
                  },
                Label("Treasure Table"),
                Form.Dropdown
                  .labeled(true)
                  .search(false)
                  .clearable(true)
                  .fluid(true)
                  .placeholder("Select a Treasure Table")
                  .value(state.treasureTable.map(_.id.value.toDouble).getOrElse(RandomTableId.empty.value.toDouble))
                  .options(
                    js.Array(
                      DropdownItemProps().setValue(RandomTableId.empty.value.toDouble).setText("") +:
                        state.treasureTables
                          .filter(table =>
                            state.treasureTheme
                              .fold(true)(theme => table.subType.toLowerCase.startsWith(theme.toString.toLowerCase)) &&
                              state.treasureRarity
                                .fold(true)(rarity => table.subType.toLowerCase.endsWith(rarity.toString.toLowerCase))
                          )
                          .map(treasureTable =>
                            DropdownItemProps()
                              .setValue(treasureTable.id.value.toDouble)
                              .setText(treasureTable.name)
                          )*
                    )
                  )
                  .onChange {
                    (
                      _,
                      changedData
                    ) =>
                      val newVal: RandomTableId = changedData.value match {
                        case s: String => RandomTableId(s.toLong)
                        case d: Double => RandomTableId(d.toLong)
                        case _ => RandomTableId.empty
                      }
                      treasureTableSelect(newVal)
                  },
                Checkbox
                  .toggle(true)
                  .label("Is Hoard")
                  .checked(state.encounterInfo.treasure.isHoard)
                  .onChange(
                    (
                      _,
                      changedData
                    ) =>
                      $.modState(s =>
                        s.copy(encounterInfo =
                          s.encounterInfo.copy(treasure =
                            s.encounterInfo.treasure.copy(isHoard = changedData.checked.getOrElse(false))
                          )
                        )
                      )
                  ),
                Button
                  .disabled(state.treasureTable.isEmpty).onClick(
                    (
                      _,
                      _
                    ) =>
                      state.treasureTable.fold(Callback.empty) { treasureTable =>
                        DiceRoller
                          .roll(treasureTable.diceRoll).map { roll =>
                            val treasure = treasureTable.findEntry(roll.head.value)

                            $.modState(s =>
                              s.copy(encounterInfo =
                                s.encounterInfo.copy(treasure =
                                  s.encounterInfo.treasure
                                    .copy(items = s.encounterInfo.treasure.items ++ treasure.map(_.name))
                                )
                              )
                            )

                          }.completeWith(_.get)
                      }
                  )("Roll current treasure table"),
                Button
                  .disabled(state.treasureTheme.isEmpty).onClick(
                    (
                      _,
                      _
                    ) => rollTreasure(state)
                  )("Roll recommended treasure")
              )
            ),
            Grid.Row(
              Grid.Column
                .width(SemanticWIDTHS.`8`)(
                  Table
                    .inverted(DND5eUI.tableInverted)
                    .color(DND5eUI.tableColor)(
                      Table.Header(Table.Row(Table.HeaderCell.colSpan(3)("Available Treasure"))),
                      Table.Body(
                        state.treasureTable.toList.flatMap(_.entries).map { entry =>
                          Table.Row
                            .withKey(s"${entry.rangeLow}-${entry.rangeHigh}")(
                              Table.Cell(
                                if (entry.rangeLow == entry.rangeHigh)
                                  s"${entry.rangeLow}"
                                else
                                  s"${entry.rangeLow}-${entry.rangeHigh}"
                              ),
                              Table.Cell(entry.name),
                              Table.Cell(
                                Button
                                  .icon(true)
                                  .color(SemanticCOLORS.violet)
                                  .onClick(
                                    (
                                      _,
                                      _
                                    ) =>
                                      $.modState(state =>
                                        state.copy(encounterInfo =
                                          state.encounterInfo
                                            .copy(treasure =
                                              state.encounterInfo.treasure
                                                .copy(items = state.encounterInfo.treasure.items :+ entry.name)
                                            )
                                        )
                                      )
                                  )(Icon.name(SemanticICONS.`plus`))
                              )
                            )
                        }*
                      )
                    ).when(state.treasureTable.nonEmpty)
                ),
              Grid.Column
                .width(SemanticWIDTHS.`8`)(
                  Table
                    .inverted(DND5eUI.tableInverted)
                    .color(DND5eUI.tableColor)(
                      Table.Header(
                        Table.Row(
                          Table.HeaderCell.colSpan(5)("Selected Treasure")
                        ),
                        Table.Row(
                          Table.HeaderCell("CP"),
                          Table.HeaderCell("SP"),
                          Table.HeaderCell("EP"),
                          Table.HeaderCell("GP"),
                          Table.HeaderCell("PP")
                        )
                      ),
                      Table.Body(
                        Table.Row(
                          Table.Cell(
                            Input
                              .`type`("number")
                              .min(0)
                              .size(SemanticSIZES.mini)
                              .onChange(
                                (
                                  _,
                                  changedData
                                ) =>
                                  $.modState(s =>
                                    s.copy(encounterInfo =
                                      s.encounterInfo.copy(
                                        treasure = s.encounterInfo.treasure.copy(cp = changedData.value.asLong(0))
                                      )
                                    )
                                  )
                              )
                              .value(state.encounterInfo.treasure.cp.toDouble)
                          ),
                          Table.Cell(
                            Input
                              .`type`("number")
                              .min(0)
                              .size(SemanticSIZES.mini)
                              .onChange(
                                (
                                  _,
                                  changedData
                                ) =>
                                  $.modState(s =>
                                    s.copy(encounterInfo =
                                      s.encounterInfo.copy(
                                        treasure = s.encounterInfo.treasure.copy(sp = changedData.value.asLong(0))
                                      )
                                    )
                                  )
                              )
                              .value(state.encounterInfo.treasure.sp.toDouble)
                          ),
                          Table.Cell(
                            Input
                              .`type`("number")
                              .min(0)
                              .size(SemanticSIZES.mini)
                              .onChange(
                                (
                                  _,
                                  changedData
                                ) =>
                                  $.modState(s =>
                                    s.copy(encounterInfo =
                                      s.encounterInfo.copy(
                                        treasure = s.encounterInfo.treasure.copy(ep = changedData.value.asLong(0))
                                      )
                                    )
                                  )
                              )
                              .value(state.encounterInfo.treasure.ep.toDouble)
                          ),
                          Table.Cell(
                            Input
                              .`type`("number")
                              .min(0)
                              .size(SemanticSIZES.mini)
                              .onChange(
                                (
                                  _,
                                  changedData
                                ) =>
                                  $.modState(s =>
                                    s.copy(encounterInfo =
                                      s.encounterInfo.copy(
                                        treasure = s.encounterInfo.treasure.copy(gp = changedData.value.asLong(0))
                                      )
                                    )
                                  )
                              )
                              .value(state.encounterInfo.treasure.gp.toDouble)
                          ),
                          Table.Cell(
                            Input
                              .`type`("number")
                              .min(0)
                              .size(SemanticSIZES.mini)
                              .onChange(
                                (
                                  _,
                                  changedData
                                ) =>
                                  $.modState(s =>
                                    s.copy(encounterInfo =
                                      s.encounterInfo.copy(
                                        treasure = s.encounterInfo.treasure.copy(pp = changedData.value.asLong(0))
                                      )
                                    )
                                  )
                              )
                              .value(state.encounterInfo.treasure.pp.toDouble)
                          )
                        )
                      )
                    ),
                  Table
                    .inverted(DND5eUI.tableInverted)
                    .color(DND5eUI.tableColor)(
                      Table.Body(
                        state.encounterInfo.treasure.items.map { t =>
                          Table.Row.withKey(t)(
                            Table.Cell(t),
                            Table.Cell(
                              Button
                                .icon(true)
                                .color(SemanticCOLORS.violet)
                                .onClick(
                                  (
                                    _,
                                    _
                                  ) =>
                                    $.modState(s =>
                                      s.copy(encounterInfo =
                                        s.encounterInfo.copy(treasure =
                                          s.encounterInfo.treasure.copy(items =
                                            s.encounterInfo.treasure.items
                                              .patch(s.encounterInfo.treasure.items.indexWhere(_ == t), Nil, 1)
                                          )
                                        )
                                      )
                                    )
                                )(Icon.name(SemanticICONS.`minus`))
                            )
                          )
                        }*
                      )
                    )
                )
            )
          )
      )
    }

    private def readyToSave(state: State): Boolean = {
      state.encounterHeader.name.nonEmpty &&
      state.encounterInfo.combatants.nonEmpty
    }

    private def ConfirmWizard(
      props: Props,
      state: State
    ): VdomNode = {
      val info = state.encounterInfo
      val header = state.encounterHeader

      <.div(
        <.div(
          Button
            .primary(true)
            .disabled(!readyToSave(state))
            .onClick {
              (
                _,
                _
              ) =>
                val encounter = Encounter(
                  header = header,
                  jsonInfo = info.toJsonAST.getOrElse(Json.Null)
                )
                DND5eGraphQLRepository.live
                  .upsert(encounter.header, encounter.jsonInfo)
                  .map(id => props.onSaved(encounter.copy(header = encounter.header.copy(id = id))))
                  .completeWith(_.get)
            }("Save and Close"),
          Button
            .secondary(true).onClick(
              (
                _,
                _
              ) =>
                Confirm.confirm("Are you sure you want to cancel and undo all that work?", onConfirm = props.onCancel)
            )("Cancel")
        ),
        <.div(
          ^.className := "ui segment",
          ^.maxHeight := 600.px,
          ^.overflowY := "auto",
          ^.padding   := 20.px,
          Segment(
            Header.as("h2")(s"Encounter: ${header.name}"),
            Divider(),
            Segment(
              <.p(<.b("Time of Day: "), info.timeOfDay.toString.capitalize),
              <.p(<.b("Biome: "), info.biome.toString.capitalize),
              <.p(<.b("Location Notes: "), info.locationNotes),
              <.p(<.b("Initial Description: "), info.initialDescription),
              <.p(<.b("Difficulty: "), info.desiredDifficulty.toString.capitalize),
              <.div(
                <.b("Generated Description: "),
                Button
                  .size(small)
                  .compact(true)
                  .primary(true)
                  .onClick(
                    (
                      _,
                      _
                    ) =>
                      $.modState(
                        s => s.copy(generating = true),
                        DND5eGraphQLRepository.live
                          .aiGenerateEncounterDescription(
                            Encounter(header = header, jsonInfo = info.toJsonAST.getOrElse(Json.Null))
                          )
                          .map(str =>
                            $.modState(s =>
                              s.copy(
                                encounterInfo = s.encounterInfo.copy(generatedDescription = str),
                                generating = false
                              )
                            )
                          )
                          .completeWith(_.get)
                      )
                  )("Generate"),
                <.div(
                  ^.dangerouslySetInnerHtml := info.generatedDescription
                ).when(!state.generating),
                Loader
                  .inline(true)
                  .active(state.generating)
                  .indeterminate(true)("Generating Description")
              )
            ),
            Header.as("h3")("Combatants"),
            Segment(
              Table.celled(true)(
                TableHeader()(
                  TableRow()(
                    TableHeaderCell("Name"),
                    TableHeaderCell("Type"),
                    TableHeaderCell("HP")
                  )
                ),
                TableBody()(
                  info.combatants.map {
                    case c: MonsterCombatant =>
                      TableRow(
                        TableCell(c.name),
                        TableCell(c.monsterHeader.name),
                        TableCell(s"${c.health.maxHitPoints}")
                      )
                    case c: NonPlayerCharacterCombatant =>
                      val npc = state.npcs.find(_.id == c.nonPlayerCharacterId).get
                      TableRow(
                        TableCell(c.name),
                        TableCell(
                          npc.info.classes.headOption.fold(<.div("Click to add Classes"))(_ =>
                            npc.info.classes.zipWithIndex.map {
                              (
                                cl,
                                i
                              ) =>
                                <.div(
                                  ^.key := s"characterClass_$i",
                                  s"${cl.characterClass.name} ${cl.subclass.fold("")(sc => s"(${sc.name})")} ${cl.level}"
                                )
                            }.toVdomArray
                          )
                        ),
                        TableCell(s"${npc.info.health.maxHitPoints}")
                      )
                    case _ => EmptyVdom
                  }*
                )
              )
            ),
            Header.as("h3")("Treasure"),
            Segment(
              <.p(
                <.b("Coins: "),
                s"${info.treasure.cp} cp".when(info.treasure.cp > 0),
                s"${info.treasure.sp} sp".when(info.treasure.sp > 0),
                s"${info.treasure.ep} ep".when(info.treasure.ep > 0),
                s"${info.treasure.gp} gp".when(info.treasure.gp > 0),
                s"${info.treasure.pp} pp".when(info.treasure.pp > 0)
              ),
              if (info.treasure.items.nonEmpty)
                <.div(
                  <.b("Items: "),
                  <.ul(
                    info.treasure.items.map(item => <.li(item))*
                  )
                )
              else EmptyVdom
            ),
            Divider()
          )
        )
      )
    }

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      def renderStep(step: WizardStepType): VdomNode = {
        step match {
          case WizardStepType.setTheScene      => SetTheScene(state)
          case WizardStepType.encounterDetails => EncounterDetails(state)
          case WizardStepType.selectNPCs       => SelectNPCs(state)
          case WizardStepType.selectCreatures  => SelectCreatures(state)
          case WizardStepType.lootGenerator    => LootGenerator(state)
          case WizardStepType.confirmWizard    => ConfirmWizard(props, state)
        }
      }

      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          <.div(
//        ^.className := "ui container",
            ^.style := CSSProperties().set("backgroundColor", "#ffffff").set("border", "1px solid black"),
            Step.Group
              .size(mini)
              .ordered(true)(
                WizardStepType.values.map { step =>
                  Step
                    .withKey(step.name)
                    .active(state.currentStep == step)
                    .onClick(
                      (
                        _,
                        _
                      ) => $.modState(_.copy(currentStep = step))
                    )(
                      Step.Content(
                        Step.Title(step.name),
                        Step.Description(step.name)
                      )
                    )
                }*
              ),
            <.div(
              ^.display       := "inline-block",
              ^.padding       := 5.px,
              ^.verticalAlign := "top",
              ^.fontSize      := "9pt",
              <.div(s"XP Budget: ${state.encounterInfo.xpBudget(state.pcs).xp(state.encounterInfo.desiredDifficulty)}"),
              <.div(s"XP Used: ${state.encounterInfo.enemyXP(state.npcs)}"),
              <.div(s"Difficulty: ${state.encounterInfo.calculateDifficulty(state.pcs, state.npcs)}")
            ),
            WizardStepType.values
              .find(_ == state.currentStep).fold(EmptyVdom)(s => <.div(^.marginLeft := 8.px, renderStep(s)))
          )
        }
      }

    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("EncounterWizard")
    .initialStateFromProps { props =>
      val s = State()
      s.copy(encounterHeader = s.encounterHeader.copy(campaignId = props.campaign.id))
    }
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState())
    .build

  def apply(
    campaign: Campaign,
    onCancel: Callback = Callback.empty,
    onSaved:  Encounter => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(campaign, onCancel, onSaved))

}
