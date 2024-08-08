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

import caliban.*
import caliban.CalibanError.ExecutionError
import caliban.interop.zio.*
import caliban.interop.zio.json.*
import caliban.introspection.adt.__Type
import caliban.schema.*
import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import just.semver.SemVer
import zio.ZIO
import zio.json.*
import zio.json.ast.Json
import zio.stream.*

object DMScreenAPI {

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
    campaigns:    ZIO[DMScreenZIORepository, DMScreenError, Seq[CampaignHeader]],
    campaign:     CampaignId => ZIO[DMScreenZIORepository, DMScreenError, Option[Campaign]],
    campaignLogs: CampaignLogRequest => ZIO[DMScreenZIORepository, DMScreenError, Seq[CampaignLogEntry]]
  )

  case class Mutations(
    // All these mutations are temporary, eventually, only the headers will be saved, and the infos will be saved in the events
    upsertCampaign: Campaign => ZIO[DMScreenZIORepository, DMScreenError, CampaignId],
    campaignLog:    CampaignLogInsertRequest => ZIO[DMScreenZIORepository, DMScreenError, Unit],
    deleteEntity:   EntityDeleteArgs => ZIO[DMScreenZIORepository, DMScreenError, Unit]
  )

  case class Subscriptions(
    campaignStream: CampaignEventsArgs => ZStream[DMScreenZIORepository, DMScreenError, DMScreenEvent]
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

  lazy val api: GraphQL[DMScreenServerEnvironment] =
    graphQL[
      DMScreenServerEnvironment,
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
          deleteEntity = deleteArgs =>
            ZIO.serviceWithZIO[DMScreenZIORepository](
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
