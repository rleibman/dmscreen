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
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticICONS
import net.leibman.dmscreen.semanticUiReact.components.Icon

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
