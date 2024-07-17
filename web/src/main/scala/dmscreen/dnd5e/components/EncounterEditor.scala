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
import dmscreen.components.{EditableNumber, EditableText}
import dmscreen.{*, given}
import dmscreen.dnd5e.{*, given}
import dmscreen.dnd5e.components.*
import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{CtorType, *}
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

def createMonsterCombatant(
  e:      Encounter,
  header: MonsterHeader
): MonsterCombatant = {
  val number =
    e.info.combatants.collect {
      case monsterCombatant: MonsterCombatant if monsterCombatant.monsterHeader.id == header.id => monsterCombatant
    }.size + 1

  MonsterCombatant(
    id = CombatantId.create,
    monsterHeader = header,
    health = Health(
      deathSave = DeathSave.empty,
      currentHitPoints = header.maximumHitPoints,
      maxHitPoints = header.maximumHitPoints
    ),
    armorClass = header.armorClass,
    name = s"${header.name} #$number",
    initiativeBonus = header.initiativeBonus
  )
}

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

  enum CloneMonster {

    case yes, no

  }

  case class State(
    encounter:      Encounter,
    monsterSearch:  MonsterSearch = MonsterSearch(),
    monsters:       List[MonsterHeader] = List.empty,
    monsterCount:   Long = 0,
    editingMonster: Option[(MonsterId, CloneMonster)] = None,
    viewMonsterId:  Option[MonsterId] = None
  )

  case class Props(
    encounter: Encounter,
    onDelete:  Encounter => Callback,
    onChange:  Encounter => Callback
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
                CalibanMonsterHeader.sourceId ~
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
                sourceId,
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
                  sourceId = SourceId(sourceId),
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

      ajax.completeWith(_.get)

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

      DMScreenState.ctx.consume { dmScreenState =>
        val pcs = dmScreenState.campaignState.fold(List.empty[PlayerCharacter])(_.asInstanceOf[DND5eCampaignState].pcs)
        val encounter = props.encounter

        Container(
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
                Modal.Content(^.padding := 5.px, MonsterStatBlock(monsterId))
              ): VdomNode
          ),
          state.editingMonster.fold(EmptyVdom)(
            (
              monsterId,
              cloneMonster
            ) =>
              MonsterEditor(
                monsterId,
                cloneMonster = cloneMonster == CloneMonster.yes,
                onClose = monster =>
                  $.modState(_.copy(editingMonster = None), modMonsterSearch(_.copy(name = Some(monster.header.name))))
              )
          ),
          Table(
            Table.Header(
              Table.Row(
                Table.HeaderCell.colSpan(3)(<.h2(s"${encounter.header.name}")),
                Table.HeaderCell
                  .colSpan(3).textAlign(semanticUiReactStrings.center)(
                    s"Difficulty: ${encounter.calculateDifficulty(pcs)}, XP: ${encounter.info.xp}"
                  ),
                Table.HeaderCell
                  .colSpan(4)
                  .singleLine(true)
                  .textAlign(semanticUiReactStrings.right)(
                    Button
                      .compact(true)
                      .icon(true)
                      .title("Archive this encounter")
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
                      .title("Delete this encounter")
                      .onClick(
                        (
                          _,
                          _
                        ) =>
                          _root_.components.Confirm.confirm(
                            question = "Are you 100% sure you want to delete this encounter?",
                            onConfirm = props.onDelete(encounter)
                          )
                      )(Icon.name(SemanticICONS.`trash`))
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
              encounter.info.combatants
                .sortBy {
                  case monsterCombatant: MonsterCombatant         => monsterCombatant.name
                  case pcCombatant:      PlayerCharacterCombatant => pcCombatant.name // Not really important
                }.collect { case combatant: MonsterCombatant =>
                  Table.Row
                    .withKey(s"combatant ${combatant.id.value}")(
                      Table.Cell(
                        EditableText(
                          value = combatant.name,
                          allowEditing = encounter.header.status != EncounterStatus.archived,
                          onChange = name =>
                            modEncounter(e => {
                              e
                                .copy(jsonInfo =
                                  e.info
                                    .copy(combatants = e.info.combatants.map {
                                      case monsterCombatant2: MonsterCombatant
                                          if monsterCombatant2.id == combatant.id =>
                                        monsterCombatant2.copy(name = name)
                                      case _ => combatant
                                    }).toJsonAST.toOption.get
                                )
                            })
                        )
                      ),
                      Table.Cell(combatant.monsterHeader.monsterType.toString.capitalize),
                      Table.Cell(combatant.monsterHeader.biome.fold("")(_.toString.capitalize)),
                      Table.Cell(combatant.monsterHeader.alignment.fold("")(_.name)),
                      Table.Cell(combatant.monsterHeader.cr.toString),
                      Table.Cell(combatant.monsterHeader.xp),
                      Table.Cell(
                        EditableNumber(
                          value = combatant.monsterHeader.armorClass,
                          allowEditing = encounter.header.status != EncounterStatus.archived,
                          min = 0,
                          max = 30,
                          onChange = v =>
                            modEncounter { e =>
                              e
                                .copy(jsonInfo =
                                  e.info
                                    .copy(combatants = e.info.combatants.map {
                                      case monsterCombatant2: MonsterCombatant
                                          if monsterCombatant2.id == combatant.id =>
                                        monsterCombatant2.copy(armorClass = v.toInt)
                                      case _ => combatant
                                    }).toJsonAST.toOption.get
                                )
                            }
                        )
                      ),
                      Table.Cell(
                        EditableNumber(
                          value = combatant.health.maxHitPoints,
                          allowEditing = encounter.header.status != EncounterStatus.archived,
                          min = 0,
                          max = 1000,
                          onChange = v =>
                            modEncounter { e =>
                              e
                                .copy(jsonInfo =
                                  e.info
                                    .copy(combatants = e.info.combatants.map {
                                      case monsterCombatant2: MonsterCombatant
                                          if monsterCombatant2.id == combatant.id =>
                                        monsterCombatant2
                                          .copy(health = monsterCombatant2.health.copy(maxHitPoints = v.toInt))
                                      case _ => combatant
                                    }).toJsonAST.toOption.get
                                )
                            }
                        )
                      ),
                      Table.Cell(combatant.monsterHeader.size.toString.capitalize),
                      Table.Cell.singleLine(true)(
                        Button
                          .compact(true)
                          .size(SemanticSIZES.mini)
                          .title("Delete this combatant from the encounter")
                          .icon(true)
                          .onClick {
                            (
                              _,
                              _
                            ) =>
                              modEncounter(e => {
                                e
                                  .copy(jsonInfo =
                                    e.info
                                      .copy(combatants = e.info.combatants.filter(_.id != combatant.id))
                                      .toJsonAST.toOption.get
                                  )
                              })
                          }(Icon.name(SemanticICONS.`trash`))
                          .when(encounter.header.status != EncounterStatus.archived),
                        Button
                          .compact(true)
                          .size(SemanticSIZES.mini)
                          .title("More of this combatant!")
                          .onClick {
                            (
                              _,
                              _
                            ) =>
                              modEncounter(encounter => {
                                encounter
                                  .copy(jsonInfo =
                                    encounter.info
                                      .copy(combatants =
                                        encounter.info.combatants :+
                                          createMonsterCombatant(encounter, combatant.monsterHeader)
                                      )
                                      .toJsonAST.toOption.get
                                  )

                              })
                          }
                          .icon(true)(Icon.name(SemanticICONS.`clone outline`))
                          .when(state.encounter.header.status != EncounterStatus.archived),
                        Button
                          .title("View Monster Stat Block")
                          .compact(true)
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
          ),
          Divider.section(true),
          // ENHANCEMENT move this into a component of it's own
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
                        .title("Delete this monster")
                        .onClick(
                          (
                            _,
                            _
                          ) =>
                            _root_.components.Confirm.confirm(
                              question = "Are you 100% sure you want to delete this monster?",
                              onConfirm = GraphQLRepository.live
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
                      Button
                        .size(SemanticSIZES.mini)
                        .compact(true)
                        .title("Add this combatant to the encounter")
                        .onClick(
                          (
                            _,
                            _
                          ) =>
                            modEncounter { e =>
                              e
                                .copy(jsonInfo =
                                  e.info
                                    .copy(combatants = e.info.combatants :+ createMonsterCombatant(e, header))
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

                          modMonsterSearch(_.copy(page = newVal - 1))
                      }
                      .activePage(state.monsterSearch.page + 1)
                  )
                )
              )
            ).when(state.encounter.header.status != EncounterStatus.archived)
        )
      }
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("EncounterEditor")
    .initialStateFromProps { p =>
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
