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

import dmscreen.{*, given}
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, CtorType, ScalaComponent}
import net.leibman.dmscreen.apexcharts.*
import net.leibman.dmscreen.apexcharts.ApexCharts.*
import net.leibman.dmscreen.apexcharts.anon.*
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.reactApexcharts.components.*
import net.leibman.dmscreen.reactApexcharts.reactApexchartsStrings.{bar, radar}
import net.leibman.dmscreen.reactQuill.components.ReactQuill
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import zio.json.*

import scala.collection.StrictOptimizedIterableOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

@js.native
trait FormattingOptions extends js.Object {

  val dataPointIndex: Double = js.native

}

extension [A, CC[_], C](a: StrictOptimizedIterableOps[A, CC, C]) {

  private def unzip4[A1, A2, A3, A4](implicit asQuad: A => (A1, A2, A3, A4)): (CC[A1], CC[A2], CC[A3], CC[A4]) = {
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

object DashboardPage extends DMScreenPage {

  case class State(
    campaignNotes: String = "",
    pcs:           Seq[PlayerCharacter] = Seq.empty,
    scenes:        Seq[Scene] = Seq.empty
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

  class Backend($ : BackendScope[CampaignId, State]) {

    def loadState(campaignId: CampaignId): Callback = {
      (for {
        pcs    <- DND5eGraphQLRepository.live.playerCharacters(campaignId)
        scenes <- DND5eGraphQLRepository.live.scenes(campaignId)
      } yield $.modState(_.copy(pcs = pcs, scenes = scenes))).completeWith(_.get)
    }

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
                ^.width   := 100.pct,
                ^.display := "contents",
                Container.className("radarCard")(
                  ^.width := 33.pct,
                  <.h2("Ability Scores"),
                  ReactApexcharts
                    .`type`(radar)
                    .series(state.pcs.zipWithIndex.map {
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
                    .width(100.pct)
                    .height(400)
                    .options(
                      ApexOptions()
                        .setLabels(js.Array("Str", "Con", "Dex", "Int", "Wis", "Cha"))
                    )
                ),
                Container.className("radarCard")(
                  ^.width := 33.pct,
                  <.h2("Saving Throws"),
                  ReactApexcharts
                    .`type`(radar)
                    .series(state.pcs.zipWithIndex.map {
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
                    .width(100.pct)
                    .height(400)
                    .options(
                      ApexOptions()
                        .setLabels(js.Array("Str", "Con", "Dex", "Int", "Wis", "Cha"))
                    )
                ),
                Container.className("radarCard")(
                  ^.width := 33.pct,
                  <.h2("Passive Scores"),
                  ReactApexcharts
                    .`type`(radar)
                    .series(state.pcs.zipWithIndex.map {
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
                    .width(100.pct)
                    .height(400)
                    .options(
                      ApexOptions()
                        .setLabels(js.Array("Insp", "Perc", "Inv"))
                    )
                ),
                Container.className("radarCard")(
                  ^.width := 33.pct,
                  <.h2("Skills"),
                  ReactApexcharts
                    .`type`(radar)
                    .series(state.pcs.zipWithIndex.map {
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
                    .width(100.pct)
                    .height(400)
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
                    )
                ),
                Container.className("radarCard")(
                  ^.width := 33.pct,
                  <.h2("Health"), {
                    val (names, ratios, ratioStrings, lifeColors) = {
                      state.pcs.toList.map { pc =>
                        val ratio = pc.info.health.currentHitPoints.toDouble / pc.info.health.currentMax.toDouble
                        (
                          pc.header.name.take(10),
                          ratio,
                          s"${pc.info.health.currentHitPoints.toDouble}/${pc.info.health.currentMax}",
                          pc.info.health.lifeColor()
                        )
                      }
                    }.unzip4

                    ReactApexcharts
                      .`type`(bar)
                      .width(100.pct)
                      .height(400)
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
                )
              ),
              <.div(
                ^.width   := 100.pct,
                ^.display := "contents",
                VdomArray(
                  Container.className("radarCard")(
                    ^.key   := "campaignNotes",
                    ^.style := js.Dictionary("width" -> "540px", "height" -> "100%"),
                    <.h2(
                      "Campaign Notes",
                      Button.onClick(
                        (
                          _,
                          _
                        ) =>
                          dmScreenState.onModifyCampaignState(
                            campaignState.copy(
                              campaign = campaign.copy(jsonInfo =
                                campaign.info.copy(notes = state.campaignNotes).toJsonAST.toOption.get
                              ),
                              changeStack = campaignState.changeStack.logCampaignChange()
                            ),
                            ""
                          )
                      )("Save")
                    ),
                    ReactQuill
                      .defaultValue(campaign.info.notes)
                      .onChange(
                        (
                          newValue,
                          _,
                          _,
                          _
                        ) => $.modState(_.copy(campaignNotes = newValue))
                      )
                  ),
                  if (state.scenes.isEmpty) EmptyVdom
                  else
                    Container.className("radarCard")(
                      ^.key := "sceneNotes",
                      <.h2("Scene Notes"),
                      state.scenes.map(_.header.name).mkString(",")
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

  private val component: Component[CampaignId, State, Backend, CtorType.Props] = ScalaComponent
    .builder[CampaignId]("dashboardPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props))
    .build

  def apply(
    campaignId: CampaignId
  ): Unmounted[CampaignId, State, Backend] = component(campaignId)

}
