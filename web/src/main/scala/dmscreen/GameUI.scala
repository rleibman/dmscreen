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

import dmscreen.components.DieType
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.component.Generic.UnmountedRaw
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode}

trait AppPageType {

  def name: String

}

trait AppMenuItem {

  def pageType: AppPageType
  def title:    TagMod

}

case class PageAppMenuItem(
  override val pageType: AppPageType,
  override val title:    TagMod,
  createComponentFn:     CampaignId => VdomElement
) extends AppMenuItem

case class ButtonAppMenuItem(
  override val pageType: AppPageType,
  override val title:    TagMod,
  onClick:               CampaignId => Callback
) extends AppMenuItem

trait GameUI {

  def menuItems: Seq[AppMenuItem]
  def cssFiles:  Seq[String]

  // Enhancement, allow different game ui to decide what dice are available, and what custom dice rolls are available
  // Put all of this into a DiceUIConfiguration
  def diceTypes:           Seq[DieType] = Seq.empty
  def diceRolls:           Seq[DiceRoll] = Seq.empty // Enhancement, allow it to set names, icons, etc.
  def allowCustomDiceRoll: Boolean = true
  def diceTheme:           String = "default"

}
