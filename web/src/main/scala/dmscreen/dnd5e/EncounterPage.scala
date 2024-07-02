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

import caliban.ScalaJSClientAdapter.*
import caliban.client.SelectionBuilder
import caliban.client.scalajs.given
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.given
import dmscreen.{CampaignId, DMScreenState, DMScreenTab, DialogMode}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, Table, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHS}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesAccordionAccordionTitleMod.*
import org.scalajs.dom.*
import zio.json.*
import zio.json.ast.Json

import scala.scalajs.js.JSConverters.*

object EncounterPage extends DMScreenTab {

  enum EncounterMode {

    case edit, combat

  }

  case class State(
    accordionState:     (Int, Int) = (0, 0),
    encounterMode:      EncounterMode = EncounterMode.combat,
    currentEncounterId: Option[EncounterId] = None
  )

  class Backend($ : BackendScope[Unit, State]) {

    private def onAccordionChange(
      index: (Int, Int)
    )(
      event: ReactMouseEventFrom[HTMLDivElement],
      data:  AccordionTitleProps
    ): Callback = {
      $.modState(_.copy(accordionState = index))
    }

    def render(state: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          def runCombat(encounter: Encounter): Callback = {
            def renumberCombatants(combatants: List[EncounterCombatant]): List[EncounterCombatant] = {
              combatants.zipWithIndex.map { case (c, i) =>
                c match {
                  case m: MonsterCombatant         => m.copy(id = CombatantId(i))
                  case p: PlayerCharacterCombatant => p.copy(id = CombatantId(i))
                }
              }
            }

            // Prep for combat, if the combat doesn't have the pcs yet, create them, if it does, update them.
            val playerCombatants = campaignState.pcs
              .map { pc =>
                encounter.info.combatants
                  .find(pcc => pcc.isInstanceOf[PlayerCharacterCombatant] && pcc.id == pc.id).fold(
                    PlayerCharacterCombatant(
                      id = CombatantId.empty,
                      name = pc.header.name,
                      playerCharacterId = pc.id,
                      notes = "",
                      initiative = 10,
                      initiativeBonus = pc.info.initiativeBonus
                    )
                  ) { pcc =>
                    pcc.asInstanceOf[PlayerCharacterCombatant].copy(initiativeBonus = pc.info.initiativeBonus)
                  }
              }
            val modifiedEncounter = encounter
              .copy(
                header = encounter.header.copy(status = EncounterStatus.active),
                jsonInfo = encounter.info
                  .copy(
                    combatants = renumberCombatants(encounter.info.monsters ++ playerCombatants),
                    round = if (encounter.info.round == 0) 1 else encounter.info.round // This is the first time we're running this encounter
                  ).toJsonAST.toOption.get
              )
            modEncounter(modifiedEncounter) >> $.modState(
              _.copy(
                currentEncounterId = Some(encounter.header.id),
                encounterMode = EncounterMode.combat
              )
            )
          }

          def doDelete(deleteMe: Encounter): Callback =
            dmScreenState.onModifyCampaignState(
              campaignState
                .copy(encounters = campaignState.encounters.filter(_.header.id != deleteMe.header.id))
                // TODO delete from server
            )

          def modEncounter(encounter: Encounter): Callback = {
            dmScreenState.onModifyCampaignState(
              campaignState.copy(
                encounters = campaignState.encounters.map {
                  case e if e.header.id == encounter.header.id => encounter
                  case e                                       => e
                },
                changeStack = campaignState.changeStack.logEncounterChanges(encounter.header.id)
              )
            )
          }

          val encounters = campaignState.encounters
          val currentEncounter: Option[Encounter] =
            state.currentEncounterId.flatMap(id => encounters.find(_.header.id == id))

          val campaign = campaignState.campaign

          <.div(
            ^.className := "pageContainer",
            Grid.className("encounterGrid")(
              Grid.Column
                .width(SemanticWIDTHS.`6`)
                .withKey("scenes")(
                  Accordion.Accordion
                    .styled(true)
                    .fluid(true)({
                      val allEncounters = encounters
                        .sortBy(_.header.orderCol)
                        .groupBy(_.header.sceneId)

                      (None +: campaignState.scenes.map(Some.apply)).zipWithIndex.map {
                        (
                          sceneOpt,
                          sceneIndex
                        ) =>
                          val encounters = allEncounters.get(sceneOpt.map(_.header.id)).toList.flatten
                          VdomArray(
                            Accordion.Title
                              .active(state.accordionState._1 == sceneIndex).onClick(
                                onAccordionChange((sceneIndex, 0))
                              )(
                                sceneOpt.fold("No Scene")(_.header.name),
                                Button
                                  .title("Add Encounter to Scene")
                                  .icon(true)(Icon.name(SemanticICONS.`plus circle`)) // TODO add encounter to scene
                                // TODO add "don't show archived" checkbox, default to true
                              ),
                            Accordion.Content
                              .active(state.accordionState._1 == sceneIndex)(
                                Accordion.Accordion
                                  .fluid(true)
                                  .styled(true)(
                                    encounters.zipWithIndex
                                      .map { case (encounter, encounterIndex) =>
                                        val encounterInfo = encounter.info
                                        VdomArray(
                                          Accordion.Title
                                            .active(state.accordionState == (sceneIndex, encounterIndex))
                                            .onClick(
                                              onAccordionChange((sceneIndex, encounterIndex))
                                            )(
                                              <.table(
                                                ^.cellPadding := 0,
                                                ^.cellSpacing := 0,
                                                ^.border      := "0px",
                                                <.tbody(
                                                  <.tr(
                                                    <.td(
                                                      ^.textAlign := "left",
                                                      s"${encounter.header.name} (${encounter.header.status.name})"
                                                    ),
                                                    <.td(
                                                      ^.textAlign  := "right",
                                                      ^.whiteSpace := "nowrap",
                                                      ^.width      := 190.px,
                                                      Button
                                                        .compact(true)
                                                        .size(SemanticSIZES.tiny)
                                                        .title("Run combat!")
                                                        .icon(true)
                                                        .onClick(
                                                          (
                                                            _,
                                                            _
                                                          ) => runCombat(encounter)
                                                        )(
                                                          Icon.name(SemanticICONS.`play`)
                                                        )
                                                        .when(encounter.header.status != EncounterStatus.archived),
                                                      Button
                                                        .compact(true)
                                                        .size(SemanticSIZES.tiny)
                                                        .title("Edit encounter")
                                                        .icon(true)
                                                        .onClick(
                                                          (
                                                            _,
                                                            _
                                                          ) =>
                                                            $.modState(
                                                              _.copy(
                                                                currentEncounterId = Some(encounter.header.id),
                                                                encounterMode = EncounterMode.edit
                                                              )
                                                            )
                                                        )(
                                                          Icon.name(
                                                            if (encounter.header.status != EncounterStatus.archived)
                                                              SemanticICONS.`edit`
                                                            else
                                                              SemanticICONS.`eye`
                                                          )
                                                        ),
                                                      Button
                                                        .compact(true)
                                                        .size(SemanticSIZES.tiny)
                                                        .title("Delete Encounter")
                                                        .icon(true)
                                                        .onClick(
                                                          (
                                                            _,
                                                            _
                                                          ) =>
                                                            _root_.components.Confirm.confirm(
                                                              question =
                                                                "Are you 100% sure you want to delete this encounter?",
                                                              onConfirm = doDelete(encounter)
                                                            )
                                                        )(Icon.name(SemanticICONS.`trash`)),
                                                      Button
                                                        .compact(true)
                                                        .size(SemanticSIZES.tiny)
                                                        .title("Move encounter up")
                                                        .icon(true)
                                                        // TODO Move the encounter to earlier in the list, or to the previous scene if it's at the beginning of the scene
                                                        (
                                                          Icon.name(SemanticICONS.`arrow up`)
                                                        ).when(encounterIndex != 0),
                                                      Button
                                                        .compact(true)
                                                        .size(SemanticSIZES.tiny)
                                                        .title("Move encounter down")
                                                        .icon(true)
                                                        // TODO move the encounter to later in the list, or to the next scene if it's at the end of this scene
                                                        (
                                                          Icon.name(SemanticICONS.`arrow down`)
                                                        ) // .when(encounterIndex != campaignState.encounters.size - 1)
                                                      ,
                                                      Button
                                                        .compact(true)
                                                        .title("clone encounter")
                                                        .size(SemanticSIZES.tiny)
                                                        .icon(true)
                                                        // TODO Make an exact copy of this ecounter, put it immediately after this one.
                                                        (
                                                          Icon.name(SemanticICONS.`clone outline`)
                                                        )
                                                    )
                                                  )
                                                )
                                              )
                                            ),
                                          Accordion.Content
                                            .active(state.accordionState == ((sceneIndex, encounterIndex)))(
                                              encounterInfo.monsters.map(_.name).mkString(", "),
                                              <.div(s"Notes: ${encounterInfo.notes}")
                                            )
                                        )

                                      }*
                                  )
                              )
                          )
                      }

                    }*)
                ),
              Grid.Column
                .width(SemanticWIDTHS.`8`)
                .withKey("currentEncounter")(
                  currentEncounter.fold(Container.fluid(true)("Choose an encounter to edit or run"))(encounter =>
                    state.encounterMode match {
                      case EncounterMode.edit =>
                        Container.fluid(true)(
                          EncounterEditor(
                            encounter,
                            onDelete = deleteMe => doDelete(deleteMe),
                            onChange = encounter => modEncounter(encounter)
                          )
                        )
                      case EncounterMode.combat =>
                        Container.fluid(true)(
                          CombatRunner(
                            encounter = encounter,
                            pcs = campaignState.pcs,
                            onChange = encounter => modEncounter(encounter),
                            logChange = s => Callback.log(s), // TODO add to encounter log
                            onModeChange = mode =>
                              dmScreenState.changeDialogMode(
                                if (mode == EditableComponent.Mode.edit) DialogMode.open else DialogMode.closed
                              ),
                            onEditEncounter = encounter =>
                              $.modState(
                                _.copy(
                                  currentEncounterId = Some(encounter.header.id),
                                  encounterMode = EncounterMode.edit
                                )
                              ), // TODO test this
                            onArchiveEncounter = encounter =>
                              modEncounter(
                                encounter.copy(header = encounter.header.copy(status = EncounterStatus.archived))
                              ) >> $.modState(
                                _.copy(currentEncounterId = None)
                              ) // TODO test this
                          )
                        )
                    }
                  )
                ),
              Grid.Column
                .withKey("encounterLog")
                .width(SemanticWIDTHS.`2`)(
                  Container
                    .fluid(true)(
                      if (state.encounterMode == EncounterMode.combat && currentEncounter.nonEmpty) {
                        VdomArray(
                          Container(
                            <.h2("Encounter Log")
                          ),
                          Container(
                            <.h2("Dice Roller"),
                            Button.compact(true).fluid(true).title("Roll a d4")("d4"), // TODO connect this
                            Button.compact(true).fluid(true).title("Roll a d6")("d6"), // TODO connect this
                            Button.compact(true).fluid(true).title("Roll a d10")("d10"), // TODO connect this
                            Button.compact(true).fluid(true).title("Roll a d12")("d12"), // TODO connect this
                            Button.compact(true).fluid(true).title("Roll a d20")("d20"), // TODO connect this
                            Button.compact(true).fluid(true).title("Roll a percentile dice")("d100"), // TODO connect this
                            Input
                              .id("diceRollerInput").title("Roll a bunch of dice").value(
                                "2d20"
                              ) // TODO connect this, and paint a button
                          )
                        )

                      } else <.div()
                    )
                )
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
