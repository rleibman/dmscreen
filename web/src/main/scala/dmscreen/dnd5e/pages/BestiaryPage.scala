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

package dmscreen.dnd5e.pages

import dmscreen.CampaignId
import dmscreen.dnd5e.components.MonsterTable
import dmscreen.dnd5e.given
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.js.JSConverters.*

object BestiaryPage {

  case class State()
  case class Props(campaignId: CampaignId)

  class Backend($ : BackendScope[Props, State]) {

    def loadState(campaignId: CampaignId): Callback = Callback.empty

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      <.div(
        MonsterTable(props.campaignId)
      )
    }

  }

  private val component = ScalaComponent
    .builder[Props]("BestiaryPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount { $ =>
      $.backend.loadState($.props.campaignId)
    }
    .build

  def apply(campaignId: CampaignId): Unmounted[Props, State, Backend] = component(Props(campaignId))

}
