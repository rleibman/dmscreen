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
import dmscreen.components.EditableComponent.EditingMode
import dmscreen.components.{EditableComponent, EditableText}
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{CtorType, *}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticSIZES
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import zio.json.*

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object NPCEditComponent {

  case class State(
    npc:         NonPlayerCharacter,
    editingMode: EditableComponent.EditingMode = EditableComponent.EditingMode.view
  )
  case class Props(
    npc:                 NonPlayerCharacter,
    onEditingModeChange: EditableComponent.EditingMode => Callback,
    onChange:            NonPlayerCharacter => Callback,
    onDelete:            NonPlayerCharacter => Callback,
    onComponentClose:    NonPlayerCharacter => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def doModeChange(editingMode: EditableComponent.EditingMode): Callback =
      $.modState(_.copy(editingMode = editingMode), $.props.flatMap(_.onEditingModeChange(editingMode)))

    def profStr(proficiencyLevel: ProficiencyLevel): String =
      proficiencyLevel match {
        case ProficiencyLevel.none       => ""
        case ProficiencyLevel.half       => "½"
        case ProficiencyLevel.proficient => "*"
        case ProficiencyLevel.expert     => "**"
      }

    def render(
      props: Props,
      state: State
    ): VdomElement = {
      DMScreenState.ctx.consume { dmScreenState =>
        val campaignState = dmScreenState.campaignState
          .map(_.asInstanceOf[DND5eCampaignState]).getOrElse(throw RuntimeException("No campaign"))

        {
          def modNPC(npc: NonPlayerCharacter): Callback = {
            $.modState(_.copy(npc = npc), props.onChange(npc))
          }
          def modNPCInfo(info: NonPlayerCharacterInfo): Callback = {
            modNPC(state.npc.copy(jsonInfo = info.toJsonAST.toOption.get))
          }

          val info: NonPlayerCharacterInfo = state.npc.info
          def allBackgrounds =
            (campaignState.backgrounds ++ info.background.fold(Seq.empty)(bk =>
              Seq(
                campaignState.backgrounds
                  .find(_.name.equalsIgnoreCase(bk.name)).getOrElse(Background(name = bk.name))
              )
            ))
              .sortBy(_.name)
              .distinctBy(_.name)
          def allRaces =
            (campaignState.races ++ Seq(info.race))
              .sortBy(_.name)
              .distinctBy(_.name)

          <.div(
            Container.className("characterCard")(
              Button("Delete")
                .title("Delete this character")
                .size(SemanticSIZES.tiny).compact(true).onClick(
                  (
                    _,
                    _
                  ) =>
                    _root_.components.Confirm.confirm(
                      question = "Are you 100% sure you want to delete this character?",
                      onConfirm = props.onDelete(state.npc)
                    )
                ),
              <.div(
                ^.className := "characterHeader",
                <.h2(
                  EditableText(
                    state.npc.header.name,
                    onChange = str =>
                      modNPC(
                        state.npc.copy(header = state.npc.header.copy(name = str))
                      )
                  )
                )
              ),
              <.div(
                ^.className := "characterDetails",
                ^.minHeight := 180.px,
                <.table(
                  ^.width := "auto",
                  <.tbody(
                    <.tr(
                      <.td(
                        ^.colSpan := 2,
                        EditableComponent(
                          view = info.classes.headOption.fold(<.div("Click to add Classes"))(_ =>
                            info.classes.zipWithIndex.map {
                              (
                                cl,
                                i
                              ) =>
                                <.div(
                                  ^.key := s"characterClass_$i",
                                  s"${cl.characterClass.name} ${cl.subclass.fold("")(sc => s"(${sc.name})")} ${cl.level}"
                                )
                            }.toVdomArray
                          ),
                          edit = CharacterClassEditor(
                            info.classes,
                            onChange = classes => modNPCInfo(info.copy(classes = classes))
                          ),
                          title = "Classes/Subclasses/Levels",
                          onEditingModeChange = doModeChange
                        )
                      )
                    ),
                    <.tr(
                      <.th("Race"),
                      <.td(
                        Dropdown
                          .placeholder("Choose")
                          .clearable(true)
                          .compact(true)
                          .allowAdditions(true)
                          .search(true)
                          .options(
                            allRaces
                              .map(race =>
                                DropdownItemProps()
                                  .setValue(race.name)
                                  .setText(race.name)
                              ).toJSArray
                          )
                          .onChange(
                            (
                              _,
                              changedData
                            ) =>
                              modNPCInfo(
                                info.copy(race = changedData.value match {
                                  case s: String if s.isEmpty => info.race
                                  case s: String =>
                                    allRaces.find(_.name == s).getOrElse(Race(s))
                                  case _ => throw RuntimeException("Unexpected value")
                                })
                              )
                          )
                          .value(info.race.name)
                      )
                    ),
                    <.tr(
                      <.th("Size"),
                      <.td(
                        Dropdown
                          .placeholder("Choose").compact(true).options(
                            CreatureSize.values
                              .map(s =>
                                DropdownItemProps()
                                  .setValue(s.ordinal)
                                  .setText(s.toString.capitalize)
                              ).toJSArray
                          ).onChange {
                            (
                              _,
                              changedData
                            ) =>
                              val newVal = changedData.value match {
                                case s: String => s.toInt
                                case s: Double => s.toInt
                                case _ => throw RuntimeException("Unexpected value")
                              }

                              modNPCInfo(info.copy(size = CreatureSize.fromOrdinal(newVal)))
                          }.value(info.size.ordinal)
                      )
                    ),
                    <.tr(
                      <.th("Background"),
                      <.td(
                        Dropdown
                          .placeholder("Choose")
                          .clearable(true)
                          .compact(true)
                          .allowAdditions(true)
                          .search(true)
                          .options(
                            allBackgrounds
                              .map(background =>
                                DropdownItemProps()
                                  .setValue(background.name)
                                  .setText(background.name)
                              ).toJSArray
                          )
                          .onChange(
                            (
                              _,
                              changedData
                            ) =>
                              modNPCInfo(
                                info.copy(background = changedData.value match {
                                  case s: String if s.isEmpty => None
                                  case s: String =>
                                    allBackgrounds.find(_.name == s).orElse(Some(Background(s)))
                                  case _ => throw RuntimeException("Unexpected value")
                                })
                              )
                          )
                          .value(info.background.fold("")(_.name))
                      )
                    ),
                    <.tr(
                      <.th("AC"),
                      <.td(
                        EditableComponent(
                          view = <.div(info.armorClass),
                          edit = ArmorClassEditor(
                            info.armorClass,
                            onChange = armorClass => modNPCInfo(info.copy(armorClass = armorClass))
                          ),
                          title = "Armor Class",
                          onEditingModeChange = doModeChange
                        )
                      )
                    ),
                    <.tr(
                      <.th("Proficiency Bonus"),
                      <.td(info.proficiencyBonusString)
                    ),
                    <.tr(
                      <.th("Spell DC"),
                      <.td(info.spellDC.map(s => s"${s._1.abilityType.short} (${s._2})").mkString(" / "))
                    ).when(info.spellDC.nonEmpty),
                    <.tr(
                      <.th("Initiative"),
                      <.td(info.initiativeBonusString)
                    )
                  )
                )
              ),
              <.div(
                ^.className       := "characterDetails",
                ^.backgroundColor := info.health.lifeColor(),
                EditableComponent(
                  view = <.table(
                    <.thead(
                      <.tr(<.th("HP"), <.th("Temp HP"))
                    ),
                    <.tbody(
                      <.tr(
                        <.td(
                          s"${info.health.currentHitPoints}/${info.health.currentMax}",
                          " (stabilized)".when(info.health.currentHitPoints <= 0 && info.health.deathSave.isStabilized)
                        ),
                        <.td(info.health.temporaryHitPoints.toString)
                      )
                    )
                  ),
                  edit = HealthEditor(
                    info.health,
                    onChange = hitPoints => modNPCInfo(info.copy(health = hitPoints))
                  ),
                  title = "Hit Points",
                  onEditingModeChange = doModeChange
                )
              ),
              <.div(
                ^.className := "characterDetails",
                EditableComponent(
                  view = <.table(
                    <.thead(
                      <.tr(
                        <.th(^.width := "16.667%", "Str"),
                        <.th(^.width := "16.666%", "Dex"),
                        <.th(^.width := "16.666%", "Con"),
                        <.th(^.width := "16.666%", "Int"),
                        <.th(^.width := "16.666%", "Wis"),
                        <.th(^.width := "16.666%", "Cha")
                      )
                    ),
                    <.tbody(
                      <.tr(
                        <.td(s"${info.abilities.strength.overridenValue}"),
                        <.td(s"${info.abilities.dexterity.overridenValue}"),
                        <.td(s"${info.abilities.constitution.overridenValue}"),
                        <.td(s"${info.abilities.intelligence.overridenValue}"),
                        <.td(s"${info.abilities.wisdom.overridenValue}"),
                        <.td(s"${info.abilities.charisma.overridenValue}")
                      ),
                      <.tr(
                        <.td(s"(${info.abilities.strength.modifierString})"),
                        <.td(s"(${info.abilities.dexterity.modifierString})"),
                        <.td(s"(${info.abilities.constitution.modifierString})"),
                        <.td(s"(${info.abilities.intelligence.modifierString})"),
                        <.td(s"(${info.abilities.wisdom.modifierString})"),
                        <.td(s"(${info.abilities.charisma.modifierString})")
                      )
                    ),
                    <.thead(
                      <.tr(<.th(^.colSpan := 6, <.div("Saving Throws"))),
                      <.tr(
                        <.th("Str"),
                        <.th("Dex"),
                        <.th("Con"),
                        <.th("Int"),
                        <.th("Wis"),
                        <.th("Cha")
                      )
                    ),
                    <.tbody(
                      <.tr(
                        <.td(s"${info.abilities.strength.savingThrowString(info.proficiencyBonus)}"),
                        <.td(s"${info.abilities.dexterity.savingThrowString(info.proficiencyBonus)}"),
                        <.td(s"${info.abilities.constitution.savingThrowString(info.proficiencyBonus)}"),
                        <.td(s"${info.abilities.intelligence.savingThrowString(info.proficiencyBonus)}"),
                        <.td(s"${info.abilities.wisdom.savingThrowString(info.proficiencyBonus)}"),
                        <.td(s"${info.abilities.charisma.savingThrowString(info.proficiencyBonus)}")
                      )
                    )
                  ),
                  edit = AbilitiesEditor(
                    info.abilities,
                    onChange = abilities => modNPCInfo(info.copy(abilities = abilities))
                  ),
                  title = "Abilities",
                  onEditingModeChange = doModeChange
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Passive"),
                <.table(
                  <.thead(
                    <.tr(
                      <.th("Perception"),
                      <.th("Investigation"),
                      <.th("Insight")
                    )
                  ),
                  <.tbody(
                    <.tr(
                      <.td(info.passivePerception),
                      <.td(info.passiveInvestigation),
                      <.td(info.passiveInsight)
                    )
                  )
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Conditions"),
                EditableComponent(
                  view = <.span(
                    info.conditions.headOption
                      .fold("Click to change")(_ => info.conditions.map(_.toString.capitalize).mkString(", "))
                  ),
                  edit = ConditionsEditor(
                    info.conditions,
                    onChange = conditions => modNPCInfo(info.copy(conditions = conditions))
                  ),
                  title = "Conditions",
                  onEditingModeChange = doModeChange
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Speed"),
                EditableComponent(
                  edit = SpeedsEditor(
                    info.speeds,
                    speeds => modNPCInfo(info.copy(speeds = speeds))
                  ),
                  view = info.speeds.headOption.fold(<.div("Click to add")) { _ =>
                    <.table(
                      <.thead(
                        <.tr(
                          info.speeds.map(sp => <.th(^.key := sp.speedType.toString, sp.speedType.toString)).toVdomArray
                        )
                      ),
                      <.tbody(
                        <.tr(
                          info.speeds.map(sp => <.th(^.key := sp.speedType.toString, sp.value.toString)).toVdomArray
                        )
                      )
                    )
                  },
                  title = "Speeds",
                  onEditingModeChange = doModeChange
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Skills"),
                EditableComponent(
                  view = <.table(
                    ^.minHeight := 330.px,
                    <.tbody(
                      <.tr(
                        <.th(^.width := 23.px, s"${profStr(info.skills.acrobatics.proficiencyLevel)}Acrobatics"),
                        <.td(^.width := "50%", info.skills.acrobatics.modifierString(info.abilities)),
                        <.th(^.width := 23.px, s"${profStr(info.skills.medicine.proficiencyLevel)}Medicine"),
                        <.td(^.width := "50%", info.skills.medicine.modifierString(info.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(info.skills.animalHandling.proficiencyLevel)}Animal H."),
                        <.td(info.skills.animalHandling.modifierString(info.abilities)),
                        <.th(s"${profStr(info.skills.nature.proficiencyLevel)}Nature"),
                        <.td(info.skills.nature.modifierString(info.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(info.skills.arcana.proficiencyLevel)}Arcana"),
                        <.td(info.skills.arcana.modifierString(info.abilities)),
                        <.th(s"${profStr(info.skills.perception.proficiencyLevel)}Perception"),
                        <.td(info.skills.perception.modifierString(info.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(info.skills.athletics.proficiencyLevel)}Athletics"),
                        <.td(info.skills.athletics.modifierString(info.abilities)),
                        <.th(s"${profStr(info.skills.performance.proficiencyLevel)}Perf."),
                        <.td(info.skills.performance.modifierString(info.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(info.skills.deception.proficiencyLevel)}Deception"),
                        <.td(info.skills.deception.modifierString(info.abilities)),
                        <.th(s"${profStr(info.skills.persuasion.proficiencyLevel)}Persuasion"),
                        <.td(info.skills.persuasion.modifierString(info.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(info.skills.history.proficiencyLevel)}History"),
                        <.td(info.skills.history.modifierString(info.abilities)),
                        <.th(s"${profStr(info.skills.religion.proficiencyLevel)}Religion"),
                        <.td(info.skills.religion.modifierString(info.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(info.skills.insight.proficiencyLevel)}Insight"),
                        <.td(info.skills.insight.modifierString(info.abilities)),
                        <.th(s"${profStr(info.skills.sleightOfHand.proficiencyLevel)}Sleight of H."),
                        <.td(info.skills.sleightOfHand.modifierString(info.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(info.skills.intimidation.proficiencyLevel)}Intim."),
                        <.td(info.skills.intimidation.modifierString(info.abilities)),
                        <.th(s"${profStr(info.skills.stealth.proficiencyLevel)}Stealth"),
                        <.td(info.skills.stealth.modifierString(info.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(info.skills.investigation.proficiencyLevel)}Invest."),
                        <.td(info.skills.investigation.modifierString(info.abilities)),
                        <.th(s"${profStr(info.skills.survival.proficiencyLevel)}Survival"),
                        <.td(info.skills.survival.modifierString(info.abilities))
                      )
                    )
                  ),
                  edit = SkillsEditor(
                    info.skills,
                    info.abilities,
                    onChange = skills => modNPCInfo(info.copy(skills = skills))
                  ),
                  title = "Skills",
                  onEditingModeChange = doModeChange
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Languages"),
                EditableComponent(
                  view = info.languages.headOption.fold("Click to add")(_ => info.languages.map(_.name).mkString(", ")),
                  edit = LanguageEditor(info.languages, languages => modNPCInfo(info.copy(languages = languages))),
                  title = "Languages",
                  onEditingModeChange = doModeChange
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Feats"),
                EditableComponent(
                  view = info.feats.headOption.fold("Click to add")(_ => info.feats.map(_.name).mkString(", ")),
                  edit = FeatsEditor(info.feats, feats => modNPCInfo(info.copy(feats = feats))),
                  title = "Feats",
                  onEditingModeChange = doModeChange
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Senses"),
                EditableComponent(
                  view = <.div(info.senses.headOption.fold("Click to add") { _ =>
                    info.senses.map(s => s"${s.sense} ${s.range}").mkString(", ")
                  }),
                  edit = SensesEditor(info.senses, senses => modNPCInfo(info.copy(senses = senses))),
                  title = "Senses",
                  onEditingModeChange = doModeChange
                )
              ),
              <.div(
                ^.className := "notesSection",
                ^.height    := 700.px,
                <.div(^.className := "sectionTitle", "Notes"),
                EditableComponent(
                  view = <.div(
                    ^.dangerouslySetInnerHtml := info.notes.trim.headOption.fold("Click here to add")(_ => info.notes)
                  ),
                  edit = {
                    CharacterNotesEditor(
                      info.notes,
                      onChange = (
                        notes,
                        personalityTraits,
                        ideals,
                        bonds,
                        flaws
                      ) => modNPCInfo(info.copy(notes = notes))
                    )
                  },
                  title = "Notes",
                  onEditingModeChange = doModeChange
                )
              )
            )
          )
        }
      }
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] =
    ScalaComponent
      .builder[Props]("NonPlayerCharacterComponent")
      .initialStateFromProps(p => State(p.npc))
      .renderBackend[Backend]
      .shouldComponentUpdatePure($ => $.nextState.editingMode != EditingMode.edit) // Don't update while we have a dialog open
      .componentWillUnmount($ => $.props.onComponentClose($.state.npc))
      .build

  def apply(
    npc:                 NonPlayerCharacter,
    onEditingModeChange: EditableComponent.EditingMode => Callback,
    onChange:            NonPlayerCharacter => Callback,
    onDelete:            NonPlayerCharacter => Callback,
    onComponentClose:    NonPlayerCharacter => Callback
  ): Unmounted[Props, State, Backend] = {
    // Note the "withKey" here, this is to make sure that the component is properly updated when the key changes
    component.withKey(npc.header.id.value.toString)(
      Props(
        npc = npc,
        onEditingModeChange = onEditingModeChange,
        onChange = onChange,
        onDelete = onDelete,
        onComponentClose = onComponentClose
      )
    )
  }

}
