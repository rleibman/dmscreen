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
import caliban.client.scalajs.DND5eClient.{
  Alignment as CalibanAlignment,
  Biome as CalibanBiome,
  CreatureSize as CalibanCreatureSize,
  Encounter as CalibanEncounter,
  EncounterHeader as CalibanEncounterHeader,
  Queries
}
import caliban.client.scalajs.{*, given}
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
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

object EncounterPage extends DMScreenTab {

  enum EncounterMode {

    case edit, combat

  }

  case class State(
    encounters:         List[Encounter] = List.empty,
    accordionState:     (Int, Int) = (0, 0),
    encounterMode:      EncounterMode = EncounterMode.combat,
    currentEncounterId: Option[EncounterId] = None,
    dialogOpen:         Boolean = false
  ) {

    def currentEncounter: Option[Encounter] = currentEncounterId.flatMap(id => encounters.find(_.header.id == id))

  }

  class Backend($ : BackendScope[Unit, State]) {

    val crs: List[(String, Double)] =
      List("0" -> 0.0, "1/8" -> .125, "1/4" -> .25, "1/2" -> .5) ++ (1 to 30).map(i => i.toString -> i.toDouble).toList

    def load(): Callback = {
      val ajax = for {
        oldState <- $.state.asAsyncCallback
        _        <- Callback.log("Loading Encounters from server...").asAsyncCallback
        encounters <- {
          val encounterSB: SelectionBuilder[CalibanEncounter, Encounter] = (CalibanEncounter.header(
            CalibanEncounterHeader.id ~
              CalibanEncounterHeader.campaignId ~
              CalibanEncounterHeader.name ~
              CalibanEncounterHeader.status ~
              CalibanEncounterHeader.sceneId ~
              CalibanEncounterHeader.order
          ) ~ CalibanEncounter.jsonInfo).map {
            (
              id:         Long,
              campaignId: Long,
              name:       String,
              status:     String,
              sceneId:    Option[Long],
              order:      Int,
              info:       Json
            ) =>
              Encounter(
                EncounterHeader(
                  EncounterId(id),
                  CampaignId(campaignId),
                  name,
                  EncounterStatus.valueOf(status),
                  sceneId.map(SceneId.apply),
                  order
                ),
                info
              )
          }

          val sb = Queries.encounters(CampaignId(1).value)(encounterSB)
          asyncCalibanCall(sb).map(_.toSeq.flatten.toList)

        }
      } yield {
        $.modState { s =>
          s.copy(encounters = encounters)
        }
      }

      for {
        modedState <- ajax.completeWith(_.get)
      } yield modedState

    }

    def onAccordionChange(
      index: (Int, Int)
    )(
      event: ReactMouseEventFrom[HTMLDivElement],
      data:  AccordionTitleProps
    ): Callback = {
      $.modState(_.copy(accordionState = index))
    }

    def delete(encounter: Encounter): Callback =
      $.modState(s => s.copy(encounters = s.encounters.filterNot(_.header.id == encounter.header.id)) // TODO make it stick
      )

    private def CombatRunner(
      campaignState: DND5eCampaignState,
      state:         State,
      encounter:     Encounter
    ) = {
      def modEncounter(fn: Encounter => Encounter) = {
        // TODO it may be ugly, but we have this in the EncounterEditor too, so we should probably merge them together
        // TODO make it stick
        $.modState(s =>
          s.copy(encounters = s.encounters.map {
            case e if e.header.id == encounter.header.id => fn(e)
            case e                                       => e
          })
        )
      }

      Table(
        Table.Header(
          Table.Row(
            Table.HeaderCell.colSpan(2)(<.h2(s"${encounter.header.name}")),
            Table.HeaderCell
              .colSpan(3).textAlign(semanticUiReactStrings.center)(
                s"Difficulty: ${encounter.info.difficulty}, xp: ${encounter.info.xp}"
              ),
            Table.HeaderCell
              .colSpan(3)
              .singleLine(true)
              .textAlign(semanticUiReactStrings.right)(
                Button
                  .compact(true)
                  .icon(true)(Icon.className("d20icon")), // TODO roll NPC initiative
                Button
                  .compact(true)
                  .icon(true)(Icon.className("clearIcon")), // TODO clear NPC
                Button
                  .compact(true)
                  .icon(true)(Icon.name(SemanticICONS.`edit`)), // TODO edit encounter
                Button
                  .compact(true)
                  .icon(true)(Icon.name(SemanticICONS.`repeat`)), // TODO reset
                Button
                  .compact(true)
                  .icon(true)
                  .onClick(
                    (
                      _,
                      _
                    ) => modEncounter(e => e.copy(header = e.header.copy(status = EncounterStatus.archived)))
                  )(Icon.name(SemanticICONS.`archive`))
                  .when(encounter.header.status != EncounterStatus.archived),
                Button
                  .compact(true)
                  .icon(true)(Icon.name(SemanticICONS.`step forward`)) // TODO next turn, or next round if it's the last turn
              )
          ),
          Table.Row(
            Table.HeaderCell("Initiative"),
            Table.HeaderCell("Name"),
            Table.HeaderCell("Damage"),
            Table.HeaderCell("HP"),
            Table.HeaderCell("AC"),
            Table.HeaderCell("Conditions"),
            Table.HeaderCell("Other"),
            Table.HeaderCell( /*For actions*/ )
          )
        ),
        Table.Body(
          encounter.info.creatures
            .sortBy(-_.initiative)
            .zipWithIndex
            .map {
              case (entity: PlayerCharacterEncounterCreature, i: Int) =>
                val pc = campaignState.pcs.find(_.header.id.value == entity.playerCharacterId.value).get
                val pcInfo = pc.info

                Table.Row.withKey(s"entity #$i")(
                  Table.Cell(
                    Icon.name(SemanticICONS.`arrow right`).when(i == encounter.info.currentTurn),
                    entity.initiative
                  ), // TODO editable  // Add and round > 0
                  Table.Cell(pc.header.name),
                  Table.Cell(
                    Button("Heal")
                      .compact(true)
                      .color(SemanticCOLORS.green)
                      .basic(true)
                      .size(SemanticSIZES.mini)
                      // TODO heal
                      .style(CSSProperties().set("width", 60.px)), // TODO to css
                    Input
                      .className("damageInput")
                      .size(SemanticSIZES.mini)
                      .`type`("number")
                      .min(0)
                      .maxLength(4), // TODO connect to state
                    Button("Damage")
                      .compact(true)
                      .color(SemanticCOLORS.red)
                      .basic(true)
                      .size(SemanticSIZES.mini)
                      // TODO damage
                      .style(CSSProperties().set("width", 60.px)) // TODO to css
                  ),
                  Table.Cell
                    .singleLine(true).style(CSSProperties().set("background-color", pcInfo.hitPoints.lifeColor))(
                      s"${pcInfo.hitPoints.currentHitPoints match {
                          case ds: DeathSave => 0
                          case i:  Int       => i
                        }} / ${pcInfo.hitPoints.maxHitPoints}"
                    ), // TODO editable
                  Table.Cell.textAlign(semanticUiReactStrings.center)(pcInfo.armorClass), // TODO editable
                  Table.Cell.textAlign(semanticUiReactStrings.center)(
                    pcInfo.conditions.headOption.fold(
                      Icon.name(SemanticICONS.`plus circle`)
                    )(_ => Container(pcInfo.conditions.mkString(", ")))
                  ), // TODO editable
                  Table.Cell.textAlign(semanticUiReactStrings.center)(
                    entity.otherMarkers.headOption.fold(
                      Icon.name(SemanticICONS.`plus circle`)
                    )(_ => Container(entity.otherMarkers.map(_.name).mkString(", ")))
                  ), // TODO editable
                  Table.Cell.singleLine(true)(
                    Button
                      .compact(true)
                      .size(SemanticSIZES.mini)
                      .icon(true)(Icon.name(SemanticICONS.`eye`)) // TODO view character stats
                  )
                )
              case (entity: MonsterEncounterCreature, i: Int) =>
                Table.Row.withKey(s"entity #$i")(
                  Table.Cell(
                    Icon.name(SemanticICONS.`arrow right`).when(i == encounter.info.currentTurn),
                    entity.initiative
                  ), // Add and round > 0
                  Table.Cell(entity.name), // TODO editable
                  Table.Cell(
                    Button("Heal")
                      .compact(true)
                      .color(SemanticCOLORS.green)
                      .basic(true)
                      .size(SemanticSIZES.mini)
                      // TODO heal
                      .style(CSSProperties().set("width", 60.px)), // TODO to css
                    Input
                      .className("damageInput")
                      .size(SemanticSIZES.mini)
                      .`type`("number")
                      .min(0)
                      .maxLength(4), // TODO connect to state
                    Button("Damage")
                      .compact(true)
                      .color(SemanticCOLORS.red)
                      .basic(true)
                      .size(SemanticSIZES.mini)
                      // TODO damage
                      .style(CSSProperties().set("width", 60.px)) // TODO to css
                  ),
                  Table.Cell
                    .singleLine(true).style(CSSProperties().set("background-color", entity.hitPoints.lifeColor))(
                      s"${entity.hitPoints.currentHitPoints match {
                          case ds: DeathSave => 0
                          case i:  Int       => i
                        }} / ${entity.hitPoints.maxHitPoints}"
                    ), // TODO editable
                  Table.Cell.textAlign(semanticUiReactStrings.center)(entity.armorClass), // TODO editable
                  Table.Cell.textAlign(semanticUiReactStrings.center)(
                    entity.conditions.headOption.fold(
                      Icon.name(SemanticICONS.`plus circle`)
                    )(_ => Container(entity.conditions.mkString(", ")))
                  ), // TODO editable
                  Table.Cell.textAlign(semanticUiReactStrings.center)(
                    entity.otherMarkers.headOption.fold(
                      Icon.name(SemanticICONS.`plus circle`)
                    )(_ => Container(entity.otherMarkers.map(_.name).mkString(", ")))
                  ), // TODO editable
                  Table.Cell.singleLine(true)(
                    Button
                      .compact(true)
                      .size(SemanticSIZES.mini)
                      .icon(true)(Icon.name(SemanticICONS.`delete`)), // TODO delete monster
                    Button
                      .compact(true)
                      .size(SemanticSIZES.mini)
                      .icon(true)(Icon.name(SemanticICONS.`clone outline`)), // TODO clone monster
                    Button
                      .compact(true)
                      .size(SemanticSIZES.mini)
                      .icon(true)(Icon.name(SemanticICONS.`eye`)) // View Monster Stats
                  )
                )
            }*
        )
      )
    }

    def render(state: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
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
                      val allEncounters = state.encounters
                        .sortBy(_.header.sceneId.map(_.value)) // TODO change to order by scene order
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
                                Button.icon(true)(Icon.name(SemanticICONS.`plus circle`)) // TODO add encounter to scene
                              ),
                            Accordion.Content.active(state.accordionState._1 == sceneIndex)(
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
                                                      .icon(true)
                                                      .onClick(
                                                        (
                                                          _,
                                                          _
                                                        ) =>
                                                          $.modState(
                                                            _.copy(
                                                              currentEncounterId = Some(encounter.header.id),
                                                              encounterMode = EncounterMode.combat
                                                              // TODO change the encounter status to active
                                                            )
                                                          )
                                                      )(
                                                        Icon.name(SemanticICONS.`play`)
                                                      )
                                                      .when(encounter.header.status != EncounterStatus.archived),
                                                    Button
                                                      .compact(true)
                                                      .size(SemanticSIZES.tiny)
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
                                                        Icon.name(SemanticICONS.`edit`)
                                                      ),
                                                    Button
                                                      .compact(true)
                                                      .size(SemanticSIZES.tiny)
                                                      .icon(true)
                                                      .onClick(
                                                        (
                                                          _,
                                                          _
                                                        ) =>
                                                          _root_.components.Confirm.confirm(
                                                            question =
                                                              "Are you 100% sure you want to delete this encounter?",
                                                            onConfirm = delete(encounter)
                                                          )
                                                      )(Icon.name(SemanticICONS.`delete`)),
                                                    Button
                                                      .compact(true)
                                                      .size(SemanticSIZES.tiny)
                                                      .icon(true)
                                                      // TODO Move the encounter to earlier in the list, or to the previous scene if it's at the beginning of the scene
                                                      (
                                                        Icon.name(SemanticICONS.`arrow up`)
                                                      ).when(encounterIndex != 0),
                                                    Button
                                                      .compact(true)
                                                      .size(SemanticSIZES.tiny)
                                                      .icon(true)
                                                      // TODO move the encounter to later in the list, or to the next scene if it's at the end of this scene
                                                      (
                                                        Icon.name(SemanticICONS.`arrow down`)
                                                      ) // .when(encounterIndex != campaignState.encounters.size - 1)
                                                    ,
                                                    Button
                                                      .compact(true)
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
                                            VdomArray(
                                              encounterInfo.monsters.map(_.name).mkString(", "),
                                              s"Notes: ${encounterInfo.notes}"
                                            )
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
                  state.currentEncounter.fold(Container.fluid(true)("Choose an encounter to edit or run"))(encounter =>
                    state.encounterMode match {
                      case EncounterMode.edit =>
                        Container.fluid(true)(
                          EncounterEditor(
                            encounter,
                            onDelete = encounter => delete(encounter),
                            onChange = encounter =>
                              $.modState(s =>
                                s.copy(encounters = s.encounters.map {
                                  case e if e.header.id == encounter.header.id => encounter
                                  case e                                       => e
                                })
                              )
                          )
                        )
                      case EncounterMode.combat => CombatRunner(campaignState, state, encounter)
                    }
                  )
                ),
              Grid.Column
                .withKey("encounterLog")
                .width(SemanticWIDTHS.`2`)(
                  Container
                    .fluid(true)(
                      if (state.encounterMode == EncounterMode.combat && state.currentEncounter.nonEmpty) {
                        VdomArray(
                          Container(
                            <.h2("Encounter Log")
                          ),
                          Container(
                            <.h2("Dice Roller"),
                            Button("d4"),
                            Button("d6"),
                            Button("d10"),
                            Button("d12"),
                            Button("d20"),
                            Button("d100"),
                            Input.action(Button("Roll")()).value("2d20")
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
    .initialState {
      State()
    }
    .renderBackend[Backend]
//    .shouldComponentUpdatePure($ => ! $.nextState.dialogOpen)
    .componentDidMount(_.backend.load())
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
