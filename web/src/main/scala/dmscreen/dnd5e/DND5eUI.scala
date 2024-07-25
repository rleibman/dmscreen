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

import dmscreen.dnd5e.pages.*
import dmscreen.*

enum DND5eUIPages {

  case dashboard extends DND5eUIPages with AppPageType
  case encounters extends DND5eUIPages with AppPageType
  case pcs extends DND5eUIPages with AppPageType
  case npcs extends DND5eUIPages with AppPageType
  case scenes extends DND5eUIPages with AppPageType

}

case object DND5eUI extends GameUI {

  override def pages: Seq[AppPage] =
    Seq(
      AppPage(DND5eUIPages.dashboard, "Dashboard", campaignId => DashboardPage(campaignId)),
      AppPage(DND5eUIPages.pcs, "PCs", campaignId => PCPage(campaignId)),
      AppPage(DND5eUIPages.encounters, "Encounters", campaignId => EncounterPage(campaignId)),
      AppPage(DND5eUIPages.npcs, "NPCs", campaignId => NPCPage(campaignId)),
      AppPage(DND5eUIPages.scenes, "Scenes", campaignId => ScenePage(campaignId))
    )

  override def cssFiles: Seq[String] =
    Seq(
      "css/sui-dnd5e.css",
      "css/statBlock.css"
    )

}
