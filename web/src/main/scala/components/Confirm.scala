/*
 * Copyright 2020 Roberto Leibman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package components

import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, Ref, ScalaComponent}
import net.leibman.chuti.semanticUiReact.components.{Confirm => SuiConfirm}

object Confirm {

  case class ConfirmState(
    question:    String = "",
    open:        Boolean = false,
    onConfirm:   Callback = Callback.empty,
    onCancel:    Option[Callback] = None,
    cancelText:  String = "Cancel",
    confirmText: String = "Ok",
    header:      Option[String] = None
  )

  class Backend($ : BackendScope[Unit, ConfirmState]) {

    def render(state: ConfirmState): VdomNode =
      <.div(
        SuiConfirm()
          .open(state.open)
          .content(state.question)
          .cancelButton(state.cancelText)
          .confirmButton(state.confirmText)
          .header(state.header.getOrElse("").toString)
          .onConfirm(
            (
              _,
              _
            ) => $.modState(s => s.copy(open = false), state.onConfirm)
          )
          .onCancel {
            (
              _,
              _
            ) =>
              $.modState(s => s.copy(open = false), state.onCancel.getOrElse(Callback.empty))
          }()
      )

    def confirm(
      question:    String,
      onConfirm:   Callback,
      onCancel:    Option[Callback] = None,
      cancelText:  String = "Cancel",
      confirmText: String = "Ok",
      header:      Option[String] = None
    ): Callback =
      $.setState(
        ConfirmState(question, open = true, onConfirm, onCancel, cancelText, confirmText, header)
      )

  }

  private val component =
    ScalaComponent
      .builder[Unit]("LeibmanConfirm")
      .initialState(ConfirmState())
      .renderBackend[Backend]
      .build

  private val ref = Ref.toScalaComponent(component)

  def render(): TagMod = ref.component()

  def confirm(
    question:    String,
    onConfirm:   Callback,
    onCancel:    Option[Callback] = None,
    cancelText:  String = "Cancel",
    confirmText: String = "Ok",
    header:      Option[String] = None
  ): Callback =
    ref.get
      .flatMap(
        _.fold(Callback.empty)(_.backend.confirm(question, onConfirm, onCancel, cancelText, confirmText, header))
      )

}
