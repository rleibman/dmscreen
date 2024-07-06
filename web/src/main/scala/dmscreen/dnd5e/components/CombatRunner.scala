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
import japgolly.scalajs.react.vdom.html_<^.*
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

object CombatRunner {

  case class State(
    encounter:     Encounter,
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
      val encounter = state.encounter

      def modEncounter(
        f:   Encounter => Encounter,
        log: String
      ) =
        $.modState(
          s => s.copy(encounter = f(s.encounter)),
          $.state.flatMap(s => props.onChange(s.encounter, log))
        )

      def modEncounterInfo(
        f:   EncounterInfo => EncounterInfo,
        log: String
      ) = modEncounter(e => e.copy(jsonInfo = f(e.info).toJsonAST.toOption.get), log)

      DMScreenState.ctx.consume { dmScreenState =>
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
          Table(
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
                      .onClick(
                        (
                          _,
                          _
                        ) =>
                          modEncounterInfo(
                            info =>
                              info
                                .copy(combatants = info.combatants.map {
                                  case m: MonsterCombatant =>
                                    m.copy(initiative = scala.util.Random.between(1, 21) + m.initiativeBonus)
                                  case pc => pc
                                }),
                            "Rolled initiative for all NPCs"
                          )
                      )
                      .icon(true)(Icon.className("d20icon")),
                    Button
                      .compact(true)
                      .title("Clear all NPC stats to beginning of combat")
                      .onClick(
                        (
                          _,
                          _
                        ) =>
                          modEncounterInfo(
                            info =>
                              info
                                .copy(
                                  round = 1,
                                  combatants = info.combatants.map {
                                    case m: MonsterCombatant =>
                                      m.copy(
                                        hitPoints = m.hitPoints.copy(currentHitPoints = m.hitPoints.maxHitPoints),
                                        initiative = scala.util.Random.between(1, 21) + m.initiativeBonus,
                                        conditions = Set.empty,
                                        otherMarkers = List.empty
                                      )
                                    case pc: PlayerCharacterCombatant =>
                                      pc.copy(otherMarkers = List.empty)
                                  }
                                ),
                            "Cleared all NPC stats to beginning of combat"
                          )
                      )
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
                  case (combatant: PlayerCharacterCombatant, i: Int) =>
                    val pc = props.pcs.find(_.header.id.value == combatant.playerCharacterId.value).get
                    val pcInfo = pc.info

                    Table.Row
                      .withKey(s"combatant ${combatant.id.value}")(
                        Table.Cell(
                          Icon.name(SemanticICONS.`arrow right`).when(i == encounter.info.currentTurn),
                          EditableNumber(
                            value = combatant.initiative,
                            min = 1,
                            max = 30,
                            onChange = v =>
                              modEncounterInfo(
                                info =>
                                  info.copy(
                                    currentTurn = 0, // Need to reset the current turn to the beginning, otherwise things can get woird
                                    combatants = info.combatants.filter(_.id.value != combatant.id.value) :+ combatant
                                      .copy(initiative = v.toInt)
                                  ),
                                s"Setting initiative for ${combatant.name} to $v"
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
                            .disabled(!state.healOrDamage.contains(combatant.id))
                            .onClick(
                              (
                                _,
                                _
                              ) =>
                                modPC(
                                  pc.copy(jsonInfo =
                                    pcInfo
                                      .copy(hitPoints =
                                        pcInfo.hitPoints
                                          .copy(currentHitPoints = pcInfo.hitPoints.currentHitPoints match {
                                            case d: DeathSave => state.healOrDamage(combatant.id)
                                            case i: Int       => i + state.healOrDamage(combatant.id)
                                          })
                                      ).toJsonAST.toOption.get
                                  ),
                                  s"${combatant.name} healed ${state.healOrDamage(combatant.id)} points"
                                ) >> $.modState(s => s.copy(healOrDamage = s.healOrDamage.filter(_._1 != combatant.id)))
                                // TODO if healed from incapacited, log to the combat log

                            ),
                          Input
                            .id(s"combatantDamageInput${combatant.id.value}")
                            .className("damageInput")
                            .size(SemanticSIZES.mini)
                            .`type`("number")
                            .min(0)
                            .maxLength(4)
                            .value(state.healOrDamage.get(combatant.id).fold("")(identity))
                            .onChange {
                              (
                                _,
                                data
                              ) =>
                                val newVal = data.value match {
                                  case s: String => s.toDouble
                                  case d: Double => d
                                }
                                $.modState(s => s.copy(healOrDamage = s.healOrDamage + (combatant.id -> newVal.toInt)))
                            },
                          Button("Damage")
                            .className("damageButton")
                            .compact(true)
                            .title("Enter points in the damage box and click here for damage")
                            .color(SemanticCOLORS.red)
                            .basic(true)
                            .size(SemanticSIZES.mini)
                            .disabled(!state.healOrDamage.contains(combatant.id))
                            .onClick(
                              (
                                _,
                                _
                              ) =>
                                modPC(
                                  pc.copy(jsonInfo =
                                    pcInfo
                                      .copy(hitPoints =
                                        pcInfo.hitPoints
                                          .copy(currentHitPoints = pcInfo.hitPoints.currentHitPoints match {
                                            case d: DeathSave => d
                                            case i: Int       => i - state.healOrDamage(combatant.id)
                                          })
                                      ).toJsonAST.toOption.get
                                  ),
                                  s"${pc.header.name} got ${state.healOrDamage(combatant.id)} points of damage"
                                ) >> $.modState(s => s.copy(healOrDamage = s.healOrDamage.filter(_._1 != combatant.id)))
                            )
                          // TODO damage
                          // TODO death saves
                          // TODO actual death
                          // TODO if incapacitated or dead, log to the combat log
                        ),
                        Table.Cell
                          .singleLine(true).style(CSSProperties().set("backgroundColor", pcInfo.hitPoints.lifeColor))(
                            EditableComponent(
                              view = s"${pcInfo.hitPoints.currentHitPoints match {
                                  case ds: DeathSave => 0
                                  case i:  Int       => i
                                }} / ${pcInfo.hitPoints.maxHitPoints}",
                              edit = HitPointsEditor(
                                pcInfo.hitPoints,
                                hp =>
                                  modPC(
                                    pc.copy(jsonInfo = pcInfo.copy(hitPoints = hp).toJsonAST.toOption.get),
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
                            view = combatant.otherMarkers.headOption
                              .fold(
                                Icon.name(SemanticICONS.`plus circle`): VdomNode
                              )(_ => VdomNode(combatant.otherMarkers.map(_.name).mkString(", "))),
                            edit = OtherMarkersEditor(
                              otherMarkers = combatant.otherMarkers,
                              onChange = m =>
                                modEncounterInfo(
                                  info =>
                                    info.copy(
                                      combatants = info.combatants.map {
                                        case pc: PlayerCharacterCombatant if pc.id == combatant.id =>
                                          pc.copy(otherMarkers = m)
                                        case c => c
                                      }
                                    ),
                                  if (m.isEmpty) s"Removing all other markers from ${combatant.name}"
                                  else
                                    s"Setting other markers for ${combatant.name} to ${m.map(_.name).mkString(", ")}"
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
                  case (combatant: MonsterCombatant, i: Int) =>
                    Table.Row
                      .withKey(s"combatant ${combatant.id.value}")(
                        Table.Cell(
                          Icon.name(SemanticICONS.`arrow right`).when(i == encounter.info.currentTurn),
                          EditableNumber(
                            value = combatant.initiative,
                            min = 1,
                            max = 30,
                            onChange = v =>
                              modEncounterInfo(
                                info =>
                                  info
                                    .copy(
                                      currentTurn = 0, // Need to reset the current turn to the beginning, otherwise things can get woird
                                      combatants = info.combatants.filter(_.id.value != combatant.id.value) :+ combatant
                                        .copy(initiative = v.toInt)
                                    ),
                                s"Setting initiative for ${combatant.name} to $v"
                              )
                          )
                        ), // Add and round > 0
                        Table.Cell(
                          EditableText(
                            value = combatant.name,
                            onChange = name =>
                              modEncounterInfo(
                                info =>
                                  info.copy(
                                    combatants = info.combatants.map {
                                      case m: MonsterCombatant if m.id == combatant.id => m.copy(name = name)
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
                            .disabled(!state.healOrDamage.contains(combatant.id))
                            .onClick(
                              (
                                _,
                                _
                              ) =>
                                modEncounterInfo(
                                  info =>
                                    info.copy(combatants = info.combatants.map {
                                      case m: MonsterCombatant if m.id == combatant.id =>
                                        m.copy(hitPoints =
                                          m.hitPoints.copy(currentHitPoints = m.hitPoints.currentHitPoints match {
                                            case d: DeathSave => state.healOrDamage(combatant.id)
                                            case i: Int       => i + state.healOrDamage(combatant.id)
                                          })
                                        )
                                      case c => c
                                    }),
                                  s"${combatant.name} healed ${state.healOrDamage(combatant.id)} points"
                                ) >> $.modState(s => s.copy(healOrDamage = s.healOrDamage.filter(_._1 != combatant.id)))
                                // TODO if healed from dead, add that to the combat log
                            ),
                          Input
                            .id(s"combatantDamageInput${combatant.id.value}")
                            .className("damageInput")
                            .size(SemanticSIZES.mini)
                            .`type`("number")
                            .min(0)
                            .maxLength(4)
                            .value(state.healOrDamage.get(combatant.id).fold("")(identity))
                            .onChange {
                              (
                                _,
                                data
                              ) =>
                                val newVal = data.value match {
                                  case s: String => s.toDouble
                                  case d: Double => d
                                }
                                $.modState(s => s.copy(healOrDamage = s.healOrDamage + (combatant.id -> newVal.toInt)))
                            },
                          Button("Damage")
                            .className("damageButton")
                            .compact(true)
                            .title("Enter points in the damage box and click here for damage")
                            .color(SemanticCOLORS.red)
                            .basic(true)
                            .size(SemanticSIZES.mini)
                            .disabled(!state.healOrDamage.contains(combatant.id))
                            .onClick(
                              (
                                _,
                                _
                              ) =>
                                modEncounterInfo(
                                  info =>
                                    info.copy(combatants = info.combatants.map {
                                      case m: MonsterCombatant if m.id == combatant.id =>
                                        m.copy(hitPoints =
                                          m.hitPoints.copy(currentHitPoints = m.hitPoints.currentHitPoints match {
                                            case d: DeathSave => state.healOrDamage(combatant.id)
                                            case i: Int       => i - state.healOrDamage(combatant.id)
                                          })
                                        )
                                      case c => c
                                    }),
                                  ""
                                )
//                                    s.encounter.info.combatants
//                                      .collectFirst {
//                                        case c: MonsterCombatant if c.id == combatant.id && c.hitPoints.isDead => c
//                                      }.fold(s"${combatant.name} got ${state.healOrDamage(combatant.id)} points of damage")(changed =>
//                                            s"${combatant.name} got ${state.healOrDamage(combatant.id)} points of damage, ${changed.name} is finished! (${changed.hitPoints.currentHitPoints})"
//                                        )
//                                  )
                                  >> $.modState(s => s.copy(healOrDamage = s.healOrDamage.filter(_._1 != combatant.id)))
                            )
                        ),
                        Table.Cell
                          .singleLine(true).style(
                            CSSProperties().set("backgroundColor", combatant.hitPoints.lifeColor)
                          )(
                            s"${combatant.hitPoints.currentHitPoints match {
                                case ds: DeathSave => 0
                                case i:  Int       => i
                              }} / ${combatant.hitPoints.maxHitPoints}"
                          ),
                        Table.Cell.textAlign(semanticUiReactStrings.center)(
                          EditableNumber(
                            value = combatant.armorClass,
                            min = 1,
                            max = 30,
                            onChange = v =>
                              modEncounterInfo(
                                info =>
                                  info.copy(combatants = info.combatants.map {
                                    case m: MonsterCombatant if m.id == combatant.id => m.copy(armorClass = v.toInt)
                                    case c => c
                                  }),
                                ""
                              )
                          )
                        ),
                        Table.Cell.textAlign(semanticUiReactStrings.center)(
                          EditableComponent(
                            view = combatant.conditions.headOption.fold(
                              Icon.name(SemanticICONS.`plus circle`): VdomNode
                            )(_ => VdomNode(combatant.conditions.mkString(", "))),
                            edit = ConditionsEditor(
                              conditions = combatant.conditions,
                              onChange = c =>
                                modEncounterInfo(
                                  info =>
                                    info.copy(
                                      combatants = info.combatants.map {
                                        case m: MonsterCombatant if m.id == combatant.id =>
                                          m.copy(conditions = c)
                                        case c => c
                                      }
                                    ),
                                  if (c.isEmpty) s"Removing all conditions from ${combatant.name}"
                                  else
                                    s"Setting conditions for ${combatant.name} to ${c.map(_.toString).mkString(", ")}"
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
                            view = combatant.otherMarkers.headOption
                              .fold(
                                Icon.name(SemanticICONS.`plus circle`): VdomNode
                              )(_ => VdomNode(combatant.otherMarkers.map(_.name).mkString(", "))),
                            edit = OtherMarkersEditor(
                              otherMarkers = combatant.otherMarkers,
                              onChange = m =>
                                modEncounterInfo(
                                  info =>
                                    info.copy(
                                      combatants = info.combatants.map {
                                        case pc: PlayerCharacterCombatant if pc.id == combatant.id =>
                                          pc.copy(otherMarkers = m)
                                        case c => c
                                      }
                                    ),
                                  if (m.isEmpty)
                                    s"Removing all other markers from ${combatant.name}"
                                  else
                                    s"Setting other markers for ${combatant.name} to ${m.map(_.name).mkString(", ")}"
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
                                    info.copy(combatants = info.combatants.filter(_.id.value != combatant.id.value)),
                                  s"${combatant.name} was removed from the encounter"
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
                                    combatant match {
                                      case m: MonsterCombatant =>
                                        info.copy(combatants =
                                          info.combatants :+
                                            createMonsterCombatant(encounter, m.monsterHeader)
                                        )
                                    },
                                  s"Added another ${combatant.name} to the encounter"
                                )
                            ),
                          Button
                            .compact(true)
                            .title("View monster stats")
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
          )
        )
      }
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("CombatRunner")
    .initialStateFromProps(p => State(p.encounter))
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
      .withKey(encounter.header.id.value.toString)(
        Props(encounter, pcs, onChange, onEditEncounter, onArchiveEncounter, onModeChange)
      )

}
