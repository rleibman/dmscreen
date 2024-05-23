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

package dmscreen.dnd5e

import dmscreen.{DMScreenState, DMScreenTab}
import dmscreen.dnd5e.NPCPage.State
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*

object PlayerPage extends DMScreenTab {

  case class State()

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State) = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          val campaign = campaignState.campaign
          <.div(
            campaignState.pcs.map { pc =>
              val info = pc.info.toOption.get // Should never fail

              <.div(
                pc.header.name,
                pc.header.playerName,
                info.alignment.toString,
                info.race.name,
                info.faith,
                info.classes.map(c => s"${c.name}: ${c.subclass.name} (l ${c.level})").mkString(", "),
                info.abilities
                  .map(a => <.div(^.className := "shortAbility", s"${a.abilityType.short}${a.value}")).toVdomArray,
                <.div(^.className := "hitPoints", s"${info.baseHitPoints}"),
                info.armorClass,
                info.background.name,
                info.conditions.map(_.toString).mkString(", "),
                info.deathSaves.successes,
                info.feats.map(f => s"${f.name}").mkString(", "),
                info.inspiration.toString,
                info.modifiers.toString,
                info.notes,
                info.languages.mkString(","),
                info.traits.toString
              )

            }.toVdomArray
          )
        }
      }
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("router")
    .initialState {
      State()
    }
    .renderBackend[Backend]
    .componentDidMount(
      // _.backend.refresh(initial = true)()
      $ => Callback.empty
    )
    .componentWillUnmount($ =>
      // TODO close down streams here
      Callback.empty
    )
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
