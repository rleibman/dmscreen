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
import dmscreen.dnd5e.components.*
import dmscreen.dnd5e.components.EditableComponent.Mode
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.{^, *}
import japgolly.scalajs.react.{Callback, CtorType, *}
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

def DeathSaveComponent(
  deathSave: DeathSave,
  onChange:  DeathSave => Callback = _ => Callback.empty
) = {
  case class DeathSaveComponentProps(
    deathSave: DeathSave,
    onChange:  DeathSave => Callback
  )

  ScalaFnComponent
    .withHooks[DeathSaveComponentProps]
    .render { props =>
      <.table(
        ^.className := "deathSave",
        <.tr(<.th("Fails")),
        <.tr(
          <.td(
            ^.className := "deathSaveFail",
            <.input(
              ^.`type`   := "checkbox",
              ^.checked  := props.deathSave.fails > 0,
              ^.readOnly := props.deathSave.fails != 0,
              ^.onChange ==> ((e: ReactEventFromInput) =>
                props.onChange(
                  props.deathSave.copy(fails = props.deathSave.fails + (if (e.currentTarget.checked) 1 else -1))
                )
              )
            ),
            <.input(
              ^.`type`   := "checkbox",
              ^.checked  := props.deathSave.fails > 1,
              ^.readOnly := props.deathSave.fails != 1,
              ^.onChange ==> ((e: ReactEventFromInput) =>
                props.onChange(
                  props.deathSave.copy(fails = props.deathSave.fails + (if (e.currentTarget.checked) 1 else -1))
                )
              )
            ),
            <.input(
              ^.`type`   := "checkbox",
              ^.checked  := props.deathSave.fails > 2,
              ^.readOnly := props.deathSave.fails != 2,
              ^.onChange ==> ((e: ReactEventFromInput) =>
                props.onChange(
                  props.deathSave.copy(fails = props.deathSave.fails + (if (e.currentTarget.checked) 1 else -1))
                )
              )
            )
          )
        ),
        <.tr(<.th("Successes")),
        <.tr(
          <.td(
            ^.className := "deathSaveSucceed",
            <.input(
              ^.`type`   := "checkbox",
              ^.checked  := props.deathSave.successes > 0,
              ^.readOnly := props.deathSave.successes != 0,
              ^.onChange ==> ((e: ReactEventFromInput) =>
                props.onChange(
                  props.deathSave
                    .copy(successes = props.deathSave.successes + (if (e.currentTarget.checked) 1 else -1))
                )
              )
            ),
            <.input(
              ^.`type`   := "checkbox",
              ^.checked  := props.deathSave.successes > 1,
              ^.readOnly := props.deathSave.successes != 1,
              ^.onChange ==> ((e: ReactEventFromInput) =>
                props.onChange(
                  props.deathSave
                    .copy(successes = props.deathSave.successes + (if (e.currentTarget.checked) 1 else -1))
                )
              )
            ),
            <.input(
              ^.`type`   := "checkbox",
              ^.checked  := props.deathSave.successes > 2,
              ^.readOnly := props.deathSave.successes != 2,
              ^.onChange ==> ((e: ReactEventFromInput) =>
                props.onChange(
                  props.deathSave
                    .copy(successes = props.deathSave.successes + (if (e.currentTarget.checked) 1 else -1))
                )
              )
            )
          )
        )
      )

    }.apply(DeathSaveComponentProps(deathSave, onChange))
}
