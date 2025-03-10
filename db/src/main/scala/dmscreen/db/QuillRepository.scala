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

package dmscreen.db

import auth.UserId
import dmscreen.*
import dmscreen.dnd5e.DND5eRepository
import dmscreen.sta.STARepository
import io.getquill.*
import io.getquill.jdbczio.Quill
import just.semver.SemVer
import zio.*
import zio.cache.*
import zio.json.*
import zio.json.ast.Json

import java.sql.SQLException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.sql.DataSource
import scala.reflect.ClassTag

trait DMScreenZIORepository extends DMScreenRepository[DMScreenTask] {}

object DMScreenSchema {

  given MappedEncoding[String, GameSystem] = MappedEncoding[String, GameSystem](GameSystem.valueOf)

  given MappedEncoding[String, CampaignStatus] = MappedEncoding[String, CampaignStatus](CampaignStatus.valueOf)

  given MappedEncoding[GameSystem, String] = MappedEncoding[GameSystem, String](_.toString)

  given MappedEncoding[CampaignStatus, String] = MappedEncoding[CampaignStatus, String](_.toString)

  inline def qCampaigns =
    quote {
      querySchema[DBObject[Campaign]](
        "campaign",
        _.value.header.id             -> "id",
        _.value.header.name           -> "name",
        _.value.header.dmUserId       -> "dmUserId",
        _.value.header.gameSystem     -> "gameSystem",
        _.value.header.campaignStatus -> "campaignStatus",
        _.value.jsonInfo              -> "info",
        _.value.version               -> "version",
        _.deleted                     -> "deleted"
      )
    }

  inline def qCampaignLog =
    quote {
      querySchema[CampaignLogEntry]("campaignLog")
    }

}

object QuillRepository {

  def db: ZLayer[ConfigurationService, DMScreenError, DMScreenZIORepository] =
    ZLayer.fromZIO {
      object ctx extends MysqlZioJdbcContext(MysqlEscape)

      import DMScreenSchema.{*, given}
      import ctx.*

      for {
        config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
        dataSourceLayer = Quill.DataSource.fromDataSource(config.dataSource)
        cache <- zio.cache.Cache.make[(CampaignId, UserId), Any, RepositoryError, Option[Campaign]](
          capacity = 10,
          timeToLive = 1.hour,
          lookup = Lookup {
            (
              campaignId,
              userId
            ) =>
              ctx
                .run(
                  qCampaigns.filter(v =>
                    !v.deleted && v.value.header.id == lift(campaignId) && v.value.header.dmUserId == lift(userId)
                  )
                )
                .map(_.headOption.map(_.value))
                .provideLayer(dataSourceLayer)
                .mapError(RepositoryError.apply)
                .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
          }
        )
      } yield new DMScreenZIORepository() {

        override def campaign(campaignId: CampaignId): DMScreenTask[Option[Campaign]] = {
          for {
            userId <- ZIO.serviceWith[DMScreenSession](_.user.id)
            res    <- cache.get((campaignId, userId))
          } yield res
        }

        override def campaigns: DMScreenTask[Seq[CampaignHeader]] = {
          for {
            userId <- ZIO.serviceWith[DMScreenSession](_.user.id)
            res <- ctx
              .run(
                qCampaigns
                  .filter(v => !v.deleted && v.value.header.dmUserId == lift(userId))
                  .map(a =>
                    (
                      a.value.header.id,
                      a.value.header.dmUserId,
                      a.value.header.name,
                      a.value.header.gameSystem,
                      a.value.header.campaignStatus
                    )
                  )
              )
              .map(
                _.map(t => CampaignHeader.apply.tupled(t))
              )

          } yield res
        }.provideSomeLayer[DMScreenSession](dataSourceLayer)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def upsert(
          header: CampaignHeader,
          info:   Json
        ): DMScreenTask[CampaignId] = {
          for {
            userId <- ZIO.serviceWith[DMScreenSession](_.user.id)
            res <-
              if (header.id != CampaignId.empty) {
                cache.invalidate((header.id, userId)) *>
                  ctx
                    .run(
                      qCampaigns
                        .filter(v =>
                          !v.deleted && v.value.id == lift(header.id) && v.value.header.dmUserId == lift(userId)
                        )
                        .updateValue(lift(DBObject(Campaign(header, info))))
                    )
                    .as(header.id)
              } else {
                ctx
                  .run(
                    qCampaigns
                      .insertValue(lift(DBObject(Campaign(header, info))))
                      .returningGenerated(_.value.header.id)
                  )
              }
          } yield res

        }.provideSomeLayer[DMScreenSession](dataSourceLayer)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def campaignLogs(
          campaignId: CampaignId,
          maxNum:     Int
        ): DMScreenTask[Seq[CampaignLogEntry]] = {
          for {
            userId <- ZIO.serviceWith[DMScreenSession](_.user.id)
            res <- ctx
              .run(
                qCampaignLog
                  .filter(_.campaignId == lift(campaignId))
                  .join(
                    qCampaigns.filter(v =>
                      !v.deleted && v.value.header.id == lift(campaignId) && v.value.header.dmUserId == lift(userId)
                    )
                  ).on(
                    (
                      log,
                      campaign
                    ) => log.campaignId == campaign.value.header.id
                  )
                  .map(_._1)
                  .sortBy(_.timestamp)(Ord.desc)
                  .take(lift(maxNum))
              )
          } yield res
        }.provideSomeLayer[DMScreenSession](dataSourceLayer)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def campaignLog(
          campaignId: CampaignId,
          message:    String
        ): DMScreenTask[Unit] = {
          val entry = CampaignLogEntry(
            campaignId = campaignId,
            message = message,
            timestamp = LocalDateTime.now()
          )
          ctx.transaction(for {
            userId <- ZIO.serviceWith[DMScreenSession](_.user.id)
            campaignExists <- ctx.run(
              qCampaigns
                .filter(v =>
                  !v.deleted && v.value.header.id == lift(campaignId) && v.value.header.dmUserId == lift(userId)
                ).nonEmpty
            )
            _ <- ZIO.fail(RepositoryError(s"Invalid campaign id: $campaignId")).when(!campaignExists)
            _ <- ctx
              .run(
                qCampaignLog
                  .insertValue(
                    lift(entry)
                  )
              )
          } yield ())
        }.unit
          .provideSomeLayer[DMScreenSession](dataSourceLayer)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def deleteCampaign(
          id:         CampaignId,
          softDelete: Boolean
        ): DMScreenTask[Unit] = {
          ctx.transaction(for {
            userId <- ZIO.serviceWith[DMScreenSession](_.user.id)
            _ <-
              if (softDelete) {
                ctx
                  .run(
                    qCampaigns
                      .filter(v => v.value.header.id == lift(id) && v.value.header.dmUserId == lift(userId))
                      .update(_.deleted -> lift(softDelete))
                  )
              } else {
                ctx
                  .run(
                    qCampaigns
                      .filter(v => v.value.header.id == lift(id) && v.value.header.dmUserId == lift(userId))
                      .delete
                  )
              }
          } yield ())
        }.unit
          .provideSomeLayer[DMScreenSession](dataSourceLayer)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        def snapshotCampaign(campaignId: CampaignId): DMScreenTask[CampaignHeader] = {
          ctx.transaction(for {
            oldCampaignOpt <- campaign(campaignId)
            campaign       <- oldCampaignOpt.fold(ZIO.fail(RepositoryError("Campaign not found")))(e => ZIO.succeed(e))
            now            <- Clock.localDateTime.map(_.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")))
            newId <- upsert(
              campaign.header.copy(
                id = CampaignId.empty,
                name = s"${campaign.header.name.replaceAll("\\s*\\(snapshot \\d{8}_\\d{4}\\)", "")} (snapshot ${now})"
              ),
              campaign.jsonInfo
            )
          } yield campaign.header.copy(id = newId))
        }.provideSomeLayer[DMScreenSession](dataSourceLayer)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

      }
    }

}
