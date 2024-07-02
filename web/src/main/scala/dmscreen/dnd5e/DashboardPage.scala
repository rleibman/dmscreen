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
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import org.scalablytyped.runtime.StringDictionary

import scala.reflect.Selectable.reflectiveSelectable
import scala.collection.StrictOptimizedIterableOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import zio.json.*

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

    def render(state: State): VdomElement = {
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
                      Data(data =
                        js.Array(
                          pc.info.abilities.strength.overridenValue.toDouble,
                          pc.info.abilities.constitution.overridenValue.toDouble,
                          pc.info.abilities.dexterity.overridenValue.toDouble,
                          pc.info.abilities.intelligence.overridenValue.toDouble,
                          pc.info.abilities.wisdom.overridenValue.toDouble,
                          pc.info.abilities.charisma.overridenValue.toDouble
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
                      Data(data =
                        js.Array(
                          pc.info.abilities.strength.savingThrow(pc.info.proficiencyBonus).toDouble,
                          pc.info.abilities.constitution.savingThrow(pc.info.proficiencyBonus).toDouble,
                          pc.info.abilities.dexterity.savingThrow(pc.info.proficiencyBonus).toDouble,
                          pc.info.abilities.intelligence.savingThrow(pc.info.proficiencyBonus).toDouble,
                          pc.info.abilities.wisdom.savingThrow(pc.info.proficiencyBonus).toDouble,
                          pc.info.abilities.charisma.savingThrow(pc.info.proficiencyBonus).toDouble
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
                      Data(data =
                        js.Array(
                          pc.info.passiveInsight.toDouble,
                          pc.info.passivePerception.toDouble,
                          pc.info.passiveInvestigation.toDouble
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
                      Data(data =
                        js.Array(
                          pc.info.skills.acrobatics.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.animalHandling.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.arcana.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.athletics.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.deception.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.history.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.insight.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.intimidation.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.investigation.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.medicine.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.nature.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.perception.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.performance.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.persuasion.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.religion.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.sleightOfHand.modifier(pc.info.abilities).toDouble,
                          pc.info.skills.stealth.modifier(pc.info.abilities).toDouble
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
                      val currentHP = pc.info.hitPoints.currentHitPoints match {
                        case _: DeathSave => 0
                        case i: Int       => i
                      }
                      val ratio = currentHP.toDouble / pc.info.hitPoints.currentMax.toDouble
                      (
                        pc.header.name.take(10),
                        ratio,
                        s"$currentHP/${pc.info.hitPoints.currentMax}",
                        pc.info.hitPoints.lifeColor
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
              VdomArray(
                <.div(
                  ^.className := "radarCard",
                  ^.key       := "campaignNotes",
                  ^.style     := js.Dictionary("width" -> "540px", "height" -> "310px"), // TODO move sizes to css
                  <.h2("Campaign Notes"),
                  ReactQuill
                    .value(campaign.info.notes)
                    .style(CSSProperties().set("background-color", "#ced9e4").set("color", "#000000")) // TODO move colors to css
                    .onChange(
                      (
                        newValue,
                        deltaStatic,
                        sources,
                        editor
                      ) =>
                        dmScreenState.onModifyCampaignState(
                          campaignState.copy(
                            campaign =
                              campaign.copy(jsonInfo = campaign.info.copy(notes = newValue).toJsonAST.toOption.get),
                            changeStack = campaignState.changeStack.logCampaignChange()
                          )
                        )
                    )
                ),
                if (state.scenes.isEmpty) EmptyVdom
                else
                  <.div(
                    ^.className := "radarCard",
                    ^.key       := "sceneNotes",
                    <.h2("Scene Notes"),
                    state.scenes.map(_.header.name).mkString(",")
                    //              campaignInfo.scenes
                    //                .find(_.isActive).orElse(campaignInfo.scenes.headOption).map { scene =>
                    //                  <.div(if (scene.isActive) "Current Scene" else "First Scene", scene.name, scene.notes)
                    //                }.toVdomArray

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
