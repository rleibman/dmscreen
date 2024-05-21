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

object GraphQLClientGameService {

  val live: ZLayer[ClientConfiguration, Nothing, DND5eGameService] = ZLayer.fromZIO(for {
    config <- ZIO.service[ClientConfiguration]
  } yield new DND5eGameService {
    override def campaigns: ZIO[Any, DMScreenError, Seq[CampaignHeader]] = ???

    override def campaign(campaignId: CampaignId): ZIO[Any, DMScreenError, Option[Campaign]] = ???

    override def insert(
      campaignHeader: CampaignHeader,
      info:           Json
    ): ZIO[Any, DMScreenError, CampaignId] = ???

    override def applyOperation[IDType](
      id:        IDType,
      operation: DMScreenOperation
    ): ZIO[Any, DMScreenError, Unit] = ???

    override def delete(
      campaignId: CampaignId,
      softDelete: Boolean
    ): ZIO[Any, DMScreenError, Unit] = ???

    override def playerCharacters(campaignId: CampaignId): ZIO[Any, DMScreenError, Seq[PlayerCharacter]] = ???

    override def nonPlayerCharacters(campaignId: CampaignId): ZIO[Any, DMScreenError, Seq[NonPlayerCharacter]] = ???

    override def encounters(campaignId: CampaignId): ZIO[Any, DMScreenError, Seq[EncounterHeader]] = ???

    override def encounter(encounterId: EncounterId): ZIO[Any, DMScreenError, Seq[Encounter]] = ???

    override def bestiary(search: MonsterSearch): ZIO[Any, DMScreenError, Seq[Monster]] = ???

    override def sources: ZIO[Any, DMScreenError, Seq[Source]] = ???

    override def classes: ZIO[Any, DMScreenError, Seq[CharacterClass]] = ???

    override def races: ZIO[Any, DMScreenError, Seq[Race]] = ???

    override def backgrounds: ZIO[Any, DMScreenError, Seq[Background]] = ???

    override def subClasses(characterClass: CharacterClassId): ZIO[Any, DMScreenError, Seq[Subclass]] = ???
  })

}

//given Conversion[ZIO[Any, Throwable, A], AsynCallback[A]] = { zio =>
//  ???
//}
