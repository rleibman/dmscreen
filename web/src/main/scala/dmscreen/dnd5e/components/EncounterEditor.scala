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
import dmscreen.dnd5e.{OrderDirection, *, given}
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
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.*
import org.scalablytyped.runtime.StObject
import org.scalajs.dom.*
import zio.json.*
import zio.json.ast.Json

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object EncounterEditor {

  extension (orderCol: MonsterSearchOrder) {

    def toReactSoreDirection(
      compareMe: MonsterSearchOrder,
      direction: OrderDirection
    ): ascending | descending = {
      import scala.language.unsafeNulls
      if (orderCol == compareMe) {
        direction match {
          case OrderDirection.asc  => ascending
          case OrderDirection.desc => descending
        }

      } else {
        null
      }
    }

  }

  case class State(
    encounter:     Encounter,
    monsterSearch: MonsterSearch = MonsterSearch(),
    monsters:      List[MonsterHeader] = List.empty,
    monsterCount:  Long = 0
  )

  case class Props(
    encounter: Encounter,
    onDelete:  Encounter => Callback,
    onChange:  Encounter => Callback
  )

  def createMonsterCreature(
    e:      Encounter,
    header: MonsterHeader
  ): MonsterEncounterCreature = {
    val number =
      e.info.creatures.collect { case c: MonsterEncounterCreature if c.monsterHeader.id == header.id => c }.size + 1

    MonsterEncounterCreature(
      id = CreatureId(e.info.creatures.size),
      monsterHeader = header,
      hitPoints = HitPoints(
        currentHitPoints = header.maximumHitPoints,
        maxHitPoints = header.maximumHitPoints
      ),
      armorClass = header.armorClass,
      name = s"${header.name} #$number",
      initiativeBonus = header.initiativeBonus
    )
  }

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
                CalibanMonsterHeader.size ~
                CalibanMonsterHeader.initiativeBonus
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
                size,
                initiativeBonus
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
                  size = CreatureSize.valueOf(size.value),
                  initiativeBonus = initiativeBonus
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
            orderCol = oldState.monsterSearch.orderCol,
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

    def modMonsterSearch(f: MonsterSearch => MonsterSearch): Callback =
      $.modState(s => s.copy(monsterSearch = f(s.monsterSearch)), monsterSearch)

    def changeSort(orderCol: MonsterSearchOrder): Callback = {
      modMonsterSearch { currentSearch =>
        currentSearch.copy(
          orderCol = orderCol,
          orderDir = if (currentSearch.orderCol == orderCol) {
            currentSearch.orderDir.toggle
          } else OrderDirection.asc
        )
      }
    }

    def render(
      props: Props,
      state: State
    ): VdomNode = {

      def modEncounter(
        f:        Encounter => Encounter,
        callback: => Callback = Callback.empty
      ) =
        $.modState(
          s => s.copy(encounter = f(s.encounter)),
          callback >> $.state.flatMap(s => props.onChange(s.encounter))
        )

      val encounter = props.encounter

      Container(
        Table(
          Table.Header(
            Table.Row(
              Table.HeaderCell.colSpan(3)(<.h2(s"${encounter.header.name}")),
              Table.HeaderCell
                .colSpan(3).textAlign(semanticUiReactStrings.center)(
                  s"Difficulty: ${encounter.info.difficulty}, XP: ${encounter.info.xp}"
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
                    )(Icon.name(SemanticICONS.`archive`)).when(
                      encounter.header.status != EncounterStatus.archived
                    ),
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
            encounter.info.creatures
              .sortBy {
                case creature: MonsterEncounterCreature         => creature.name
                case creature: PlayerCharacterEncounterCreature => creature.name // Not really important
              }.collect { case creature: MonsterEncounterCreature =>
                Table.Row
                  .withKey(s"creature ${creature.id}")(
                    Table.Cell(
                      EditableText(
                        value = creature.name,
                        allowEditing = encounter.header.status != EncounterStatus.archived,
                        onChange = name =>
                          modEncounter(e => {
                            e
                              .copy(jsonInfo =
                                e.info
                                  .copy(creatures = e.info.creatures.map {
                                    case creature: MonsterEncounterCreature if creature.id == creature.id =>
                                      creature.copy(name = name)
                                    case _ => creature
                                  }).toJsonAST.toOption.get // TODO this conversion is akward, it would be nice to change it, but how?
                              )
                          })
                      )
                    ),
                    Table.Cell(creature.monsterHeader.monsterType.toString.capitalize),
                    Table.Cell(creature.monsterHeader.biome.fold("")(_.toString.capitalize)),
                    Table.Cell(creature.monsterHeader.alignment.fold("")(_.name)),
                    Table.Cell(creature.monsterHeader.cr.toString),
                    Table.Cell(creature.monsterHeader.xp),
                    Table.Cell(
                      EditableNumber(
                        value = creature.monsterHeader.armorClass,
                        allowEditing = encounter.header.status != EncounterStatus.archived,
                        min = 0,
                        max = 30,
                        onChange = v =>
                          modEncounter { e =>
                            e
                              .copy(jsonInfo =
                                e.info
                                  .copy(creatures = e.info.creatures.map {
                                    case creature: MonsterEncounterCreature if creature.id == creature.id =>
                                      creature.copy(armorClass = v.toInt)
                                    case _ => creature
                                  }).toJsonAST.toOption.get
                              )
                          }
                      )
                    ),
                    Table.Cell(
                      EditableNumber(
                        value = creature.hitPoints.maxHitPoints,
                        allowEditing = encounter.header.status != EncounterStatus.archived,
                        min = 0,
                        max = 1000,
                        onChange = v =>
                          modEncounter { e =>
                            e
                              .copy(jsonInfo =
                                e.info
                                  .copy(creatures = e.info.creatures.map {
                                    case creature: MonsterEncounterCreature if creature.id == creature.id =>
                                      creature.copy(hitPoints = creature.hitPoints.copy(maxHitPoints = v.toInt))
                                    case _ => creature
                                  }).toJsonAST.toOption.get
                              )
                          }
                      )
                    ),
                    Table.Cell(creature.monsterHeader.size.toString.capitalize),
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
                            modEncounter(
                              e => {
                                e
                                  .copy(jsonInfo =
                                    e.info
                                      .copy(creatures = e.info.creatures.filter(_.id != creature.id))
                                      .toJsonAST.toOption.get
                                  )
                              },
                              $.state.flatMap(state =>
                                Callback.log(s"deleted creature ${creature.name}, newState = ${encounter.info.creatures.map(_.asInstanceOf[MonsterEncounterCreature].name)}")
                              )
                            )
                        }(Icon.name(SemanticICONS.`delete`))
                        .when(encounter.header.status != EncounterStatus.archived),
                      Button
                        .compact(true)
                        .size(SemanticSIZES.mini)
                        .onClick {
                          (
                            _,
                            _
                          ) =>
                            val newId = CreatureId(encounter.info.creatures.size)
                            modEncounter(encounter => {
                              encounter
                                .copy(jsonInfo =
                                  encounter.info
                                    .copy(creatures =
                                      encounter.info.creatures :+
                                        createMonsterCreature(encounter, creature.monsterHeader)
                                    )
                                    .toJsonAST.toOption.get
                                )

                            })
                        }
                        .icon(true)(Icon.name(SemanticICONS.`clone outline`))
                        .when(state.encounter.header.status != EncounterStatus.archived)
                    )
                  )
              }*
          )
        ),
        Table
          .sortable(true)(
            Table.Header(
              Table.Row( // Search Row
                Table.Cell.colSpan(10)(
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

                                modMonsterSearch(_.copy(name = newVal))
                            }
                            .value(state.monsterSearch.name.getOrElse(""))
                        ),
                        <.div(^.width := 100.pct, ""), // This is just a spacer
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
                            Icon.name(SemanticICONS.`random`) // Add random creature that matches the results
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
                Table.HeaderCell
                  .sorted(
                    state.monsterSearch.orderCol
                      .toReactSoreDirection(MonsterSearchOrder.name, state.monsterSearch.orderDir)
                  ).onClick(_ => changeSort(MonsterSearchOrder.name))("Name"),
                Table.HeaderCell
                  .sorted(
                    state.monsterSearch.orderCol
                      .toReactSoreDirection(MonsterSearchOrder.monsterType, state.monsterSearch.orderDir)
                  ).onClick(_ => changeSort(MonsterSearchOrder.monsterType))("Type"),
                Table.HeaderCell
                  .sorted(
                    state.monsterSearch.orderCol
                      .toReactSoreDirection(MonsterSearchOrder.biome, state.monsterSearch.orderDir)
                  ).onClick(_ => changeSort(MonsterSearchOrder.biome))("Biome"),
                Table.HeaderCell
                  .sorted(
                    state.monsterSearch.orderCol
                      .toReactSoreDirection(MonsterSearchOrder.alignment, state.monsterSearch.orderDir)
                  ).onClick(_ => changeSort(MonsterSearchOrder.alignment))("Alignment"),
                Table.HeaderCell
                  .sorted(
                    state.monsterSearch.orderCol
                      .toReactSoreDirection(MonsterSearchOrder.challengeRating, state.monsterSearch.orderDir)
                  ).onClick(_ => changeSort(MonsterSearchOrder.challengeRating))("CR"),
                Table.HeaderCell("XP"),
                Table.HeaderCell("AC"),
                Table.HeaderCell("HP"),
                Table.HeaderCell
                  .sorted(
                    state.monsterSearch.orderCol
                      .toReactSoreDirection(MonsterSearchOrder.size, state.monsterSearch.orderDir)
                  ).onClick(_ => changeSort(MonsterSearchOrder.size))("Size"),
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
                      .onClick(
                        (
                          _,
                          _
                        ) =>
                          modEncounter { e =>
                            e
                              .copy(jsonInfo =
                                e.info
                                  .copy(creatures = e.info.creatures :+ createMonsterCreature(e, header))
                                  .toJsonAST.toOption.get
                              )
                          }
                      )
                      .icon(true)(Icon.name(SemanticICONS.`add`))
                  )
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
          ).when(state.encounter.header.status != EncounterStatus.archived)
      )
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("EncounterEditor")
    .initialStateFromProps { p =>
      println(s"starting with = ${p.encounter.info.creatures.map(_.asInstanceOf[MonsterEncounterCreature].name)}")
      State(p.encounter)
    }
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.monsterSearch)
    .build

  def apply(
    encounter: Encounter,
    onDelete:  Encounter => Callback = _ => Callback.empty,
    onChange:  Encounter => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] =
    component.withKey(encounter.header.id.value.toString)(Props(encounter, onDelete, onChange))

}
