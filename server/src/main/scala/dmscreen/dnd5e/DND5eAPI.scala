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

import caliban.*
import caliban.CalibanError.ExecutionError
import caliban.interop.zio.*
import caliban.interop.zio.json.*
import caliban.introspection.adt.__Type
import caliban.schema.*
import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import caliban.schema.Types.makeScalar
import dmscreen.*
import dmscreen.dnd5e.QuillDND5eRepository.DND5eZIORepository
import just.semver.SemVer
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.stream.*

import java.net.URL

object DND5eAPI {

  case class CampaignEventsArgs(
    entityType: DND5eEntityType,
    id:         Long,
    events:     Seq[Json]
  )

  private given Schema[Any, UserId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CampaignId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CharacterClassId] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, MonsterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, PlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, NonPlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, EncounterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, SceneId] = Schema.longSchema.contramap(_.value)

  private given Schema[Any, ChallengeRating] = Schema.doubleSchema.contramap(_.value)
  private given Schema[Any, SourceId] = Schema.stringSchema.contramap(_.value)
  private given Schema[Any, URL] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, SemVer] = Schema.stringSchema.contramap(_.render)
  private given Schema[Any, EncounterStatus] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, GeneralLog] = Schema.gen[Any, GeneralLog]
  private given Schema[Any, CombatLog] = Schema.gen[Any, CombatLog]
  private given Schema[Any, DMScreenEvent] = Schema.gen[Any, DMScreenEvent]
  private given Schema[Any, Source] = Schema.gen[Any, Source]
  private given Schema[Any, MonsterSearch] = Schema.gen[Any, MonsterSearch]

  private given ArgBuilder[PlayerCharacterId] = ArgBuilder.long.map(PlayerCharacterId.apply)
  private given ArgBuilder[SceneId] = ArgBuilder.long.map(SceneId.apply)
  private given ArgBuilder[NonPlayerCharacterId] = ArgBuilder.long.map(NonPlayerCharacterId.apply)
  private given ArgBuilder[CharacterClassId] =
    ArgBuilder.string.map(s => CharacterClassId.values.find(a => s.equalsIgnoreCase(a.toString)).get)
  private given ArgBuilder[SourceId] = ArgBuilder.string.map(SourceId.apply)
  private given ArgBuilder[CampaignId] = ArgBuilder.long.map(CampaignId.apply)
  private given ArgBuilder[EncounterId] = ArgBuilder.long.map(EncounterId.apply)
  private given ArgBuilder[ChallengeRating] = ArgBuilder.double.map(n => ChallengeRating.fromDouble(n).get)

  private given ArgBuilder[GeneralLog] = ArgBuilder.gen[GeneralLog]
  private given ArgBuilder[CombatLog] = ArgBuilder.gen[CombatLog]
  private given ArgBuilder[DMScreenEvent] = ArgBuilder.gen[DMScreenEvent]
  private given ArgBuilder[Add] = ArgBuilder.gen[Add]
  private given ArgBuilder[Copy] = ArgBuilder.gen[Copy]
  private given ArgBuilder[Move] = ArgBuilder.gen[Move]
  private given ArgBuilder[Remove] = ArgBuilder.gen[Remove]
  private given ArgBuilder[Replace] = ArgBuilder.gen[Replace]
  private given ArgBuilder[Test] = ArgBuilder.gen[Test]
  private given ArgBuilder[MonsterSearch] = ArgBuilder.gen[MonsterSearch]
  private given ArgBuilder[CampaignEventsArgs] = ArgBuilder.gen[CampaignEventsArgs]

  case class Queries(
    campaigns:           ZIO[DND5eZIORepository, DMScreenError, Seq[CampaignHeader]],
    campaign:            CampaignId => ZIO[DND5eZIORepository, DMScreenError, Option[DND5eCampaign]],
    playerCharacters:    CampaignId => ZIO[DND5eZIORepository, DMScreenError, Seq[PlayerCharacter]],
    scenes:              CampaignId => ZIO[DND5eZIORepository, DMScreenError, Seq[Scene]],
    nonPlayerCharacters: CampaignId => ZIO[DND5eZIORepository, DMScreenError, Seq[NonPlayerCharacter]],
    encounters:          CampaignId => ZIO[DND5eZIORepository, DMScreenError, Seq[Encounter]],
    bestiary:            MonsterSearch => ZIO[DND5eZIORepository, DMScreenError, MonsterSearchResults],
    sources:             ZIO[DND5eZIORepository, DMScreenError, Seq[Source]],
    classes:             ZIO[DND5eZIORepository, DMScreenError, Seq[CharacterClass]],
    races:               ZIO[DND5eZIORepository, DMScreenError, Seq[Race]],
    backgrounds:         ZIO[DND5eZIORepository, DMScreenError, Seq[Background]],
    subclasses:          CharacterClassId => ZIO[DND5eZIORepository, DMScreenError, Seq[SubClass]]
  )
  case class Mutations(
    applyOperations: CampaignEventsArgs => ZIO[DND5eZIORepository, DMScreenError, Unit]
  )
  case class Subscriptions(
    campaignStream: CampaignEventsArgs => ZStream[DND5eZIORepository, DMScreenError, DMScreenEvent]
  )

  lazy val api: GraphQL[DMScreenServerEnvironment] =
    graphQL[
      DMScreenServerEnvironment,
      Queries,
      Mutations,
      Subscriptions
    ](
      RootResolver(
        Queries(
          campaigns = ZIO.serviceWithZIO[DND5eZIORepository](_.campaigns),
          campaign = campaignId => ZIO.serviceWithZIO[DND5eZIORepository](_.campaign(campaignId)),
          playerCharacters = campaignId => ZIO.serviceWithZIO[DND5eZIORepository](_.playerCharacters(campaignId)),
          scenes = campaignId => ZIO.serviceWithZIO[DND5eZIORepository](_.scenes(campaignId)),
          nonPlayerCharacters = campaignId => ZIO.serviceWithZIO[DND5eZIORepository](_.nonPlayerCharacters(campaignId)),
          encounters = campaignId => ZIO.serviceWithZIO[DND5eZIORepository](_.encounters(campaignId)),
          bestiary = search => ZIO.serviceWithZIO[DND5eZIORepository](_.bestiary(search)),
          sources = ZIO.serviceWithZIO[DND5eZIORepository](_.sources),
          classes = ZIO.serviceWithZIO[DND5eZIORepository](_.classes),
          races = ZIO.serviceWithZIO[DND5eZIORepository](_.races),
          backgrounds = ZIO.serviceWithZIO[DND5eZIORepository](_.backgrounds),
          subclasses = characterClassId => ZIO.serviceWithZIO[DND5eZIORepository](_.subClasses(characterClassId))
        ),
        Mutations(applyOperations =
          args => ???
          // ZIO.serviceWithZIO[DND5eRepository](_.applyOperations(args.entityType.asInstanceOf[EntityType], args.id, args.operations *))
        ),
        Subscriptions(campaignStream = operationArgs => ???)
      )
    )

}
