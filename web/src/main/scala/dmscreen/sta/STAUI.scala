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

package dmscreen.sta

import dmscreen.*
import dmscreen.sta.pages.*
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.vdom.*
import japgolly.scalajs.react.vdom.html_<^.*

object STAUIPages {

  sealed trait STAUIPages(override val name: String) extends AppPageType

  case object dashboard extends STAUIPages("STADashboard")
  case object characters extends STAUIPages("STACharacters")
  case object ships extends STAUIPages("STAShips")
  case object encounters extends STAUIPages("STAEncounters")
  case object npcs extends STAUIPages("STANpcs")
  case object scenes extends STAUIPages("STAScenes")

}

case object STAUI extends GameUI {

  override def menuItems: Seq[AppMenuItem] =
    Seq(
      PageAppMenuItem(STAUIPages.dashboard, "Dashboard", _ => DashboardPage()),
      PageAppMenuItem(STAUIPages.characters, "Characters", campaignId => PCPage(campaignId)),
      PageAppMenuItem(STAUIPages.ships, "Ships", _ => ShipPage()),
      PageAppMenuItem(STAUIPages.encounters, "Encounters", _ => EncounterPage()),
      PageAppMenuItem(STAUIPages.scenes, "Scenes", _ => ScenePage()),
      PageAppMenuItem(STAUIPages.npcs, "NPCs", _ => NPCPage())
    )

  override def cssFiles: Seq[String] =
    Seq(
      "css/sui-lcars.css"
    )

}
