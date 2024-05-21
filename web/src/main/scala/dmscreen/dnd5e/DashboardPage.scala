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
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.chartJs.distTypesIndexMod.ChartData
import net.leibman.dmscreen.reactChartjs2.components.Radar
import net.leibman.dmscreen.reactChartjs2.reactChartjs2Strings.radar
import net.leibman.dmscreen.chartJs.distTypesIndexMod.DefaultDataPoint

object DashboardPage extends DMScreenTab {

  case class State(
    campaign: Option[Campaign] = None,
    pcs:      Seq[PlayerCharacter] = Seq.empty,
    scenes:   Seq[Scene] = Seq.empty
  )

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State) = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          val campaign = campaignState.campaign

          val abilityScores: ChartData[radar, DefaultDataPoint[radar], Any] = ??? // = s.pcs.map { pc =>
          //          pc.info.abilities
          //
          //        }

          val passiveScores: ChartData[radar, DefaultDataPoint[radar], Any] = ??? // = s.pcs.map  { pc =>
          //          // TODO calculate passive scores
          //        }

          val proficiencies: ChartData[radar, DefaultDataPoint[radar], Any] = ??? // = s.pcs.map { pc =>
          //          // TODO calculate proficiencies (skills)
          //        }

          <.div(
            <.div("Ability Score Radar"),
            Radar(abilityScores),
            <.div("Passive Score Radar"),
            Radar(passiveScores),
            <.div("Proficiency Radar"),
            Radar(proficiencies),
            <.div("Campaign Notes"),
            campaign.info.notes,
            <.div("Scene Notes"),
            campaign.info.scenes.find(_.isActive).orElse(campaign.info.scenes.headOption).map { scene =>
              <.div(
                if (scene.isActive) "Current Scene" else "First Scene",
                scene.name,
                scene.notes
              )
            }
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

  def apply(
//    campaign: Campaign,
//    pcs:      Seq[PlayerCharacter],
//    scenes:   Seq[Scene]
  ): Unmounted[Unit, State, Backend] = component()

}
