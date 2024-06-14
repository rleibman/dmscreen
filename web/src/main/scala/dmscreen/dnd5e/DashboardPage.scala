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
import net.leibman.dmscreen.reactApexcharts.components.{ReactApexcharts, *}
import net.leibman.dmscreen.reactApexcharts.reactApexchartsStrings.{bar, radar}
import net.leibman.dmscreen.apexcharts.anon.*
import net.leibman.dmscreen.apexcharts.*
import net.leibman.dmscreen.apexcharts.ApexCharts.*
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.reactQuill.components.ReactQuill
import org.scalablytyped.runtime.StringDictionary

import scala.reflect.Selectable.reflectiveSelectable
import scala.collection.StrictOptimizedIterableOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

@js.native
trait FormattingOptions extends js.Object {

  val dataPointIndex: Double = js.native

}

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

            <.div(
              ^.className := "pageContainer",
              <.div(
                ^.className := "radarCard",
                <.h2("Ability Scores"),
                ReactApexcharts
                  .`type`(radar)
                  .series(campaignState.pcs.zipWithIndex.map {
                    (
                      pc,
                      i
                    ) =>
                      val info = pc.info.toOption.get
                      Data(data =
                        js.Array(
                          info.abilities.strength.overridenValue.toDouble,
                          info.abilities.constitution.overridenValue.toDouble,
                          info.abilities.dexterity.overridenValue.toDouble,
                          info.abilities.intelligence.overridenValue.toDouble,
                          info.abilities.wisdom.overridenValue.toDouble,
                          info.abilities.charisma.overridenValue.toDouble
                        )
                      )
                        .setName(pc.header.name)
                        .setColor(radarColors(i))
                  }.toJSArray)
                  .width(250)
                  .height(270)
                  .options(
                    ApexOptions()
                      .setLabels(js.Array("Str", "Con", "Dex", "Int", "Wis", "Cha"))
                      .setLegend(
                        ApexLegend().setLabels(
                          UseSeriesColors()
                            .setColors("#ecf0f1")
                        )
                      )
                  )
              ),
              <.div(
                ^.className := "radarCard",
                <.h2("Saving Throws"),
                ReactApexcharts
                  .`type`(radar)
                  .series(campaignState.pcs.zipWithIndex.map {
                    (
                      pc,
                      i
                    ) =>
                      val info = pc.info.toOption.get
                      Data(data =
                        js.Array(
                          info.abilities.strength.savingThrow(info.proficiencyBonus).toDouble,
                          info.abilities.constitution.savingThrow(info.proficiencyBonus).toDouble,
                          info.abilities.dexterity.savingThrow(info.proficiencyBonus).toDouble,
                          info.abilities.intelligence.savingThrow(info.proficiencyBonus).toDouble,
                          info.abilities.wisdom.savingThrow(info.proficiencyBonus).toDouble,
                          info.abilities.charisma.savingThrow(info.proficiencyBonus).toDouble
                        )
                      )
                        .setName(pc.header.name)
                        .setColor(radarColors(i))
                  }.toJSArray)
                  .width(250)
                  .height(270)
                  .options(
                    ApexOptions()
                      .setLabels(js.Array("Str", "Con", "Dex", "Int", "Wis", "Cha"))
                      .setLegend(
                        ApexLegend().setLabels(
                          UseSeriesColors()
                            .setColors("#ecf0f1")
                        )
                      )
                  )
              ),
              <.div(
                ^.className := "radarCard",
                <.h2("Passive Scores"),
                ReactApexcharts
                  .`type`(radar)
                  .series(campaignState.pcs.zipWithIndex.map {
                    (
                      pc,
                      i
                    ) =>
                      val info = pc.info.toOption.get
                      Data(data =
                        js.Array(
                          info.passiveInsight.toDouble,
                          info.passivePerception.toDouble,
                          info.passiveInvestigation.toDouble
                        )
                      )
                        .setName(pc.header.name)
                        .setColor(radarColors(i))
                  }.toJSArray)
                  .width(250)
                  .height(270)
                  .options(
                    ApexOptions()
                      .setLabels(js.Array("Insp", "Perc", "Inv"))
                      .setLegend(
                        ApexLegend().setLabels(
                          UseSeriesColors()
                            .setColors("#ecf0f1")
                        )
                      )
                  )
              ),
              <.div(
                ^.className := "radarCard",
                <.h2("Skills"),
                ReactApexcharts
                  .`type`(radar)
                  .series(campaignState.pcs.zipWithIndex.map {
                    (
                      pc,
                      i
                    ) =>
                      val info = pc.info.toOption.get
                      Data(data =
                        js.Array(
                          info.skills.acrobatics.modifier(info.abilities).toDouble,
                          info.skills.animalHandling.modifier(info.abilities).toDouble,
                          info.skills.arcana.modifier(info.abilities).toDouble,
                          info.skills.athletics.modifier(info.abilities).toDouble,
                          info.skills.deception.modifier(info.abilities).toDouble,
                          info.skills.history.modifier(info.abilities).toDouble,
                          info.skills.insight.modifier(info.abilities).toDouble,
                          info.skills.intimidation.modifier(info.abilities).toDouble,
                          info.skills.investigation.modifier(info.abilities).toDouble,
                          info.skills.medicine.modifier(info.abilities).toDouble,
                          info.skills.nature.modifier(info.abilities).toDouble,
                          info.skills.perception.modifier(info.abilities).toDouble,
                          info.skills.performance.modifier(info.abilities).toDouble,
                          info.skills.persuasion.modifier(info.abilities).toDouble,
                          info.skills.religion.modifier(info.abilities).toDouble,
                          info.skills.sleightOfHand.modifier(info.abilities).toDouble,
                          info.skills.stealth.modifier(info.abilities).toDouble
                        )
                      )
                        .setName(pc.header.name)
                        .setColor(radarColors(i))
                  }.toJSArray)
                  .width(250)
                  .height(270)
                  .options(
                    ApexOptions()
                      .setLabels(
                        js.Array(
                          "Acrobatics",
                          "A. Handling",
                          "Arcana",
                          "Athletics",
                          "Deception",
                          "History",
                          "Insight",
                          "Int.",
                          "Inv.",
                          "Medicine",
                          "Nature",
                          "Perception",
                          "Performance",
                          "Persuasion",
                          "Religion",
                          "S. of Hand",
                          "Stealth"
                        )
                      )
                      .setLegend(
                        ApexLegend().setLabels(
                          UseSeriesColors()
                            .setColors("#ecf0f1")
                        )
                      )
                  )
              ),
              <.div(
                ^.className := "radarCard",
                <.h2("Health"), {
                  extension [A, CC[_], C](a: StrictOptimizedIterableOps[A, CC, C]) {
                    def unzip4[A1, A2, A3, A4](implicit asQuad: A => (A1, A2, A3, A4))
                      : (CC[A1], CC[A2], CC[A3], CC[A4]) = {
                      val b1 = a.iterableFactory.newBuilder[A1]
                      val b2 = a.iterableFactory.newBuilder[A2]
                      val b3 = a.iterableFactory.newBuilder[A3]
                      val b4 = a.iterableFactory.newBuilder[A4]

                      a.foreach { xyza =>
                        val triple = asQuad(xyza)
                        b1 += triple._1
                        b2 += triple._2
                        b3 += triple._3
                        b4 += triple._4
                      }
                      (b1.result(), b2.result(), b3.result(), b4.result())
                    }

                  }

                  val (names, ratios, ratioStrings, lifeColors) = {
                    campaignState.pcs.map { pc =>
                      val info = pc.info.toOption.get
                      val currentHP = info.hitPoints.currentHitPoints match {
                        case _: DeathSave => 0
                        case i: Int       => i
                      }
                      val ratio = currentHP.toDouble / info.hitPoints.currentMax.toDouble
                      (
                        pc.header.name.take(10),
                        ratio,
                        s"$currentHP/${info.hitPoints.currentMax}",
                        info.hitPoints.lifeColor
                      )
                    }
                  }.unzip4

                  ReactApexcharts
                    .`type`(bar)
                    .height(270)
                    .series {
                      js.Array(Data(data = ratios.toJSArray).setName("Health"))
                    }
                    .options(
                      ApexOptions()
                        .setDataLabels(
                          ApexDataLabels()
                            .setEnabled(true)
                            .setFormatter {
                              case (value, opt: js.Object) =>
                                val i = opt.asInstanceOf[FormattingOptions].dataPointIndex.toInt
                                ratioStrings(i)
                              case _ => "goodbye"
                            }
                            .setTextAnchor(apexchartsStrings.start)
                            .setOffsetX(0)
                        )
                        .setXaxis(
                          ApexXAxis()
                            .setCategories(names.toJSArray)
                            .setLabels(DatetimeFormatter().setShow(false))
                            .setAxisTicks(BorderType().setShow(false))
                        )
                        .setYaxis(
                          ApexYAxis()
                            .setLabels(Align().setStyle(CssClassFontFamily().setColors(js.Array("#ecf0f1"))))
                        )
                        .setPlotOptions(
                          ApexPlotOptions()
                            .setBar(
                              BarHeight()
                                .setHorizontal(true)
                                .setColors(BackgroundBarColors().setBackgroundBarColors(lifeColors.toJSArray))
                            )
                        )
                    )
                }
              ),
              campaign.info.fold(
                _ => EmptyVdom,
                campaignInfo =>
                  VdomArray(
                    <.div(
                      ^.className := "radarCard",
                      ^.style     := js.Dictionary("width" -> "540px", "height" -> "310px"), // TODO move sizes to css
                      <.h2("Campaign Notes"),
                      ReactQuill
                        .value(campaignInfo.notes)
                        .style(CSSProperties().set("background-color", "#ced9e4").set("color", "#000000")) // TODO move colors to css
                    ),
                    if (campaignInfo.scenes.isEmpty) EmptyVdom
                    else
                      <.div(
                        ^.className := "radarCard",
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
