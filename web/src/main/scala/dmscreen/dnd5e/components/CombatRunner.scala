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

import caliban.ScalaJSClientAdapter.*
import caliban.client.SelectionBuilder
import caliban.client.scalajs.DND5eClient.{
  Monster as CalibanMonster,
  MonsterHeader as CalibanMonsterHeader,
  MonsterSearchOrder as CalibanMonsterSearchOrder,
  MonsterSearchResults as CalibanMonsterSearchResults,
  MonsterType as CalibanMonsterType,
  OrderDirection as CalibanOrderDirection,
  Queries
}
import caliban.client.scalajs.{*, given}
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.components.EditableComponent.Mode
import dmscreen.dnd5e.{*, given}
import dmscreen.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.{^, *}
import japgolly.scalajs.react.{Callback, CtorType, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, Table, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsAddonsPaginationPaginationMod.PaginationProps
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{
  SemanticCOLORS,
  SemanticICONS,
  SemanticSIZES,
  SemanticWIDTHS
}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesAccordionAccordionTitleMod.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import org.scalajs.dom.*
import zio.json.*
import zio.json.ast.Json

import scala.scalajs.js.JSConverters.*

//TODO, BUG rerolling initiative is messing with things, the monsters don't reflect the new initiative.
object CombatRunner {

  case class State(
//    encounter:     Encounter,
    healOrDamage:  Map[CombatantId, Int] = Map.empty,
    dialogMode:    DialogMode = DialogMode.closed,
    viewMonsterId: Option[MonsterId] = None
  )
  case class Props(
    encounter:          Encounter,
    pcs:                Seq[PlayerCharacter],
    onChange:           (Encounter, String) => Callback,
    onEditEncounter:    Encounter => Callback,
    onArchiveEncounter: Encounter => Callback,
    onModeChange:       Mode => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      state: State,
      props: Props
    ): VdomNode = {

      val encounter = props.encounter

      DMScreenState.ctx.consume { dmScreenState =>
        def modEncounter(
          f:   Encounter => Encounter,
          log: String
        ) = props.onChange(f(props.encounter), log)

        def modEncounterInfo(
          f:   EncounterInfo => EncounterInfo,
          log: String
        ) = modEncounter(e => e.copy(jsonInfo = f(e.info).toJsonAST.toOption.get), log)
        def modPC(
          pc:  PlayerCharacter,
          log: String
        ) =
          dmScreenState.campaignState.fold(Callback.empty) { case dnd5eCampaignState: DND5eCampaignState =>
            dmScreenState.onModifyCampaignState(
              dnd5eCampaignState.copy(
                pcs = dnd5eCampaignState.pcs.map {
                  case p if p.header.id == pc.header.id => p.copy(jsonInfo = pc.jsonInfo)
                  case p                                => p
                },
                changeStack = dnd5eCampaignState.changeStack.logPCChanges(pc.header.id)
              ),
              log
            )
          }

        val pcs = dmScreenState.campaignState.fold(List.empty[PlayerCharacter])(_.asInstanceOf[DND5eCampaignState].pcs)

        VdomArray(
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
                Modal.Content(MonsterStatBlock(monsterId))
              ): VdomNode
          ),
          Table
            .withKey("CombatRunnerTable")(
              Table.Header(
                Table.Row(
                  Table.HeaderCell.colSpan(2)(<.h2(s"${encounter.header.name}")),
                  Table.HeaderCell
                    .colSpan(2).textAlign(semanticUiReactStrings.center)(
                      s"Difficulty: ${encounter.calculateDifficulty(pcs)}",
                      <.br(),
                      s"XP: ${encounter.info.xp}"
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
                        .title("Roll Initiative for all NPCs")
                        .onClick {
                          (
                            _,
                            _
                          ) =>

                            val monsterCount = encounter.info.combatants.count(_.isInstanceOf[MonsterCombatant])
                            DiceRoller
                              .roll(s"${monsterCount}d20")
                              .map { results =>
                                modEncounterInfo(
                                  { e =>
                                    val (monsters, pcs) =
                                      encounter.info.combatants.partition(_.isInstanceOf[MonsterCombatant])
                                    val initiatives = results.map(_.value)

                                    val newCombatants = monsters
                                      .map(_.asInstanceOf[MonsterCombatant])
                                      .zip(initiatives)
                                      .map(
                                        (
                                          monsterCombatant,
                                          initiative
                                        ) =>
                                          monsterCombatant.copy(initiative =
                                            scala.math.max(1, initiative + monsterCombatant.initiativeBonus)
                                          )
                                      ) ++
                                      pcs

                                    e.copy(
                                      currentTurn = 0, // Need to reset the current turn to the beginning, otherwise things can get woird
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
                            val monsterCount = encounter.info.combatants.count(_.isInstanceOf[MonsterCombatant])
                            DiceRoller
                              .roll(s"${monsterCount}d20")
                              .map { results =>
                                modEncounterInfo(
                                  { e =>
                                    val (monsters, pcs) =
                                      encounter.info.combatants.partition(_.isInstanceOf[MonsterCombatant])
                                    val initiatives = results.map(_.value)

                                    val newCombatants = monsters
                                      .map(_.asInstanceOf[MonsterCombatant])
                                      .zip(initiatives)
                                      .map(
                                        (
                                          monsterCombatant,
                                          initiative
                                        ) =>
                                          monsterCombatant.copy(
                                            initiative =
                                              scala.math.max(1, initiative + monsterCombatant.initiativeBonus),
                                            health = monsterCombatant.health
                                              .copy(currentHitPoints = monsterCombatant.health.maxHitPoints),
                                            conditions = Set.empty,
                                            otherMarkers = List.empty
                                          )
                                      ) ++
                                  pcs.map(_.asInstanceOf[PlayerCharacterCombatant].copy(otherMarkers = List.empty))

                                  e.copy(currentTurn = 0, round = 0, combatants = newCombatants)
                                  },
                                  "Cleared all NPC stats to beginning of combat"
                                )
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
                        .onClick(
                          (
                            _,
                            _
                          ) =>
                            modEncounterInfo(
                              info =>
                                if (info.currentTurn == info.combatants.length - 1) {
                                  info.copy(round = info.round + 1, currentTurn = 0)
                                } else {
                                  info.copy(currentTurn = info.currentTurn + 1)
                                },
                              ""
                            )
                        )
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
                encounter.info.combatants
                  .sortBy(-_.initiative)
                  .zipWithIndex
                  .map {
                    case (pcCombatant: PlayerCharacterCombatant, i: Int) =>
                      val pc = props.pcs.find(_.header.id.value == pcCombatant.playerCharacterId.value).get
                      val pcInfo = pc.info

                      Table.Row
                        .withKey(pcCombatant.id.value.toString)(
                          Table.Cell(
                            Icon.name(SemanticICONS.`arrow right`).when(i == encounter.info.currentTurn),
                            EditableNumber(
                              value = pcCombatant.initiative,
                              min = 1,
                              max = 30,
                              onChange = v =>
                                modEncounterInfo(
                                  info =>
                                    info.copy(
                                      currentTurn = 0, // Need to reset the current turn to the beginning, otherwise things can get woird
                                      combatants =
                                        info.combatants.filter(_.id.value != pcCombatant.id.value) :+ pcCombatant
                                          .copy(initiative = v.toInt)
                                    ),
                                  s"Setting initiative for ${pcCombatant.name} to $v"
                                )
                            )
                          ),
                          Table.Cell(pc.header.name),
                          Table.Cell(
                            Button("Heal")
                              .className("healButton")
                              .compact(true)
                              .title("Enter points in the damage box and click here to heal")
                              .color(SemanticCOLORS.green)
                              .basic(true)
                              .size(SemanticSIZES.mini)
                              .disabled(!state.healOrDamage.contains(pcCombatant.id))
                              .onClick {
                                (
                                  _,
                                  _
                                ) =>
                                  val fromIncapacitated = pc.info.health.isDead
                                  val newHitPoints = pcInfo.health.currentHitPoints + state.healOrDamage(pcCombatant.id)
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
                                  val newVal = data.value match {
                                    case s: String => s.toDouble
                                    case d: Double => d
                                  }
                                  $.modState(s =>
                                    s.copy(healOrDamage = s.healOrDamage + (pcCombatant.id -> newVal.toInt))
                                  )
                              },
                            Button("Damage")
                              .className("damageButton")
                              .compact(true)
                              .title("Enter points in the damage box and click here for damage")
                              .color(SemanticCOLORS.red)
                              .basic(true)
                              .size(SemanticSIZES.mini)
                              .disabled(!state.healOrDamage.contains(pcCombatant.id))
                              .onClick {
                                (
                                  _,
                                  _
                                ) =>
                                  val fromIncapacitated = pc.info.health.isDead
                                  val newHitPoints = pcInfo.health.currentHitPoints - state.healOrDamage(pcCombatant.id)
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
                            .singleLine(true).style(CSSProperties().set("backgroundColor", pcInfo.health.lifeColor))(
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
                                onModeChange = mode =>
                                  dmScreenState.changeDialogMode(
                                    if (mode == EditableComponent.Mode.edit) DialogMode.open else DialogMode.closed
                                  )
                              )
                            ),
                          Table.Cell.textAlign(semanticUiReactStrings.center)(
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
                          Table.Cell.textAlign(semanticUiReactStrings.center)(
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
                              onModeChange = mode =>
                                dmScreenState.changeDialogMode(
                                  if (mode == EditableComponent.Mode.edit) DialogMode.open else DialogMode.closed
                                )
                            )
                          ),
                          Table.Cell.textAlign(semanticUiReactStrings.center)(
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
                                    if (otherMarkers.isEmpty) s"Removing all other markers from ${pcCombatant.name}"
                                    else
                                      s"Setting other markers for ${pcCombatant.name} to ${otherMarkers.map(_.name).mkString(", ")}"
                                  )
                              ),
                              title = "Other Markers",
                              onModeChange = mode =>
                                dmScreenState.changeDialogMode(
                                  if (mode == EditableComponent.Mode.edit) DialogMode.open else DialogMode.closed
                                ) >> $.modState(s =>
                                  s.copy(dialogMode =
                                    if (mode == EditableComponent.Mode.edit) DialogMode.open else DialogMode.closed
                                  )
                                )
                            )
                          ),
                          Table.Cell.singleLine(true)(
                            Button
                              .title("View character stats")
                              .compact(true)
                              .size(SemanticSIZES.mini)
                              .icon(true)(Icon.name(SemanticICONS.`eye`))
                          )
                        )
                    case (monsterCombatant: MonsterCombatant, i: Int) =>
                      Table.Row
                        .withKey(monsterCombatant.id.value.toString)(
                          Table.Cell(
                            Icon.name(SemanticICONS.`arrow right`).when(i == encounter.info.currentTurn),
                            EditableNumber(
                              value = monsterCombatant.initiative,
                              min = 1,
                              max = 30,
                              onChange = v =>
                                modEncounterInfo(
                                  info =>
                                    info
                                      .copy(
                                        currentTurn = 0, // Need to reset the current turn to the beginning, otherwise things can get woird
                                        combatants = info.combatants
                                          .filter(_.id.value != monsterCombatant.id.value) :+ monsterCombatant
                                          .copy(initiative = v.toInt)
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
                              .basic(true)
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
                                  val newVal = data.value match {
                                    case s: String => s.toDouble
                                    case d: Double => d
                                  }
                                  $.modState(s =>
                                    s.copy(healOrDamage = s.healOrDamage + (monsterCombatant.id -> newVal.toInt))
                                  )
                              },
                            Button("Damage")
                              .className("damageButton")
                              .compact(true)
                              .title("Enter points in the damage box and click here for damage")
                              .color(SemanticCOLORS.red)
                              .basic(true)
                              .size(SemanticSIZES.mini)
                              .disabled(!state.healOrDamage.contains(monsterCombatant.id))
                              .onClick {
                                (
                                  _,
                                  _
                                ) =>
                                  val fromIsDead = monsterCombatant.health.isDead
                                  val newHitPoints =
                                    monsterCombatant.health.currentHitPoints - state.healOrDamage(monsterCombatant.id)
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
                              CSSProperties().set("backgroundColor", monsterCombatant.health.lifeColor)
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
                              onModeChange = mode =>
                                dmScreenState.changeDialogMode(
                                  if (mode == EditableComponent.Mode.edit) DialogMode.open else DialogMode.closed
                                )
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
                              onModeChange = mode =>
                                dmScreenState.changeDialogMode(
                                  if (mode == EditableComponent.Mode.edit) DialogMode.open else DialogMode.closed
                                ) >> $.modState(s =>
                                  s.copy(dialogMode =
                                    if (mode == EditableComponent.Mode.edit) DialogMode.open else DialogMode.closed
                                  )
                                )
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
      }
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("CombatRunner")
    .initialState(State())
    .renderBackend[Backend]
    .shouldComponentUpdatePure { $ =>
      $.nextState.dialogMode == DialogMode.closed
    }
    .build

  def apply(
    encounter: Encounter,
    pcs:       Seq[PlayerCharacter],
    onChange: (Encounter, String) => Callback = (
      _,
      _
    ) => Callback.empty,
    onEditEncounter:    Encounter => Callback = _ => Callback.empty,
    onArchiveEncounter: Encounter => Callback = _ => Callback.empty,
    onModeChange:       Mode => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] =
    component
      .withKey(encounter.header.id.value)(
        Props(encounter, pcs, onChange, onEditEncounter, onArchiveEncounter, onModeChange)
      )

}
