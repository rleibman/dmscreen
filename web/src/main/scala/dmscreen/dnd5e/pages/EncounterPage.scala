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

import caliban.ScalaJSClientAdapter.*
import caliban.client.SelectionBuilder
import caliban.client.scalajs.given
import dmscreen.components.*
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenState, DMScreenTab, DialogMode}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.{<, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{Button, List as SList, Table, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHS}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesAccordionAccordionTitleMod.*
import org.scalajs.dom.*
import zio.json.*
import zio.json.ast.Json

import scala.scalajs.js.JSConverters.*

object EncounterPage extends DMScreenTab {

  enum EncounterMode {

    case edit, combat, wizard

  }

  case class State(
    encounters:             Seq[Encounter] = List.empty,
    scenes:                 Seq[Scene] = List.empty,
    pcs:                    Seq[PlayerCharacter] = List.empty,
    npcs:                   Seq[NonPlayerCharacter] = List.empty,
    accordionState:         (Int, Int) = (0, 0),
    encounterMode:          EncounterMode = EncounterMode.edit,
    currentEncounterId:     Option[EncounterId] = None,
    hideArchivedEncounters: Boolean = true
  )

  class Backend($ : BackendScope[CampaignId, State]) {

    def loadState(campaignId: CampaignId): Callback = {
      (for {
        encounters <- DND5eGraphQLRepository.live.encounters(campaignId)
        scenes     <- DND5eGraphQLRepository.live.scenes(campaignId)
        pcs        <- DND5eGraphQLRepository.live.playerCharacters(campaignId)
        npcs       <- DND5eGraphQLRepository.live.nonPlayerCharacters(campaignId)
      } yield $.modState(_.copy(encounters = encounters, scenes = scenes, pcs = pcs, npcs = npcs))).completeWith(_.get)
    }

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
            $.modState(
              _.copy(
                currentEncounterId = Some(encounter.header.id),
                encounterMode = EncounterMode.combat
              )
            )
          }

          def doDelete(deleteMe: Encounter): Callback =
            DND5eGraphQLRepository.live
              .deleteEntity(entityType = DND5eEntityType.encounter, id = deleteMe.header.id)
              .map(_ =>
                $.modState(
                  s =>
                    state.copy(
                      currentEncounterId = state.currentEncounterId.filter(deleteMe.header.id != _),
                      encounters = state.encounters.filter(deleteMe.header.id != _.header.id)
                    ),
                  dmScreenState.log(s"Deleted encounter ${deleteMe.header.name}")
                )
              )
              .completeWith(_.get)

          def modEncounter(
            encounter: Encounter,
            log:       String
          ): Callback =
            $.modState(
              s =>
                s.copy(encounters = s.encounters.map {
                  case e if e.header.id == encounter.header.id => encounter
                  case e                                       => e
                }),
              dmScreenState.onModifyCampaignState(
                campaignState.copy(
                  changeStack = campaignState.changeStack.logEncounterChanges(encounter)
                ),
                log
              )
            )

          val encounters = state.encounters
          val currentEncounter: Option[Encounter] =
            state.currentEncounterId.flatMap(id => encounters.find(_.header.id == id))

          val campaign = campaignState.campaign

          <.div(
            ^.key       := "encounterPage",
            ^.className := "pageContainer",
            state.encounterMode match {
              case EncounterMode.combat =>
                currentEncounter.fold(
                  <.div("Error, we should never be running an encounter without first choosing it!")
                ) { encounter =>
                  Container.fluid(true)(
                    CombatRunner(
                      campaignId = campaign.id,
                      encounterId = encounter.header.id,
                      onChange = (
                        encounter,
                        log
                      ) => modEncounter(encounter, log),
                      onModeChange = mode =>
                        dmScreenState.changeDialogMode(
                          if (mode == EditableComponent.EditingMode.edit) DialogMode.open else DialogMode.closed
                        ),
                      onEditEncounter = encounter =>
                        $.modState(
                          _.copy(
                            currentEncounterId = Some(encounter.header.id),
                            encounterMode = EncounterMode.edit
                          )
                        ),
                      onArchiveEncounter = encounter =>
                        modEncounter(
                          encounter.copy(header = encounter.header.copy(status = EncounterStatus.archived)),
                          s"Archived encounter ${encounter.header.name}"
                        ) >> $.modState(
                          _.copy(currentEncounterId = None)
                        )
                    )
                  )
                }
              case EncounterMode.edit =>
                Grid.className("encounterGrid")(
                  Grid.Column
                    .width(SemanticWIDTHS.`6`)
                    .withKey("scenes")(
                      <.div(
                        Header.as("h1")(
                          Checkbox
                            .checked(state.hideArchivedEncounters)
                            .onClick(
                              (
                                _,
                                d
                              ) => $.modState(_.copy(hideArchivedEncounters = d.checked.getOrElse(false)))
                            ),
                          Label.size(SemanticSIZES.big)("Hide archived encounters"),
                          Button
                            .onClick(
                              (
                                _,
                                _
                              ) =>
                                $.modState(
                                  _.copy(
                                    currentEncounterId = None,
                                    encounterMode = EncounterMode.wizard
                                  )
                                )
                            )("Encounter Wizard")
                        ),
                        Accordion.Accordion
                          .styled(true)
                          .fluid(true)({
                            val allEncounters = encounters
                              .sortBy(_.header.orderCol)
                              .groupBy(_.header.sceneId)

                            (None +: state.scenes.map(Some.apply)).zipWithIndex.map {
                              (
                                sceneOpt,
                                sceneIndex
                              ) =>
                                val encounters = allEncounters.get(sceneOpt.map(_.header.id)).toList.flatten
                                VdomArray(
                                  Accordion.Title
                                    .withKey(s"sceneTitle${sceneIndex}AccordionTitle")
                                    .active(state.accordionState._1 == sceneIndex).onClick(
                                      onAccordionChange((sceneIndex, 0))
                                    )(
                                      <.table(
                                        ^.cellPadding := 0,
                                        ^.cellSpacing := 0,
                                        ^.border      := "0px",
                                        ^.width       := 100.pct,
                                        <.tbody(
                                          <.tr(
                                            <.td(
                                              ^.textAlign := "left",
                                              s"${sceneOpt.fold("0")(_.header.orderCol + 1)}.${sceneOpt
                                                  .fold("No Scene")(_.header.name)}"
                                            ),
                                            <.td(
                                              ^.textAlign := "right",
                                              Button
                                                .title("Add Encounter to Scene")
                                                .icon(true)(Icon.name(SemanticICONS.`plus circle`))
                                                .onClick {
                                                  (
                                                    _,
                                                    _
                                                  ) =>
                                                    val newEncounter = Encounter(
                                                      header = EncounterHeader(
                                                        id = EncounterId.empty,
                                                        campaignId = campaign.header.id,
                                                        name = s"Encounter ${encounters.size + 1}",
                                                        status = EncounterStatus.planned,
                                                        sceneId = sceneOpt.map(_.header.id),
                                                        orderCol = encounters.size
                                                      ),
                                                      jsonInfo = EncounterInfo().toJsonAST.toOption.get
                                                    )
                                                    DND5eGraphQLRepository.live
                                                      .upsert(newEncounter.header, newEncounter.jsonInfo)
                                                      .map { id =>
                                                        $.modState(
                                                          s =>
                                                            s.copy(
                                                              currentEncounterId = Some(id),
                                                              encounterMode = EncounterMode.edit,
                                                              encounters = s.encounters :+ newEncounter
                                                                .copy(header = newEncounter.header.copy(id = id))
                                                            ),
                                                          dmScreenState.log("Added new encounter")
                                                        )
                                                      }
                                                      .completeWith(_.get)
                                                }
                                            )
                                          )
                                        )
                                      )
                                    ),
                                  Accordion.Content
                                    .withKey(s"sceneTitle${sceneIndex}AccordionContent")
                                    .active(state.accordionState._1 == sceneIndex)(
                                      Accordion.Accordion
                                        .fluid(true)
                                        .styled(true)(
                                          encounters
                                            .filterNot(
                                              _.header.status == EncounterStatus.archived && state.hideArchivedEncounters
                                            )
                                            .zipWithIndex
                                            .map { case (encounter, encounterIndex) =>
                                              val encounterInfo = encounter.info
                                              VdomArray(
                                                Accordion.Title
                                                  .withKey(
                                                    s"encounterTitle${sceneIndex}_${encounterIndex}AccordionTitle"
                                                  )
                                                  .active(state.accordionState == (sceneIndex, encounterIndex))
                                                  .onClick(
                                                    onAccordionChange((sceneIndex, encounterIndex))
                                                  )(
                                                    <.table(
                                                      ^.cellPadding := 0,
                                                      ^.cellSpacing := 0,
                                                      ^.border      := "0px",
                                                      ^.width       := 100.pct,
                                                      <.tbody(
                                                        <.tr(
                                                          <.td(
                                                            ^.textAlign := "left",
                                                            s"${sceneOpt.fold("0")(_.header.orderCol + 1)}.${encounter.header.orderCol + 1}. ",
                                                            EditableText(
                                                              value = s"${encounter.header.name}",
                                                              onChange = str =>
                                                                modEncounter(
                                                                  encounter
                                                                    .copy(header = encounter.header.copy(name = str)),
                                                                  s"Changed encounter name to $str"
                                                                )
                                                            ),
                                                            s" (${encounter.header.status.name})"
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
                                                              .when(
                                                                encounter.header.status != EncounterStatus.archived
                                                              ),
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
                                                                  if (
                                                                    encounter.header.status != EncounterStatus.archived
                                                                  )
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
                                                                    question = "Are you 100% sure you want to delete this encounter?",
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
                                                          )
                                                        )
                                                      )
                                                    )
                                                  ),
                                                Accordion.Content
                                                  .withKey(
                                                    s"encounterTitle${sceneIndex}_${encounterIndex}AccordionContent"
                                                  )
                                                  .active(state.accordionState == ((sceneIndex, encounterIndex)))(
                                                    encounterInfo.monsters.map(_.name).mkString(", "),
                                                    <.div(s"Notes: ${encounterInfo.notes}"),
                                                    <.div(
                                                      ^.dangerouslySetInnerHtml := s"<div>Description</div>${encounterInfo.generatedDescription}"
                                                    )
                                                  )
                                              )

                                            }*
                                        )
                                    )
                                )
                            }

                          }*)
                      )
                    ),
                  Grid.Column
                    .width(SemanticWIDTHS.`8`)
                    .withKey("currentEncounter")(
                      currentEncounter.fold(Container.fluid(true)("Choose an encounter to edit or run"))(encounter =>
                        Container.fluid(true)(
                          EncounterEditor(
                            encounter,
                            encounter.info.calculateDifficulty(state.pcs, state.npcs),
                            onDelete = deleteMe => doDelete(deleteMe),
                            onChange = encounter => modEncounter(encounter, "")
                          )
                        )
                      )
                    )
                )
              case EncounterMode.wizard =>
                EncounterWizard(
                  campaignState.campaign,
                  onCancel = $.modState(_.copy(encounterMode = EncounterMode.edit)),
                  onSaved = encounter =>
                    $.modState(_.copy(encounterMode = EncounterMode.edit, encounters = state.encounters :+ encounter))
                )
            }
          )

        }
      }
    }

  }

  private val component = ScalaComponent
    .builder[CampaignId]("encounterPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props))
    .build

  def apply(campaignId: CampaignId): Unmounted[CampaignId, State, Backend] = component(campaignId)

}
