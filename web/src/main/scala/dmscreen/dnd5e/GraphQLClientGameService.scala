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
