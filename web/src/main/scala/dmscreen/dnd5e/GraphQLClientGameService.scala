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

import dmscreen.{ClientConfiguration, DMScreenError, DMScreenOperation}
import zio.*
import zio.json.ast.Json

import java.util.ResourceBundle
import scala.reflect.ClassTag

object GraphQLClientGameService {

  val live: ZLayer[ClientConfiguration, Nothing, DND5eGameService] = ZLayer.fromZIO(for {
    config <- ZIO.service[ClientConfiguration]
  } yield {

    new DND5eGameService {
      override def campaigns: IO[DMScreenError, Seq[CampaignHeader]] = ???

      override def campaign(campaignId: CampaignId): IO[DMScreenError, Option[Campaign]] = ???

      override def insert(
        campaignHeader: CampaignHeader,
        info:           Json
      ): IO[DMScreenError, CampaignId] = ???

      override def applyOperation[IDType: ClassTag](
        id:        IDType,
        operation: DMScreenOperation
      )(using ev:  ClassTag[IDType]
      ): IO[DMScreenError, Unit] = ???

      override def delete(
        campaignId: CampaignId,
        softDelete: Boolean
      ): IO[DMScreenError, Unit] = ???

      override def playerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[PlayerCharacter]] = ???

      override def nonPlayerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[NonPlayerCharacter]] = ???

      override def encounters(campaignId: CampaignId): IO[DMScreenError, Seq[EncounterHeader]] = ???

      override def encounter(encounterId: EncounterId): IO[DMScreenError, Seq[Encounter]] = ???

      override def bestiary(search: MonsterSearch): IO[DMScreenError, Seq[Monster]] = ???

      override def sources: IO[DMScreenError, Seq[Source]] = ???

      override def classes: IO[DMScreenError, Seq[CharacterClass]] = ???

      override def races: IO[DMScreenError, Seq[Race]] = ???

      override def backgrounds: IO[DMScreenError, Seq[Background]] = ???

      override def subClasses(characterClass: CharacterClassId): IO[DMScreenError, Seq[Subclass]] = {
        ZIO.succeed(Seq(Subclass("Oath of Vengance")))
      }

      override def spells: IO[DMScreenError, Seq[Spell]] = ???

      override def insert(
        playerCharacterHeader: PlayerCharacterHeader,
        info:                  Json
      ): IO[DMScreenError, PlayerCharacterId] = ???

      override def insert(
        nonPlayerCharacterHeader: NonPlayerCharacterHeader,
        info:                     Json
      ): IO[DMScreenError, NonPlayerCharacterId] = ???

      override def insert(
        monsterHeader: MonsterHeader,
        info:          Json
      ): IO[DMScreenError, MonsterId] = ???

      override def insert(
        spellHeader: SpellHeader,
        info:        Json
      ): IO[DMScreenError, SpellId] = ???

      override def insert(
        encounterHeader: EncounterHeader,
        info:            Json
      ): IO[DMScreenError, EncounterId] = ???

      override def deletePlayerCharacter(
        playerCharacterId: PlayerCharacterId,
        softDelete:        Boolean
      ): IO[DMScreenError, Unit] = ???

      override def deleteNonPlayerCharacter(
        nonPlayerCharacterId: NonPlayerCharacterId,
        softDelete:           Boolean
      ): IO[DMScreenError, Unit] = ???

      override def deleteMonster(
        monsterId:  MonsterId,
        softDelete: Boolean
      ): IO[DMScreenError, Unit] = ???

      override def deleteSpell(
        spellId:    SpellId,
        softDelete: Boolean
      ): IO[DMScreenError, Unit] = ???

      override def deleteEncounter(
        encounterId: EncounterId,
        softDelete:  Boolean
      ): IO[DMScreenError, Unit] = ???
    }
  })

}

//given Conversion[IO[Throwable, A], AsynCallback[A]] = { zio =>
//  ???
//}
