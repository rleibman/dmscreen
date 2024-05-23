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

package dmscreen

import components.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.TimerSupport
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom.{Event, window}

import java.net.URI
import java.util.UUID

object Content {

  private val connectionId = UUID.randomUUID().toString

  case class State(dmScreenState: DMScreenState)

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State) = {
      DMScreenState.ctx.provide(s.dmScreenState) {
        <.div(
          ^.key    := "contentDiv",
          ^.height := 100.pct,
          Confirm.render(),
          Toast.render(),
          AppRouter.router()
        )
      }
    }

    def refresh(initial: Boolean): Callback = {
      val ajax = for {
        oldState <- $.state.asAsyncCallback

        // TODO use asyncCalibanCall to get all the pieces of data needed to create a new global state
      } yield $.modState { s =>
        import scala.language.unsafeNulls
        val copy = if (initial) {
          s.copy()
        } else {
          s
        }

        copy.copy()
      }
      for {
        _ <- Callback.log(
          if (initial) "Initializing Content Component" else "Refreshing Content Component"
        )
        modedState <- ajax.completeWith(_.get)
      } yield modedState
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("content")
    .initialState {
      // Get from window.sessionStorage anything that we may need later to fill in the state.
      State(dmScreenState = DMScreenState())
    }
    .renderBackend[Backend]
    .componentDidMount(_.backend.refresh(initial = true))
    .componentWillUnmount($ =>
      Callback.log("Closing down operationStream")
//        >>
//        $.state.dmScreenState.operationStream.fold(Callback.empty)(_.close())
    )
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
