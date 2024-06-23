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
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.{CtorType, *}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
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

object EncounterEditor {

  case class State(
    encounter:     Encounter,
    monsterSearch: MonsterSearch = MonsterSearch(),
    monsters:      List[MonsterHeader] = List.empty,
    monsterCount:  Long = 0,
    dialogOpen:    Boolean = false
  )

  case class Props(
    encounter: Encounter,
    onDelete:  Encounter => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

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

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      def modMonsterSearch(f: MonsterSearch => MonsterSearch): Callback =
        $.modState(s => s.copy(monsterSearch = f(s.monsterSearch)), monsterSearch) // TODO change search

      def modEncounter(f: Encounter => Encounter) = $.modState(s => s.copy(encounter = f(s.encounter)))

      val encounter = state.encounter

      Container(
        Table(
          Table.Header(
            Table.Row(
              Table.HeaderCell.colSpan(3)(<.h2(s"${encounter.header.name}")),
              Table.HeaderCell
                .colSpan(3).textAlign(semanticUiReactStrings.center)(
                  s"Difficulty: ${encounter.info.difficulty}, xp: ${encounter.info.xp}"
                ),
              Table.HeaderCell
                .colSpan(4)
                .singleLine(true)
                .textAlign(semanticUiReactStrings.right)(
                  Button
                    .compact(true)
                    .icon(true)
                    .onClick(
                      (
                        _,
                        _
                      ) => modEncounter(e => e.copy(header = e.header.copy(status = EncounterStatus.archived)))
                    )(Icon.name(SemanticICONS.`archive`)).when(encounter.header.status != EncounterStatus.archived),
                  Button
                    .compact(true)
                    .icon(true)
                    .onClick(
                      (
                        _,
                        _
                      ) =>
                        _root_.components.Confirm.confirm(
                          question = "Are you 100% sure you want to delete this encounter?",
                          onConfirm = props.onDelete(encounter)
                        )
                    )(Icon.name(SemanticICONS.`delete`))
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
            encounter.info.entities.collect { case entity: MonsterEncounterEntity =>
              Table.Row(
                Table.Cell(
                  EditableText(
                    value = s"${entity.id.value}:${entity.name}",
                    onChange = name =>
                      modEncounter(encounter => {
                        encounter
                          .copy(jsonInfo =
                            encounter.info
                              .copy(entities = encounter.info.entities.map {
                                case entity: MonsterEncounterEntity if entity.id == entity.id =>
                                  entity.copy(name = name)
                                case _ => entity
                              }).toJsonAST.toOption.get // TODO this conversion is akward, change it
                          )
                      })
                  )
                ),
                Table.Cell(entity.monsterHeader.monsterType.toString.capitalize),
                Table.Cell(entity.monsterHeader.biome.fold("")(_.toString.capitalize)),
                Table.Cell(entity.monsterHeader.alignment.fold("")(_.name)),
                Table.Cell(entity.monsterHeader.cr.toString),
                Table.Cell(entity.monsterHeader.xp),
                Table.Cell(
                  EditableNumber(
                    value = entity.monsterHeader.armorClass,
                    min = 0,
                    max = 30,
                    onChange = v =>
                      modEncounter { encounter =>
                        encounter
                          .copy(jsonInfo =
                            encounter.info
                              .copy(entities = encounter.info.entities.map {
                                case entity: MonsterEncounterEntity if entity.id == entity.id =>
                                  entity.copy(armorClass = v.toInt)
                                case _ => entity
                              }).toJsonAST.toOption.get
                          )
                      }
                  )
                ),
                Table.Cell(
                  EditableNumber(
                    value = entity.hitPoints.maxHitPoints,
                    min = 0,
                    max = 1000,
                    onChange = v =>
                      modEncounter { encounter =>
                        encounter
                          .copy(jsonInfo =
                            encounter.info
                              .copy(entities = encounter.info.entities.map {
                                case entity: MonsterEncounterEntity if entity.id == entity.id =>
                                  entity.copy(hitPoints = entity.hitPoints.copy(maxHitPoints = v.toInt))
                                case _ => entity
                              }).toJsonAST.toOption.get
                          )
                      }
                  )
                ),
                Table.Cell(entity.monsterHeader.size.toString.capitalize),
                Table.Cell.singleLine(true)(
                  Button
                    .compact(true)
                    .size(SemanticSIZES.mini)
                    .icon(true)
                    .onClick {
                      (
                        _,
                        _
                      ) =>
                        modEncounter(encounter => {
                          println(s"deleting ${entity.id}, name=${entity.name}")
                          println(encounter.info.entities.map(_.id).mkString(","))
                          val res = encounter
                            .copy(jsonInfo =
                              encounter.info
                                .copy(entities = encounter.info.entities.filter(_.id != entity.id))
                                .toJsonAST.toOption.get
                            )
                          println(res.info.entities.map(_.id).mkString(","))
                          res
                        })
                    }(Icon.name(SemanticICONS.`delete`)),
                  Button
                    .compact(true)
                    .size(SemanticSIZES.mini)
                    .onClick {
                      (
                        _,
                        _
                      ) =>
                        val newId = EntityId(encounter.info.entities.size)
                        modEncounter(encounter => {
                          encounter
                            .copy(jsonInfo =
                              encounter.info
                                .copy(entities =
                                  encounter.info.entities :+
                                    entity.copy(id = newId)
                                )
                                .toJsonAST.toOption.get
                            )

                        })
                    }
                    .icon(true)(Icon.name(SemanticICONS.`clone outline`))
                )
              )
            }*
          )
        ),
        Table(
          Table.Header(
            Table.Row( // Search Row
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
                Table.Cell(
                  Button
                    .size(SemanticSIZES.mini)
                    .compact(true)
                    .icon(true)(Icon.name(SemanticICONS.`add`))
                ) // TODO add monster
              )
            )*
          ),
          Table.Footer(
            Table.Row(
              Table.Cell.colSpan(10)(
                Pagination(state.monsterCount / state.monsterSearch.pageSize.toDouble)
                  .onPageChange {
                    (
                      _,
                      data
                    ) =>
                      val newVal = data.activePage match {
                        case s: String => s.toInt
                        case d: Double => d.toInt
                      }

                      modMonsterSearch(_.copy(page = newVal))
                  }
                  .activePage(state.monsterSearch.page)
              )
            )
          )
        )
      )
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("EncounterEditor")
    .initialStateFromProps(p => State(p.encounter))
    .renderBackend[Backend]
    .componentDidMount($ => Callback.empty)
    .shouldComponentUpdatePure($ => ! $.nextState.dialogOpen)
    .build

  def apply(
    encounter: Encounter,
    onDelete:  Encounter => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] =
    component.withKey(encounter.header.id.value.toString)(Props(encounter, onDelete))

}
