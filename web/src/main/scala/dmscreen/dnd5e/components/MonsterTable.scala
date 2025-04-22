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

import dmscreen.CampaignId
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{CtorType, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsAddonsPaginationPaginationMod.PaginationProps
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.*
import org.scalablytyped.runtime.StObject
import org.scalajs.dom.*

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object MonsterTable {

  enum CloneMonster {

    case yes, no

  }

  extension (orderCol: MonsterSearchOrder) {

    def toReactSortDirection(
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
    monsters:       List[MonsterHeader] = List.empty,
    monsterCount:   Long = 0,
    monsterSearch:  MonsterSearch = MonsterSearch(),
    editingMonster: Option[(MonsterId, CloneMonster)] = None,
    viewMonsterId:  Option[MonsterId] = None
  )
  case class Props(
    campaignId:   CampaignId,
    extraActions: MonsterHeader => VdomNode
  )

  class Backend($ : BackendScope[Props, State]) {

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

    def modMonsterSearch(fn: MonsterSearch => MonsterSearch): Callback =
      $.modState(
        s => s.copy(monsterSearch = fn(s.monsterSearch)),
        $.state.flatMap(s =>
          DND5eGraphQLRepository.live
            .bestiary(s.monsterSearch)
            .map(monsters => $.modState(_.copy(monsters = monsters.results, monsterCount = monsters.total)))
            .completeWith(_.get)
        )
      )

    def loadState(campaignId: CampaignId): Callback = modMonsterSearch(_ => MonsterSearch())

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      (state.viewMonsterId, state.editingMonster) match {
        case (Some(monsterId), _) =>
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
        case (_, Some((monsterId, cloneMonster))) =>
          MonsterEditor(
            monsterId,
            cloneMonster = cloneMonster == CloneMonster.yes,
            onCancel = $.modState(_.copy(editingMonster = None)),
            onSave = monster =>
              $.modState(
                _.copy(editingMonster = None),
                modMonsterSearch(_.copy(name = Some(monster.header.name)))
              )
          )
        case (_, _) =>
          <.div(
            Table
              .inverted(DND5eUI.tableInverted)
              .color(DND5eUI.tableColor)
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
                            Button
                              .title("Clear the search criteria")
                              .compact(true)
                              .icon(true)
                              .onClick(
                                (
                                  _,
                                  _
                                ) => modMonsterSearch(_ => MonsterSearch())
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
                                ) => modMonsterSearch(_.copy(orderCol = MonsterSearchOrder.random))
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
                                    modMonsterSearch(_ => MonsterSearch()) // Clear the search
                              )(
                                Icon.name(SemanticICONS.add)
                              )
                          ),
                        Form.Group(
                          Form.Field(
                            Label("Type"),
                            Dropdown
                              .fluid(true)
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
                            Form
                              .Dropdown()
                              .fluid(true)
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
                                    case s: String => Biome.values.find(_.toString.equalsIgnoreCase(s))
                                    case _ => None
                                  }

                                  modMonsterSearch(_.copy(biome = newVal))
                              }
                              .value(state.monsterSearch.biome.fold("")(_.toString.capitalize))
                          ),
                          Form.Field(
                            Label("Aligment"),
                            Form
                              .Dropdown()
                              .fluid(true)
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
                                    case s: String => Alignment.values.find(_.name.equalsIgnoreCase(s))
                                    case _ => None
                                  }

                                  modMonsterSearch(_.copy(alignment = newVal))
                              }
                              .value(state.monsterSearch.alignment.fold("")(_.name))
                          ),
                          Form.Field(
                            Label("CR"),
                            Form
                              .Dropdown()
                              .fluid(true)
                              .search(false)
                              .clearable(true)
                              .placeholder("All")
                              .options(
                                ChallengeRating.values
                                  .map(s => DropdownItemProps().setValue(s.toString).setText(s.name)).toJSArray
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
                            Form
                              .Dropdown()
                              .fluid(true)
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
                          .toReactSortDirection(MonsterSearchOrder.name, state.monsterSearch.orderDir)
                      ).onClick(_ => changeSort(MonsterSearchOrder.name))("Name"),
                    Table.HeaderCell
                      .sorted(
                        state.monsterSearch.orderCol
                          .toReactSortDirection(MonsterSearchOrder.monsterType, state.monsterSearch.orderDir)
                      ).onClick(_ => changeSort(MonsterSearchOrder.monsterType))("Type"),
                    Table.HeaderCell
                      .sorted(
                        state.monsterSearch.orderCol
                          .toReactSortDirection(MonsterSearchOrder.biome, state.monsterSearch.orderDir)
                      ).onClick(_ => changeSort(MonsterSearchOrder.biome))("Biome"),
                    Table.HeaderCell
                      .sorted(
                        state.monsterSearch.orderCol
                          .toReactSortDirection(MonsterSearchOrder.alignment, state.monsterSearch.orderDir)
                      ).onClick(_ => changeSort(MonsterSearchOrder.alignment))("Alignment"),
                    Table.HeaderCell
                      .sorted(
                        state.monsterSearch.orderCol
                          .toReactSortDirection(MonsterSearchOrder.challengeRating, state.monsterSearch.orderDir)
                      ).onClick(_ => changeSort(MonsterSearchOrder.challengeRating))("CR"),
                    Table.HeaderCell("XP"),
                    Table.HeaderCell("AC"),
                    Table.HeaderCell("HP"),
                    Table.HeaderCell
                      .sorted(
                        state.monsterSearch.orderCol
                          .toReactSortDirection(MonsterSearchOrder.size, state.monsterSearch.orderDir)
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
                      Table.Cell(header.cr.name),
                      Table.Cell(header.xp),
                      Table.Cell(header.armorClass),
                      Table.Cell(header.maximumHitPoints),
                      Table.Cell(header.size.toString.capitalize),
                      Table.Cell(
                        Button
                          .size(SemanticSIZES.mini)
                          .compact(true)
                          .title("Delete this monster")
                          .onClick(
                            (
                              _,
                              _
                            ) =>
                              _root_.components.Confirm.confirm(
                                question = "Are you 100% sure you want to delete this monster?",
                                onConfirm = DND5eGraphQLRepository.live
                                  .deleteEntity(DND5eEntityType.monster, header.id)
                                  .map(_ => modMonsterSearch(_ => MonsterSearch()))
                                  .completeWith(_.get)
                              )
                          )
                          .icon(true)(Icon.name(SemanticICONS.`trash`)).when(header.sourceId == SourceId.homebrew),
                        Button
                          .size(SemanticSIZES.mini)
                          .compact(true)
                          .title("Create a new monster with this one as template")
                          .onClick(
                            (
                              _,
                              _
                            ) =>
                              $.modState(_.copy(editingMonster = Some((header.id, CloneMonster.yes)))) >>
                                modMonsterSearch(_ => MonsterSearch()) // Clear the search
                          )
                          .icon(true)(Icon.name(SemanticICONS.`clone outline`)),
                        Button
                          .title("View Monster Stat Block")
                          .compact(true)
                          .size(SemanticSIZES.mini)
                          .icon(true)(Icon.name(SemanticICONS.`eye`))
                          .onClick(
                            (
                              _,
                              _
                            ) => $.modState(_.copy(viewMonsterId = Some(header.id)))
                          ),
                        Button
                          .title("Edit Monster")
                          .compact(true)
                          .size(SemanticSIZES.mini)
                          .icon(true)(Icon.name(SemanticICONS.`edit`))
                          .onClick(
                            (
                              _,
                              _
                            ) =>
                              $.modState(_.copy(editingMonster = Some((header.id, CloneMonster.no)))) >>
                                modMonsterSearch(_ => MonsterSearch()) // Clear the search
                          ).when(header.sourceId == SourceId.homebrew),
                        props.extraActions(header)
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
                              case _: Unit   => 1
                            }

                            modMonsterSearch(_.copy(page = newVal - 1))
                        }
                        .activePage(state.monsterSearch.page + 1)
                    )
                  )
                )
              )
          )
      }
    }

  }

  private val component = ScalaComponent
    .builder[Props]("MonsterTable")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount { $ =>
      $.backend.loadState($.props.campaignId)
    }
    .build

  def apply(
    campaignId:   CampaignId,
    extraActions: MonsterHeader => VdomNode = _ => EmptyVdom
  ): Unmounted[Props, State, Backend] = component(Props(campaignId, extraActions))

}
