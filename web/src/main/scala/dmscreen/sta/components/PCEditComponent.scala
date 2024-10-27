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

package dmscreen.sta.components

import dmscreen.components.EditableComponent.EditingMode
import dmscreen.components.{EditableComponent, EditableText}
import dmscreen.sta.{*, given}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{CtorType, *}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticSIZES, SemanticWIDTHS}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import zio.json.*

object PCEditComponent {

  case class State(
    character:   Character,
    editingMode: EditableComponent.EditingMode = EditableComponent.EditingMode.view
  )
  case class Props(
    character:           Character,
    onEditingModeChange: EditableComponent.EditingMode => Callback,
    onChange:            Character => Callback,
    onDelete:            Character => Callback,
    onComponentClose:    Character => Callback
  )
  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ) = {
      val header = state.character.header
      val info = state.character.info

      <.div(
        Container(
          <.h1(^.className := "lcars-text-bar", <.div(s"${header.name.toString} (${header.playerName.toString})")), // TODO add rank
          <.table(
            ^.width         := 100.pct,
            ^.borderSpacing := 5.px,
            ^.className     := "lcars-character-table",
            <.tbody(
              <.tr(
                <.th(^.width := 15.pct, "Pronouns"),
                <.td(^.width := 35.pct, info.pronouns.toString),
                <.th(^.width := 15.pct, "Roles"),
                <.td(^.width := 35.pct, info.roles.map(_.toString).mkString(", "))
              ),
              <.tr(
                <.th("Rank"),
                <.td(info.rank.toString),
                <.th("Lineage"),
                <.td(info.lineage.map(_.lineageType.name).mkString(", "))
              ),
              <.tr(<.th("Traits"), <.td(^.colSpan := 3, info.traits.map(_.name).mkString(",")))
            )
          ),
          Grid(
            Grid.Column.width(SemanticWIDTHS.`8`)(
              <.h2(^.className := "lcars-text-bar", <.div("Attributes")),
              info.attributes.toString,
              <.h2(^.className := "lcars-text-bar", <.div("Departments")),
              info.departments.toString
            ),
            Grid.Column.width(SemanticWIDTHS.`8`)(
              Grid(
                Grid.Column.width(SemanticWIDTHS.`8`)(
                  <.h2(^.className := "lcars-text-bar", <.div("Stress")),
                  info.stress.toString
                ),
                Grid.Column.width(SemanticWIDTHS.`8`)(
                  <.h2(^.className := "lcars-text-bar", <.div("Focuses")),
                  info.focuses.toString
                )
              ),
              <.h2(^.className := "lcars-text-bar", <.div("Values")),
              info.values.toString,
              <.h2(^.className := "lcars-text-bar", <.div("Weapons")),
              info.weapons.toString
            )
          ),
          info.determination.toString,
          info.organization.toString,
          info.jobAssignment.toString,
          info.reputation.toString,
          info.environment.toString,
          info.upbringing.toString,
          info.careerPath.toString,
          info.experience.toString,
          info.careerEvents.toString,
          info.pastimes.toString,
          info.attacks.toString,
          info.speciesAbilities.toString,
          info.talents.toString,
          info.specialRules.toString,
          info.inventoryItems.toString,
          info.reprimands.toString,
          info.assignedShip.toString,
          info.age.toString,
          info.notes.toString
        )
      )
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] =
    ScalaComponent
      .builder[Props]("CharacterComponent")
      .initialStateFromProps(p => State(p.character))
      .renderBackend[Backend]
      .shouldComponentUpdatePure($ => $.nextState.editingMode != EditingMode.edit) // Don't update while we have a dialog open
      .componentWillUnmount($ => $.props.onComponentClose($.state.character))
      .build

  def apply(
    character:           Character,
    onEditingModeChange: EditableComponent.EditingMode => Callback,
    onChange:            Character => Callback,
    onDelete:            Character => Callback,
    onComponentClose:    Character => Callback
  ): Unmounted[Props, State, Backend] = {
    // Note the "withKey" here, this is to make sure that the component is properly updated when the key changes
    component.withKey(character.header.id.value.toString)(
      Props(
        character = character,
        onEditingModeChange = onEditingModeChange,
        onChange = onChange,
        onDelete = onDelete,
        onComponentClose = onComponentClose
      )
    )
  }

}
