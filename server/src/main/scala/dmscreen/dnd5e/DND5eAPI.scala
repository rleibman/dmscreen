package dmscreen.dnd5e

import caliban.*
import caliban.schema.*
import caliban.schema.Schema.auto.*
import caliban.schema.ArgBuilder.auto.*
import dmscreen.{DMScreenEnvironment, DMScreenError}
import zio.*

import java.net.URL

object DND5eAPI {

  private given Schema[Any, CampaignId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CharacterClassId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, MonsterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, PlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, URL] = Schema.stringSchema.contramap(_.toString)

  private given ArgBuilder[PlayerCharacterId] = ArgBuilder.long.map(PlayerCharacterId.apply)
  private given ArgBuilder[CharacterClassId] = ArgBuilder.long.map(CharacterClassId.apply)
  private given ArgBuilder[CampaignId] = ArgBuilder.long.map(CampaignId.apply)

  case class Queries(
    campaigns:           ZIO[DMScreenEnvironment, DMScreenError, Seq[CampaignHeader]],
    campaign:            CampaignId => ZIO[DMScreenEnvironment, DMScreenError, Option[Campaign]],
    playerCharacters:    CampaignId => ZIO[DMScreenEnvironment, DMScreenError, Seq[PlayerCharacter]],
    nonPlayerCharacters: CampaignId => ZIO[DMScreenEnvironment, DMScreenError, Seq[NonPlayerCharacter]],
    bestiary:            MonsterSearch => ZIO[DMScreenEnvironment, DMScreenError, Seq[Monster]],
    sources:             ZIO[DMScreenEnvironment, DMScreenError, Seq[Source]],
    classes:             ZIO[DMScreenEnvironment, DMScreenError, Seq[CharacterClass]],
    races:               ZIO[DMScreenEnvironment, DMScreenError, Seq[Race]],
    backgrounds:         ZIO[DMScreenEnvironment, DMScreenError, Seq[Background]],
    subclasses:          CharacterClassId => ZIO[DMScreenEnvironment, DMScreenError, Seq[Subclass]]
  )
  case class Mutations()
  case class Subscriptions()

  lazy val api: GraphQL[DMScreenEnvironment] = graphQL[DMScreenEnvironment, Queries, Mutations, Subscriptions](
    RootResolver(
      Queries(
        campaigns = ZIO.serviceWithZIO[DND5eGameService](_.campaigns),
        campaign = campaignId => ZIO.serviceWithZIO[DND5eGameService](_.campaign(campaignId)),
        playerCharacters = campaignId => ZIO.serviceWithZIO[DND5eGameService](_.playerCharacters(campaignId)),
        nonPlayerCharacters = campaignId => ZIO.serviceWithZIO[DND5eGameService](_.nonPlayerCharacters(campaignId)),
        bestiary = search => ZIO.serviceWithZIO[DND5eGameService](_.bestiary(search)),
        sources = ZIO.serviceWithZIO[DND5eGameService](_.sources),
        classes = ZIO.serviceWithZIO[DND5eGameService](_.classes),
        races = ZIO.serviceWithZIO[DND5eGameService](_.races),
        backgrounds = ZIO.serviceWithZIO[DND5eGameService](_.backgrounds),
        subclasses = characterClassId => ZIO.serviceWithZIO[DND5eGameService](_.subClasses(characterClassId))
      ),
      Mutations(),
      Subscriptions()
    )
  )

}
