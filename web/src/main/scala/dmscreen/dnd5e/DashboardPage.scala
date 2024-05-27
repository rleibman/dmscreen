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

import dmscreen.{Campaign, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.{BackendScope, Callback, CtorType, ScalaComponent}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.reactSvgRadarChart.anon.Color
import net.leibman.dmscreen.reactSvgRadarChart.components.ReactSvgRadarChart
import net.leibman.dmscreen.reactSvgRadarChart.mod.{ChartData, ChartOptionsProps, ChartProps}
import org.scalablytyped.runtime.StringDictionary

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object DashboardPage extends DMScreenTab {

  case class State(
    campaign: Option[DND5eCampaign] = None,
    pcs:      Seq[PlayerCharacter] = Seq.empty,
    scenes:   Seq[Scene] = Seq.empty
  )

  private val radarColors = List(
    "#ff0000",
    "#00ff00",
    "#0000ff",
    "#ffff00",
    "#ff00ff",
    "#00ffff",
    "#880000",
    "#008800",
    "#000088",
    "#888800",
    "#880088",
    "#008888"
  )

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State): VdomElement = {
      DMScreenState.ctx.consume { dmScreenState =>
        {
          dmScreenState.campaignState.fold {
            <.div("Campaign Loading")
          } { case campaignState: DND5eCampaignState =>
            val campaign = campaignState.campaign
            val abilityScores = Array(
              ChartData(
                StringDictionary(
                  "Str" -> 18 / 20.0,
                  "Con" -> 15 / 20.0,
                  "Dex" -> 10 / 20.0,
                  "Int" -> 9 / 20.0,
                  "Wis" -> 12 / 20.0,
                  "Cha" -> 14 / 20.0
                ),
                Color(radarColors(0))
              ),
              ChartData(
                StringDictionary(
                  "Str" -> 15 / 20.0,
                  "Con" -> 18 / 20.0,
                  "Dex" -> 12 / 20.0,
                  "Int" -> 11 / 20.0,
                  "Wis" -> 9 / 20.0,
                  "Cha" -> 20 / 20.0
                ),
                Color(radarColors(1))
              )
            ).toJSArray

            <.div(
              <.h1("Dashboard"),
              <.div(
                <.h2("Ability Scores"),
                ReactSvgRadarChart
                  .withProps(
                    ChartProps(
                      captions = StringDictionary[String](
                        "Str" -> "Str",
                        "Con" -> "Con",
                        "Dex" -> "Dex",
                        "Int" -> "Int",
                        "Wis" -> "Wis",
                        "Cha" -> "Cha"
                      ),
                      data = abilityScores, // Array.empty[ChartData].toJSArray,
                      size = 200
                    )
                  )
              ),
              <.div(
                <.h2("Saving Throws")
              ),
              <.div(
                <.h2("Passive Scores")
              ),
              <.div(
                <.h2("Proficiencies")
              ),
              <.div(
                <.h2("Legend")
              ),
              campaign.info.fold(
                _ => EmptyVdom,
                campaignInfo =>
                  VdomArray(
                    <.div(<.h2("Campaign Notes"), campaignInfo.notes),
                    if (campaignInfo.scenes.isEmpty) EmptyVdom
                    else
                      <.div(
                        <.h2("Scene Notes"),
                        campaignInfo.scenes.mkString(",")
                        //              campaignInfo.scenes
                        //                .find(_.isActive).orElse(campaignInfo.scenes.headOption).map { scene =>
                        //                  <.div(if (scene.isActive) "Current Scene" else "First Scene", scene.name, scene.notes)
                        //                }.toVdomArray

                      )
                  )
              )
            )
          }
        }
      }
    }

  }

  private val component: Component[Unit, State, Backend, CtorType.Nullary] = ScalaComponent
    .builder[Unit]("dashboardPage")
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
