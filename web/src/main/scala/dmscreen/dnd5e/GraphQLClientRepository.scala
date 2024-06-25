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
import japgolly.scalajs.react.callback.AsyncCallback
import zio.*
import zio.json.ast.Json

import java.util.ResourceBundle
import scala.reflect.ClassTag

object GraphQLClientRepository {

  val live: ZLayer[ClientConfiguration, Nothing, DND5eRepository[AsyncCallback]] = ZLayer.fromZIO(for {
    config <- ZIO.service[ClientConfiguration]
  } yield {

    new DND5eRepository[AsyncCallback] {
      override def campaigns: AsyncCallback[Seq[CampaignHeader]] = ???

      override def campaign(campaignId: CampaignId): AsyncCallback[Option[DND5eCampaign]] = ???

      override def scene(sceneId: SceneId): AsyncCallback[Option[Scene]] = ???

      override def applyOperations[IDType](
        entityType: EntityType,
        id:         IDType,
        operations: DMScreenEvent*
      ): AsyncCallback[Unit] = ???

      override def deleteEntity[IDType](
        entityType: EntityType,
        id:         IDType,
        softDelete: Boolean
      ): AsyncCallback[Unit] = ???

      override def playerCharacters(campaignId: CampaignId): AsyncCallback[Seq[PlayerCharacter]] = ???

      override def scenes(campaignId: CampaignId): AsyncCallback[Seq[Scene]] = ???

      override def playerCharacter(playerCharacterId: PlayerCharacterId): AsyncCallback[Option[PlayerCharacter]] = ???

      override def nonPlayerCharacters(campaignId: CampaignId): AsyncCallback[Seq[NonPlayerCharacter]] = ???

      override def encounters(campaignId: CampaignId): AsyncCallback[Seq[Encounter]] = ???

      override def bestiary(search: MonsterSearch): AsyncCallback[MonsterSearchResults] = ???

      override def sources: AsyncCallback[Seq[Source]] = ???

      override def classes: AsyncCallback[Seq[CharacterClass]] = ???

      override def races: AsyncCallback[Seq[Race]] = ???

      override def backgrounds: AsyncCallback[Seq[Background]] = ???

      override def subClasses(characterClass: CharacterClassId): AsyncCallback[Seq[SubClass]] = ???

      override def spells: AsyncCallback[Seq[Spell]] = ???

      override def upsert(
        campaignHeader: CampaignHeader,
        info:           Json
      ): AsyncCallback[CampaignId] = ???

      override def upsert(
        playerCharacterHeader: PlayerCharacterHeader,
        info:                  Json
      ): AsyncCallback[PlayerCharacterId] = ???

      override def upsert(
        nonPlayerCharacterHeader: NonPlayerCharacterHeader,
        info:                     Json
      ): AsyncCallback[NonPlayerCharacterId] = ???

      override def upsert(
        monsterHeader: MonsterHeader,
        info:          Json
      ): AsyncCallback[MonsterId] = ???

      override def upsert(
        spellHeader: SpellHeader,
        info:        Json
      ): AsyncCallback[SpellId] = ???

      override def upsert(
        encounterHeader: EncounterHeader,
        info:            Json
      ): AsyncCallback[EncounterId] = ???

      override def upsert(
        sceneHeader: SceneHeader,
        info:        Json
      ): AsyncCallback[SceneId] = ???
    }
  })

}
