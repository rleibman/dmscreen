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

package components

import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, Ref, ScalaComponent}
import net.leibman.dmscreen.semanticUiReact.components.Confirm as SuiConfirm

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
