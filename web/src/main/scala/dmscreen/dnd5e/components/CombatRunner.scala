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

import dmscreen.*
import dmscreen.components.EditableComponent.EditingMode
import dmscreen.components.{DiceRoller, EditableComponent, EditableNumber, EditableText}
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.{*, given}
import dmscreen.util.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, Table, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticCOLORS, SemanticICONS, SemanticSIZES}
import org.scalajs.dom.*
import zio.json.*
import zio.json.ast.Json

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
object CombatRunner {

  enum DialogType {

    case none, initiative, viewTreasure, info

  }

  given Conversion[EditingMode, DialogMode] = {
    case EditingMode.edit => DialogMode.open
    case EditingMode.view => DialogMode.closed
  }

  case class State(
    encounter:          Option[Encounter] = None,
    pcs:                Seq[PlayerCharacter] = Seq.empty,
    npcs:               Seq[NonPlayerCharacter] = Seq.empty,
    healOrDamage:       Map[CombatantId, Int] = Map.empty,
    dialogMode:         DialogMode = DialogMode.closed,
    viewMonsterId:      Option[MonsterId] = None,
    viewPCId:           Option[PlayerCharacterId] = None,
    viewNPCId:          Option[NonPlayerCharacterId] = None,
    filterDeadMonsters: Boolean = true,
    dialogType:         DialogType = DialogType.none
  )
  case class Props(
    campaignId:         CampaignId,
    encounterId:        EncounterId,
    onChange:           (Encounter, String) => Callback,
    onEditEncounter:    Encounter => Callback,
    onArchiveEncounter: Encounter => Callback,
    onModeChange:       EditingMode => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def initiativeTuple(
      pcs:  Seq[PlayerCharacter],
      npcs: Seq[NonPlayerCharacter]
    )(
      combatant: EncounterCombatant
    ): (Int, Int, Int, Int, Int, Int, Int, String) = {

      val t = combatant match {
        case pc: PlayerCharacterCombatant =>
          pcs
            .find(_.header.id == pc.playerCharacterId).fold((0, 0, 0, 0, 0, 0))(
              _.info.initiativeTuple
            )
        case npc: NonPlayerCharacterCombatant =>
          npcs
            .find(_.header.id == npc.nonPlayerCharacterId).fold((0, 0, 0, 0, 0, 0))(
              _.info.initiativeTuple
            )

        case _: MonsterCombatant => (100, 100, 100, 100, 100, 100)
      }

      t match {
        case (initiativeBonus, dex, int, cha, str, con) =>
          (-combatant.initiative, initiativeBonus, dex, int, cha, str, con, combatant.name)
      }

    }

    private def initializeEncounter(
      encounter: Encounter,
      pcs:       Seq[PlayerCharacter],
      npcs:      Seq[NonPlayerCharacter]
    ): Encounter = {
      // Prep for combat, if the combat doesn't have the pcs yet, create them, if it does, update them.
      val playerCombatants = pcs
        .map { pc =>
          encounter.info.combatants
            .collectFirst {
              case pcCombatant: PlayerCharacterCombatant if pcCombatant.playerCharacterId == pc.id => pcCombatant
            }
            .fold(
              PlayerCharacterCombatant(
                id = CombatantId.create,
                name = pc.header.name,
                playerCharacterId = pc.id,
                notes = "",
                initiative = 10,
                initiativeBonus = pc.info.initiativeBonus
              )
            )(
              _.copy(
                name = pc.header.name,
                initiativeBonus = pc.info.initiativeBonus
              )
            )
        }

      val sortedCombatants =
        (encounter.info.monsters ++ encounter.info.npcs ++ playerCombatants).sortBy(initiativeTuple(pcs, npcs))
      encounter.copy(
        header = encounter.header.copy(status = EncounterStatus.active),
        jsonInfo = encounter.info
          .copy(
            combatants = sortedCombatants,
            currentTurnId =
              if (encounter.info.currentTurnId == CombatantId.empty)
                sortedCombatants.find(_.canTakeTurn).fold(CombatantId.empty)(_.id)
              else encounter.info.currentTurnId,
            round = if (encounter.info.round == 0) 1 else encounter.info.round // This is the first time we're running this encounter
          ).toJsonAST.toOption.get
      )
    }

    def loadState(props: Props): Callback = {
      Callback.log("CombatRunner: Loading state") >>
        (for {
          encounter <- DND5eGraphQLRepository.live.encounter(props.campaignId, props.encounterId)
          pcs       <- DND5eGraphQLRepository.live.playerCharacters(props.campaignId)
          npcs      <- DND5eGraphQLRepository.live.nonPlayerCharacters(props.campaignId)
          initialized = encounter.map(e => initializeEncounter(e, pcs, npcs))
        } yield $.modState(
          _.copy(encounter = initialized, pcs = pcs, npcs = npcs),
          $.state.flatMap(_.encounter.fold(Callback.empty)(e => props.onChange(e, "Initialized Encounter for combat")))
        ))
          .completeWith(_.get)
    }

    def render(
      state: State,
      props: Props
    ): VdomNode =
      DMScreenState.ctx.consume { dmScreenState =>
        (for {
          campaignState <- dmScreenState.campaignState.map(_.asInstanceOf[DND5eCampaignState])
          encounter     <- state.encounter
        } yield {

          val pcs = state.pcs
          val npcs = state.npcs
          val sortedCombatants = encounter.info.combatants.sortBy(initiativeTuple(pcs, npcs))

          def modeChange(mode: EditingMode): Callback = {
            $.modState(_.copy(dialogMode = mode)) // , props.onModeChange(mode))
          }

          def modEncounter(
            f:   Encounter => Encounter,
            log: String
          ): Callback = {
            $.modState(
              s => s.copy(encounter = s.encounter.map(f)),
              $.state.flatMap(_.encounter.fold(Callback.empty)(e => props.onChange(e, log)))
            )
          }

          def modEncounterInfo(
            f:   EncounterInfo => EncounterInfo,
            log: String
          ): Callback =
            modEncounter(encounter => encounter.copy(jsonInfo = f(encounter.info).toJsonAST.toOption.get), log)

          def modPC(
            updatedPC: PlayerCharacter,
            log:       String
          ): Callback = {
            $.modState(
              _.copy(pcs = state.pcs.map(p => if (p.header.id == updatedPC.header.id) updatedPC else p)),
              dmScreenState.onModifyCampaignState(
                campaignState.copy(
                  changeStack = campaignState.changeStack.logPCChanges(updatedPC)
                ),
                log
              )
            )
          }

//          def nextCombatantIfCurrentDead(combatant: EncounterCombatant): Callback = {
//            if (state.encounter.info.currentTurnId == combatant.id && !combatant.canTakeTurn) {
//              // Find the next combatant that can take a turn, or go back to the beginning if we're at the end
//              val combatantIndex = sortedCombatants.indexWhere(_.id == combatant.id)
//              val nextOpt = sortedCombatants
//                .drop(combatantIndex + 1)
//                .find(_.canTakeTurn)
//
//              val firstOne =
//                nextOpt.orElse(sortedCombatants.find(_.canTakeTurn))
//
//              val nextRound = nextOpt.fold(encounter.info.round + 1)(_ => encounter.info.round)
//
//              modEncounterInfo(
//                _.copy(
//                  currentTurnId = firstOne.fold(CombatantId.empty)(_.id),
//                  round = nextRound
//                ),
//                ""
//              )
//
//            } else Callback.empty
//          }

          def modNPC(
            updatedNPC: NonPlayerCharacter,
            log:        String
          ): Callback = {
            $.modState(
              _.copy(npcs = state.npcs.map(p => if (p.header.id == updatedNPC.header.id) updatedNPC else p)),
              dmScreenState.onModifyCampaignState(
                campaignState.copy(
                  changeStack = campaignState.changeStack.logNPCChanges(updatedNPC)
                ),
                log
              )
            )
          }

          VdomArray(
            PCInitativeEditor(
              pcs = pcs,
              onChange = { initatives =>
                $.modState(
                  _.copy(dialogType = DialogType.none),
                  modEncounterInfo(
                    info =>
                      info.copy(
                        currentTurnId = CombatantId.empty, // Need to reset the current turn to the beginning, otherwise things can get woird
                        combatants = info.combatants.map {
                          case c: PlayerCharacterCombatant =>
                            c.copy(initiative = initatives(c.playerCharacterId))
                          case o => o
                        }
                      ),
                    "Set initiative for PCs"
                  )
                )
              },
              onCancel = $.modState(_.copy(dialogType = DialogType.none)),
              open = state.dialogType == DialogType.initiative
            ),
            Modal
              .open(state.dialogType == DialogType.viewTreasure)(
                Modal.Header("Treasure"),
                Modal.Content(
                  <.p(
                    <.b("Coins: "),
                    s"${encounter.info.treasure.cp} cp".when(encounter.info.treasure.cp > 0),
                    s"${encounter.info.treasure.sp} sp".when(encounter.info.treasure.sp > 0),
                    s"${encounter.info.treasure.ep} ep".when(encounter.info.treasure.ep > 0),
                    s"${encounter.info.treasure.gp} gp".when(encounter.info.treasure.gp > 0),
                    s"${encounter.info.treasure.pp} pp".when(encounter.info.treasure.pp > 0)
                  ),
                  <.div(
                    <.b("Items: "),
                    <.ul(
                      encounter.info.treasure.items.map(item => <.li(item))*
                    )
                  ).when(encounter.info.treasure.items.nonEmpty)
                ),
                Modal.Actions(
                  Button("Close").onClick(
                    (
                      _,
                      _
                    ) => $.modState(_.copy(dialogType = DialogType.none))
                  )
                )
              ),
            Modal
              .open(state.dialogType == DialogType.info)(
                Modal.Header(s"Info: Encounter: ${encounter.header.name}"),
                Modal.Content(
                  <.div(
                    ^.className := "ui segment",
                    ^.maxHeight := 600.px,
                    ^.overflowY := "auto",
                    ^.padding   := 20.px,
                    Segment(
                      <.p(<.b("Time of Day: "), encounter.info.timeOfDay.toString.capitalize),
                      <.p(<.b("Biome: "), encounter.info.biome.toString.capitalize),
                      <.p(<.b("Location Notes: "), encounter.info.locationNotes),
                      <.p(<.b("Initial Description: "), encounter.info.initialDescription),
                      <.div(
                        <.b("Generated Description: "),
                        <.div(
                          ^.dangerouslySetInnerHtml := encounter.info.generatedDescription
                        )
                      )
                    )
                  )
                ),
                Modal.Actions(
                  Button("Close").onClick(
                    (
                      _,
                      _
                    ) => $.modState(_.copy(dialogType = DialogType.none))
                  )
                )
              ),
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
            state.viewPCId.fold(EmptyVdom: VdomNode)(pcId =>
              Modal
                .withKey("monsterStackBlockModal")
                .open(true)
                .size(semanticUiReactStrings.tiny)
                .closeIcon(true)
                .onClose(
                  (
                    _,
                    _
                  ) => $.modState(_.copy(viewPCId = None))
                )(
                  Modal.Content(PCStatBlock(pcId))
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
            Table
              .inverted(DND5eUI.tableInverted)
              .color(DND5eUI.tableColor)
              .withKey("CombatRunnerTable")(
                Table.Header(
                  Table.Row(
                    Table.HeaderCell.colSpan(8)(
                      Checkbox
                        .toggle(true)
                        .checked(state.filterDeadMonsters)
                        .onChange {
                          (
                            _,
                            changedData
                          ) =>
                            $.modState(
                              _.copy(filterDeadMonsters = changedData.checked.getOrElse(false))
                            )
                        }
                        .label("Filter out dead monsters")
                    )
                  ),
                  Table.Row(
                    Table.HeaderCell.colSpan(2)(<.h2(s"${encounter.header.name}")),
                    Table.HeaderCell
                      .colSpan(2).textAlign(semanticUiReactStrings.center)(
                        s"Difficulty: ${encounter.info.calculateDifficulty(pcs, npcs)}",
                        <.br(),
                        s"XP: ${encounter.info.enemyXP(npcs)}"
                      ),
                    Table.HeaderCell
                      .colSpan(2).textAlign(semanticUiReactStrings.center)(
                        s"Round: ${encounter.info.round}",
                        <.br(),
                        s"(${encounter.info.round * 6} seconds)"
                      ),
                    Table.HeaderCell
                      .colSpan(2)
                      .singleLine(true)
                      .textAlign(semanticUiReactStrings.right)(
                        Button
                          .compact(true)
                          .title("Info")
                          .onClick(
                            (
                              _,
                              _
                            ) => $.modState(_.copy(dialogType = DialogType.info))
                          )
                          .icon(true)(Icon.name(SemanticICONS.`info circle`)),
                        Button
                          .compact(true)
                          .title("Treasure")
                          .onClick(
                            (
                              _,
                              _
                            ) => $.modState(_.copy(dialogType = DialogType.viewTreasure))
                          )
                          .icon(true)(Icon.name(SemanticICONS.`gem outline`))
                          .when(encounter.info.treasure.nonEmpty),
                        Button
                          .compact(true)
                          .title("Set initative for PCs")
                          .onClick {
                            (
                              _,
                              _
                            ) => $.modState(_.copy(dialogType = DialogType.initiative))
                          }
                          .icon(true)(Icon.name(SemanticICONS.`lightbulb outline`)),
                        Button
                          .compact(true)
                          .title("Roll Initiative for all NPCs")
                          .onClick {
                            (
                              _,
                              _
                            ) =>

                              val monsterCount = encounter.info.combatants
                                .collect { case m: MonsterCombatant => m }.groupBy(_.monsterHeader.id).size
                              val npcCount =
                                encounter.info.combatants.count(_.isInstanceOf[NonPlayerCharacterCombatant])

                              DiceRoller
                                .roll(s"${monsterCount + npcCount}d20")
                                .map { results =>
                                  modEncounterInfo(
                                    { info =>
                                      val monsters = info.combatants
                                        .collect { case m: MonsterCombatant => m }.groupBy(_.monsterHeader.id)
                                      val pcs = info.combatants.collect { case pc: PlayerCharacterCombatant =>
                                        pc
                                      }
                                      val npcs =
                                        info.combatants.collect { case npc: NonPlayerCharacterCombatant =>
                                          npc
                                        }
                                      val npcInitatives = results.take(npcs.size).map(_.value)
                                      val monsterInitiatives = results.takeRight(monsters.size).map(_.value)
                                      val newNpcs = npcs
                                        .zip(npcInitatives).map(
                                          (
                                            combatant,
                                            initiative
                                          ) =>
                                            combatant.copy(initiative =
                                              scala.math.max(1, initiative + combatant.initiativeBonus)
                                            )
                                        )
                                      val newMonsters = monsters.zip(monsterInitiatives).flatMap {
                                        (
                                          monsters,
                                          initiative
                                        ) =>
                                          monsters._2.map(combatant =>
                                            combatant.copy(initiative =
                                              scala.math.max(1, initiative + combatant.initiativeBonus)
                                            )
                                          )
                                      }

                                      val newCombatants = newNpcs ++ newMonsters ++ pcs

                                      info.copy(
                                        currentTurnId = CombatantId.empty, // Need to reset the current turn to the beginning, otherwise things can get woird
                                        combatants = newCombatants
                                      )
                                    },
                                    "Rolled initiative for all NPCs"
                                  )
                                }
                                .completeWith(_.get)

                          }
                          .icon(true)(Icon.className("d20smallicon")),
                        Button
                          .compact(true)
                          .title("Clear all NPC stats to beginning of combat")
                          .onClick {
                            (
                              _,
                              _
                            ) =>
                              val npcCount = encounter.info.combatants.count(_.isNPC)
                              DiceRoller
                                .roll(s"${npcCount}d20")
                                .map { results =>
                                  modEncounterInfo(
                                    {
                                      info =>
                                        val (npcs, pcs) = info.combatants.partition(_.isNPC)
                                        val initiatives = results.map(_.value)

                                        val newCombatants =
                                          npcs
                                            .zip(initiatives)
                                            .collect {
                                              case (combatant: MonsterCombatant, initiative) =>
                                                combatant.copy(
                                                  initiative =
                                                    scala.math.max(1, initiative + combatant.initiativeBonus),
                                                  health = combatant.health
                                                    .copy(currentHitPoints = combatant.health.maxHitPoints),
                                                  conditions = Set.empty,
                                                  otherMarkers = List.empty
                                                )
                                              case (combatant: NonPlayerCharacterCombatant, initiative) =>
                                                combatant.copy(
                                                  initiative =
                                                    scala.math.max(1, initiative + combatant.initiativeBonus),
                                                  otherMarkers = List.empty
                                                )
                                            } ++
                                            pcs.map(
                                              _.asInstanceOf[PlayerCharacterCombatant].copy(otherMarkers = List.empty)
                                            )

                                      info
                                        .copy(currentTurnId = CombatantId.empty, round = 0, combatants = newCombatants)
                                    },
                                    "Cleared all NPC stats to beginning of combat"
                                  ) >> Callback.traverse(encounter.info.combatants.collect {
                                    case c: NonPlayerCharacterCombatant => c
                                  }) { npcCombatant =>
                                    state.npcs
                                      .find(_.header.id == npcCombatant.nonPlayerCharacterId).fold(Callback.empty) {
                                        npc =>
                                          modNPC(
                                            npc.copy(
                                              jsonInfo = npc.info
                                                .copy(
                                                  conditions = Set.empty,
                                                  health = npc.info.health.copy(
                                                    currentHitPoints = npc.info.health.maxHitPoints,
                                                    deathSave = DeathSave.empty
                                                  )
                                                ).toJsonAST.toOption.get
                                            ),
                                            ""
                                          )
                                      }
                                  }
                                }
                                .completeWith(_.get)
                          }
                          .icon(true)(Icon.className("clearIcon")),
                        Button
                          .compact(true)
                          .title("Edit encounter")
                          .onClick(
                            (
                              _,
                              _
                            ) => props.onEditEncounter(encounter)
                          )
                          .icon(true)(Icon.name(SemanticICONS.`edit`)),
                        Button
                          .compact(true)
                          .title("Archive Encounter")
                          .icon(true)
                          .onClick(
                            (
                              _,
                              _
                            ) =>
                              props.onArchiveEncounter(
                                encounter.copy(header = encounter.header.copy(status = EncounterStatus.archived))
                              )
                          )(Icon.name(SemanticICONS.`archive`))
                          .when(encounter.header.status != EncounterStatus.archived),
                        Button
                          .compact(true)
                          .onClick {
                            (
                              _,
                              _
                            ) =>
                              val combatingCombatants = sortedCombatants.filter(_.canTakeTurn)

                              val currentIndex =
                                combatingCombatants.indexWhere(c => c.id == encounter.info.currentTurnId)

                              val (nextCombatant, nextRound) = if (currentIndex < 0) {
                                (
                                  combatingCombatants.headOption.fold(CombatantId.empty)(_.id),
                                  encounter.info.round
                                )
                              } else if (currentIndex == combatingCombatants.size - 1) {
                                // Back to the beginning
                                (
                                  combatingCombatants.headOption.fold(CombatantId.empty)(_.id),
                                  encounter.info.round + 1
                                )
                              } else {
                                combatingCombatants
                                  .drop(currentIndex + 1).find(_.canTakeTurn).fold(
                                    (CombatantId.empty, encounter.info.round)
                                  ) { c =>
                                    (c.id, encounter.info.round)
                                  }
                              }

                              modEncounterInfo(
                                _.copy(round = nextRound, currentTurnId = nextCombatant),
                                ""
                              )
                          }
                          .title("Next turn")
                          .icon(true)(Icon.name(SemanticICONS.`step forward`))
                      )
                  ),
                  Table.Row(
                    Table.HeaderCell("Initiative"),
                    Table.HeaderCell("Name"),
                    Table.HeaderCell("Damage"),
                    Table.HeaderCell("HP"),
                    Table.HeaderCell("AC"),
                    Table.HeaderCell("Conditions"),
                    Table.HeaderCell("Other Markers"),
                    Table.HeaderCell( /*For actions*/ )
                  )
                ),
                Table.Body(
                  sortedCombatants
                    .filter(p =>
                      if (state.filterDeadMonsters)
                        p match {
                          case _: PlayerCharacterCombatant    => true
                          case _: NonPlayerCharacterCombatant => true
                          case m: MonsterCombatant            => !m.health.isDead
                        }
                      else true
                    )
                    .zipWithIndex // The index is used to figure out who's current turn it is.
                    .map {
                      case (pcCombatant: PlayerCharacterCombatant, i: Int) =>
                        val pc = pcs.find(_.header.id.value == pcCombatant.playerCharacterId.value).get
                        val pcInfo = pc.info

                        Table.Row
                          .withKey(pcCombatant.id.value.toString)(
                            Table.Cell(
                              Icon
                                .name(SemanticICONS.`arrow right`).when(
                                  pcCombatant.id == encounter.info.currentTurnId
                                ),
                              EditableNumber(
                                value = pcCombatant.initiative,
                                min = 1,
                                max = 30,
                                onChange = v =>
                                  modEncounterInfo(
                                    info =>
                                      info.copy(
                                        currentTurnId = CombatantId.empty, // Need to reset the current turn to the beginning, otherwise things can get woird
                                        combatants = info.combatants.map {
                                          case c: PlayerCharacterCombatant if c.id == pcCombatant.id =>
                                            c.copy(initiative = v.toInt)
                                          case c => c
                                        }
                                      ),
                                    s"Setting initiative for ${pcCombatant.name} to $v"
                                  )
                              )
                            ),
                            Table.Cell(
                              s"${pc.header.name}${pc.header.playerName.fold("")(n => s" ($n)")}"
                            ),
                            Table.Cell(
                              Button("Heal")
                                .className("healButton")
                                .compact(true)
                                .title("Enter points in the damage box and click here to heal")
                                .color(SemanticCOLORS.green)
                                .size(SemanticSIZES.mini)
                                .disabled(!state.healOrDamage.contains(pcCombatant.id))
                                .onClick {
                                  (
                                    _,
                                    _
                                  ) =>
                                    val fromIncapacitated = pc.info.health.isDead
                                    val newHitPoints =
                                      if (fromIncapacitated)
                                        state.healOrDamage(pcCombatant.id)
                                      else
                                        pcInfo.health.currentHitPoints + state.healOrDamage(pcCombatant.id)
                                    val toIncapacitated = newHitPoints <= 0
                                    modPC(
                                      pc.copy(jsonInfo =
                                        pcInfo
                                          .copy(health =
                                            pcInfo.health.copy(
                                              deathSave = DeathSave.empty, // A bit inefficient, but it's ok
                                              currentHitPoints = newHitPoints
                                            )
                                          ).toJsonAST.toOption.get
                                      ), {
                                        if (fromIncapacitated && !toIncapacitated)
                                          s"${pcCombatant.name} healed ${state.healOrDamage(pcCombatant.id)} points and is no longer incapacitated"
                                        else
                                          s"${pcCombatant.name} healed ${state.healOrDamage(pcCombatant.id)} points"
                                      }
                                    ) >> $.modState(s =>
                                      s.copy(healOrDamage = s.healOrDamage.filter(_._1 != pcCombatant.id))
                                    )

                                },
                              Input
                                .id(pcCombatant.id.value.toString)
                                .className("damageInput")
                                .size(SemanticSIZES.mini)
                                .`type`("number")
                                .min(0)
                                .maxLength(4)
                                .value(state.healOrDamage.get(pcCombatant.id).fold("")(identity))
                                .onChange {
                                  (
                                    _,
                                    data
                                  ) =>
                                    $.modState(s =>
                                      s.copy(healOrDamage = s.healOrDamage + (pcCombatant.id -> data.value.asInt()))
                                    )
                                },
                              Button("Damage")
                                .className("damageButton")
                                .compact(true)
                                .title("Enter points in the damage box and click here for damage")
                                .color(SemanticCOLORS.red)
                                .size(SemanticSIZES.mini)
                                .disabled(!state.healOrDamage.contains(pcCombatant.id))
                                .onClick {
                                  (
                                    _,
                                    _
                                  ) =>
                                    val fromIncapacitated = pc.info.health.isDead
                                    val newHitPoints =
                                      pcInfo.health.currentHitPoints - state.healOrDamage(pcCombatant.id)
                                    val toIncapacitated = newHitPoints <= 0
                                    modPC(
                                      pc.copy(jsonInfo =
                                        pcInfo
                                          .copy(health =
                                            pcInfo.health.copy(
                                              deathSave = pcInfo.health.deathSave
                                                .copy(isStabilized = !toIncapacitated),
                                              currentHitPoints = newHitPoints
                                            )
                                          ).toJsonAST.toOption.get
                                      ),
                                      if (toIncapacitated && !fromIncapacitated)
                                        s"${pc.header.name} got ${state.healOrDamage(pcCombatant.id)} points of damage, and is now below zero hitpoints!"
                                      else
                                        s"${pc.header.name} got ${state.healOrDamage(pcCombatant.id)} points of damage"
                                    ) >> $.modState(s =>
                                      s.copy(healOrDamage = s.healOrDamage.filter(_._1 != pcCombatant.id))
                                    )
                                },
                              DeathSaveComponent(
                                pc.info.health.deathSave,
                                onChange = deathSave =>
                                  modPC(
                                    pc.copy(jsonInfo =
                                      pcInfo
                                        .copy(health = pcInfo.health.copy(deathSave = deathSave)).toJsonAST.toOption.get
                                    ),
                                    ""
                                  )
                              )
                                .when(pc.info.health.currentHitPoints <= 0)
                            ),
                            Table.Cell
                              .singleLine(true)
                              .style {
                                CSSProperties().set("backgroundColor", pcInfo.health.lifeColor())
                              }(
                                EditableComponent(
                                  view = s"${pcInfo.health.currentHitPoints} / ${pcInfo.health.maxHitPoints}",
                                  edit = HealthEditor(
                                    pcInfo.health,
                                    hp =>
                                      modPC(
                                        pc.copy(jsonInfo = pcInfo.copy(health = hp).toJsonAST.toOption.get),
                                        s"Setting hit points for ${pc.header.name} to $hp"
                                      )
                                  ),
                                  title = "Hit points",
                                  onEditingModeChange = modeChange
                                )
                              ),
                            Table.Cell
                              .textAlign(semanticUiReactStrings.center)(
                                EditableNumber(
                                  value = pcInfo.armorClass,
                                  min = 1,
                                  max = 30,
                                  onChange = v =>
                                    modPC(
                                      pc.copy(jsonInfo = pcInfo.copy(armorClass = v.toInt).toJsonAST.toOption.get),
                                      s"Setting armor class for ${pc.header.name} to $v"
                                    )
                                )
                              ),
                            Table.Cell
                              .textAlign(semanticUiReactStrings.center)(
                                EditableComponent(
                                  view = pcInfo.conditions.headOption.fold(
                                    Icon.name(SemanticICONS.`plus circle`): VdomNode
                                  )(_ => VdomNode(pcInfo.conditions.mkString(", "))),
                                  edit = ConditionsEditor(
                                    conditions = pcInfo.conditions,
                                    onChange = c =>
                                      modPC(
                                        pc.copy(jsonInfo = pcInfo.copy(conditions = c).toJsonAST.toOption.get),
                                        if (c.isEmpty) s"Removing all conditions from ${pc.header.name}"
                                        else
                                          s"Setting conditions for ${pc.header.name} to ${c.map(_.toString).mkString(", ")}"
                                      )
                                  ),
                                  title = "Conditions",
                                  onEditingModeChange = modeChange
                                )
                              ),
                            Table.Cell
                              .textAlign(semanticUiReactStrings.center)(
                                EditableComponent(
                                  view = pcCombatant.otherMarkers.headOption
                                    .fold(
                                      Icon.name(SemanticICONS.`plus circle`): VdomNode
                                    )(_ => VdomNode(pcCombatant.otherMarkers.map(_.name).mkString(", "))),
                                  edit = OtherMarkersEditor(
                                    otherMarkers = pcCombatant.otherMarkers,
                                    onChange = otherMarkers =>
                                      modEncounterInfo(
                                        info =>
                                          info.copy(
                                            combatants = info.combatants.map {
                                              case pcCombatant2: PlayerCharacterCombatant
                                                  if pcCombatant2.id == pcCombatant.id =>
                                                pcCombatant2.copy(otherMarkers = otherMarkers)
                                              case c => c
                                            }
                                          ),
                                        if (otherMarkers.isEmpty)
                                          s"Removing all other markers from ${pcCombatant.name}"
                                        else
                                          s"Setting other markers for ${pcCombatant.name} to ${otherMarkers
                                              .map(_.name).mkString(", ")}"
                                      )
                                  ),
                                  title = "Other Markers",
                                  onEditingModeChange = modeChange
                                )
                              ),
                            Table.Cell
                              .singleLine(true)(
                                Button
                                  .title("View character stats")
                                  .compact(true)
                                  .size(SemanticSIZES.mini)
                                  .icon(true)(Icon.name(SemanticICONS.`eye`))
                                  .onClick(
                                    (
                                      _,
                                      _
                                    ) => $.modState(_.copy(viewPCId = Some(pc.header.id)))
                                  )
                              )
                          )
                      case (npcCombatant: NonPlayerCharacterCombatant, i: Int) =>
                        val npc = npcs.find(_.header.id.value == npcCombatant.nonPlayerCharacterId.value).get
                        val npcInfo = npc.info

                        Table.Row
                          .withKey(npcCombatant.id.value.toString)(
                            Table.Cell(
                              Icon
                                .name(SemanticICONS.`arrow right`).when(
                                  npcCombatant.id == encounter.info.currentTurnId
                                ),
                              EditableNumber(
                                value = npcCombatant.initiative,
                                min = 1,
                                max = 30,
                                onChange = v =>
                                  modEncounterInfo(
                                    info =>
                                      info.copy(
                                        currentTurnId = CombatantId.empty, // Need to reset the current turn to the beginning, otherwise things can get woird
                                        combatants = info.combatants.map {
                                          case c: NonPlayerCharacterCombatant if c.id == npcCombatant.id =>
                                            c.copy(initiative = v.toInt)
                                          case c => c
                                        }
                                      ),
                                    s"Setting initiative for ${npcCombatant.name} to $v"
                                  )
                              )
                            ),
                            Table.Cell(npc.header.name),
                            Table.Cell(
                              Button("Heal")
                                .className("healButton")
                                .compact(true)
                                .title("Enter points in the damage box and click here to heal")
                                .color(SemanticCOLORS.green)
                                .size(SemanticSIZES.mini)
                                .disabled(!state.healOrDamage.contains(npcCombatant.id))
                                .onClick {
                                  (
                                    _,
                                    _
                                  ) =>
                                    val fromIncapacitated = npc.info.health.isDead
                                    val newHitPoints =
                                      if (fromIncapacitated)
                                        state.healOrDamage(npcCombatant.id)
                                      else
                                        npcInfo.health.currentHitPoints + state.healOrDamage(npcCombatant.id)
                                    val toIncapacitated = newHitPoints <= 0
                                    modNPC(
                                      npc.copy(jsonInfo =
                                        npcInfo
                                          .copy(health =
                                            npcInfo.health.copy(
                                              deathSave = DeathSave.empty, // A bit inefficient, but it's ok
                                              currentHitPoints = newHitPoints
                                            )
                                          ).toJsonAST.toOption.get
                                      ), {
                                        if (fromIncapacitated && !toIncapacitated)
                                          s"${npcCombatant.name} healed ${state.healOrDamage(npcCombatant.id)} points and is no longer incapacitated"
                                        else
                                          s"${npcCombatant.name} healed ${state.healOrDamage(npcCombatant.id)} points"
                                      }
                                    ) >> $.modState(s =>
                                      s.copy(healOrDamage = s.healOrDamage.filter(_._1 != npcCombatant.id))
                                    )

                                },
                              Input
                                .id(npcCombatant.id.value.toString)
                                .className("damageInput")
                                .size(SemanticSIZES.mini)
                                .`type`("number")
                                .min(0)
                                .maxLength(4)
                                .value(state.healOrDamage.get(npcCombatant.id).fold("")(identity))
                                .onChange {
                                  (
                                    _,
                                    data
                                  ) =>
                                    $.modState(s =>
                                      s.copy(healOrDamage = s.healOrDamage + (npcCombatant.id -> data.value.asInt()))
                                    )
                                },
                              Button("Damage")
                                .className("damageButton")
                                .compact(true)
                                .title("Enter points in the damage box and click here for damage")
                                .color(SemanticCOLORS.red)
                                .size(SemanticSIZES.mini)
                                .disabled(!state.healOrDamage.contains(npcCombatant.id))
                                .onClick {
                                  (
                                    _,
                                    _
                                  ) =>
                                    val fromIncapacitated = npc.info.health.isDead
                                    val newHitPoints =
                                      npcInfo.health.currentHitPoints - state.healOrDamage(npcCombatant.id)
                                    val toIncapacitated = newHitPoints <= 0
                                    modNPC(
                                      npc.copy(jsonInfo =
                                        npcInfo
                                          .copy(health =
                                            npcInfo.health.copy(
                                              deathSave = npcInfo.health.deathSave
                                                .copy(isStabilized = !toIncapacitated),
                                              currentHitPoints = newHitPoints
                                            )
                                          ).toJsonAST.toOption.get
                                      ),
                                      if (toIncapacitated && !fromIncapacitated)
                                        s"${npc.header.name} got ${state.healOrDamage(npcCombatant.id)} points of damage, and is now below zero hitpoints!"
                                      else
                                        s"${npc.header.name} got ${state.healOrDamage(npcCombatant.id)} points of damage"
                                    ) >> $.modState(s =>
                                      s.copy(healOrDamage = s.healOrDamage.filter(_._1 != npcCombatant.id))
                                    )
                                },
                              DeathSaveComponent(
                                npc.info.health.deathSave,
                                onChange = deathSave =>
                                  modNPC(
                                    npc.copy(jsonInfo =
                                      npcInfo
                                        .copy(health =
                                          npcInfo.health.copy(deathSave = deathSave)
                                        ).toJsonAST.toOption.get
                                    ),
                                    ""
                                  )
                              )
                                .when(npc.info.health.currentHitPoints <= 0)
                            ),
                            Table.Cell
                              .singleLine(true).style(
                                CSSProperties().set("backgroundColor", npcInfo.health.lifeColor())
                              )(
                                EditableComponent(
                                  view = s"${npcInfo.health.currentHitPoints} / ${npcInfo.health.maxHitPoints}",
                                  edit = HealthEditor(
                                    npcInfo.health,
                                    hp =>
                                      modNPC(
                                        npc.copy(jsonInfo = npcInfo.copy(health = hp).toJsonAST.toOption.get),
                                        s"Setting hit points for ${npc.header.name} to $hp"
                                      )
                                  ),
                                  title = "Hit points",
                                  onEditingModeChange = modeChange
                                )
                              ),
                            Table.Cell.textAlign(semanticUiReactStrings.center)(
                              EditableNumber(
                                value = npcInfo.armorClass,
                                min = 1,
                                max = 30,
                                onChange = v =>
                                  modNPC(
                                    npc.copy(jsonInfo = npcInfo.copy(armorClass = v.toInt).toJsonAST.toOption.get),
                                    s"Setting armor class for ${npc.header.name} to $v"
                                  )
                              )
                            ),
                            Table.Cell.textAlign(semanticUiReactStrings.center)(
                              EditableComponent(
                                view = npcInfo.conditions.headOption.fold(
                                  Icon.name(SemanticICONS.`plus circle`): VdomNode
                                )(_ => VdomNode(npcInfo.conditions.mkString(", "))),
                                edit = ConditionsEditor(
                                  conditions = npcInfo.conditions,
                                  onChange = c =>
                                    modNPC(
                                      npc.copy(jsonInfo = npcInfo.copy(conditions = c).toJsonAST.toOption.get),
                                      if (c.isEmpty) s"Removing all conditions from ${npc.header.name}"
                                      else
                                        s"Setting conditions for ${npc.header.name} to ${c.map(_.toString).mkString(", ")}"
                                    )
                                ),
                                title = "Conditions",
                                onEditingModeChange = modeChange
                              )
                            ),
                            Table.Cell.textAlign(semanticUiReactStrings.center)(
                              EditableComponent(
                                view = npcCombatant.otherMarkers.headOption
                                  .fold(
                                    Icon.name(SemanticICONS.`plus circle`): VdomNode
                                  )(_ => VdomNode(npcCombatant.otherMarkers.map(_.name).mkString(", "))),
                                edit = OtherMarkersEditor(
                                  otherMarkers = npcCombatant.otherMarkers,
                                  onChange = otherMarkers =>
                                    modEncounterInfo(
                                      info =>
                                        info.copy(
                                          combatants = info.combatants.map {
                                            case npcCombatant2: NonPlayerCharacterCombatant
                                                if npcCombatant2.id == npcCombatant.id =>
                                              npcCombatant2.copy(otherMarkers = otherMarkers)
                                            case c => c
                                          }
                                        ),
                                      if (otherMarkers.isEmpty)
                                        s"Removing all other markers from ${npcCombatant.name}"
                                      else
                                        s"Setting other markers for ${npcCombatant.name} to ${otherMarkers
                                            .map(_.name).mkString(", ")}"
                                    )
                                ),
                                title = "Other Markers",
                                onEditingModeChange = modeChange
                              )
                            ),
                            Table.Cell.singleLine(true)(
                              Button
                                .title("View character stats")
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
                      case (monsterCombatant: MonsterCombatant, i: Int) =>
                        Table.Row
                          .withKey(monsterCombatant.id.value.toString)(
                            Table.Cell(
                              Icon
                                .name(SemanticICONS.`arrow right`).when(
                                  monsterCombatant.id == encounter.info.currentTurnId
                                ),
                              EditableNumber(
                                value = monsterCombatant.initiative,
                                min = 1,
                                max = 30,
                                onChange = v =>
                                  modEncounterInfo(
                                    info =>
                                      info
                                        .copy(
                                          currentTurnId = CombatantId.empty, // Need to reset the current turn to the beginning, otherwise things can get woird
                                          combatants = info.combatants.map {
                                            case c: MonsterCombatant if c.id == monsterCombatant.id =>
                                              c.copy(initiative = v.toInt)
                                            case c => c
                                          }
                                        ),
                                    s"Setting initiative for ${monsterCombatant.name} to $v"
                                  )
                              )
                            ), // Add and round > 0
                            Table.Cell(
                              EditableText(
                                value = monsterCombatant.name,
                                onChange = name =>
                                  modEncounterInfo(
                                    info =>
                                      info.copy(
                                        combatants = info.combatants.map {
                                          case monsterCombatant2: MonsterCombatant
                                              if monsterCombatant2.id == monsterCombatant.id =>
                                            monsterCombatant2.copy(name = name)
                                          case c => c
                                        }
                                      ),
                                    ""
                                  )
                              )
                            ),
                            Table.Cell(
                              Button("Heal")
                                .className("healButton")
                                .compact(true)
                                .title("Enter points in the damage box and click here to heal")
                                .color(SemanticCOLORS.green)
                                .size(SemanticSIZES.mini)
                                .disabled(!state.healOrDamage.contains(monsterCombatant.id))
                                .onClick {
                                  (
                                    _,
                                    _
                                  ) =>

                                    val fromIsDead = monsterCombatant.health.isDead
                                    val toIsDead = state.healOrDamage.get(monsterCombatant.id).exists(_ <= 0)

                                    modEncounterInfo(
                                      info => {
                                        info.copy(combatants = info.combatants.map {
                                          case monsterCombatant2: MonsterCombatant
                                              if monsterCombatant2.id == monsterCombatant.id =>
                                            monsterCombatant2.copy(health =
                                              monsterCombatant2.health.copy(
                                                deathSave = DeathSave.empty,
                                                currentHitPoints = monsterCombatant2.health.currentHitPoints + state
                                                  .healOrDamage(monsterCombatant.id)
                                              )
                                            )
                                          case c => c
                                        })
                                      },
                                      log =
                                        if (fromIsDead && !toIsDead)
                                          s"${monsterCombatant.name} healed ${state.healOrDamage(monsterCombatant.id)} points, thus coming back to life"
                                        else
                                          s"${monsterCombatant.name} healed ${state.healOrDamage(monsterCombatant.id)} points"
                                    ) >> $.modState(s =>
                                      s.copy(healOrDamage = s.healOrDamage.filter(_._1 != monsterCombatant.id))
                                    )
                                },
                              Input
                                .id(s"combatantDamageInput${monsterCombatant.id.value}")
                                .className("damageInput")
                                .size(SemanticSIZES.mini)
                                .`type`("number")
                                .min(0)
                                .maxLength(4)
                                .value(state.healOrDamage.get(monsterCombatant.id).fold("")(identity))
                                .onChange {
                                  (
                                    _,
                                    data
                                  ) =>
                                    $.modState(s =>
                                      s.copy(healOrDamage =
                                        s.healOrDamage + (monsterCombatant.id -> data.value.asInt())
                                      )
                                    )
                                },
                              Button("Damage")
                                .className("damageButton")
                                .compact(true)
                                .title("Enter points in the damage box and click here for damage")
                                .color(SemanticCOLORS.red)
                                .size(SemanticSIZES.mini)
                                .disabled(!state.healOrDamage.contains(monsterCombatant.id))
                                .onClick {
                                  (
                                    _,
                                    _
                                  ) =>
                                    val fromIsDead = monsterCombatant.health.isDead
                                    val newHitPoints =
                                      monsterCombatant.health.currentHitPoints - state
                                        .healOrDamage(monsterCombatant.id)
                                    val toIsDead = newHitPoints <= 0
                                    modEncounterInfo(
                                      info =>
                                        info.copy(combatants = info.combatants.map {
                                          case monsterCombatant2: MonsterCombatant
                                              if monsterCombatant2.id == monsterCombatant.id =>
                                            monsterCombatant2.copy(health =
                                              monsterCombatant2.health.copy(
                                                deathSave = monsterCombatant2.health.deathSave
                                                  .copy(isStabilized = !toIsDead),
                                                currentHitPoints = monsterCombatant2.health.currentHitPoints - state
                                                  .healOrDamage(monsterCombatant.id)
                                              )
                                            )
                                          case c => c
                                        }), {
                                        if (toIsDead && !fromIsDead)
                                          s"${monsterCombatant.name} took ${state.healOrDamage(monsterCombatant.id)} points of damage, it's now dead"
                                        else
                                          s"${monsterCombatant.name} took ${state.healOrDamage(monsterCombatant.id)} points of damage"

                                      }
                                    ) >> $.modState(s =>
                                      s.copy(healOrDamage = s.healOrDamage.filter(_._1 != monsterCombatant.id))
                                    )
                                }
                            ),
                            Table.Cell
                              .singleLine(true).style(
                                CSSProperties()
                                  .set("backgroundColor", monsterCombatant.health.lifeColor(transparency = 0.7))
                              )(
                                s"${monsterCombatant.health.currentHitPoints} / ${monsterCombatant.health.maxHitPoints}"
                              ),
                            Table.Cell.textAlign(semanticUiReactStrings.center)(
                              EditableNumber(
                                value = monsterCombatant.armorClass,
                                min = 1,
                                max = 30,
                                onChange = v =>
                                  modEncounterInfo(
                                    info =>
                                      info.copy(combatants = info.combatants.map {
                                        case monsterCombatant2: MonsterCombatant
                                            if monsterCombatant2.id == monsterCombatant.id =>
                                          monsterCombatant2.copy(armorClass = v.toInt)
                                        case c => c
                                      }),
                                    ""
                                  )
                              )
                            ),
                            Table.Cell.textAlign(semanticUiReactStrings.center)(
                              EditableComponent(
                                view = monsterCombatant.conditions.headOption.fold(
                                  Icon.name(SemanticICONS.`plus circle`): VdomNode
                                )(_ => VdomNode(monsterCombatant.conditions.mkString(", "))),
                                edit = ConditionsEditor(
                                  conditions = monsterCombatant.conditions,
                                  onChange = c =>
                                    modEncounterInfo(
                                      info =>
                                        info.copy(
                                          combatants = info.combatants.map {
                                            case monsterCombatant2: MonsterCombatant
                                                if monsterCombatant2.id == monsterCombatant.id =>
                                              monsterCombatant2.copy(conditions = c)
                                            case c => c
                                          }
                                        ),
                                      if (c.isEmpty) s"Removing all conditions from ${monsterCombatant.name}"
                                      else
                                        s"Setting conditions for ${monsterCombatant.name} to ${c.map(_.toString).mkString(", ")}"
                                    )
                                ),
                                title = "Conditions",
                                onEditingModeChange = modeChange
                              )
                            ),
                            Table.Cell.textAlign(semanticUiReactStrings.center)(
                              EditableComponent(
                                view = monsterCombatant.otherMarkers.headOption
                                  .fold(
                                    Icon.name(SemanticICONS.`plus circle`): VdomNode
                                  )(_ => VdomNode(monsterCombatant.otherMarkers.map(_.name).mkString(", "))),
                                edit = OtherMarkersEditor(
                                  otherMarkers = monsterCombatant.otherMarkers,
                                  onChange = otherMarkers =>
                                    modEncounterInfo(
                                      info =>
                                        info.copy(
                                          combatants = info.combatants.map {
                                            case monsterCombatant2: MonsterCombatant
                                                if monsterCombatant2.id == monsterCombatant.id =>
                                              monsterCombatant2.copy(otherMarkers = otherMarkers)
                                            case c => c
                                          }
                                        ),
                                      if (otherMarkers.isEmpty)
                                        s"Removing all other markers from ${monsterCombatant.name}"
                                      else
                                        s"Setting other markers for ${monsterCombatant.name} to ${otherMarkers
                                            .map(_.name).mkString(", ")}"
                                    )
                                ),
                                title = "Other Markers",
                                onEditingModeChange = modeChange
                              )
                            ),
                            Table.Cell.singleLine(true)(
                              Button
                                .compact(true)
                                .title("Delete Monster")
                                .size(SemanticSIZES.mini)
                                .icon(true)(Icon.name(SemanticICONS.`trash`))
                                .onClick(
                                  (
                                    _,
                                    _
                                  ) =>
                                    modEncounterInfo(
                                      info =>
                                        info.copy(combatants =
                                          info.combatants.filter(_.id.value != monsterCombatant.id.value)
                                        ),
                                      s"${monsterCombatant.name} was removed from the encounter"
                                    )
                                ),
                              Button
                                .compact(true)
                                .title("Add another monster of this type, more monsters!")
                                .size(SemanticSIZES.mini)
                                .icon(true)(Icon.name(SemanticICONS.`clone outline`))
                                .onClick(
                                  (
                                    _,
                                    _
                                  ) =>
                                    modEncounterInfo(
                                      info =>
                                        monsterCombatant match {
                                          case monsterCombatant: MonsterCombatant =>
                                            info.copy(combatants =
                                              info.combatants :+
                                                createMonsterCombatant(encounter, monsterCombatant.monsterHeader)
                                            )
                                        },
                                      s"Added another ${monsterCombatant.name} to the encounter"
                                    )
                                ),
                              Button
                                .compact(true)
                                .title("View monster stat block")
                                .size(SemanticSIZES.mini)
                                .icon(true)(Icon.name(SemanticICONS.`eye`))
                                .onClick(
                                  (
                                    _,
                                    _
                                  ) => $.modState(_.copy(viewMonsterId = Some(monsterCombatant.monsterHeader.id)))
                                )
                            )
                          )
                    }*
                )
              )
          )
        }).getOrElse(EmptyVdom)
      }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("CombatRunner")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props))
    .shouldComponentUpdatePure { $ =>
      $.nextState.dialogMode == DialogMode.closed
    }
    .build

  def apply(
    campaignId:  CampaignId,
    encounterId: EncounterId,
    onChange: (Encounter, String) => Callback = (
      _,
      _
    ) => Callback.empty,
    onEditEncounter:    Encounter => Callback = _ => Callback.empty,
    onArchiveEncounter: Encounter => Callback = _ => Callback.empty,
    onModeChange:       EditingMode => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] =
    component
      .withKey(encounterId.value)(
        Props(
          campaignId = campaignId,
          encounterId = encounterId,
          onChange = onChange,
          onEditEncounter = onEditEncounter,
          onArchiveEncounter = onArchiveEncounter,
          onModeChange = onModeChange
        )
      )

}
