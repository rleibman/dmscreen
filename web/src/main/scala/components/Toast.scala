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
import net.leibman.dmscreen.semanticUiReact.components.Icon
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticICONS

import scala.concurrent.duration.{Duration, *}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.timers.*

enum HorizontalPosition {

  case right, left

}
enum VerticalPosition {

  case top, bottom

}

object Toast {

  import HorizontalPosition.*
  import VerticalPosition.*

  private case class Toast(
    icon:      UndefOr[SemanticICONS] = js.undefined,
    className: String = "toast",
    message:   VdomNode = "",
    position:  (VerticalPosition, HorizontalPosition) = (top, right),
    onClose:   () => Callback = { () => Callback.empty }
  )

  private[Toast] case class ToastState(toasts: Seq[Toast] = Seq.empty)

  class Backend($ : BackendScope[Unit, ToastState]) {

    def toastMsg(
      message:   VdomNode,
      icon:      UndefOr[SemanticICONS],
      className: String,
      duration:  Duration = 6 seconds,
      position:  (VerticalPosition, HorizontalPosition) = (top, right),
      autoHide:  Boolean = true,
      onClose:   () => Callback = { () => Callback.empty }
    ): Callback = {
      val newToast = Toast(icon, className, message, position, onClose)
      val ret = $.modState(s => s.copy(toasts = s.toasts :+ newToast))
      if (autoHide) {
        setTimeout(duration.toMillis.toDouble) {
          ($.modState(s => s.copy(toasts = s.toasts.filter(_ != newToast))) >> newToast.onClose())
            .runNow()
        }
      }
      ret

    }

    def render(state: ToastState): VdomNode =
      state.toasts.groupBy(_.position).toVdomArray { case (pos, toasts) =>
        <.div(
          ^.key           := s"toastRegion_${pos._1.toString}${pos._2.toString}",
          ^.className     := "toast",
          ^.boxSizing     := "border-box",
          ^.maxHeight     := 100.pct,
          ^.overflowX     := "hidden",
          ^.overflowY     := "auto",
          ^.pointerEvents := "auto",
          ^.position      := "fixed",
          (^.top          := 0.px).when(pos._1 == VerticalPosition.top),
          (^.bottom       := 0.px).when(pos._1 == VerticalPosition.bottom),
          (^.right        := 0.px).when(pos._2 == HorizontalPosition.right),
          (^.left         := 0.px).when(pos._2 == HorizontalPosition.left),
          ^.padding       := 8.px,
          if (toasts.isEmpty)
            EmptyVdom
          else {
            toasts.zipWithIndex.toVdomArray { case (toast, index) =>
              <.div(
                ^.key       := s"toast$index",
                ^.className := s"${toast.className}",
                <.div(
                  ^.className := "iconRegion",
                  <.div(^.className := "countdown", ^.opacity := "0"),
                  <.div(
                    Icon()
                      .className("icon")
                      .name(toast.icon.getOrElse(SemanticICONS.asterisk))()
                  )
                ),
                <.div(^.className := "textRegion", toast.message),
                <.div(
                  ^.className := "closeButtonRegion",
                  ^.role      := "button",
                  ^.onClick --> {
                    $.modState(
                      s => s.copy(toasts = s.toasts.filter(_ != toast)),
                      toast.onClose()
                    )
                  },
                  Icon().name(SemanticICONS.close)(),
                  <.span(^.className := "closeSpan", "Close")
                )
              )
            }
          }
        )
      }

  }

  private val component =
    ScalaComponent
      .builder[Unit]("LeibmanToast")
      .initialState(ToastState())
      .renderBackend[Backend]
      .build

  private val toastRef = Ref.toScalaComponent(component)

  def render(): TagMod = toastRef.component()

  def warning(
    message:  VdomNode,
    duration: Duration = 6 seconds,
    position: (VerticalPosition, HorizontalPosition) = (top, right),
    autoHide: Boolean = true,
    onClose:  () => Callback = { () => Callback.empty }
  ): Callback = toast(message, SemanticICONS.`warning sign`, "warning", duration, position, autoHide, onClose)

  def info(
    message:  VdomNode,
    duration: Duration = 6 seconds,
    position: (VerticalPosition, HorizontalPosition) = (top, right),
    autoHide: Boolean = true,
    onClose:  () => Callback = { () => Callback.empty }
  ): Callback = toast(message, SemanticICONS.`info circle`, "info", duration, position, autoHide, onClose)

  def success(
    message:  VdomNode,
    duration: Duration = 6 seconds,
    position: (VerticalPosition, HorizontalPosition) = (top, right),
    autoHide: Boolean = true,
    onClose:  () => Callback = { () => Callback.empty }
  ): Callback = toast(message, SemanticICONS.check, "success", duration, position, autoHide, onClose)

  def error(
    message:  VdomNode,
    duration: Duration = 6 seconds,
    position: (VerticalPosition, HorizontalPosition) = (top, right),
    autoHide: Boolean = true,
    onClose:  () => Callback = { () => Callback.empty }
  ): Callback = toast(message, SemanticICONS.fire, "error", duration, position, autoHide, onClose)

  def toast(
    message:   VdomNode,
    icon:      UndefOr[SemanticICONS],
    className: String,
    duration:  Duration = 6 seconds,
    position:  (VerticalPosition, HorizontalPosition) = (top, right),
    autoHide:  Boolean = true,
    onClose:   () => Callback = { () => Callback.empty }
  ): Callback =
    toastRef.get
      .flatMap(
        _.fold(Callback.empty)(_.backend.toastMsg(message, icon, className, duration, position, autoHide, onClose))
      )

}
