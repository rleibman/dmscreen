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
import caliban.schema.*
import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import dmscreen.{DMScreenEnvironment, DMScreenError, UserId}
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.stream.*

import java.net.URL

enum EventType {

  case add, copy, move, remove, replace, test

}

case class JsonPath(value: String)

sealed trait Operation

case class Add(
  path:  JsonPath,
  value: Json
) extends Operation

case class Copy(
  from: JsonPath,
  to:   JsonPath
) extends Operation

case class Move(
  from: JsonPath,
  to:   JsonPath
) extends Operation

case class Remove(path: JsonPath) extends Operation

case class Replace(
  path:  JsonPath,
  value: Json
) extends Operation

case class Test(
  path:  JsonPath,
  value: Json
) extends Operation

object DND5eAPI {

  case class OperationStreamArgs()

  private given Schema[Any, UserId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CampaignId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CharacterClassId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, MonsterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, PlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, NonPlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, EncounterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, URL] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, Operation] = Schema.gen[Any, Operation]
  private given Schema[Any, Json] = Schema.stringSchema.contramap(_.toString)

  private given ArgBuilder[PlayerCharacterId] = ArgBuilder.long.map(PlayerCharacterId.apply)
  private given ArgBuilder[NonPlayerCharacterId] = ArgBuilder.long.map(NonPlayerCharacterId.apply)
  private given ArgBuilder[CharacterClassId] = ArgBuilder.long.map(CharacterClassId.apply)
  private given ArgBuilder[CampaignId] = ArgBuilder.long.map(CampaignId.apply)
  private given ArgBuilder[EncounterId] = ArgBuilder.long.map(EncounterId.apply)
  private given ArgBuilder[OperationStreamArgs] = ArgBuilder.gen[OperationStreamArgs]
  private given ArgBuilder[Operation] = ArgBuilder.gen[Operation]
  private given ArgBuilder[Json] =
    ArgBuilder.string.map(s => s.fromJson[Json].getOrElse(throw new Exception(s"Invalid json: $s")))
  private given ArgBuilder[Add] = ArgBuilder.gen[Add]
  private given ArgBuilder[Copy] = ArgBuilder.gen[Copy]
  private given ArgBuilder[Move] = ArgBuilder.gen[Move]
  private given ArgBuilder[Remove] = ArgBuilder.gen[Remove]
  private given ArgBuilder[Replace] = ArgBuilder.gen[Replace]
  private given ArgBuilder[Test] = ArgBuilder.gen[Test]

  case class Queries(
    campaigns:           ZIO[DMScreenEnvironment, DMScreenError, Seq[CampaignHeader]],
    campaign:            CampaignId => ZIO[DMScreenEnvironment, DMScreenError, Option[Campaign]],
    playerCharacters:    CampaignId => ZIO[DMScreenEnvironment, DMScreenError, Seq[PlayerCharacter]],
    nonPlayerCharacters: CampaignId => ZIO[DMScreenEnvironment, DMScreenError, Seq[NonPlayerCharacter]],
    encounters:          CampaignId => ZIO[DMScreenEnvironment, DMScreenError, Seq[EncounterHeader]],
    encounter:           EncounterId => ZIO[DMScreenEnvironment, DMScreenError, Seq[Encounter]],
    bestiary:            MonsterSearch => ZIO[DMScreenEnvironment, DMScreenError, Seq[Monster]],
    sources:             ZIO[DMScreenEnvironment, DMScreenError, Seq[Source]],
    classes:             ZIO[DMScreenEnvironment, DMScreenError, Seq[CharacterClass]],
    races:               ZIO[DMScreenEnvironment, DMScreenError, Seq[Race]],
    backgrounds:         ZIO[DMScreenEnvironment, DMScreenError, Seq[Background]],
    subclasses:          CharacterClassId => ZIO[DMScreenEnvironment, DMScreenError, Seq[Subclass]]
  )
  case class Mutations(event: Operation => ZIO[DMScreenEnvironment, DMScreenError, Boolean])
  case class Subscriptions(
    operationStream: OperationStreamArgs => ZStream[DMScreenEnvironment, DMScreenError, Operation]
  )

  lazy val api: GraphQL[DMScreenEnvironment] = graphQL[DMScreenEnvironment, Queries, Mutations, Subscriptions](
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
      Mutations(event = event => ???),
      Subscriptions(operationStream = operationStreamArgs => ???)
    )
  )

}
