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

import dmscreen.{Campaign, CampaignHeader, CampaignState, GameUI}
import japgolly.scalajs.react.callback.*

case class DND5eCampaignState(
  campaign:    Campaign,
  backgrounds: Seq[Background] = Seq.empty,
  classes:     Seq[CharacterClass] = Seq.empty,
  pcs:         List[PlayerCharacter] = List.empty,
  npcs:        List[NonPlayerCharacter] = List.empty,
  scenes:      List[Scene] = List.empty,
  encounters:  List[Encounter] = List.empty,
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
          s"Saving Changes: (campaign = ${changeStack.campaign}, pcs = ${changeStack.pcs.mkString(",")}, npcs = ${changeStack.npcs
              .mkString(",")}, scenes = ${changeStack.scenes.mkString(",")}, encounters = ${changeStack.encounters
              .mkString(",")}, monsters = ${changeStack.monsters.mkString(",")})"
        ).asAsyncCallback
      campaign <- GraphQLRepository.live
        .upsert(campaign.header, campaign.jsonInfo)
        .when(changeStack.campaign)
      _ <- AsyncCallback.traverse(changeStack.pcs)(pcId =>
        pcs
          .find(_.id == pcId)
          .fold(AsyncCallback.throwException(RuntimeException("Attempted to save a pc that doesn't exist")))(pc =>
            GraphQLRepository.live.upsert(pc.header, pc.jsonInfo)
          )
      )
      _ <- AsyncCallback.traverse(changeStack.npcs)(npcId =>
        npcs
          .find(_.id == npcId)
          .fold(AsyncCallback.throwException(RuntimeException("Attempted to save a npc that doesn't exist")))(npc =>
            GraphQLRepository.live.upsert(npc.header, npc.jsonInfo)
          )
      )
      _ <- AsyncCallback.traverse(changeStack.scenes)(sceneId =>
        scenes
          .find(_.id == sceneId)
          .fold(AsyncCallback.throwException(RuntimeException("Attempted to save a scene that doesn't exist")))(scene =>
            GraphQLRepository.live.upsert(scene.header, scene.jsonInfo)
          )
      )
      _ <- AsyncCallback.traverse(changeStack.encounters)(encounterId =>
        encounters
          .find(_.id == encounterId)
          .fold(AsyncCallback.throwException(RuntimeException("Attempted to save an encounter that doesn't exist")))(
            encounter => GraphQLRepository.live.upsert(encounter.header, encounter.jsonInfo)
          )
      )
    } yield copy(changeStack = ChangeStack())
  }

  override def loadChanges(): AsyncCallback[CampaignState] = ???

  override def gameUI: GameUI = DND5eUI

}
