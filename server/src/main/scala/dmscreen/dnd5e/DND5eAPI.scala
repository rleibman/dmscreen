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
import caliban.interop.zio.*
import caliban.interop.zio.json.*
import caliban.introspection.adt.__Type
import caliban.schema.*
import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import dmscreen.*
import dmscreen.dnd5e.DND5eZIORepository
import dmscreen.dnd5e.dndbeyond.DNDBeyondImporter
import just.semver.SemVer
import zio.{ZIO, *}
import zio.http.{Client, Request}
import zio.json.*
import zio.json.ast.Json
import zio.nio.file.Files
import zio.stream.*

import java.net.URI
import java.nio.file.StandardOpenOption

object DND5eAPI {

  val dndBeyondIdRegex = "^[1-9][0-9]{8}$".r

  private def doImportCharacterDNDBeyond(
    request: ImportCharacterRequest
  ): ZIO[DND5eZIORepository & DNDBeyondImporter & ConfigurationService, DMScreenError, PlayerCharacter] = {
    (for {
      config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
      fileStore = config.dmscreen.dndBeyondFileStore

      filePath = fileStore / s"${request.dndBeyondId}.json"
      // Check if we have the character in our file system
      exists <- zio.nio.file.Files.exists(filePath)
      shouldLoad = request.fresh || !exists

      _ <- {
        val dndBeyondURI: String =
          s"https://character-service.dndbeyond.com/character/v5/character/${request.dndBeyondId}?includeCustomItems=true"
        for {
          // If we want a fresh import, or we don't have the character already
          // Get the text from the URL
          response <- Client.request(Request.get(dndBeyondURI))

          dndBeyondResponse <- response.body.asString
          // Save the file we just got
          _ <- {
            Files.writeBytes(
              filePath,
              Chunk.fromArray(dndBeyondResponse.getBytes),
              StandardOpenOption.WRITE,
              StandardOpenOption.TRUNCATE_EXISTING,
              StandardOpenOption.CREATE
            )
          }
        } yield ()
      }.when(shouldLoad)

      localURI <- filePath.toUri
      importer <- ZIO.service[DNDBeyondImporter]
      pc       <- importer.importPlayerCharacter(localURI)
      repo     <- ZIO.service[DND5eZIORepository]
      // See if we already have that character in the db, if we do, use that id
      existing <- repo.playerCharacters(
        request.campaignId,
        PlayerCharacterSearch(dndBeyondId = Some(request.dndBeyondId))
      )
      pcId = existing.headOption.map(_.header.id).getOrElse(PlayerCharacterId.empty)

      saveMe = pc.copy(header = pc.header.copy(id = pcId, campaignId = request.campaignId))
      id <- repo.upsert(pc.header, pc.jsonInfo) // Save the character
    } yield pc.copy(header = pc.header.copy(id = id)))
      .provideSome[DND5eZIORepository & DNDBeyondImporter & ConfigurationService](Client.default, Scope.default)
      .tapError { e =>
        ZIO.logErrorCause(Cause.fail(e))
      }
      .mapError { e =>
        DMScreenError("Error importing from dndbeyond", Some(e))
      }
  }

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

  case class ImportCharacterRequest(
    campaignId:  CampaignId,
    dndBeyondId: DndBeyondId,
    fresh:       Boolean
  )

  case class EncounterByIdRequest(
    campaignId:  CampaignId,
    encounterId: EncounterId
  )

  case class PlayerCharacterSearchRequest(
    campaignId:            CampaignId,
    playerCharacterSearch: PlayerCharacterSearch
  )

  case class CampaignLogRequest(
    campaignId: CampaignId,
    maxNum:     Int
  )

  case class CampaignLogInsertRequest(
    campaignId: CampaignId,
    message:    String
  )

  private given Schema[Any, UserId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CampaignId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, SemVer] = Schema.stringSchema.contramap(_.render)
  private given Schema[Any, EntityType[?]] = Schema.stringSchema.contramap(_.name)
  private given Schema[Any, java.net.URL] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, java.net.URI] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, EntityDeleteArgs] = Schema.gen[Any, EntityDeleteArgs]

  private given Schema[Any, DndBeyondId] = Schema.stringSchema.contramap(_.value)
  private given Schema[Any, CharacterClassId] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, MonsterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, PlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, NonPlayerCharacterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, EncounterId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, SceneId] = Schema.longSchema.contramap(_.value)

  private given Schema[Any, ChallengeRating] = Schema.doubleSchema.contramap(_.value)
  private given Schema[Any, SourceId] = Schema.stringSchema.contramap(_.value)
  private given Schema[Any, EncounterStatus] = Schema.stringSchema.contramap(_.toString)
  private given Schema[Any, DMScreenEvent] = Schema.gen[Any, DMScreenEvent]
  private given Schema[Any, Source] = Schema.gen[Any, Source]
  private given Schema[Any, MonsterSearch] = Schema.gen[Any, MonsterSearch]
  private given Schema[Any, MonsterSearchResults] = Schema.gen[Any, MonsterSearchResults]
  private given Schema[Any, Campaign] = Schema.gen[Any, Campaign]
  private given Schema[Any, Scene] = Schema.gen[Any, Scene]
  private given Schema[Any, Monster] = Schema.gen[Any, Monster]
  private given Schema[Any, ImportSource] = Schema.stringSchema.contramap(_.toJson)
  private given Schema[Any, PlayerCharacter] = Schema.gen[Any, PlayerCharacter]
  private given Schema[Any, NonPlayerCharacter] = Schema.gen[Any, NonPlayerCharacter]
  private given Schema[Any, Encounter] = Schema.gen[Any, Encounter]
  private given Schema[Any, EncounterByIdRequest] = Schema.gen[Any, EncounterByIdRequest]
  private given Schema[Any, CampaignLogEntry] = Schema.gen[Any, CampaignLogEntry]

  private given ArgBuilder[PlayerCharacterId] = ArgBuilder.long.map(PlayerCharacterId.apply)
  private given ArgBuilder[SceneId] = ArgBuilder.long.map(SceneId.apply)
  private given ArgBuilder[UserId] = ArgBuilder.long.map(UserId.apply)
  private given ArgBuilder[NonPlayerCharacterId] = ArgBuilder.long.map(NonPlayerCharacterId.apply)
  private given ArgBuilder[CharacterClassId] =
    ArgBuilder.string.map(s => CharacterClassId.values.find(a => s.equalsIgnoreCase(a.toString)).get)
  private given ArgBuilder[URI] = ArgBuilder.string.map(URI.create)
  private given ArgBuilder[DndBeyondId] = ArgBuilder.string.map(DndBeyondId(_).toOption.get)
  private given ArgBuilder[SourceId] = ArgBuilder.string.map(SourceId.apply)
  private given ArgBuilder[SemVer] = ArgBuilder.string.map(SemVer.unsafeParse)
  private given ArgBuilder[MonsterId] = ArgBuilder.long.map(MonsterId.apply)
  private given ArgBuilder[CampaignId] = ArgBuilder.long.map(CampaignId.apply)
  private given ArgBuilder[EncounterId] = ArgBuilder.long.map(EncounterId.apply)
  private given ArgBuilder[ChallengeRating] = ArgBuilder.double.map(n => ChallengeRating.fromDouble(n).get)
  private given ArgBuilder[EntityType[?]] = ArgBuilder.string.map(DND5eEntityType.valueOf)

  private given ArgBuilder[DMScreenEvent] = ArgBuilder.gen[DMScreenEvent]
  private given ArgBuilder[Add] = ArgBuilder.gen[Add]
  private given ArgBuilder[Copy] = ArgBuilder.gen[Copy]
  private given ArgBuilder[Move] = ArgBuilder.gen[Move]
  private given ArgBuilder[Remove] = ArgBuilder.gen[Remove]
  private given ArgBuilder[Replace] = ArgBuilder.gen[Replace]
  private given ArgBuilder[Test] = ArgBuilder.gen[Test]
  private given ArgBuilder[MonsterSearch] = ArgBuilder.gen[MonsterSearch]
  private given ArgBuilder[CampaignEventsArgs] = ArgBuilder.gen[CampaignEventsArgs]
  private given ArgBuilder[Campaign] = ArgBuilder.gen[Campaign]
  private given ArgBuilder[Scene] = ArgBuilder.gen[Scene]
  private given ArgBuilder[Monster] = ArgBuilder.gen[Monster]
  private given ArgBuilder[ImportSource] = ArgBuilder.string.map(s => s.fromJson[ImportSource].toOption.get)
  private given ArgBuilder[PlayerCharacter] = ArgBuilder.gen[PlayerCharacter]
  private given ArgBuilder[NonPlayerCharacter] = ArgBuilder.gen[NonPlayerCharacter]
  private given ArgBuilder[Encounter] = ArgBuilder.gen[Encounter]
  private given ArgBuilder[ImportCharacterRequest] = ArgBuilder.gen[ImportCharacterRequest]
  private given ArgBuilder[EncounterByIdRequest] = ArgBuilder.gen[EncounterByIdRequest]
  private given ArgBuilder[CampaignLogRequest] = ArgBuilder.gen[CampaignLogRequest]
  private given ArgBuilder[CampaignLogInsertRequest] = ArgBuilder.gen[CampaignLogInsertRequest]

  case class Queries(
    campaigns:           ZIO[DND5eZIORepository, DMScreenError, Seq[CampaignHeader]],
    campaign:            CampaignId => ZIO[DND5eZIORepository, DMScreenError, Option[Campaign]],
    monster:             MonsterId => ZIO[DND5eZIORepository, DMScreenError, Option[Monster]],
    playerCharacters:    PlayerCharacterSearchRequest => ZIO[DND5eZIORepository, DMScreenError, Seq[PlayerCharacter]],
    scenes:              CampaignId => ZIO[DND5eZIORepository, DMScreenError, Seq[Scene]],
    nonPlayerCharacters: CampaignId => ZIO[DND5eZIORepository, DMScreenError, Seq[NonPlayerCharacter]],
    playerCharacter:     PlayerCharacterId => ZIO[DND5eZIORepository, DMScreenError, Option[PlayerCharacter]],
    nonPlayerCharacter:  NonPlayerCharacterId => ZIO[DND5eZIORepository, DMScreenError, Option[NonPlayerCharacter]],
    encounters:          CampaignId => ZIO[DND5eZIORepository, DMScreenError, Seq[Encounter]],
    encounter:           EncounterByIdRequest => ZIO[DND5eZIORepository, DMScreenError, Option[Encounter]],
    bestiary:            MonsterSearch => ZIO[DND5eZIORepository, DMScreenError, MonsterSearchResults],
    sources:             ZIO[DND5eZIORepository, DMScreenError, Seq[Source]],
    classes:             ZIO[DND5eZIORepository, DMScreenError, Seq[CharacterClass]],
    races:               ZIO[DND5eZIORepository, DMScreenError, Seq[Race]],
    backgrounds:         ZIO[DND5eZIORepository, DMScreenError, Seq[Background]],
    subclasses:          CharacterClassId => ZIO[DND5eZIORepository, DMScreenError, Seq[SubClass]],
    campaignLogs:        CampaignLogRequest => ZIO[DND5eZIORepository, DMScreenError, Seq[CampaignLogEntry]]
  )
  case class Mutations(
    // All these mutations are temporary, eventually, only the headers will be saved, and the infos will be saved in the events
    upsertCampaign:           Campaign => ZIO[DND5eZIORepository, DMScreenError, CampaignId],
    upsertScene:              Scene => ZIO[DND5eZIORepository, DMScreenError, SceneId],
    upsertPlayerCharacter:    PlayerCharacter => ZIO[DND5eZIORepository, DMScreenError, PlayerCharacterId],
    upsertNonPlayerCharacter: NonPlayerCharacter => ZIO[DND5eZIORepository, DMScreenError, NonPlayerCharacterId],
    upsertMonster:            Monster => ZIO[DND5eZIORepository, DMScreenError, MonsterId],
    upsertEncounter:          Encounter => ZIO[DND5eZIORepository, DMScreenError, EncounterId],
    deleteEntity:             EntityDeleteArgs => ZIO[DND5eZIORepository, DMScreenError, Unit],
    importCharacterDNDBeyond: ImportCharacterRequest => ZIO[
      DND5eZIORepository & DNDBeyondImporter & ConfigurationService,
      DMScreenError,
      PlayerCharacter
    ],
    campaignLog: CampaignLogInsertRequest => ZIO[DND5eZIORepository, DMScreenError, Unit],
    // This one is the one that will be used to apply operations to the entities
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
          monster = monsterId => ZIO.serviceWithZIO[DND5eZIORepository](_.monster(monsterId)),
          playerCharacters = request =>
            ZIO.serviceWithZIO[DND5eZIORepository](
              _.playerCharacters(request.campaignId, request.playerCharacterSearch)
            ),
          scenes = campaignId => ZIO.serviceWithZIO[DND5eZIORepository](_.scenes(campaignId)),
          nonPlayerCharacters = campaignId => ZIO.serviceWithZIO[DND5eZIORepository](_.nonPlayerCharacters(campaignId)),
          playerCharacter = id => ZIO.serviceWithZIO[DND5eZIORepository](_.playerCharacter(id)),
          nonPlayerCharacter = id => ZIO.serviceWithZIO[DND5eZIORepository](_.nonPlayerCharacter(id)),
          encounters = campaignId => ZIO.serviceWithZIO[DND5eZIORepository](_.encounters(campaignId)),
          encounter =
            request => ZIO.serviceWithZIO[DND5eZIORepository](_.encounter(request.campaignId, request.encounterId)),
          bestiary = search => ZIO.serviceWithZIO[DND5eZIORepository](_.bestiary(search)),
          sources = ZIO.serviceWithZIO[DND5eZIORepository](_.sources),
          classes = ZIO.serviceWithZIO[DND5eZIORepository](_.classes),
          races = ZIO.serviceWithZIO[DND5eZIORepository](_.races),
          backgrounds = ZIO.serviceWithZIO[DND5eZIORepository](_.backgrounds),
          subclasses = characterClassId => ZIO.serviceWithZIO[DND5eZIORepository](_.subClasses(characterClassId)),
          campaignLogs =
            request => ZIO.serviceWithZIO[DND5eZIORepository](_.campaignLogs(request.campaignId, request.maxNum))
        ),
        Mutations(
          upsertCampaign =
            campaign => ZIO.serviceWithZIO[DND5eZIORepository](_.upsert(campaign.header, campaign.jsonInfo)),
          upsertScene = scene => ZIO.serviceWithZIO[DND5eZIORepository](_.upsert(scene.header, scene.jsonInfo)),
          upsertPlayerCharacter = playerCharacter =>
            ZIO.serviceWithZIO[DND5eZIORepository](_.upsert(playerCharacter.header, playerCharacter.jsonInfo)),
          upsertNonPlayerCharacter = nonPlayerCharacter =>
            ZIO.serviceWithZIO[DND5eZIORepository](_.upsert(nonPlayerCharacter.header, nonPlayerCharacter.jsonInfo)),
          upsertMonster = monster => ZIO.serviceWithZIO[DND5eZIORepository](_.upsert(monster.header, monster.jsonInfo)),
          upsertEncounter =
            encounter => ZIO.serviceWithZIO[DND5eZIORepository](_.upsert(encounter.header, encounter.jsonInfo)),
          deleteEntity = deleteArgs =>
            ZIO.serviceWithZIO[DND5eZIORepository](
              _.deleteEntity(
                deleteArgs.entityType,
                deleteArgs.entityType.createId(deleteArgs.id),
                deleteArgs.softDelete
              )
            ),
          importCharacterDNDBeyond = request => doImportCharacterDNDBeyond(request),
          campaignLog =
            request => ZIO.serviceWithZIO[DND5eZIORepository](_.campaignLog(request.campaignId, request.message)),
          applyOperations = args => ???
          // ZIO.serviceWithZIO[DND5eRepository](_.applyOperations(args.entityType.asInstanceOf[EntityType], args.id, args.operations *))
        ),
        Subscriptions(campaignStream = operationArgs => ???)
      )
    )

}
