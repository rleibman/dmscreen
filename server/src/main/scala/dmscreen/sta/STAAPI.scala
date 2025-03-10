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

import auth.{User, UserId, given}
import caliban.*
import caliban.CalibanError.ExecutionError
import caliban.interop.zio.*
import caliban.interop.zio.json.*
import caliban.introspection.adt.__Type
import caliban.schema.*
import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import caliban.wrappers.Wrappers.*
import dmscreen.*
import dmscreen.db.sta.STAZIORepository
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

  private given Schema[Any, User] = Schema.gen[Any, User]
  private given Schema[Any, CharacterHeader] = Schema.gen[Any, CharacterHeader]
  private given Schema[Any, StarshipHeader] = Schema.gen[Any, StarshipHeader]
  private given Schema[Any, SceneHeader] = Schema.gen[Any, SceneHeader]
  private given Schema[Any, NonPlayerCharacterHeader] = Schema.gen[Any, NonPlayerCharacterHeader]

  private given Schema[Any, Character] = Schema.gen[Any, Character]
  private given Schema[Any, Starship] = Schema.gen[Any, Starship]
  private given Schema[Any, Scene] = Schema.gen[Any, Scene]
  private given Schema[Any, NonPlayerCharacter] = Schema.gen[Any, NonPlayerCharacter]
  private given Schema[Any, DMScreenSession] = Schema.gen[Any, DMScreenSession]

  private given ArgBuilder[SemVer] = ArgBuilder.string.map(SemVer.unsafeParse)
  private given ArgBuilder[EntityType[?]] = ArgBuilder.string.map(STAEntityType.valueOf)

  private given ArgBuilder[UserId] = ArgBuilder.long.map(UserId.apply)
  private given ArgBuilder[CampaignId] = ArgBuilder.long.map(CampaignId.apply)
  private given ArgBuilder[CharacterId] = ArgBuilder.long.map(CharacterId.apply)
  private given ArgBuilder[StarshipId] = ArgBuilder.long.map(StarshipId.apply)
  private given ArgBuilder[SceneId] = ArgBuilder.long.map(SceneId.apply)
  private given ArgBuilder[NonPlayerCharacterId] = ArgBuilder.long.map(NonPlayerCharacterId.apply)

  private given ArgBuilder[User] = ArgBuilder.gen[User]
  private given ArgBuilder[DMScreenSession] = ArgBuilder.gen[DMScreenSession]
  private given ArgBuilder[CharacterHeader] = ArgBuilder.gen[CharacterHeader]
  private given ArgBuilder[StarshipHeader] = ArgBuilder.gen[StarshipHeader]
  private given ArgBuilder[SceneHeader] = ArgBuilder.gen[SceneHeader]
  private given ArgBuilder[NonPlayerCharacterHeader] = ArgBuilder.gen[NonPlayerCharacterHeader]

  private given ArgBuilder[Character] = ArgBuilder.gen[Character]
  private given ArgBuilder[Starship] = ArgBuilder.gen[Starship]
  private given ArgBuilder[Scene] = ArgBuilder.gen[Scene]
  private given ArgBuilder[NonPlayerCharacter] = ArgBuilder.gen[NonPlayerCharacter]

  case class Queries(
    characters:          CampaignId => ZIO[STAZIORepository & DMScreenSession, DMScreenError, Seq[Character]],
    ships:               CampaignId => ZIO[STAZIORepository & DMScreenSession, DMScreenError, Seq[Starship]],
    scenes:              CampaignId => ZIO[STAZIORepository & DMScreenSession, DMScreenError, Seq[Scene]],
    nonPlayerCharacters: CampaignId => ZIO[STAZIORepository & DMScreenSession, DMScreenError, Seq[NonPlayerCharacter]]
  )
  case class Mutations(
    upsertCharacter: Character => ZIO[STAZIORepository & DMScreenSession, DMScreenError, CharacterId],
    upsertStarship:  Starship => ZIO[STAZIORepository & DMScreenSession, DMScreenError, StarshipId],
    upsertScene:     Scene => ZIO[STAZIORepository & DMScreenSession, DMScreenError, SceneId],
    upsertNonPlayerCharacter: NonPlayerCharacter => ZIO[
      STAZIORepository & DMScreenSession,
      DMScreenError,
      NonPlayerCharacterId
    ],
    deleteEntity: EntityDeleteArgs => ZIO[STAZIORepository & DMScreenSession, DMScreenError, Unit]
  )

  lazy val api: GraphQL[DMScreenServerEnvironment & DMScreenSession] =
    graphQL[
      DMScreenServerEnvironment & DMScreenSession,
      Queries,
      Mutations,
      Unit
    ](
      RootResolver(
        Queries(
          characters = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.characters(campaignId)),
          ships = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.starships(campaignId)),
          scenes = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.scenes(campaignId)),
          nonPlayerCharacters = campaignId => ZIO.serviceWithZIO[STAZIORepository](_.nonPlayerCharacters(campaignId))
        ),
        Mutations(
          upsertCharacter =
            character => ZIO.serviceWithZIO[STAZIORepository](_.upsert(character.header, character.jsonInfo)),
          upsertStarship =
            starship => ZIO.serviceWithZIO[STAZIORepository](_.upsert(starship.header, starship.jsonInfo)),
          upsertScene = scene => ZIO.serviceWithZIO[STAZIORepository](_.upsert(scene.header, scene.jsonInfo)),
          upsertNonPlayerCharacter = nonPlayerCharacter =>
            ZIO.serviceWithZIO[STAZIORepository](_.upsert(nonPlayerCharacter.header, nonPlayerCharacter.jsonInfo)),
          deleteEntity = deleteArgs =>
            ZIO.serviceWithZIO[STAZIORepository](
              _.deleteEntity(
                deleteArgs.entityType,
                deleteArgs.entityType.createId(deleteArgs.id),
                deleteArgs.softDelete
              )
            )
        )
      )
    ) @@ maxFields(200)
      @@ maxDepth(30)
      @@ printErrors

}
