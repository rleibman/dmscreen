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

import caliban.client.SelectionBuilder
//import caliban.client.scalajs.STAClient.Queries
//import caliban.client.scalajs.STAClient.{
//  Background as CalibanBackground,
//  Campaign as CalibanCampaign,
//  CampaignHeader as CalibanCampaignHeader,
//  CampaignStatus as CalibanCampaignStatus,
//  CharacterClass as CalibanCharacterClass,
//  DiceRoll as CalibanDiceRoll,
//  Encounter as CalibanEncounter,
//  EncounterHeader as CalibanEncounterHeader,
//  GameSystem as CalibanGameSystem,
//  PlayerCharacter as CalibanPlayerCharacter,
//  PlayerCharacterHeader as CalibanPlayerCharacterHeader,
//  Queries,
//  Scene as CalibanScene,
//  SceneHeader as CalibanSceneHeader
//}
import dmscreen.*
import japgolly.scalajs.react.callback.*
import zio.json.ast.Json

object STACampaignState {

  def load(campaign: Campaign): AsyncCallback[STACampaignState] = {
    for {
      _ <- AsyncCallback.pure(println("pretending to load all the info for an sta campaign"))
    } yield {
      STACampaignState(
        campaign = campaign
      )
    }
  }

}

case class STACampaignState(
  campaign:    Campaign,
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
          s"Saving Changes"
        ).asAsyncCallback
    } yield copy(changeStack = ChangeStack())
  }

  override def loadChanges(): AsyncCallback[CampaignState] = ???

  override def gameUI: GameUI = STAUI

}
