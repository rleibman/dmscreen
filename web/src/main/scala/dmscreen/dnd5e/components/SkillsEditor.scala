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

import dmscreen.dnd5e.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

import scala.scalajs.js.JSConverters.*

object SkillsEditor {

  case class State(
    skills: Skills
  )

  case class Props(
    skills:    Skills,
    abilities: Abilities,
    onChange:  Skills => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      def proficiencyEditor(
        skill: Skill,
        fn:    Skill => Skills
      ) = {
        Dropdown
          .clearable(false)
          .compact(true)
          .allowAdditions(false)
          .selection(true)
          .search(false)
          .onChange {
            (
              _,
              changedData
            ) =>
              val newVal: ProficiencyLevel = changedData.value match {
                case str: String =>
                  ProficiencyLevel.values
                    .find(_.toString.equalsIgnoreCase(str))
                    .getOrElse(skill.proficiencyLevel)
                case _ => ProficiencyLevel.none
              }
              $.modState(
                s => s.copy(skills = fn(skill.copy(proficiencyLevel = newVal))),
                $.state.flatMap(s2 => props.onChange(s2.skills))
              )
          }
          .options(
            ProficiencyLevel.values.map { l =>
              val s = l.toString.capitalize
              DropdownItemProps()
                .setValue(s)
                .setText(s)
            }.toJSArray
          )
          .value(skill.proficiencyLevel.toString.capitalize)

      }

      Table
        .inverted(DND5eUI.tableInverted)
        .color(DND5eUI.tableColor)(
          Table.Header(
            Table.Row(
              Table.HeaderCell("Skill"),
              Table.HeaderCell("Proficiency"),
              Table.HeaderCell("Modifier"),
              Table.HeaderCell("Skill"),
              Table.HeaderCell("Proficiency"),
              Table.HeaderCell("Modifier")
            )
          ),
          Table.Body(
            Table.Row(
              Table.Cell("Acrobatics"),
              Table.Cell(proficiencyEditor(state.skills.acrobatics, skill => state.skills.copy(acrobatics = skill))),
              Table.Cell(state.skills.acrobatics.modifierString(props.abilities)),
              Table.Cell("Medicine"),
              Table.Cell(proficiencyEditor(state.skills.medicine, skill => state.skills.copy(medicine = skill))),
              Table.Cell(state.skills.medicine.modifierString(props.abilities))
            ),
            Table.Row(
              Table.Cell("Animal Handling"),
              Table.Cell(
                proficiencyEditor(state.skills.animalHandling, skill => state.skills.copy(animalHandling = skill))
              ),
              Table.Cell(state.skills.animalHandling.modifierString(props.abilities)),
              Table.Cell("Nature"),
              Table.Cell(proficiencyEditor(state.skills.nature, skill => state.skills.copy(nature = skill))),
              Table.Cell(state.skills.nature.modifierString(props.abilities))
            ),
            Table.Row(
              Table.Cell("Arcana"),
              Table.Cell(proficiencyEditor(state.skills.arcana, skill => state.skills.copy(arcana = skill))),
              Table.Cell(state.skills.arcana.modifierString(props.abilities)),
              Table.Cell("Perception"),
              Table.Cell(proficiencyEditor(state.skills.perception, skill => state.skills.copy(perception = skill))),
              Table.Cell(state.skills.perception.modifierString(props.abilities))
            ),
            Table.Row(
              Table.Cell("Athletics"),
              Table.Cell(proficiencyEditor(state.skills.athletics, skill => state.skills.copy(athletics = skill))),
              Table.Cell(state.skills.athletics.modifierString(props.abilities)),
              Table.Cell("Performance"),
              Table.Cell(proficiencyEditor(state.skills.performance, skill => state.skills.copy(performance = skill))),
              Table.Cell(state.skills.performance.modifierString(props.abilities))
            ),
            Table.Row(
              Table.Cell("Deception"),
              Table.Cell(proficiencyEditor(state.skills.deception, skill => state.skills.copy(deception = skill))),
              Table.Cell(state.skills.deception.modifierString(props.abilities)),
              Table.Cell("Persuasion"),
              Table.Cell(proficiencyEditor(state.skills.persuasion, skill => state.skills.copy(persuasion = skill))),
              Table.Cell(state.skills.persuasion.modifierString(props.abilities))
            ),
            Table.Row(
              Table.Cell("History"),
              Table.Cell(proficiencyEditor(state.skills.history, skill => state.skills.copy(history = skill))),
              Table.Cell(state.skills.history.modifierString(props.abilities)),
              Table.Cell("Religion"),
              Table.Cell(proficiencyEditor(state.skills.religion, skill => state.skills.copy(religion = skill))),
              Table.Cell(state.skills.religion.modifierString(props.abilities))
            ),
            Table.Row(
              Table.Cell("Insight"),
              Table.Cell(proficiencyEditor(state.skills.insight, skill => state.skills.copy(insight = skill))),
              Table.Cell(state.skills.insight.modifierString(props.abilities)),
              Table.Cell("Sleigh of Hand"),
              Table.Cell(
                proficiencyEditor(state.skills.sleightOfHand, skill => state.skills.copy(sleightOfHand = skill))
              ),
              Table.Cell(state.skills.sleightOfHand.modifierString(props.abilities))
            ),
            Table.Row(
              Table.Cell("Intimidation"),
              Table
                .Cell(proficiencyEditor(state.skills.intimidation, skill => state.skills.copy(intimidation = skill))),
              Table.Cell(state.skills.intimidation.modifierString(props.abilities)),
              Table.Cell("Stealth"),
              Table.Cell(proficiencyEditor(state.skills.stealth, skill => state.skills.copy(stealth = skill))),
              Table.Cell(state.skills.stealth.modifierString(props.abilities))
            ),
            Table.Row(
              Table.Cell("Investigation"),
              Table.Cell(
                proficiencyEditor(state.skills.investigation, skill => state.skills.copy(investigation = skill))
              ),
              Table.Cell(state.skills.investigation.modifierString(props.abilities)),
              Table.Cell("Survival"),
              Table.Cell(proficiencyEditor(state.skills.survival, skill => state.skills.copy(survival = skill))),
              Table.Cell(state.skills.survival.modifierString(props.abilities))
            )
          )
        )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props).skills)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("SkillsDialog")
    .initialStateFromProps(p => State(p.skills))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    skills:    Skills,
    abilities: Abilities,
    onChange:  Skills => Callback
  ): Unmounted[Props, State, Backend] = component(Props(skills, abilities, onChange))

}
