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

import caliban.ScalaJSClientAdapter.asyncCalibanCall
import caliban.client.SelectionBuilder
import caliban.client.scalajs.DND5eClient.Queries
import caliban.client.scalajs.DND5eClient.{
  Background as CalibanBackground,
  Campaign as CalibanCampaign,
  CampaignHeader as CalibanCampaignHeader,
  CampaignStatus as CalibanCampaignStatus,
  CharacterClass as CalibanCharacterClass,
  DiceRoll as CalibanDiceRoll,
  Encounter as CalibanEncounter,
  EncounterHeader as CalibanEncounterHeader,
  GameSystem as CalibanGameSystem,
  PlayerCharacter as CalibanPlayerCharacter,
  PlayerCharacterHeader as CalibanPlayerCharacterHeader,
  Queries,
  Scene as CalibanScene,
  SceneHeader as CalibanSceneHeader
}
import dmscreen.{
  Campaign,
  CampaignHeader,
  CampaignId,
  CampaignState,
  CampaignStatus,
  DiceRoll,
  GameSystem,
  GameUI,
  UserId
}
import japgolly.scalajs.react.callback.*
import zio.json.ast.Json

object DND5eCampaignState {

  def load(campaign: Campaign): AsyncCallback[DND5eCampaignState] = {
    for {
      backgrounds <- {
        val sb = Queries.backgrounds(CalibanBackground.name.map(Background.apply))
        asyncCalibanCall(sb).map(_.toList.flatten)
      }
      classes <- {
        val sb = Queries.classes(
          (CalibanCharacterClass.id ~ CalibanCharacterClass.hitDice(CalibanDiceRoll.roll)).map(
            (
              id,
              hd
            ) =>
              CharacterClass(
                CharacterClassId.values.find(_.name.equalsIgnoreCase(id)).getOrElse(CharacterClassId.unknown),
                DiceRoll(hd)
              )
          )
        )
        asyncCalibanCall(sb).map(_.toList.flatten)
      }
      campaignData <- {
        val playerCharacterSB: SelectionBuilder[CalibanPlayerCharacter, PlayerCharacter] =
          (CalibanPlayerCharacter.header(
            CalibanPlayerCharacterHeader.campaignId ~
              CalibanPlayerCharacterHeader.id ~
              CalibanPlayerCharacterHeader.name ~
              CalibanPlayerCharacterHeader.playerName
          ) ~ CalibanPlayerCharacter.jsonInfo).map {
            (
              campaignId: Long,
              pcId:       Long,
              name:       String,
              playerName: Option[String],
              info:       Json
            ) =>
              PlayerCharacter(
                PlayerCharacterHeader(
                  id = PlayerCharacterId(pcId),
                  campaignId = CampaignId(campaignId),
                  name = name,
                  playerName = playerName
                ),
                info
              )
          }

        val sceneSB: SelectionBuilder[CalibanScene, Scene] = (CalibanScene.header(
          CalibanSceneHeader.id ~ CalibanSceneHeader.campaignId ~ CalibanSceneHeader.name ~ CalibanSceneHeader.orderCol ~ CalibanSceneHeader.isActive
        ) ~ CalibanScene.jsonInfo).map {
          (
            id:         Long,
            campaignId: Long,
            name:       String,
            orderCol:   Int,
            isActive:   Boolean,
            info:       Json
          ) =>
            Scene(
              SceneHeader(
                id = SceneId(id),
                campaignId = CampaignId(campaignId),
                name = name,
                orderCol = orderCol,
                isActive = isActive
              ),
              info
            )
        }

        val encounterSB: SelectionBuilder[CalibanEncounter, Encounter] = (CalibanEncounter.header(
          CalibanEncounterHeader.id ~
            CalibanEncounterHeader.campaignId ~
            CalibanEncounterHeader.name ~
            CalibanEncounterHeader.status ~
            CalibanEncounterHeader.sceneId ~
            CalibanEncounterHeader.orderCol
        ) ~ CalibanEncounter.jsonInfo).map {
          (
            id:         Long,
            campaignId: Long,
            name:       String,
            status:     String,
            sceneId:    Option[Long],
            orderCol:   Int,
            info:       Json
          ) =>
            Encounter(
              EncounterHeader(
                EncounterId(id),
                CampaignId(campaignId),
                name,
                EncounterStatus.valueOf(status),
                sceneId.map(SceneId.apply),
                orderCol
              ),
              info
            )
        }

        val totalSB =
          (
            Queries.playerCharacters(campaign.header.id.value)(playerCharacterSB) ~
              Queries.scenes(campaign.header.id.value)(sceneSB) ~
              Queries.encounters(campaign.header.id.value)(encounterSB)
          )
            .map {
              (
                pcsOpt,
                scenesOpt,
                encountersOpt
              ) =>
                (pcsOpt.toList.flatten, scenesOpt.toList.flatten, encountersOpt.toList.flatten)
            }
        asyncCalibanCall(totalSB)
      }
    } yield {
      DND5eCampaignState(
        campaign = campaign,
        backgrounds = backgrounds,
        classes = classes,
        pcs = campaignData._1,
        scenes = campaignData._2,
        encounters = campaignData._3
      )
    }
  }

}

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
