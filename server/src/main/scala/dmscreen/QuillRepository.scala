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

import io.getquill.*
import io.getquill.jdbczio.Quill
import just.semver.SemVer
import zio.*
import zio.json.*
import zio.json.ast.Json

import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource
import scala.reflect.ClassTag

trait DMScreenZIORepository extends DMScreenRepository[DMScreenTask]

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
        _.value.version           -> "version",
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
      for {
        config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)

      } yield new DMScreenZIORepository() {
        import DMScreenSchema.{*, given}

        private object ctx extends MysqlZioJdbcContext(MysqlEscape)

        import ctx.*
        private val dataSourceLayer: ZLayer[Any, Throwable, DataSource] =
          Quill.DataSource.fromDataSource(config.dataSource)

        override def campaign(campaignId: CampaignId): IO[DMScreenError, Option[Campaign]] =
          ctx
            .run(qCampaigns.filter(v => !v.deleted && v.value.header.id == lift(campaignId)))
            .map(_.headOption.map(_.value))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def campaigns: IO[DMScreenError, Seq[CampaignHeader]] =
          ctx
            .run(
              qCampaigns.map(a =>
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
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def upsert(
          header: CampaignHeader,
          info:   Json
        ): IO[DMScreenError, CampaignId] =
          (if (header.id != CampaignId.empty) {
             ctx
               .run(
                 qCampaigns
                   .filter(v => !v.deleted && v.value.id == lift(header.id))
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
           })
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        override def campaignLogs(
          campaignId: CampaignId,
          maxNum:     Int
        ): DMScreenTask[Seq[CampaignLogEntry]] =
          ctx
            .run(
              qCampaignLog
                .filter(v => v.campaignId == lift(campaignId))
                .sortBy(_.timestamp)(Ord.desc)
                .take(lift(maxNum))
            )
            .provideLayer(dataSourceLayer)
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
          ctx
            .run(
              qCampaignLog
                .insertValue(
                  lift(entry)
                )
            )
            .unit
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        }

        override def deleteEntity[IDType](
          entityType: EntityType[IDType],
          id:         IDType,
          softDelete: Boolean
        ): DMScreenTask[Unit] = ???
      }
    }

}
