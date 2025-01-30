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

import dmscreen.util.*
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.{*, given}
import dmscreen.{Campaign, CampaignId, DMScreenState}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.{<, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{
  SemanticCOLORS,
  SemanticICONS,
  SemanticSIZES,
  SemanticWIDTHS
}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesAccordionAccordionTitleMod.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.*

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

  case class Props(campaign: Campaign)

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
    treasureTables:  List[RandomTable] = List.empty,
    treasureTable:   Option[RandomTable] = None,
    treasureTheme:   Option[TreasureTheme] = None,
    treasureRarity:  Option[TreasureRarity] = None,
    treasureIsHoard: Boolean = false,
    treasureWallet:  Wallet = Wallet.empty
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

    def treasureTableSelect(id: RandomTableId): Callback =
      (
        for {
          table <- DND5eGraphQLRepository.live.randomTable(id)
        } yield $.modState(_.copy(treasureTable = table))
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

    def loadState(campaignId: CampaignId): Callback =
      (
        for {
          oldState       <- $.state.asAsyncCallback
          props          <- $.props.asAsyncCallback
          monsters       <- DND5eGraphQLRepository.live.bestiary(oldState.monsterSearch)
          scenes         <- DND5eGraphQLRepository.live.scenes(campaignId)
          npcs           <- DND5eGraphQLRepository.live.nonPlayerCharacters(props.campaign.id)
          treasureTables <- DND5eGraphQLRepository.live.randomTables(Some(RandomTableType.treasure))
        } yield {
          $.modState(
            _.copy(
              monsters = monsters.results,
              monsterCount = monsters.total,
              npcs = npcs.toList,
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
              EncounterDifficulty.values.map { difficulty =>
                DropdownItemProps().setValue(difficulty.toString).setText(difficulty.toString)
              }.toJSArray
            ),
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
            Form.Group(
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
                Header.size(small).as("h3")("Available NPCs"),
                Segment.Group(
                  state.npcs
                    .filter(npc =>
                      !state.encounterInfo.npcs.exists(_.nonPlayerCharacterId == npc.id) // Filter out already selected NPCs
                        && (state.npcSearch.name.isEmpty || npc.header.name.toLowerCase
                          .contains(state.npcSearch.name.toLowerCase))
                        && (!state.npcSearch.removeDead || !npc.info.health.isDead)
                    )
                    .map { npc =>
                      Segment.withKey(npc.id.value.toString)(
                        <.div(
                          ^.display        := "flex",
                          ^.justifyContent := "space-between",
                          ^.alignItems     := "center",
                          <.div(
                            <.div(
                              ^.color := "#000000",
                              s"${npc.header.name} (${(npc.info.race.name +: npc.info.classes
                                  .map(_.characterClass.name)).distinct.mkString(", ")})"
                            )
                          ),
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
              ),
            Grid.Column
              .width(SemanticWIDTHS.`8`)(
                Header.size(small).as("h3")("Selected NPCs"),
                Segment.Group(
                  state.encounterInfo.npcs.map { npcCombatant =>
                    val npc = state.npcs.find(_.id == npcCombatant.nonPlayerCharacterId).get
                    Segment.withKey(npc.id.value.toString)(
                      <.div(
                        ^.display        := "flex",
                        ^.justifyContent := "space-between",
                        ^.alignItems     := "center",
                        <.div(
                          <.div(
                            ^.color := "#000000",
                            s"${npc.header.name} (${(npc.info.race.name +: npc.info.classes
                                .map(_.characterClass.name)).distinct.mkString(", ")})"
                          )
                        ),
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
                  }*
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
                    .compact(true)
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
                    .compact(true)
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
                    .compact(true)
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
                    .compact(true)
                    .search(false)
                    .clearable(true)
                    .placeholder("All")
                    .options(
                      ChallengeRating.values
                        .map(s => DropdownItemProps().setValue(s.toString).setText(s.toString)).toJSArray
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
                    .compact(true)
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
                Segment.Group(state.monsters.map { header =>
                  Segment.withKey(header.id.value.toString)(
                    <.div(
                      ^.display        := "flex",
                      ^.justifyContent := "space-between",
                      ^.alignItems     := "center",
                      <.div(
                        <.div(
                          ^.color := "#000000",
                          s"${header.name} (${header.monsterType.toString.capitalize}, CR: ${header.cr.toString})"
                        )
                      ),
                      Button
                        .icon(true)
                        .color(SemanticCOLORS.violet)
                        .onClick(
                          (
                            _,
                            _
                          ) =>
                            $.modInfo(info =>
                              info.copy(combatants = info.combatants :+ toCombatant(state.encounterInfo, header))
                            )
                        )(Icon.name(SemanticICONS.`plus`))
                    )
                  )
                }*),
                Segment.Group(
                  Segment(
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
                )
              ),
            Grid.Column
              .width(SemanticWIDTHS.`8`)(
                Segment.Group(
                  state.encounterInfo.monsters.map { monsterCombatant =>
                    val monster = state.monsters.find(_.id == monsterCombatant.monsterHeader.id).get
                    Segment.withKey(monster.id.value.toString)(
                      <.div(
                        ^.display        := "flex",
                        ^.justifyContent := "space-between",
                        ^.alignItems     := "center",
                        <.div(
                          <.div(
                            ^.color := "#000000",
                            s"${monsterCombatant.name}"
                          )
                        ),
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

    private def LootGenerator(
      state: State
    ): VdomNode =
      Form(
        Grid
          .container(true)(
            Grid.Row(
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
                .checked(state.treasureIsHoard)
                .onChange(
                  (
                    _,
                    changedData
                  ) => $.modState(_.copy(treasureIsHoard = changedData.checked.getOrElse(false)))
                )
            ),
            Grid.Row(
              Grid.Column
                .width(SemanticWIDTHS.`8`)(
                  Table(
                    Table.Header(Table.Row(Table.HeaderCell.colSpan(2)("Available Treasure"))),
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
                            Table.Cell(entry.name)
                          )
                      }*
                    )
                  ).when(state.treasureTable.nonEmpty)
                ),
              Grid.Column
                .width(SemanticWIDTHS.`8`)(
                  Table(
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
                            .min(1)
                            .size(SemanticSIZES.mini)
                            .onChange(
                              (
                                _,
                                changedData
                              ) =>
                                $.modState(s =>
                                  s.copy(treasureWallet = s.treasureWallet.copy(cp = changedData.value.asLong(0)))
                                )
                            )
                            .value(state.treasureWallet.cp.toDouble)
                        ),
                        Table.Cell(
                          Input
                            .`type`("number")
                            .min(1)
                            .size(SemanticSIZES.mini)
                            .onChange(
                              (
                                _,
                                changedData
                              ) =>
                                $.modState(s =>
                                  s.copy(treasureWallet = s.treasureWallet.copy(sp = changedData.value.asLong(0)))
                                )
                            )
                            .value(state.treasureWallet.sp.toDouble)
                        ),
                        Table.Cell(
                          Input
                            .`type`("number")
                            .min(1)
                            .size(SemanticSIZES.mini)
                            .onChange(
                              (
                                _,
                                changedData
                              ) =>
                                $.modState(s =>
                                  s.copy(treasureWallet = s.treasureWallet.copy(ep = changedData.value.asLong(0)))
                                )
                            )
                            .value(state.treasureWallet.ep.toDouble)
                        ),
                        Table.Cell(
                          Input
                            .`type`("number")
                            .min(1)
                            .size(SemanticSIZES.mini)
                            .onChange(
                              (
                                _,
                                changedData
                              ) =>
                                $.modState(s =>
                                  s.copy(treasureWallet = s.treasureWallet.copy(gp = changedData.value.asLong(0)))
                                )
                            )
                            .value(state.treasureWallet.gp.toDouble)
                        ),
                        Table.Cell(
                          Input
                            .`type`("number")
                            .min(1)
                            .size(SemanticSIZES.mini)
                            .onChange(
                              (
                                _,
                                changedData
                              ) =>
                                $.modState(s =>
                                  s.copy(treasureWallet = s.treasureWallet.copy(pp = changedData.value.asLong(0)))
                                )
                            )
                            .value(state.treasureWallet.pp.toDouble)
                        )
                      )
                    )
                  )
                )
            )
          )
      )

    private def ConfirmWizard(
      state: State
    ): VdomNode =
      <.div(
        "All the details",
        "Confirm Button",
        "Cancel Button",
        "Run Encounter Button"
      )

    def render(s: State): VdomNode = {
      def renderStep(step: WizardStepType): VdomNode = {
        step match {
          case WizardStepType.setTheScene      => SetTheScene(s)
          case WizardStepType.encounterDetails => EncounterDetails(s)
          case WizardStepType.selectNPCs       => SelectNPCs(s)
          case WizardStepType.selectCreatures  => SelectCreatures(s)
          case WizardStepType.lootGenerator    => LootGenerator(s)
          case WizardStepType.confirmWizard    => ConfirmWizard(s)
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
                WizardStepType.values.zipWithIndex.map {
                  (
                    step,
                    index
                  ) =>
                    Step
                      .withKey(step.name)
                      .active(s.currentStep == step)
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
            WizardStepType.values.find(_ == s.currentStep).fold(EmptyVdom)(renderStep),
            Button("Cancel")
          )
        }
      }

    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("EncounterWizard")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props.campaign.id))
    .build

  def apply(campaign: Campaign): Unmounted[Props, State, Backend] = component(Props(campaign))

}
