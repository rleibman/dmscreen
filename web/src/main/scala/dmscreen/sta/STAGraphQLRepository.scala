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

import caliban.client.scalajs.{STAClient, given}
import caliban.client.{ArgEncoder, SelectionBuilder}
import dmscreen.*
import caliban.client.scalajs.STAClient.{
  Character as CalibanCharacter,
  CharacterHeader as CalibanCharacterHeader,
  CharacterHeaderInput,
  Mutations,
  Queries
}
import japgolly.scalajs.react.callback.AsyncCallback
import zio.*
import zio.json.*
import zio.json.ast.Json

import java.util.ResourceBundle
import scala.reflect.ClassTag

object STAGraphQLRepository {

  val live: STARepository[AsyncCallback] = new STARepository[AsyncCallback] {

    private val calibanClient = caliban.ScalaJSClientAdapter("sta")

    private val characterSB: SelectionBuilder[CalibanCharacter, Character] =
      (CalibanCharacter.header(
        CalibanCharacterHeader.campaignId ~
          CalibanCharacterHeader.id ~
          CalibanCharacterHeader.name ~
          CalibanCharacterHeader.playerName
      ) ~ CalibanCharacter.jsonInfo).map {
        (
          campaignId: Long,
          pcId:       Long,
          name:       Option[String],
          playerName: Option[String],
          info:       Json
        ) =>
          Character(
            CharacterHeader(
              id = CharacterId(pcId),
              campaignId = CampaignId(campaignId),
              name = name,
              playerName = playerName
            ),
            info
          )
      }

    override def deleteEntity[IDType](
      entityType: EntityType[IDType],
      id:         IDType,
      softDelete: Boolean
    ): AsyncCallback[Unit] = ???

    override def characters(campaignId: CampaignId): AsyncCallback[Seq[Character]] = {
      calibanClient
        .asyncCalibanCall(Queries.characters(campaignId.value)(characterSB))
        .map(_.toSeq.flatten)

    }

    override def starships(campaignId: CampaignId): AsyncCallback[Seq[Starship]] = ???

    override def scenes(campaignId: CampaignId): AsyncCallback[Seq[Scene]] = ???

    override def character(characterId: CharacterId): AsyncCallback[Option[Character]] = ???

    override def nonPlayerCharacters(campaignId: CampaignId): AsyncCallback[Seq[NonPlayerCharacter]] = ???

    override def upsert(
      header: CharacterHeader,
      info:   Json
    ): AsyncCallback[CharacterId] = {
      val headerInput = CharacterHeaderInput(
        id = header.id.value,
        campaignId = header.campaignId.value,
        name = header.name,
        playerName = header.playerName
      )

      val sb = Mutations.upsertCharacter(headerInput, info, dmscreen.BuildInfo.version)
      calibanClient.asyncCalibanCall(sb).map(_.fold(CharacterId.empty)(CharacterId.apply))
    }

    override def upsert(
      header: StarshipHeader,
      info:   Json
    ): AsyncCallback[StarshipId] = ???

    override def upsert(
      header: NonPlayerCharacterHeader,
      info:   Json
    ): AsyncCallback[NonPlayerCharacterId] = ???

    override def upsert(
      header: SceneHeader,
      info:   Json
    ): AsyncCallback[SceneId] = ???

    override def nonPlayerCharacter(nonPlayerCharacterId: NonPlayerCharacterId)
      : AsyncCallback[Option[NonPlayerCharacter]] = ???

    override def snapshot(
      oldId: CampaignId,
      newId: CampaignId
    ): AsyncCallback[Unit] = ???
  }

}
