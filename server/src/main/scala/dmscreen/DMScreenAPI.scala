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

package dmscreen

import auth.UserId
import caliban.*
import caliban.CalibanError.ExecutionError
import caliban.interop.zio.*
import caliban.interop.zio.json.*
import caliban.introspection.adt.__Type
import caliban.schema.*
import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import caliban.wrappers.Wrappers.*
import dmscreen.db.DMScreenZIORepository
import dmscreen.dnd5e.DND5eRepository
import dmscreen.sta.STARepository
import just.semver.SemVer
import zio.ZIO
import zio.json.*
import zio.json.ast.Json
import zio.stream.*

object DMScreenAPI {

  private def gameSystemRepository(gameSystem: GameSystem): ZIO[
    DND5eRepository[DMScreenTask] & STARepository[DMScreenTask],
    DMScreenError,
    GameSystemRepository[DMScreenTask]
  ] =
    gameSystem match {
      case GameSystem.dnd5e              => ZIO.service[DND5eRepository[DMScreenTask]]
      case GameSystem.starTrekAdventures => ZIO.service[STARepository[DMScreenTask]]
      case _                             => ZIO.fail(DMScreenError(s"Unsupported game system: $gameSystem"))
    }

  case class CampaignEventsArgs(
    entityType: EntityType[?],
    id:         Long,
    events:     Seq[Json]
  )

  case class CampaignLogRequest(
    campaignId: CampaignId,
    maxNum:     Int
  )

  case class CampaignLogInsertRequest(
    campaignId: CampaignId,
    message:    String
  )

  case class EntityDeleteArgs(
    entityType: EntityType[?],
    id:         Long,
    softDelete: Boolean
  )

  case class Queries(
    campaigns: ZIO[DMScreenZIORepository & DMScreenSession, DMScreenError, Seq[CampaignHeader]],
    campaign:  CampaignId => ZIO[DMScreenZIORepository & DMScreenSession, DMScreenError, Option[Campaign]],
    campaignLogs: CampaignLogRequest => ZIO[DMScreenZIORepository & DMScreenSession, DMScreenError, Seq[
      CampaignLogEntry
    ]]
  )

  case class Mutations(
    // All these mutations are temporary, eventually, only the headers will be saved, and the infos will be saved in the events
    upsertCampaign: Campaign => ZIO[DMScreenZIORepository & DMScreenSession, DMScreenError, CampaignId],
    campaignLog:    CampaignLogInsertRequest => ZIO[DMScreenZIORepository & DMScreenSession, DMScreenError, Unit],
    deleteCampaign: CampaignId => ZIO[DMScreenZIORepository & DMScreenSession, DMScreenError, Unit],
    snapshotCampaign: CampaignId => ZIO[
      dmscreen.dnd5e.DND5eRepository[dmscreen.DMScreenTask] & dmscreen.sta.STARepository[dmscreen.DMScreenTask] &
        DMScreenZIORepository & DMScreenSession,
      DMScreenError,
      CampaignHeader
    ]
  )

  case class Subscriptions(
    campaignStream: CampaignEventsArgs => ZStream[DMScreenZIORepository & DMScreenSession, DMScreenError, DMScreenEvent]
  )

  private given Schema[Any, EntityType[?]] = Schema.stringSchema.contramap(_.name)
  private given Schema[Any, SemVer] = Schema.stringSchema.contramap(_.render)
  private given Schema[Any, UserId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, CampaignId] = Schema.longSchema.contramap(_.value)
  private given Schema[Any, Campaign] = Schema.gen[Any, Campaign]
  private given Schema[Any, CampaignLogEntry] = Schema.gen[Any, CampaignLogEntry]

  private given ArgBuilder[SemVer] = ArgBuilder.string.map(SemVer.unsafeParse)
  private given ArgBuilder[UserId] = ArgBuilder.long.map(UserId.apply)
  private given ArgBuilder[CampaignId] = ArgBuilder.long.map(CampaignId.apply)
  private given ArgBuilder[EntityType[?]] =
    ArgBuilder.string.flatMap(s => {
      if (s != CampaignEntityType.name)
        Left(ExecutionError(s"Only CampaignEntityType in this api supported, got $s"))
      else Right(CampaignEntityType)
    })
  private given ArgBuilder[CampaignEventsArgs] = ArgBuilder.gen[CampaignEventsArgs]
  private given ArgBuilder[Campaign] = ArgBuilder.gen[Campaign]
  private given ArgBuilder[CampaignLogRequest] = ArgBuilder.gen[CampaignLogRequest]
  private given ArgBuilder[CampaignLogInsertRequest] = ArgBuilder.gen[CampaignLogInsertRequest]

  lazy val api: GraphQL[DMScreenServerEnvironment & DMScreenSession] =
    graphQL[
      DMScreenServerEnvironment & DMScreenSession,
      Queries,
      Mutations,
      Subscriptions
    ](
      RootResolver(
        Queries(
          campaigns = ZIO.serviceWithZIO[DMScreenZIORepository](_.campaigns),
          campaign = campaignId => ZIO.serviceWithZIO[DMScreenZIORepository](_.campaign(campaignId)),
          campaignLogs =
            request => ZIO.serviceWithZIO[DMScreenZIORepository](_.campaignLogs(request.campaignId, request.maxNum))
        ),
        Mutations(
          upsertCampaign =
            campaign => ZIO.serviceWithZIO[DMScreenZIORepository](_.upsert(campaign.header, campaign.jsonInfo)),
          campaignLog =
            request => ZIO.serviceWithZIO[DMScreenZIORepository](_.campaignLog(request.campaignId, request.message)),
          deleteCampaign = id => ZIO.serviceWithZIO[DMScreenZIORepository](_.deleteCampaign(id, softDelete = true)), // Force soft delete
          snapshotCampaign = id =>
            for {
              newCampaignHeader <- ZIO.serviceWithZIO[DMScreenZIORepository](_.snapshotCampaign(id))
              gameSystemRepo    <- gameSystemRepository(newCampaignHeader.gameSystem)
              gameSystemRepo    <- gameSystemRepo.snapshot(id, newCampaignHeader.id)
            } yield newCampaignHeader
        ),
        Subscriptions(campaignStream = operationArgs => ???)
      )
    ) @@ maxFields(300)
      @@ maxDepth(30)
      @@ printErrors

}
