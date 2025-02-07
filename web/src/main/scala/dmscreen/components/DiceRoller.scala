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

package dmscreen.components

import dmscreen.{DMScreenTab, DiceRoll}
import dmscreen.dnd5e.*
import dmscreen.dnd5e.components.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{CtorType, Ref, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.Confirm.component
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, Table, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsElementsLabelLabelMod.LabelProps
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{
  SemanticCOLORS,
  SemanticICONS,
  SemanticSIZES,
  SemanticWIDTHS
}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesAccordionAccordionTitleMod.*
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.`right corner`
import org.scalajs.dom
import org.scalajs.dom.*
import org.scalajs.dom.html.Div

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSGlobal, JSImport}

enum DieType {

  case d4, d6, d8, d10, d12, d20, custom

}

@js.native
@JSImport("@3d-dice/dice-ui/src/displayResults", JSImport.Default)
class DisplayResults(selector: String) extends js.Object {

  def clear():                                    Unit = js.native
  def showResults(results: js.Array[RollResult]): Unit = js.native

}

@js.native
@JSImport("@3d-dice/dice-parser-interface", JSImport.Default)
class DiceParser() extends js.Object {

  def parseNotation(notation:    String):               js.Any = js.native
  def handleRerolls(results:     js.Any):               js.Array[js.Any] = js.native
  def parseFinalResults(results: js.Array[RollResult]): js.Array[RollResult] = js.native

}

@js.native
@JSImport("@3d-dice/dice-box", JSImport.Default)
class DiceBox(
  options: DiceBoxOptions
) extends js.Object {

  def init():                                                 js.Promise[Unit] = js.native
  def show():                                                 DiceBox = js.native
  def roll(dice:                 js.Any):                     js.Promise[js.Array[DieResult]] = js.native
  def hide():                                                 DiceBox = js.native
  def clear():                                                Unit = js.native
  def onRollComplete:                                         js.Function1[js.Any, Unit] = js.native
  def onRollComplete_=(callback: js.Function1[js.Any, Unit]): Unit = js.native

}

class Roll(
  val modifier: Int, // optional - the modifier (positive or negative) to be added to the final results
  val qty:      Int, // optional - the number of dice to be rolled. Defaults to 1
  val sides: Double | String, // the type of die to be rolled. Either a number such as 20 or a die type such as "fate".
  val theme: String, // optional - the theme's 'systemName' for this roll
  val themeColor: String // optional - HEX value for the theme's material color
) extends js.Object

class DieResult(
  val groupId:    Int, // the roll group this die belongs to
  val rollId:     Int, // the unique identifier for this die within the group
  val sides:      Int, // the type of die
  val theme:      String, // the theme that was assigned to this die
  val themeColor: String, // optional - HEX value for the theme's material color
  val value:      Int // the result for this die
) extends js.Object

class RollResult(
  id: Int, // the id of this group - should match the groupId of rolls
//                  mods: [],          // the roll modifier
  qty:        Int, // the number of dice in this roll
  rolls:      js.Array[DieResult],
  sides:      Int, // the type of die used
  theme:      String, // the theme for this group of dice
  themeColor: String, // the theme color for this group of dice
  val value:  Int // the sum of the dice roll results and modifier
) extends js.Object

class ThemeConfig() extends js.Object

class DiceBoxOptions(
  val id:                 js.UndefOr[String] = js.undefined,
  val assetPath:          String = "/assets/dice-box/",
  val container:          js.UndefOr[String] = js.undefined,
  val gravity:            Double = 1,
  val mass:               Double = 1,
  val friction:           Double = 0.8,
  val restitution:        Double = .1,
  val angularDamping:     Double = 0.4,
  val linearDamping:      Double = 0.5,
  val spinForce:          Double = 6,
  val throwForce:         Double = 5,
  val startingHeight:     Double = 8,
  val settleTimeout:      Int = 5000,
  val offscreen:          Boolean = false,
  val delay:              Int = 10,
  val lightIntensity:     Double = 1,
  val enableShadows:      Boolean = true,
  val shadowTransparency: Double = 0.8,
  val theme:              String = "default",
  val preloadThemes:      js.Array[String] = js.Array(),
  val themeColor:         String = "#aa4f4a",
  val scale:              Double = 5,
  val suspendSimulation:  Boolean = false,
//  val origin:              js.UndefOr[String] = js.undefined,
  val onDieComplete:       js.UndefOr[js.Function1[DieResult, Unit]] = js.undefined,
  val onRollComplete:      js.UndefOr[js.Function1[js.Array[RollResult], Unit]] = js.undefined,
  val onRemoveComplete:    js.UndefOr[js.Function1[DieResult, Unit]] = js.undefined,
  val onThemeConfigLoaded: js.UndefOr[js.Function1[ThemeConfig, Unit]] = js.undefined,
  val onThemeLoaded:       js.UndefOr[js.Function1[ThemeConfig, Unit]] = js.undefined
) extends js.Object

object DiceRoller extends DMScreenTab {

  case class State(
    diceBox:        Option[DiceBox] = None,
    diceParser:     DiceParser = DiceParser(),
    displayResults: Option[DisplayResults] = None,
    custom:         String = ""
  )

  case class Props(
    displayTimeMs:  Int,
    dieTypes:       Set[DieType],
    onDieComplete:  DieResult => Callback,
    onRollComplete: Seq[RollResult] => Callback
  )

  class Backend($ : BackendScope[Props, State]) {

    def roll(notation: String): AsyncCallback[Sequence[DieResult]] = { // Add the roll results to the callback

      $.state.asAsyncCallback.flatMap { s =>
        s.diceBox.fold(AsyncCallback.pure(js.Array[DieResult]())) { db =>
          def promise: Promise[Sequence[DieResult]] =
            db
              .show()
              .roll(s.diceParser.parseNotation(notation))

          AsyncCallback.fromJsPromise(promise)
        }
      }
    }

    def render(
      props: Props,
      state: State
    ): VdomTagOf[Div] = {
      <.div(
        ^.width     := 160.px,
        ^.textAlign := "center",
        ^.display   := "inline-block",
        <.div(^.id := "dice-box"),
        Button
          .className("diceButton")
          .color(SemanticCOLORS.black)
          .icon(true)(Icon.className("d4icon"))
          .onClick(
            (
              _,
              _
            ) =>
              state.diceBox.fold(Callback.empty)(db =>
                Callback(
                  db
                    .show()
                    .roll(state.diceParser.parseNotation("1d4"))
                )
              )
          )
          .when(props.dieTypes.contains(DieType.d4)),
        Button
          .className("diceButton")
          .color(SemanticCOLORS.black)
          .icon(true)(Icon.className("d6icon"))
          .onClick(
            (
              _,
              _
            ) =>
              state.diceBox.fold(Callback.empty)(db =>
                Callback(
                  db
                    .show()
                    .roll(state.diceParser.parseNotation("1d6"))
                )
              )
          ).when(props.dieTypes.contains(DieType.d6)),
        Button
          .className("diceButton")
          .color(SemanticCOLORS.black)
          .icon(true)(Icon.className("d8icon"))
          .onClick(
            (
              _,
              _
            ) =>
              state.diceBox.fold(Callback.empty)(db =>
                Callback(
                  db
                    .show()
                    .roll(state.diceParser.parseNotation("1d8"))
                )
              )
          ).when(props.dieTypes.contains(DieType.d8)),
        Button
          .className("diceButton")
          .color(SemanticCOLORS.black)
          .icon(true)(Icon.className("d10icon"))
          .onClick(
            (
              _,
              _
            ) =>
              state.diceBox.fold(Callback.empty)(db =>
                Callback(
                  db
                    .show()
                    .roll(state.diceParser.parseNotation("1d10"))
                )
              )
          ).when(props.dieTypes.contains(DieType.d10)),
        Button
          .className("diceButton")
          .color(SemanticCOLORS.black)
          .icon(true)(Icon.className("d12icon"))
          .onClick(
            (
              _,
              _
            ) =>
              state.diceBox.fold(Callback.empty)(db =>
                Callback(
                  db
                    .show()
                    .roll(state.diceParser.parseNotation("1d12"))
                )
              )
          ).when(props.dieTypes.contains(DieType.d12)),
        Button
          .className("diceButton")
          .color(SemanticCOLORS.black)
          .icon(true)(Icon.className("d20icon"))
          .onClick(
            (
              _,
              _
            ) =>
              state.diceBox.fold(Callback.empty)(db =>
                Callback(
                  db
                    .show()
                    .roll(state.diceParser.parseNotation("1d20"))
                )
              )
          ).when(props.dieTypes.contains(DieType.d20)),
        Input
          .value(state.custom)
          .style(CSSProperties().set("width", 150.px))
          .color("#000000")
          .placeholder("d20+2")
          .label("Custom")
          .labelPosition(`right corner`)
          .onKeyDown(e =>
            if (e.key == "Enter" && state.custom.trim.nonEmpty)
              state.diceBox.fold(Callback.empty)(db =>
                Callback(
                  db
                    .show()
                    .roll(state.diceParser.parseNotation(state.custom))
                )
              )
            else Callback.empty
          )
          .onChange(
            (
              _,
              data
            ) => {
              val newVal = data.value match {
                case s: String => s
                case _ => state.custom
              }
              $.modState(_.copy(custom = newVal))
            }
          ).when(props.dieTypes.contains(DieType.custom))
      )
    }

  }

  private val component = ScalaComponent
    .builder[Props]("diceTest")
    .initialState {
      State()
    }
    .renderBackend[Backend]
    .componentDidMount($ => {
      val diceBox = DiceBox(
        DiceBoxOptions(
          id = "dice-canvas",
          container = "#dice-box",
          assetPath = "/assets/dice-box/",
          startingHeight = 8,
          throwForce = 6,
          spinForce = 5,
          lightIntensity = 0.9,
          theme = "theme-rust",
          onRollComplete = (results: js.Array[RollResult]) =>
            ($.props.onRollComplete(results.toSeq) >>
              $.state.diceBox
                .fold(Callback.empty) { db =>
                  Callback {
                    db.hide().clear()
                  }
                } >>
              $.state.displayResults.fold(Callback.empty) { dr =>
                val finalResults = $.state.diceParser.parseFinalResults(results)
                Callback(dr.showResults(finalResults)) >>
                  AsyncCallback
                    .pure(Callback(dr.clear())).delayMs($.props.displayTimeMs).completeWith(_.get)
              }).runNow(),
          onDieComplete = (dieResult: DieResult) => $.props.onDieComplete(dieResult).runNow()
        )
      )
      AsyncCallback
        .fromJsPromise(diceBox.init())
        .completeWith(_ =>
          $.modState(s => s.copy(diceBox = Some(diceBox), displayResults = Some(DisplayResults("#dice-box"))))
        )
    })
    .build

  def apply(
    displayTimeMs:  Int = 2000,
    dieTypes:       Set[DieType] = DieType.values.toSet,
    onDieComplete:  DieResult => Callback = _ => Callback.empty,
    onRollComplete: Seq[RollResult] => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = ref.component(Props(displayTimeMs, dieTypes, onDieComplete, onRollComplete))

  private val ref = Ref.toScalaComponent(component)

  def roll(notation: String): AsyncCallback[Sequence[DieResult]] = {
    ref.get.asAsyncCallback.flatMap(_.fold(AsyncCallback.pure(js.Array[DieResult]()))(_.backend.roll(notation)))
  }

  def roll(diceRoll: DiceRoll): AsyncCallback[Sequence[DieResult]] = {
    roll(diceRoll.roll)
  }

}
