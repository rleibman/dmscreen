package dmscreen.dnd5e

import dmscreen.{ClientConfiguration, DMScreenError, Operation}
import zio.*
import zio.json.ast.Json

object GraphQLClientGameService {

  val live = for {
    config <- ZIO.service[ClientConfiguration]
  } yield new DND5eGameService[ClientConfiguration] {
    override def campaigns: ZIO[ClientConfiguration, DMScreenError, Seq[CampaignHeader]] = ???

    override def campaign(campaignId: CampaignId): ZIO[ClientConfiguration, DMScreenError, Option[Campaign]] = ???

    override def insert(
      campaignHeader: CampaignHeader,
      info:           Json
    ): ZIO[ClientConfiguration, DMScreenError, CampaignId] = ???

    override def applyOperation(
      campaignId: CampaignId,
      operation:  Operation
    ): ZIO[ClientConfiguration, DMScreenError, Unit] = ???

    override def delete(
      campaignId: CampaignId,
      softDelete: Boolean
    ): ZIO[ClientConfiguration, DMScreenError, Unit] = ???

    override def playerCharacters(campaignId: CampaignId)
      : ZIO[ClientConfiguration, DMScreenError, Seq[PlayerCharacter]] = ???

    override def nonPlayerCharacters(campaignId: CampaignId)
      : ZIO[ClientConfiguration, DMScreenError, Seq[NonPlayerCharacter]] = ???

    override def encounters(campaignId: CampaignId): ZIO[ClientConfiguration, DMScreenError, Seq[EncounterHeader]] = ???

    override def encounter(encounterId: EncounterId): ZIO[ClientConfiguration, DMScreenError, Seq[Encounter]] = ???

    override def bestiary(search: MonsterSearch): ZIO[ClientConfiguration, DMScreenError, Seq[Monster]] = ???

    override def sources: ZIO[ClientConfiguration, DMScreenError, Seq[Source]] = ???

    override def classes: ZIO[ClientConfiguration, DMScreenError, Seq[CharacterClass]] = ???

    override def races: ZIO[ClientConfiguration, DMScreenError, Seq[Race]] = ???

    override def backgrounds: ZIO[ClientConfiguration, DMScreenError, Seq[Background]] = ???

    override def subClasses(characterClass: CharacterClassId): ZIO[ClientConfiguration, DMScreenError, Seq[Subclass]] =
      ???
  }

}

given Conversion[ZIO[Any, Throwable, A], AsynCallback[A]] = { zio =>
  ???
}
