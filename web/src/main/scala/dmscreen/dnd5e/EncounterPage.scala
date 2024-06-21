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
  Monster as CalibanMonster,
  MonsterHeader as CalibanMonsterHeader,
  MonsterSearchOrder as CalibanMonsterSearchOrder,
  MonsterSearchResults as CalibanMonsterSearchResults,
  MonsterType as CalibanMonsterType,
  OrderDirection as CalibanOrderDirection,
  Queries
}
import caliban.client.scalajs.{*, given}
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
    monsterSearch:      MonsterSearch = MonsterSearch(),
    monsters:           List[MonsterHeader] = List.empty,
    monsterCount:       Long = 0,
    accordionState:     (Int, Int) = (0, 0),
    encounterMode:      EncounterMode = EncounterMode.combat,
    currentEncounterId: Option[EncounterId] = None
  ) {

    def currentEncounter: Option[Encounter] = currentEncounterId.flatMap(id => encounters.find(_.header.id == id))

  }

  class Backend($ : BackendScope[Unit, State]) {

    val crs: List[(String, Double)] =
      List("0" -> 0.0, "1/8" -> .125, "1/4" -> .25, "1/2" -> .5) ++ (1 to 30).map(i => i.toString -> i.toDouble).toList

    def monsterSearch: Callback = {
      val ajax = for {
        oldState <- $.state.asAsyncCallback
        searchResults <- {
          val monsterSB: SelectionBuilder[CalibanMonster, MonsterHeader] = CalibanMonster
            .header(
              CalibanMonsterHeader.id ~
                CalibanMonsterHeader.name ~
                CalibanMonsterHeader.monsterType ~
                CalibanMonsterHeader.biome ~
                CalibanMonsterHeader.alignment ~
                CalibanMonsterHeader.cr ~
                CalibanMonsterHeader.xp ~
                CalibanMonsterHeader.armorClass ~
                CalibanMonsterHeader.maximumHitPoints ~
                CalibanMonsterHeader.size
            ).map {
              (
                id,
                name,
                monsterType,
                biome,
                alignment,
                cr,
                xp,
                armorClass,
                maximumHitPoints,
                size
              ) =>
                MonsterHeader(
                  id = MonsterId(id),
                  name = name,
                  monsterType = MonsterType.valueOf(monsterType.value),
                  biome = biome.map(a => Biome.valueOf(a.value)),
                  alignment = alignment.map(a => Alignment.valueOf(a.value)),
                  cr = ChallengeRating.fromDouble(cr).getOrElse(ChallengeRating.`0`),
                  xp = xp,
                  armorClass = armorClass,
                  maximumHitPoints = maximumHitPoints,
                  size = CreatureSize.valueOf(size.value)
                )
            }

          val resultsSB: SelectionBuilder[CalibanMonsterSearchResults, (List[MonsterHeader], Long)] =
            CalibanMonsterSearchResults.results(monsterSB) ~ CalibanMonsterSearchResults.total

          val sb = Queries.bestiary(
            name = oldState.monsterSearch.name,
            challengeRating = oldState.monsterSearch.challengeRating.map(_.value),
            size = oldState.monsterSearch.size,
            alignment = oldState.monsterSearch.alignment,
            biome = oldState.monsterSearch.biome,
            monsterType = oldState.monsterSearch.monsterType,
            order = oldState.monsterSearch.order,
            orderDir = oldState.monsterSearch.orderDir,
            page = oldState.monsterSearch.page,
            pageSize = oldState.monsterSearch.pageSize
          )(resultsSB)
          asyncCalibanCall(sb)
        }

      } yield $.modState(s =>
        s.copy(monsters = searchResults.fold(List.empty)(_._1), monsterCount = searchResults.fold(0L)(_._2))
      )

      for {
        modedState <- ajax.completeWith(_.get)
      } yield modedState

    }

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

    def modMonsterSearch(f: MonsterSearch => MonsterSearch): Callback =
      $.modState(s => s.copy(monsterSearch = f(s.monsterSearch)), monsterSearch) // TODO change search

    private def EncounterEditor(
      campaignState: DND5eCampaignState,
      state:         State,
      encounter:     Encounter
    ) = {
      val info = encounter.info.toOption.get
      Container(
        Table(
          Table.Header(
            Table.Row(
              Table.HeaderCell.colSpan(3)(<.h2(s"${encounter.header.name}")),
              Table.HeaderCell
                .colSpan(3).textAlign(semanticUiReactStrings.center)(
                  s"Difficulty: ${info.difficulty}, xp: ${info.xp}"
                ),
              Table.HeaderCell
                .colSpan(4)
                .singleLine(true)
                .textAlign(semanticUiReactStrings.right)(
                  Button.compact(true).icon(true)(Icon.name(SemanticICONS.`archive`)),
                  Button.compact(true).icon(true)(Icon.name(SemanticICONS.`delete`))
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
            info.entities.collect { case entity: MonsterEncounterEntity =>
              Table.Row(
                Table.Cell(entity.name),
                Table.Cell(entity.monsterHeader.monsterType.toString.capitalize),
                Table.Cell(entity.monsterHeader.biome.fold("")(_.toString.capitalize)),
                Table.Cell(entity.monsterHeader.alignment.fold("")(_.name)),
                Table.Cell(entity.monsterHeader.cr.toString),
                Table.Cell(entity.monsterHeader.xp),
                Table.Cell(entity.monsterHeader.armorClass),
                Table.Cell(entity.hitPoints.maxHitPoints),
                Table.Cell(entity.monsterHeader.size.toString.capitalize),
                Table.Cell.singleLine(true)(
                  Button.compact(true).size(SemanticSIZES.mini).icon(true)(Icon.name(SemanticICONS.`delete`)),
                  Button.compact(true).size(SemanticSIZES.mini).icon(true)(Icon.name(SemanticICONS.`clone outline`))
                )
              )
            }*
          )
        ),
        Table(
          Table.Header(
            Table.Row(
              Table.Cell.colSpan(9)(
                Form(
                  Form.Group(
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

                            modMonsterSearch(_.copy(name = newVal))
                        }
                        .value(state.monsterSearch.name.getOrElse(""))
                    ),
                    Button // TODO move these buttons to the right
                      .compact(true).icon(true).onClick(
                        (
                          _,
                          _
                        ) => modMonsterSearch(_ => MonsterSearch())
                      )(
                        Icon.className("clearIcon")
                      ),
                    Button
                      .compact(true).icon(true).onClick(
                        (
                          _,
                          _
                        ) => modMonsterSearch(_ => MonsterSearch())
                      )(
                        Icon.name(SemanticICONS.`random`)
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
                              case s: String => MonsterType.values.find(_.toString.equalsIgnoreCase(s))
                              case _ => None
                            }

                            modMonsterSearch(_.copy(monsterType = newVal))
                        }
                        .value(state.monsterSearch.monsterType.fold("")(_.toString.capitalize))
                    ),
                    Form.Field(
                      Label("Biome"),
                      Dropdown()
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

                            modMonsterSearch(_.copy(biome = newVal))
                        }
                        .value(state.monsterSearch.biome.fold("")(_.toString.capitalize))
                    ),
                    Form.Field(
                      Label("Aligment"),
                      Dropdown()
                        .compact(true)
                        .search(false)
                        .clearable(true)
                        .placeholder("All")
                        .options(
                          Alignment.values.map(s => DropdownItemProps().setValue(s.name).setText(s.name)).toJSArray
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

                            modMonsterSearch(_.copy(alignment = newVal))
                        }
                        .value(state.monsterSearch.alignment.fold("")(_.name))
                    ),
                    Form.Field(
                      Label("CR"),
                      Dropdown()
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

                            modMonsterSearch(_.copy(challengeRating = newVal))
                        }
                        .value(state.monsterSearch.challengeRating.fold("")(_.toString))
                    ),
                    Form.Field(
                      Label("Size"),
                      Dropdown()
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
                              case s: String => CreatureSize.values.find(_.toString.equalsIgnoreCase(s))
                              case _ => None
                            }

                            modMonsterSearch(_.copy(size = newVal))
                        }
                        .value(state.monsterSearch.size.fold("")(_.toString.capitalize))
                    )
                  )
                )
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
            state.monsters.map(header =>
              Table.Row(
                Table.Cell(header.name),
                Table.Cell(header.monsterType.toString.capitalize),
                Table.Cell(header.biome.fold("")(_.toString.capitalize)),
                Table.Cell(header.alignment.fold("")(_.name)),
                Table.Cell(header.cr.toString),
                Table.Cell(header.xp),
                Table.Cell(header.armorClass),
                Table.Cell(header.maximumHitPoints),
                Table.Cell(header.size.toString.capitalize),
                Table.Cell(Button.size(SemanticSIZES.mini).compact(true).icon(true)(Icon.name(SemanticICONS.`add`)))
              )
            )*
          ),
          Table.Footer(
            Table.Row(
              Table.Cell.colSpan(10)(
                Pagination(state.monsterCount / state.monsterSearch.pageSize.toDouble)
                  .activePage(state.monsterSearch.page)
              )
            )
          )
        )
      )
    }

    private def CombatRunner(
      campaignState: DND5eCampaignState,
      state:         State,
      encounter:     Encounter
    ) = {
      val info = encounter.info.toOption.get

      Table(
        Table.Header(
          Table.Row(
            Table.HeaderCell.colSpan(2)(<.h2(s"${encounter.header.name}")),
            Table.HeaderCell
              .colSpan(3).textAlign(semanticUiReactStrings.center)(
                s"Difficulty: ${info.difficulty}, xp: ${info.xp}"
              ),
            Table.HeaderCell
              .colSpan(3)
              .singleLine(true)
              .textAlign(semanticUiReactStrings.right)(
                Button.compact(true).icon(true)(Icon.className("d20icon")), // roll NPC initiative
                Button.compact(true).icon(true)(Icon.className("clearIcon")), // clear NPC
                Button.compact(true).icon(true)(Icon.name(SemanticICONS.`edit`)),
                Button
                  .compact(true).icon(true)(Icon.name(SemanticICONS.`angle double right`)).when(
                    info.round > 0 && info.currentTurn == 0
                  ),
                Button.compact(true).icon(true)(Icon.name(SemanticICONS.`repeat`)), // reset
                Button.compact(true).icon(true)(Icon.name(SemanticICONS.`archive`)),
                Button.compact(true).icon(true)(Icon.name(SemanticICONS.`step forward`))
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
          info.entities
            .sortBy(-_.initiative)
            .zipWithIndex
            .map {
              case (entity: PlayerCharacterEncounterEntity, i: Int) =>
                val pc = campaignState.pcs.find(_.header.id.value == entity.playerCharacterId.value).get
                val pcInfo = pc.info.toOption.get

                Table.Row.withKey(s"entity #$i")(
                  Table.Cell(
                    Icon.name(SemanticICONS.`arrow right`).when(i == info.currentTurn),
                    entity.initiative
                  ), // Add and round > 0
                  Table.Cell(pc.header.name),
                  Table.Cell(
                    Button("Heal")
                      .compact(true).color(SemanticCOLORS.green).basic(true).size(
                        SemanticSIZES.mini
                      ).style(CSSProperties().set("width", 60.px)),
                    Input
                      .className("damageInput")
                      .size(SemanticSIZES.mini)
                      .`type`("number")
                      .min(0)
                      .maxLength(4),
                    Button("Damage")
                      .compact(true).color(SemanticCOLORS.red).basic(true).size(SemanticSIZES.mini).style(
                        CSSProperties().set("width", 60.px) // TODO to css
                      )
                  ),
                  Table.Cell
                    .singleLine(true).style(CSSProperties().set("background-color", pcInfo.hitPoints.lifeColor))(
                      s"${pcInfo.hitPoints.currentHitPoints match {
                          case ds: DeathSave => 0
                          case i:  Int       => i
                        }} / ${pcInfo.hitPoints.maxHitPoints}"
                    ),
                  Table.Cell.textAlign(semanticUiReactStrings.center)(pcInfo.armorClass),
                  Table.Cell.textAlign(semanticUiReactStrings.center)(
                    pcInfo.conditions.headOption.fold(
                      Icon.name(SemanticICONS.`plus circle`)
                    )(_ => Container(pcInfo.conditions.mkString(", ")))
                  ),
                  Table.Cell.textAlign(semanticUiReactStrings.center)(
                    entity.otherMarkers.headOption.fold(
                      Icon.name(SemanticICONS.`plus circle`)
                    )(_ => Container(entity.otherMarkers.map(_.name).mkString(", ")))
                  ),
                  Table.Cell.singleLine(true)(
                    Button
                      .compact(true).size(SemanticSIZES.mini).icon(true)(Icon.name(SemanticICONS.`eye`))
                  )
                )
              case (entity: MonsterEncounterEntity, i: Int) =>
                Table.Row.withKey(s"entity #$i")(
                  Table.Cell(
                    Icon.name(SemanticICONS.`arrow right`).when(i == info.currentTurn),
                    entity.initiative
                  ), // Add and round > 0
                  Table.Cell(entity.name),
                  Table.Cell(
                    Button("Heal")
                      .compact(true).color(SemanticCOLORS.green).basic(true).size(
                        SemanticSIZES.mini
                      ).style(CSSProperties().set("width", 60.px)),
                    Input
                      .className("damageInput")
                      .size(SemanticSIZES.mini)
                      .`type`("number")
                      .min(0)
                      .maxLength(4),
                    Button("Damage")
                      .compact(true).color(SemanticCOLORS.red).basic(true).size(SemanticSIZES.mini).style(
                        CSSProperties().set("width", 60.px) // TODO to css
                      )
                  ),
                  Table.Cell
                    .singleLine(true).style(CSSProperties().set("background-color", entity.hitPoints.lifeColor))(
                      s"${entity.hitPoints.currentHitPoints match {
                          case ds: DeathSave => 0
                          case i:  Int       => i
                        }} / ${entity.hitPoints.maxHitPoints}"
                    ),
                  Table.Cell.textAlign(semanticUiReactStrings.center)(entity.armorClass),
                  Table.Cell.textAlign(semanticUiReactStrings.center)(
                    entity.conditions.headOption.fold(
                      Icon.name(SemanticICONS.`plus circle`)
                    )(_ => Container(entity.conditions.mkString(", ")))
                  ),
                  Table.Cell.textAlign(semanticUiReactStrings.center)(
                    entity.otherMarkers.headOption.fold(
                      Icon.name(SemanticICONS.`plus circle`)
                    )(_ => Container(entity.otherMarkers.map(_.name).mkString(", ")))
                  ),
                  Table.Cell.singleLine(true)(
                    Button
                      .compact(true).size(SemanticSIZES.mini).icon(true)(
                        Icon.name(SemanticICONS.`delete`)
                      ),
                    Button
                      .compact(true).size(SemanticSIZES.mini).icon(true)(
                        Icon.name(SemanticICONS.`clone outline`)
                      ),
                    Button
                      .compact(true).size(SemanticSIZES.mini).icon(true)(Icon.name(SemanticICONS.`eye`))
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
                    .fluid(true)(
                      state.encounters
                        .sortBy(_.header.sceneId.map(_.value)) // TODO change to order by scene order
                        .groupBy(_.header.sceneId)
                        .zipWithIndex
                        .toList
                        .map { case ((scene, encounters), sceneIndex) =>
                          VdomArray(
                            Accordion.Title
                              .active(state.accordionState._1 == sceneIndex).onClick(
                                onAccordionChange((sceneIndex, 0))
                              )(
                                scene.fold("No Scene")(_.toString),
                                Button.icon(true)(Icon.name(SemanticICONS.`plus circle`))
                              ),
                            Accordion.Content.active(state.accordionState._1 == sceneIndex)(
                              Accordion.Accordion
                                .fluid(true)
                                .styled(true)(
                                  encounters.zipWithIndex
                                    .map { case (encounter, encounterIndex) =>
                                      val encounterInfo = encounter.info.toOption.get
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
                                                            ),
                                                            monsterSearch
                                                          )
                                                      )(
                                                        Icon.name(SemanticICONS.`edit`)
                                                      ),
                                                    Button
                                                      .compact(true)
                                                      .size(SemanticSIZES.tiny)
                                                      .icon(true)
                                                      // TODO confirm, then change the encounter status to archived
                                                      (
                                                        Icon.name(SemanticICONS.`delete`)
                                                      ),
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
                        }*
                    )
                ),
              Grid.Column
                .width(SemanticWIDTHS.`8`)
                .withKey("currentEncounter")(
                  state.currentEncounter.fold(Container.fluid(true)("Choose an encounter to edit or run"))(encounter =>
                    state.encounterMode match {
                      case EncounterMode.edit   => EncounterEditor(campaignState, state, encounter)
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

                      } else <.div("Hello world")
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
    .componentDidMount(_.backend.load())
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
