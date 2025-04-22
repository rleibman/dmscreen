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

import dmscreen.*
import dmscreen.dnd5e.pages.*
import japgolly.scalajs.react.vdom.*
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticCOLORS

object DND5eUIPages {

  sealed trait DND5eUIPages(override val name: String) extends AppPageType

  case object dashboard extends DND5eUIPages("DND5eDashboard")
  case object encounters extends DND5eUIPages("DND5eEncounters")
  case object pcs extends DND5eUIPages("DND5ePcs")
  case object npcs extends DND5eUIPages("DND5eNpcs")
  case object scenes extends DND5eUIPages("DND5eScenes")
  case object bestiary extends DND5eUIPages("DND5eBestiary")
  case object randomTables extends DND5eUIPages("DND5eRandomTables")
  case object reference extends DND5eUIPages("DND5eReference")
  case object randomNameGenerator extends DND5eUIPages("DND5eRandomNameGenerator")
  case object campaignLog extends DND5eUIPages("DND5eCampaignLog")

}

case object DND5eUI extends GameUI {

  override def menuItems: Seq[AppMenuItem] =
    Seq(
      PageAppMenuItem(DND5eUIPages.dashboard, "Dashboard", campaignId => DashboardPage(campaignId)),
      PageAppMenuItem(DND5eUIPages.pcs, "PCs", campaignId => PCPage(campaignId)),
      PageAppMenuItem(DND5eUIPages.encounters, "Encounters", campaignId => EncounterPage(campaignId)),
      PageAppMenuItem(DND5eUIPages.npcs, "NPCs", campaignId => NPCPage(campaignId)),
      PageAppMenuItem(DND5eUIPages.scenes, "Scenes", campaignId => ScenePage(campaignId)),
      PageAppMenuItem(DND5eUIPages.bestiary, "Bestiary", campaignId => BestiaryPage(campaignId)),
      PageAppMenuItem(DND5eUIPages.randomTables, "Random Tables", _ => RandomTablePage()),
      PageAppMenuItem(DND5eUIPages.reference, "Reference", _ => ReferencePage()),
      PageAppMenuItem(DND5eUIPages.randomNameGenerator, "Name Generator", _ => RandomNameGeneratorPage()),
      ButtonAppMenuItem(
        DND5eUIPages.campaignLog,
        VdomArray("Campaign Log", CampaignLog.render()),
        onClick = campaignId => CampaignLog.showLog(campaignId)
      )
    )

  override def cssFiles: Seq[String] =
    Seq(
      "css/sui-dnd5e.css",
      "css/statBlock.css"
    )

  val tableColor = SemanticCOLORS.purple
  val tableInverted = true
  val menuColor = SemanticCOLORS.black
  val menuInverted = false

}
