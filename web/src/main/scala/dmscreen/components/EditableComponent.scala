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

package dmscreen.components

import dmscreen.dnd5e.components.ConditionsEditor.{Props, State}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

object EditableComponent {

  enum EditingMode {

    case view, edit

  }

  case class State(editingMode: EditingMode = EditingMode.view)
  case class Props(
    className:       String = "",
    viewComponent:   VdomNode,
    editComponent:   VdomNode,
    actionComponent: VdomNode = EmptyVdom,
    modalTitle:      String = "",
    onModeChange:    EditingMode => Callback = _ => Callback.empty
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      state.editingMode match {
        case EditingMode.view =>
          <.div(
            ^.className := props.className,
            ^.onClick --> (props.onModeChange(EditingMode.edit) >> $.modState(_.copy(editingMode = EditingMode.edit))),
            props.viewComponent
          )
        case EditingMode.edit =>
          Modal
            .open(state.editingMode == EditingMode.edit)
            .size(semanticUiReactStrings.small)
            .closeIcon(true)
            .onClose(
              (
                _,
                _
              ) => props.onModeChange(EditingMode.view) >> $.modState(_.copy(editingMode = EditingMode.view))
            )(
              ModalHeader(props.modalTitle).when(props.modalTitle.nonEmpty),
              ModalContent(
                props.editComponent
              ),
              ModalActions(props.actionComponent).when(props.actionComponent != EmptyVdom)
            )
      }
    }

  }

  private def component =
    ScalaComponent
      .builder[Props]("EditableComponent")
      .initialState(State()) // Props have nothing to do with state in this component
      .renderBackend[Backend]
      .shouldComponentUpdatePure($ => $.nextState.editingMode != $.currentState.editingMode) // Only update if the view mode has changed
      .build

  def apply(
    className:           String = "",
    view:                VdomNode,
    edit:                VdomNode,
    actionComponent:     VdomNode = EmptyVdom,
    title:               String = "",
    onEditingModeChange: EditingMode => Callback = _ => Callback.empty
  ) = component(Props(className, view, edit, actionComponent, title, onEditingModeChange))

}
