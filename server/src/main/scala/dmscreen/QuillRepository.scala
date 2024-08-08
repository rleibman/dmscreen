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

import dmscreen.dnd5e.{DND5eEntityType, PlayerCharacterId}
import io.getquill.*
import io.getquill.extras.*
import io.getquill.jdbczio.Quill
import just.semver.SemVer
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.nio.file.*

import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource
import scala.annotation.nowarn
import scala.reflect.ClassTag

trait DMScreenZIORepository extends DMScreenRepository[DMScreenTask]

object QuillRepository {

  private object CampaignRow {

    def fromModel(
      header:   CampaignHeader,
      jsonInfo: Json
    ): CampaignRow = {
      CampaignRow(
        id = header.id.value,
        name = header.name,
        dmUserId = header.dmUserId.value,
        gameSystem = header.gameSystem.toString,
        campaignStatus = header.campaignStatus.toString,
        info = jsonInfo,
        version = dmscreen.BuildInfo.version,
        deleted = false
      )
    }

  }

  private case class CampaignRow(
    id:             Long,
    name:           String,
    dmUserId:       Long,
    gameSystem:     String,
    campaignStatus: String,
    info:           Json,
    version:        String,
    deleted:        Boolean
  ) {

    def toModel: Campaign =
      Campaign(
        header = CampaignHeader(
          id = CampaignId(id),
          dmUserId = UserId(dmUserId),
          name = name,
          gameSystem = GameSystem.valueOf(gameSystem),
          campaignStatus = CampaignStatus.valueOf(campaignStatus)
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  def db: ZLayer[ConfigurationService, DMScreenError, DMScreenZIORepository] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)

      } yield new DMScreenZIORepository() {
        private object ctx extends MysqlZioJdbcContext(MysqlEscape)

        import ctx.*
        private val dataSourceLayer: ZLayer[Any, Throwable, DataSource] =
          Quill.DataSource.fromDataSource(config.dataSource)

        given MappedEncoding[Json, String] = MappedEncoding[Json, String](_.toJson)

        given MappedEncoding[String, Json] =
          MappedEncoding[String, Json](s =>
            Json.decoder
              .decodeJson(s).fold(
                msg => throw new RuntimeException(msg),
                identity
              )
          )
        given MappedEncoding[Long, CampaignId] = MappedEncoding[Long, CampaignId](CampaignId.apply)
        given MappedEncoding[CampaignId, Long] = MappedEncoding[CampaignId, Long](_.value)

        inline private def qCampaigns =
          quote {
            querySchema[CampaignRow]("campaign")
          }
        inline private def qCampaignLog =
          quote {
            querySchema[CampaignLogEntry]("campaignLog")
          }

        val jsonInsert: Quoted[(Json, String, Json) => Json] = quote {
          (
            doc:   Json,
            path:  String,
            value: Json
          ) =>
            sql"JSON_INSERT($doc, $path, $value)".as[Json]
        }
        val jsonReplace: Quoted[(Json, String, Json) => Json] = quote {
          (
            doc:   Json,
            path:  String,
            value: Json
          ) =>
            sql"JSON_REPLACE($doc, $path, $value)".as[Json]
        }
        val jsonRemove: Quoted[(Json, String) => Json] = quote {
          (
            doc:  Json,
            path: String
          ) =>
            sql"JSON_REMOVE($doc, $path)".as[Json]
        }

        override def campaign(campaignId: CampaignId): IO[DMScreenError, Option[Campaign]] =
          ctx
            .run(qCampaigns.filter(v => !v.deleted && v.id == lift(campaignId.value)))
            .map(_.headOption.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def campaigns: IO[DMScreenError, Seq[CampaignHeader]] =
          ctx
            .run(qCampaigns.map(a => (a.id, a.dmUserId, a.name, a.gameSystem, a.campaignStatus)))
            .map(
              _.map(t =>
                CampaignHeader(
                  id = CampaignId(t._1),
                  dmUserId = UserId(t._2),
                  name = t._3,
                  gameSystem = GameSystem.valueOf(t._4),
                  campaignStatus = CampaignStatus.valueOf(t._5)
                )
              )
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
                   .filter(v => !v.deleted && v.id == lift(header.id.value))
                   .updateValue(lift(CampaignRow.fromModel(header, info)))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qCampaigns
                   .insertValue(
                     lift(
                       CampaignRow(
                         id = CampaignId.empty.value,
                         name = header.name,
                         dmUserId = header.dmUserId.value,
                         gameSystem = header.gameSystem.toString,
                         campaignStatus = header.campaignStatus.toString,
                         info = info,
                         version = dmscreen.BuildInfo.version,
                         deleted = false
                       )
                     )
                   )
                   .returningGenerated(_.id)
               )
               .map(CampaignId.apply)
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
