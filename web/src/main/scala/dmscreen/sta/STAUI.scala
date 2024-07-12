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

import dmscreen.sta.pages.*
import dmscreen.*

enum STAUIPages {

  case dashboard extends STAUIPages with AppPageType
  case characters extends STAUIPages with AppPageType
  case ships extends STAUIPages with AppPageType
  case encounters extends STAUIPages with AppPageType
  case npcs extends STAUIPages with AppPageType
  case scenes extends STAUIPages with AppPageType

}

case object STAUI extends GameUI {

  override def pages: Seq[AppPage] =
    Seq(
      AppPage(STAUIPages.dashboard, "Dashboard", _ => DashboardPage()),
      AppPage(STAUIPages.characters, "Characters", _ => CharacterPage()),
      AppPage(STAUIPages.ships, "Ships", _ => ShipPage()),
      AppPage(STAUIPages.encounters, "Encounters", _ => EncounterPage()),
      AppPage(STAUIPages.scenes, "Scenes", _ => ScenePage()),
      AppPage(STAUIPages.npcs, "NPCs", _ => NPCPage())
    )

}
