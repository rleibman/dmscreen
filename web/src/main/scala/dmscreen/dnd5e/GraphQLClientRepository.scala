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
import zio.*
import zio.json.ast.Json

import java.util.ResourceBundle
import scala.reflect.ClassTag

object GraphQLClientRepository {

  val live: ZLayer[ClientConfiguration, Nothing, DND5eRepository] = ZLayer.fromZIO(for {
    config <- ZIO.service[ClientConfiguration]
  } yield {

    new DND5eRepository {
      override def campaigns: IO[DMScreenError, Seq[CampaignHeader]] = ???

      override def campaign(campaignId: CampaignId): IO[DMScreenError, Option[DND5eCampaign]] = ???

      override def scene(sceneId: SceneId): IO[DMScreenError, Option[Scene]] = ???

      override def applyOperations[IDType](
        entityType: EntityType,
        id:         IDType,
        operations: DMScreenEvent*
      ): IO[DMScreenError, Unit] = ???

      override def deleteEntity[IDType](
        entityType: EntityType,
        id:         IDType,
        softDelete: Boolean
      ): IO[DMScreenError, Unit] = ???

      override def playerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[PlayerCharacter]] = ???

      override def scenes(campaignId: CampaignId): IO[DMScreenError, Seq[Scene]] = ???

      override def playerCharacter(playerCharacterId: PlayerCharacterId): IO[DMScreenError, Option[PlayerCharacter]] =
        ???

      override def nonPlayerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[NonPlayerCharacter]] = ???

      override def encounters(campaignId: CampaignId): IO[DMScreenError, Seq[Encounter]] = ???

      override def bestiary(search: MonsterSearch): IO[DMScreenError, MonsterSearchResults] = ???

      override def sources: IO[DMScreenError, Seq[Source]] = ???

      override def classes: IO[DMScreenError, Seq[CharacterClass]] = ???

      override def races: IO[DMScreenError, Seq[Race]] = ???

      override def backgrounds: IO[DMScreenError, Seq[Background]] = ???

      override def subClasses(characterClass: CharacterClassId): IO[DMScreenError, Seq[SubClass]] = ???

      override def spells: IO[DMScreenError, Seq[Spell]] = ???

      override def upsert(
        campaignHeader: CampaignHeader,
        info:           Json
      ): IO[DMScreenError, CampaignId] = ???

      override def upsert(
        playerCharacterHeader: PlayerCharacterHeader,
        info:                  Json
      ): IO[DMScreenError, PlayerCharacterId] = ???

      override def upsert(
        nonPlayerCharacterHeader: NonPlayerCharacterHeader,
        info:                     Json
      ): IO[DMScreenError, NonPlayerCharacterId] = ???

      override def upsert(
        monsterHeader: MonsterHeader,
        info:          Json
      ): IO[DMScreenError, MonsterId] = ???

      override def upsert(
        spellHeader: SpellHeader,
        info:        Json
      ): IO[DMScreenError, SpellId] = ???

      override def upsert(
        encounterHeader: EncounterHeader,
        info:            Json
      ): IO[DMScreenError, EncounterId] = ???

      override def upsert(
        sceneHeader: SceneHeader,
        info:        Json
      ): IO[DMScreenError, SceneId] = ???
    }
  })

}

//given Conversion[IO[Throwable, A], AsynCallback[A]] = { zio =>
//  ???
//}
