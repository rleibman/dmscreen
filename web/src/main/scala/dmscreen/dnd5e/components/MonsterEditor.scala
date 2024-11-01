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

import dmscreen.*
import dmscreen.components.EditableComponent
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.vdom.{Attr, InnerHtmlAttr}
import japgolly.scalajs.react.{Callback, CtorType, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.reactQuill.components.ReactQuill
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticSIZES
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.above
import zio.json.*
//import zio.prelude.*

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object MonsterEditor {

  case class State(
    monster:    Option[Monster] = None,
    dialogOpen: Boolean = false
  )
  case class Props(
    monsterId:    MonsterId,
    cloneMonster: Boolean,
    onClose:      Monster => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    private def modMonster(
      f:        Monster => Monster,
      callback: Callback = Callback.empty
    ): Callback = {
      $.modState(s => s.copy(monster = s.monster.map(f)), callback)
    }

    private def modInfo(
      f:        MonsterInfo => MonsterInfo,
      callback: Callback = Callback.empty
    ): Callback = {
      modMonster(monster => monster.copy(jsonInfo = f(monster.info).toJsonAST.toOption.get))
    }

    def loadMonster(
      monsterId: MonsterId,
      clone:     Boolean
    ): Callback = {
      val newId = if (clone) MonsterId.empty else monsterId

      DND5eGraphQLRepository.live
        .monster(monsterId)
        .map(m =>
          $.modState(
            _.copy(monster =
              m.map(mm =>
                mm.copy(header =
                  mm.header.copy(
                    id = newId,
                    name = if (clone) s"${mm.header.name} (copy)" else mm.header.name,
                    sourceId = if (clone) SourceId.homebrew else mm.header.sourceId
                  )
                )
              )
            )
          )
        )
        .completeWith(_.get)
    }

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      state.monster.fold(EmptyVdom) { monster =>
        val info = monster.info
        Modal
          .onClose(
            (
              _,
              _
            ) =>
              DND5eGraphQLRepository.live
                .upsert(monster.header, monster.jsonInfo)
                .map(monsterId =>
                  modMonster(
                    m => m.copy(header = m.header.copy(id = monsterId)),
                    $.state.flatMap(s => props.onClose(monster))
                  )
                )
                .completeWith(_.get)
          )
          .withKey("monsterEditModel")
          .size(SemanticSIZES.small)
          .open(true)(
            Modal.Header.as("h1")(
              if (props.monsterId == MonsterId.empty) "Create Monster"
              else if (props.cloneMonster) "Copy Monster"
              else "Edit Monster"
            ),
            Modal.Content(
              Form(
                Form.Group(
                  Form.Input
                    .label("Name")
                    .value(monster.header.name)
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String => s
                          case _ => monster.header.name
                        }
                      modMonster(m => m.copy(m.header.copy(name = newVal)))
                    },
                  Form
                    .Select(
                      Alignment.values
                        .map(value => DropdownItemProps().setValue(value.ordinal).setText(value.toString)).toJSArray
                    )
                    .compact(true)
                    .label("Alignment")
                    .value(monster.header.alignment.fold("")(_.ordinal))
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case d: Double => Some(Alignment.fromOrdinal(d.toInt))
                          case s: String => Some(Alignment.fromOrdinal(s.toInt))
                          case _ => monster.header.alignment
                        }
                        modMonster(m => m.copy(m.header.copy(alignment = newVal)))

                    },
                  Form
                    .Select(
                      CreatureSize.values
                        .map(value => DropdownItemProps().setValue(value.ordinal).setText(value.toString)).toJSArray
                    )
                    .compact(true)
                    .label("Creature Size")
                    .value(monster.header.size.ordinal)
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case d: Double => CreatureSize.fromOrdinal(d.toInt)
                          case s: String => CreatureSize.fromOrdinal(s.toInt)
                          case _ => monster.header.size
                        }
                        modMonster(m => m.copy(m.header.copy(size = newVal)))

                    },
                  Form
                    .Select(
                      MonsterType.values
                        .map(value => DropdownItemProps().setValue(value.ordinal).setText(value.toString)).toJSArray
                    )
                    .compact(true)
                    .label("Monster Type")
                    .value(monster.header.monsterType.ordinal)
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case d: Double => MonsterType.fromOrdinal(d.toInt)
                          case s: String => MonsterType.fromOrdinal(s.toInt)
                          case _ => monster.header.monsterType
                        }
                        modMonster(m => m.copy(m.header.copy(monsterType = newVal)))

                    },
                  Form
                    .Select(
                      Biome.values
                        .map(value => DropdownItemProps().setValue(value.ordinal).setText(value.toString)).toJSArray
                    )
                    .compact(true)
                    .label("Biome")
                    .value(monster.header.biome.fold("")(_.ordinal))
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case d: Double => Some(Biome.fromOrdinal(d.toInt))
                          case s: String => Some(Biome.fromOrdinal(s.toInt))
                          case _ => monster.header.biome
                        }
                        modMonster(m => m.copy(m.header.copy(biome = newVal)))

                    }
                ),
                Form.Group(
                  Form.Input
                    .label("AC")
                    .`type`("Number")
                    .min(1)
                    .max(30)
                    .maxLength(5)
                    .style(CSSProperties().set("width", 80.px))
                    .value(monster.header.armorClass)
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case d: Double => d.toInt
                          case s: String => s.toInt
                          case _ => monster.header.armorClass
                        }
                        modMonster(m => m.copy(m.header.copy(armorClass = newVal)))
                    },
                  Form
                    .Select(
                      ChallengeRating.values
                        .map(value => DropdownItemProps().setValue(value.ordinal).setText(value.toString)).toJSArray
                    )
                    .compact(true)
                    .label("Challenge Rating")
                    .value(monster.header.cr.ordinal)
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case d: Double => ChallengeRating.fromOrdinal(d.toInt)
                          case s: String => ChallengeRating.fromOrdinal(s.toInt)
                          case _ => monster.header.cr
                        }
                        modMonster(m => m.copy(m.header.copy(cr = newVal)))

                    },
                  Form.Input
                    .label("XP")
                    .min(1)
                    .`type`("Number")
                    .maxLength(5)
                    .style(CSSProperties().set("width", 100.px))
                    .value(monster.header.xp.toDouble)
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case d: Double => d.toLong
                          case s: String => s.toLong
                          case _ => monster.header.xp
                        }
                        modMonster(m => m.copy(m.header.copy(xp = newVal)))
                    },
                  Form.Input
                    .label("HP")
                    .`type`("Number")
                    .min(1)
                    .maxLength(5)
                    .style(CSSProperties().set("width", 80.px))
                    .value(monster.header.maximumHitPoints)
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case d: Double => d.toInt
                          case s: String => s.toInt
                          case _ => monster.header.maximumHitPoints
                        }
                        modMonster(m => m.copy(m.header.copy(maximumHitPoints = newVal)))
                    },
                  Form.Input
                    .label("Hit Dice")
                    .value(info.hitDice.fold("")(_.roll))
                    .onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal = data.value match {
                          case s: String if s.trim.isEmpty => None
                          case s: String                   => Some(DiceRoll(s))
                          case _ => info.hitDice
                        }
                        modInfo(i => i.copy(hitDice = newVal))
                    }
                ),
                Form.Input
                  .label("Source")
                  .value(monster.header.sourceId.value)
                  .onChange {
                    (
                      _,
                      data
                    ) =>
                      val newVal = data.value match {
                        case s: String => SourceId(s)
                        case _ => monster.header.sourceId
                      }
                      modMonster(m => m.copy(m.header.copy(sourceId = newVal))) // Maybe make it a dropdown with search?
                  },
                Form.Group(
                  EditableComponent(
                    className = "field",
                    view = VdomArray(
                      <.label("Abilities"),
                      Table(
                        Table.Header(
                          Table.Row(
                            Table.HeaderCell(^.width := "16.667%", "Str"),
                            Table.HeaderCell(^.width := "16.666%", "Dex"),
                            Table.HeaderCell(^.width := "16.666%", "Con"),
                            Table.HeaderCell(^.width := "16.666%", "Int"),
                            Table.HeaderCell(^.width := "16.666%", "Wis"),
                            Table.HeaderCell(^.width := "16.666%", "Cha")
                          )
                        ),
                        Table.Body(
                          Table.Row(
                            Table.Cell(s"${info.abilities.strength.overridenValue}"),
                            Table.Cell(s"${info.abilities.dexterity.overridenValue}"),
                            Table.Cell(s"${info.abilities.constitution.overridenValue}"),
                            Table.Cell(s"${info.abilities.intelligence.overridenValue}"),
                            Table.Cell(s"${info.abilities.wisdom.overridenValue}"),
                            Table.Cell(s"${info.abilities.charisma.overridenValue}")
                          ),
                          Table.Row(
                            Table.Cell(s"(${info.abilities.strength.modifierString})"),
                            Table.Cell(s"(${info.abilities.dexterity.modifierString})"),
                            Table.Cell(s"(${info.abilities.constitution.modifierString})"),
                            Table.Cell(s"(${info.abilities.intelligence.modifierString})"),
                            Table.Cell(s"(${info.abilities.wisdom.modifierString})"),
                            Table.Cell(s"(${info.abilities.charisma.modifierString})")
                          )
                        ),
                        Table.Header(
                          Table.Row(Table.HeaderCell(^.colSpan := 6, <.div("Saving Throws"))),
                          Table.Row(
                            Table.HeaderCell("Str"),
                            Table.HeaderCell("Dex"),
                            Table.HeaderCell("Con"),
                            Table.HeaderCell("Int"),
                            Table.HeaderCell("Wis"),
                            Table.HeaderCell("Cha")
                          )
                        ),
                        Table.Body(
                          Table.Row(
                            Table.Cell(s"${info.abilities.strength.savingThrowString(info.proficiencyBonus)}"),
                            Table.Cell(s"${info.abilities.dexterity.savingThrowString(info.proficiencyBonus)}"),
                            Table.Cell(s"${info.abilities.constitution.savingThrowString(info.proficiencyBonus)}"),
                            Table.Cell(s"${info.abilities.intelligence.savingThrowString(info.proficiencyBonus)}"),
                            Table.Cell(s"${info.abilities.wisdom.savingThrowString(info.proficiencyBonus)}"),
                            Table.Cell(s"${info.abilities.charisma.savingThrowString(info.proficiencyBonus)}")
                          )
                        )
                      )
                    ),
                    edit = AbilitiesEditor(
                      info.abilities,
                      onChange = abilities => modInfo(i => i.copy(abilities = abilities))
                    ),
                    title = "Abilities",
                    onEditingModeChange =
                      mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.EditingMode.edit))
                  ),
                  EditableComponent(
                    className = "field",
                    view = VdomArray(
                      <.label("Speeds"),
                      info.speeds.headOption.fold(<.div("Click to add")) { _ =>
                        Table(
                          Table.Header(
                            Table.Row(
                              info.speeds.toSeq
                                .map(sp => Table.HeaderCell(^.key := sp.speedType.toString, sp.speedType.toString))*
                            )
                          ),
                          Table.Body(
                            Table.Row(
                              info.speeds.toSeq
                                .map(sp => Table.Cell(^.key := sp.speedType.toString, sp.value.toString))*
                            )
                          )
                        )
                      }
                    ),
                    edit = SpeedsEditor(
                      info.speeds.toList,
                      speeds => modInfo(i => i.copy(speeds = speeds))
                    ),
                    title = "Speeds",
                    onEditingModeChange =
                      mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.EditingMode.edit))
                  )
                ),
                <.div(
                  ^.className := "field",
                  <.label("Notes"),
                  ReactQuill
                    .defaultValue(info.notes)
                    .onChange(
                      (
                        newValue,
                        _,
                        _,
                        _
                      ) => modInfo(i => i.copy(notes = newValue))
                    )
                )
              )
            )
          )
      }
    }

  }
  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("MonsterEditor")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ =>
      if ($.props.monsterId == MonsterId.empty)
        $.modState(s => s.copy(Some(Monster.homeBrew)))
      else
        $.backend.loadMonster($.props.monsterId, $.props.cloneMonster)
    )
    .shouldComponentUpdatePure($ => ! $.nextState.dialogOpen)
    .build

  def apply(
    monsterId:    MonsterId,
    cloneMonster: Boolean = false,
    onClose:      Monster => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(monsterId, cloneMonster, onClose))

}
