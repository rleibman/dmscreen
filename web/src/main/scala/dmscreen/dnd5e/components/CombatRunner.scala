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
import dmscreen.dnd5e.{*, given}
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
import org.scalajs.dom.*
import zio.json.*
import zio.json.ast.Json

import scala.scalajs.js.JSConverters.*

object CombatRunner {

  case class State(
    encounter:  Encounter,
    dialogOpen: Boolean = false
  )
  case class Props(
    encounter: Encounter,
    pcs:       Seq[PlayerCharacter],
    onChange:  Encounter => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      state: State,
      props: Props
    ): VdomNode = {
      val encounter = state.encounter

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
        Table(
          Table.Header(
            Table.Row(
              Table.HeaderCell.colSpan(2)(<.h2(s"${encounter.header.name}")),
              Table.HeaderCell
                .colSpan(2).textAlign(semanticUiReactStrings.center)(
                  s"Difficulty: ${encounter.calculateDifficulty(pcs)}, xp: ${encounter.info.xp}"
                ),
              Table.HeaderCell
                .colSpan(2).textAlign(semanticUiReactStrings.center)(
                  s"Round: 1 (10 seconds)" // TODO add this to encounter
                ),
              Table.HeaderCell
                .colSpan(2)
                .singleLine(true)
                .textAlign(semanticUiReactStrings.right)(
                  Button
                    .compact(true)
                    .title("Roll Initiative for all NPCs")
                    .icon(true)(Icon.className("d20icon")), // TODO roll NPC initiative
                  Button
                    .compact(true)
                    .title("Clear all NPC stats to beginning of combat")
                    .icon(true)(Icon.className("clearIcon")), // TODO clear NPC
                  Button
                    .compact(true)
                    .title("Edit encounter")
                    .icon(true)(Icon.name(SemanticICONS.`edit`)), // TODO edit encounter
                  Button
                    .compact(true)
                    .title("Reset encounter")
                    .icon(true)(Icon.name(SemanticICONS.`repeat`)), // TODO reset
                  Button
                    .compact(true)
                    .title("Archive Encounter")
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
                    .title("Next turn")
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
                case (creature: PlayerCharacterEncounterCreature, i: Int) =>
                  val pc = props.pcs.find(_.header.id.value == creature.playerCharacterId.value).get
                  val pcInfo = pc.info

                  Table.Row.withKey(s"creature #$i")(
                    Table.Cell(
                      Icon.name(SemanticICONS.`arrow right`).when(i == encounter.info.currentTurn),
                      creature.initiative
                    ), // TODO editable  // Add and round > 0
                    Table.Cell(pc.header.name),
                    Table.Cell(
                      Button("Heal")
                        .compact(true)
                        .title("Enter points in the damage box and click here to heal")
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
                        .title("Enter points in the damage box and click here for damage")
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
                      creature.otherMarkers.headOption.fold(
                        Icon.name(SemanticICONS.`plus circle`)
                      )(_ => Container(creature.otherMarkers.map(_.name).mkString(", ")))
                    ), // TODO editable
                    Table.Cell.singleLine(true)(
                      Button
                        .title("View character stats")
                        .compact(true)
                        .size(SemanticSIZES.mini)
                        .icon(true)(Icon.name(SemanticICONS.`eye`)) // TODO view character stats
                    )
                  )
                case (creature: MonsterEncounterCreature, i: Int) =>
                  Table.Row.withKey(s"creature #$i")(
                    Table.Cell(
                      Icon.name(SemanticICONS.`arrow right`).when(i == encounter.info.currentTurn),
                      creature.initiative
                    ), // Add and round > 0
                    Table.Cell(creature.name), // TODO editable
                    Table.Cell(
                      Button("Heal")
                        .compact(true)
                        .title("Enter points in the damage box and click here to heal")
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
                        .title("Enter points in the damage box and click here for damage")
                        .color(SemanticCOLORS.red)
                        .basic(true)
                        .size(SemanticSIZES.mini)
                        // TODO damage
                        .style(CSSProperties().set("width", 60.px)) // TODO to css
                    ),
                    Table.Cell
                      .singleLine(true).style(CSSProperties().set("background-color", creature.hitPoints.lifeColor))(
                        s"${creature.hitPoints.currentHitPoints match {
                            case ds: DeathSave => 0
                            case i:  Int       => i
                          }} / ${creature.hitPoints.maxHitPoints}"
                      ), // TODO editable
                    Table.Cell.textAlign(semanticUiReactStrings.center)(creature.armorClass), // TODO editable
                    Table.Cell.textAlign(semanticUiReactStrings.center)(
                      creature.conditions.headOption.fold(
                        Icon.name(SemanticICONS.`plus circle`)
                      )(_ => Container(creature.conditions.mkString(", ")))
                    ), // TODO editable
                    Table.Cell.textAlign(semanticUiReactStrings.center)(
                      creature.otherMarkers.headOption.fold(
                        Icon.name(SemanticICONS.`plus circle`)
                      )(_ => Container(creature.otherMarkers.map(_.name).mkString(", ")))
                    ), // TODO editable
                    Table.Cell.singleLine(true)(
                      Button
                        .compact(true)
                        .title("Delete Monster")
                        .size(SemanticSIZES.mini)
                        .icon(true)(Icon.name(SemanticICONS.`delete`)), // TODO delete monster
                      Button
                        .compact(true)
                        .title("Add another monster of this type, more monsters!")
                        .size(SemanticSIZES.mini)
                        .icon(true)(Icon.name(SemanticICONS.`clone outline`)), // TODO clone monster
                      Button
                        .compact(true)
                        .title("View monster stats")
                        .size(SemanticSIZES.mini)
                        .icon(true)(Icon.name(SemanticICONS.`eye`)) // View Monster Stats
                    )
                  )
              }*
          )
        )
      }
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("CombatRunner")
    .initialStateFromProps(p => State(p.encounter))
    .renderBackend[Backend]
    .shouldComponentUpdatePure($ => ! $.nextState.dialogOpen)
    .build

  def apply(
    encounter: Encounter,
    pcs:       Seq[PlayerCharacter],
    onChange:  Encounter => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] =
    component.withKey(encounter.header.id.value.toString)(Props(encounter, pcs, onChange))

}
