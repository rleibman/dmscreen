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

import dmscreen.{Campaign, CampaignHeader, CampaignState, DMScreenGraphQLRepository, GameUI}
import japgolly.scalajs.react.callback.*

object DND5eCampaignState {

  def load(campaign: Campaign): AsyncCallback[DND5eCampaignState] = {
    for {
      races       <- DND5eGraphQLRepository.live.races
      backgrounds <- DND5eGraphQLRepository.live.backgrounds
      classes     <- DND5eGraphQLRepository.live.classes
    } yield {
      DND5eCampaignState(
        campaign = campaign,
        races = races,
        backgrounds = backgrounds,
        classes = classes
      )
    }
  }

}

// Note, don't add to the state stuff that changes often, that belongs lower down in the hierarchy
case class DND5eCampaignState(
  campaign:    Campaign,
  backgrounds: Seq[Background] = Seq.empty,
  races:       Seq[Race] = Seq.empty,
  classes:     Seq[CharacterClass] = Seq.empty,
  changeStack: ChangeStack = ChangeStack()

  // NOTES,
  // adding an entry into one of the above lists doesn't save it to the server, in fact... you should probably allow ajax to add it to the list AFTER it's been saved to the server
  // and has a proper id
  // If you delete an entry from the above lists, you should also make sure the entry doesn't exist in the changeStack
) extends CampaignState {

  override def campaignHeader: CampaignHeader = campaign.header

  override def saveChanges(): AsyncCallback[CampaignState] = {
    for {
      _ <- Callback
        .log(
          s"Saving Changes: (campaign = ${changeStack.campaign}, pcs = ${changeStack.pcs.keys
              .mkString(",")}, npcs = ${changeStack.npcs.keys.mkString(",")}, scenes = ${changeStack.scenes.keys
              .mkString(",")}, encounters = ${changeStack.encounters.keys.mkString(",")})"
        ).asAsyncCallback
      _ <- DMScreenGraphQLRepository.live
        .upsert(campaign.header, campaign.jsonInfo)
        .when(changeStack.campaign)
      _ <- AsyncCallback.traverse(changeStack.pcs.values)(pc =>
        DND5eGraphQLRepository.live.upsert(pc.header, pc.jsonInfo)
      )
      _ <- AsyncCallback.traverse(changeStack.npcs.values)(npc =>
        DND5eGraphQLRepository.live.upsert(npc.header, npc.jsonInfo)
      )
      _ <- AsyncCallback.traverse(changeStack.scenes.values)(scene =>
        DND5eGraphQLRepository.live.upsert(scene.header, scene.jsonInfo)
      )
      _ <- AsyncCallback.traverse(changeStack.encounters.values)(encounter =>
        DND5eGraphQLRepository.live.upsert(encounter.header, encounter.jsonInfo)
      )
    } yield copy(changeStack = ChangeStack())
  }

  override def loadChanges(): AsyncCallback[CampaignState] = ???

  override def gameUI: GameUI = DND5eUI

}
