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

package dmscreen.sta

import caliban.*
import caliban.interop.zio.*
import caliban.interop.zio.json.*
import caliban.introspection.adt.__Type
import caliban.schema.*
import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import dmscreen.*
import dmscreen.sta.STAZIORepository
import just.semver.SemVer
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.stream.*

import java.net.URL

object STAAPI {

  case class CampaignEventsArgs(
    entityType: EntityType[?],
    id:         Long,
    events:     Seq[Json]
  )

  case class EntityDeleteArgs(
    entityType: EntityType[?],
    id:         Long,
    softDelete: Boolean
  )

  private given Schema[Any, UserId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CampaignId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, SemVer] = Schema.stringSchema.contramap(_.render)
  private given Schema[Any, EntityType[?]] = Schema.stringSchema.contramap(_.name)
  private given Schema[Any, URL] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, EntityDeleteArgs] = Schema.gen[Any, EntityDeleteArgs]

  private given Schema[Any, CharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, StarshipId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, SceneId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, NonPlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, EncounterId] = Schema.longSchema.contramap(_.value)

  private given Schema[Any, CharacterHeader] = Schema.gen[Any, CharacterHeader]
  private given Schema[Any, StarshipHeader] = Schema.gen[Any, StarshipHeader]
  private given Schema[Any, SceneHeader] = Schema.gen[Any, SceneHeader]
  private given Schema[Any, NonPlayerCharacterHeader] = Schema.gen[Any, NonPlayerCharacterHeader]
  private given Schema[Any, EncounterHeader] = Schema.gen[Any, EncounterHeader]

  private given Schema[Any, Character] = Schema.gen[Any, Character]
  private given Schema[Any, Starship] = Schema.gen[Any, Starship]
  private given Schema[Any, Scene] = Schema.gen[Any, Scene]
  private given Schema[Any, NonPlayerCharacter] = Schema.gen[Any, NonPlayerCharacter]
  private given Schema[Any, Encounter] = Schema.gen[Any, Encounter]

  private given ArgBuilder[SemVer] = ArgBuilder.string.map(SemVer.unsafeParse)
  private given ArgBuilder[EntityType[?]] = ArgBuilder.string.map(STAEntityType.valueOf)

  private given ArgBuilder[CampaignId] = ArgBuilder.long.map(CampaignId.apply)
  private given ArgBuilder[CharacterId] = ArgBuilder.long.map(CharacterId.apply)
  private given ArgBuilder[StarshipId] = ArgBuilder.long.map(StarshipId.apply)
  private given ArgBuilder[SceneId] = ArgBuilder.long.map(SceneId.apply)
  private given ArgBuilder[NonPlayerCharacterId] = ArgBuilder.long.map(NonPlayerCharacterId.apply)
  private given ArgBuilder[EncounterId] = ArgBuilder.long.map(EncounterId.apply)

  private given ArgBuilder[CharacterHeader] = ArgBuilder.gen[CharacterHeader]
  private given ArgBuilder[StarshipHeader] = ArgBuilder.gen[StarshipHeader]
  private given ArgBuilder[SceneHeader] = ArgBuilder.gen[SceneHeader]
  private given ArgBuilder[NonPlayerCharacterHeader] = ArgBuilder.gen[NonPlayerCharacterHeader]
  private given ArgBuilder[EncounterHeader] = ArgBuilder.gen[EncounterHeader]

  private given ArgBuilder[Character] = ArgBuilder.gen[Character]
  private given ArgBuilder[Starship] = ArgBuilder.gen[Starship]
  private given ArgBuilder[Scene] = ArgBuilder.gen[Scene]
  private given ArgBuilder[NonPlayerCharacter] = ArgBuilder.gen[NonPlayerCharacter]
  private given ArgBuilder[Encounter] = ArgBuilder.gen[Encounter]

  case class Queries(
    characters:          CampaignId => ZIO[STAZIORepository, DMScreenError, Seq[Character]],
    ships:               CampaignId => ZIO[STAZIORepository, DMScreenError, Seq[Starship]],
    scenes:              CampaignId => ZIO[STAZIORepository, DMScreenError, Seq[Scene]],
    nonPlayerCharacters: CampaignId => ZIO[STAZIORepository, DMScreenError, Seq[NonPlayerCharacter]],
    encounters:          CampaignId => ZIO[STAZIORepository, DMScreenError, Seq[Encounter]]
  )
  case class Mutations(
    upsertCharacter:          Character => ZIO[STAZIORepository, DMScreenError, CharacterId],
    upsertStarship:           Starship => ZIO[STAZIORepository, DMScreenError, StarshipId],
    upsertScene:              Scene => ZIO[STAZIORepository, DMScreenError, SceneId],
    upsertNonPlayerCharacter: NonPlayerCharacter => ZIO[STAZIORepository, DMScreenError, NonPlayerCharacterId],
    upsertEncounter:          Encounter => ZIO[STAZIORepository, DMScreenError, EncounterId],
    deleteEntity:             EntityDeleteArgs => ZIO[STAZIORepository, DMScreenError, Unit]
  )
  case class Subscriptions(
    campaignStream: CampaignEventsArgs => ZStream[STAZIORepository, DMScreenError, DMScreenEvent]
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
          characters = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.characters(campaignId)),
          ships = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.starships(campaignId)),
          scenes = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.scenes(campaignId)),
          nonPlayerCharacters = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.nonPlayerCharacters(campaignId)),
          encounters = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.encounters(campaignId))
        ),
        Mutations(
          upsertCharacter =
            character => ZIO.serviceWithZIO[STAZIORepository](_.upsert(character.header, character.jsonInfo)),
          upsertStarship =
            starship => ZIO.serviceWithZIO[STAZIORepository](_.upsert(starship.header, starship.jsonInfo)),
          upsertScene = scene => ZIO.serviceWithZIO[STAZIORepository](_.upsert(scene.header, scene.jsonInfo)),
          upsertNonPlayerCharacter = nonPlayerCharacter =>
            ZIO.serviceWithZIO[STAZIORepository](_.upsert(nonPlayerCharacter.header, nonPlayerCharacter.jsonInfo)),
          upsertEncounter =
            encounter => ZIO.serviceWithZIO[STAZIORepository](_.upsert(encounter.header, encounter.jsonInfo)),
          deleteEntity = deleteArgs =>
            ZIO.serviceWithZIO[STAZIORepository](
              _.deleteEntity(
                deleteArgs.entityType,
                deleteArgs.entityType.createId(deleteArgs.id),
                deleteArgs.softDelete
              )
            ),
        ),
        Subscriptions(campaignStream = operationArgs => ???)
      )
    )

}
