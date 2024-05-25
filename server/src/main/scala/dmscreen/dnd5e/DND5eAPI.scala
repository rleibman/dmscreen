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
import caliban.introspection.adt.__Type
import caliban.schema.*
import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import caliban.schema.Types.makeScalar
import caliban.interop.zio.*
import dmscreen.*
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.stream.*

import java.net.URL

object DND5eAPI {

  case class OperationsArgs(
    entityType: DND5eEntityType,
    id:         Long,
    operations: Seq[Json]
  )

  private given Schema[Any, UserId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CampaignId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CharacterClassId] = Schema.stringSchema.contramap(_.value)
  private given Schema[Any, MonsterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, PlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, NonPlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, EncounterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, SourceId] = Schema.stringSchema.contramap(_.value)
  private given Schema[Any, URL] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, DMScreenOperation] = Schema.gen[Any, DMScreenOperation]
  private given Schema[Any, Source] = Schema.gen[Any, Source]
  private given Schema[Any, MonsterSearch] = Schema.gen[Any, MonsterSearch]
  private given Schema[Any, Json] =
    new Schema[Any, Json] {
      override def toType(
        isInput:        Boolean,
        isSubscription: Boolean
      ): __Type = makeScalar("Json")
      override def resolve(value: Json): Step[Any] =
        Step.fromEither {
          val res = value.as[ResponseValue].left.map(Exception(_))
          res
        }
    }
  private given ArgBuilder[Json] = (input: InputValue) => input.toJsonAST.left.map(ExecutionError(_))

  private given ArgBuilder[PlayerCharacterId] = ArgBuilder.long.map(PlayerCharacterId.apply)
  private given ArgBuilder[NonPlayerCharacterId] = ArgBuilder.long.map(NonPlayerCharacterId.apply)
  private given ArgBuilder[CharacterClassId] = ArgBuilder.string.map(CharacterClassId.apply)
  private given ArgBuilder[SourceId] = ArgBuilder.string.map(SourceId.apply)
  private given ArgBuilder[CampaignId] = ArgBuilder.long.map(CampaignId.apply)
  private given ArgBuilder[EncounterId] = ArgBuilder.long.map(EncounterId.apply)
  private given ArgBuilder[DMScreenOperation] = ArgBuilder.gen[DMScreenOperation]
  private given ArgBuilder[Add] = ArgBuilder.gen[Add]
  private given ArgBuilder[Copy] = ArgBuilder.gen[Copy]
  private given ArgBuilder[Move] = ArgBuilder.gen[Move]
  private given ArgBuilder[Remove] = ArgBuilder.gen[Remove]
  private given ArgBuilder[Replace] = ArgBuilder.gen[Replace]
  private given ArgBuilder[Test] = ArgBuilder.gen[Test]
  private given ArgBuilder[MonsterSearch] = ArgBuilder.gen[MonsterSearch]
  private given ArgBuilder[OperationsArgs] = ArgBuilder.gen[OperationsArgs]

  case class Queries(
    campaigns: ZIO[DND5eGameService, DMScreenError, Seq[CampaignHeader]],
    campaign:  CampaignId => ZIO[DND5eGameService, DMScreenError, Option[DND5eCampaign]],
    playerCharacters: CampaignId => ZIO[DND5eGameService, DMScreenError, Seq[
      PlayerCharacter
    ]],
    nonPlayerCharacters: CampaignId => ZIO[DND5eGameService, DMScreenError, Seq[
      NonPlayerCharacter
    ]],
    encounters:  CampaignId => ZIO[DND5eGameService, DMScreenError, Seq[EncounterHeader]],
    encounter:   EncounterId => ZIO[DND5eGameService, DMScreenError, Seq[Encounter]],
    bestiary:    MonsterSearch => ZIO[DND5eGameService, DMScreenError, Seq[Monster]],
    sources:     ZIO[DND5eGameService, DMScreenError, Seq[Source]],
    classes:     ZIO[DND5eGameService, DMScreenError, Seq[CharacterClass]],
    races:       ZIO[DND5eGameService, DMScreenError, Seq[Race]],
    backgrounds: ZIO[DND5eGameService, DMScreenError, Seq[Background]],
    subclasses:  CharacterClassId => ZIO[DND5eGameService, DMScreenError, Seq[Subclass]]
  )
  case class Mutations(
    applyOperations: OperationsArgs => ZIO[DND5eGameService, DMScreenError, Unit]
  )
  case class Subscriptions(
    operationStream: OperationsArgs => ZStream[DND5eGameService, DMScreenError, DMScreenOperation]
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
          campaigns = ZIO.serviceWithZIO[DND5eGameService](_.campaigns),
          campaign = campaignId => ZIO.serviceWithZIO[DND5eGameService](_.campaign(campaignId)),
          playerCharacters = campaignId => ZIO.serviceWithZIO[DND5eGameService](_.playerCharacters(campaignId)),
          nonPlayerCharacters = campaignId => ZIO.serviceWithZIO[DND5eGameService](_.nonPlayerCharacters(campaignId)),
          encounters = campaignId => ZIO.serviceWithZIO[DND5eGameService](_.encounters(campaignId)),
          encounter = encounterId => ZIO.serviceWithZIO[DND5eGameService](_.encounter(encounterId)),
          bestiary = search => ZIO.serviceWithZIO[DND5eGameService](_.bestiary(search)),
          sources = ZIO.serviceWithZIO[DND5eGameService](_.sources),
          classes = ZIO.serviceWithZIO[DND5eGameService](_.classes),
          races = ZIO.serviceWithZIO[DND5eGameService](_.races),
          backgrounds = ZIO.serviceWithZIO[DND5eGameService](_.backgrounds),
          subclasses = characterClassId => ZIO.serviceWithZIO[DND5eGameService](_.subClasses(characterClassId))
        ),
        Mutations(applyOperations =
          args => ???
          // ZIO.serviceWithZIO[DND5eGameService](_.applyOperations(args.entityType.asInstanceOf[EntityType], args.id, args.operations *))
        ),
        Subscriptions(operationStream = operationArgs => ???)
      )
    )

}
