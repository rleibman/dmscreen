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

import dmscreen.*
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

trait STAZIORepository extends STARepository[DMScreenTask]

object QuillRepository {

  private object CharacterRow {

    def fromModel(
      header:   CharacterHeader,
      jsonInfo: Json
    ): CharacterRow = {
      CharacterRow(
        id = header.id.value,
        campaignId = header.campaignId.value,
        name = header.name,
        playerName = header.playerName,
        info = jsonInfo,
        version = dmscreen.BuildInfo.version,
        deleted = false
      )
    }

  }

  private case class CharacterRow(
    id:         Long,
    campaignId: Long,
    name:       Option[String],
    playerName: Option[String],
    info:       Json,
    version:    String,
    deleted:    Boolean
  ) {

    def toModel: Character =
      Character(
        header = CharacterHeader(
          id = CharacterId(id),
          campaignId = CampaignId(campaignId),
          name = name,
          playerName = playerName
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  private object StarshipRow {

    def fromModel(
      header:   StarshipHeader,
      jsonInfo: Json
    ): StarshipRow = {
      StarshipRow(
        id = header.id.value,
        campaignId = header.campaignId.value,
        name = header.name,
        info = jsonInfo,
        version = dmscreen.BuildInfo.version,
        deleted = false
      )
    }

  }

  private case class StarshipRow(
    campaignId: Long,
    id:         Long,
    name:       Option[String],
    info:       Json,
    version:    String,
    deleted:    Boolean
  ) {

    def toModel: Starship =
      Starship(
        header = StarshipHeader(
          campaignId = CampaignId(campaignId),
          id = StarshipId(id),
          name = name
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  private object SceneRow {

    def fromModel(
      header:   SceneHeader,
      jsonInfo: Json
    ): SceneRow = {
      SceneRow(
        id = header.id.value,
        campaignId = header.campaignId.value,
        name = header.name,
        orderCol = header.orderCol,
        isActive = header.isActive,
        info = jsonInfo,
        version = dmscreen.BuildInfo.version,
        deleted = false
      )
    }

  }

  private case class SceneRow(
    id:         Long,
    campaignId: Long,
    name:       String,
    orderCol:   Int,
    isActive:   Boolean,
    info:       Json,
    version:    String,
    deleted:    Boolean
  ) {

    def toModel: Scene =
      Scene(
        header = SceneHeader(
          id = SceneId(id),
          campaignId = CampaignId(campaignId),
          name = name,
          orderCol = orderCol,
          isActive = isActive
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  private object NonPlayerCharacterRow {

    def fromModel(
      header:   NonPlayerCharacterHeader,
      jsonInfo: Json
    ): NonPlayerCharacterRow = {
      NonPlayerCharacterRow(
        id = header.id.value,
        campaignId = header.campaignId.value,
        name = header.name,
        isActive = header.isActive,
        info = jsonInfo,
        version = dmscreen.BuildInfo.version,
        deleted = false
      )
    }

  }

  private case class NonPlayerCharacterRow(
    id:         Long,
    campaignId: Long,
    name:       String,
    isActive:   Boolean,
    info:       Json,
    version:    String,
    deleted:    Boolean
  ) {

    def toModel: NonPlayerCharacter =
      NonPlayerCharacter(
        header = NonPlayerCharacterHeader(
          id = NonPlayerCharacterId(id),
          campaignId = CampaignId(campaignId),
          name = name,
          isActive = isActive
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  def db: ZLayer[ConfigurationService, DMScreenError, STAZIORepository] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
      } yield new STAZIORepository {
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

        inline private def qScenes =
          quote {
            querySchema[SceneRow]("STAScene")
          }
        inline private def qCharacters =
          quote {
            querySchema[CharacterRow]("STACharacter")
          }

        inline private def qNonPlayerCharacters =
          quote {
            querySchema[NonPlayerCharacterRow]("STANonPlayerCharacter")
          }

        inline private def qStarships =
          quote {
            querySchema[StarshipRow]("STAStarship")
          }

        override def starships(campaignId: CampaignId): DMScreenTask[Seq[Starship]] =
          ctx
            .run(qStarships.filter(v => !v.deleted && v.campaignId == lift(campaignId.value)))
            .map(_.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def characters(campaignId: CampaignId): DMScreenTask[Seq[Character]] = {
          ctx
            .run(qCharacters.filter(v => !v.deleted && v.campaignId == lift(campaignId.value)))
            .map(_.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        }

        override def scenes(campaignId: CampaignId): IO[DMScreenError, Seq[Scene]] =
          ctx
            .run(qScenes.filter(v => !v.deleted && v.campaignId == lift(campaignId.value)).sortBy(_.orderCol))
            .map(_.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def nonPlayerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[NonPlayerCharacter]] =
          ctx
            .run(qNonPlayerCharacters.filter(v => !v.deleted && v.campaignId == lift(campaignId.value)))
            .map(_.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        val jsonInsert: Quoted[(Json, String, Json) => Json] = quote {
          (
            doc:   Json,
            path:  String,
            value: Json
          ) => sql"JSON_INSERT($doc, $path, $value)".as[Json]
        }
        val jsonReplace: Quoted[(Json, String, Json) => Json] = quote {
          (
            doc:   Json,
            path:  String,
            value: Json
          ) => sql"JSON_REPLACE($doc, $path, $value)".as[Json]
        }
        val jsonRemove: Quoted[(Json, String) => Json] = quote {
          (
            doc:  Json,
            path: String
          ) => sql"JSON_REMOVE($doc, $path)".as[Json]
        }

        override def deleteEntity[IDType](
          entityType: EntityType[IDType],
          id:         IDType,
          softDelete: Boolean = true
        ): IO[DMScreenError, Unit] = {
          if (softDelete) {
            entityType match {
              //              case CampaignEntityType =>
              //                val idVal = id.asInstanceOf[CampaignId].value
              //                val total = for {
              //                  a <- ctx.run(qCampaigns.filter(_.id == lift(idVal)).update(_.deleted -> true))
              //                  b <- ctx.run(qScenes.filter(_.campaignId == lift(idVal)).update(_.deleted -> true))
              //                  d <- ctx.run(qCharacters.filter(_.campaignId == lift(idVal)).update(_.deleted -> true))
              //                  e <- ctx.run(qNonPlayerCharacters.filter(_.campaignId == lift(idVal)).update(_.deleted -> true))
              //                } yield a + b + c + d + e
              //                ctx.transaction(total)
              case STAEntityType.character =>
                val total = for {
                  a <- ctx
                    .run(
                      qCharacters
                        .filter(_.id == lift(id.asInstanceOf[CharacterId].value)).update(_.deleted -> true)
                    )
                } yield a
                ctx.transaction(total)
              case STAEntityType.nonPlayerCharacter =>
                val total = for {
                  a <- ctx
                    .run(
                      qNonPlayerCharacters
                        .filter(_.id == lift(id.asInstanceOf[NonPlayerCharacterId].value)).update(_.deleted -> true)
                    )
                } yield a
                ctx.transaction(total)
              case STAEntityType.starship =>
                val total = for {
                  a <- ctx
                    .run(
                      qStarships
                        .filter(_.id == lift(id.asInstanceOf[StarshipId].value)).update(_.deleted -> true)
                    )
                } yield a
                ctx.transaction(total)
              case STAEntityType.scene =>
                val total = for {
                  a <- ctx
                    .run(
                      qScenes
                        .filter(_.id == lift(id.asInstanceOf[SceneId].value))
                        .update(_.deleted -> true)
                    )
                } yield a
                ctx.transaction(total)
              case _ => ZIO.fail(DMScreenError(s"Don't know how to delete ${entityType.name}"))
            }
          } else {
            // Hard delete's are a bit easier, because we have ON DELETE CASCADE, so we don't have to delete anything else except for the main class
            entityType match {
              //              case CampaignEntityType =>
              //                ctx
              //                  .run(qCampaigns.filter(_.id == lift(id.asInstanceOf[CampaignId].value)).delete)
              case STAEntityType.character =>
                val total = for {
                  a <- ctx.run(qCharacters.filter(_.id == lift(id.asInstanceOf[CharacterId].value)).delete)
                } yield a
                ctx.transaction(total)
              case STAEntityType.nonPlayerCharacter =>
                val total = for {
                  a <- ctx.run(
                    qNonPlayerCharacters.filter(_.id == lift(id.asInstanceOf[NonPlayerCharacterId].value)).delete
                  )
                } yield a
                ctx.transaction(total)
              case STAEntityType.starship =>
                ctx.run(qStarships.filter(_.id == lift(id.asInstanceOf[SceneId].value)).delete)
              case STAEntityType.scene =>
                ctx.run(qScenes.filter(_.id == lift(id.asInstanceOf[SceneId].value)).delete)
              case _ => ZIO.fail(DMScreenError(s"Don't know how to delete ${entityType.name}"))
            }
          }

        }.unit
          .provideLayer(dataSourceLayer)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def upsert(
          header: CharacterHeader,
          info:   Json
        ): IO[DMScreenError, CharacterId] =
          (if (header.id != CharacterId.empty) {
             ctx
               .run(
                 qCharacters
                   .filter(v => !v.deleted && v.id == lift(header.id.value))
                   .updateValue(lift(CharacterRow.fromModel(header, info)))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qCharacters
                   .insertValue(
                     lift(
                       CharacterRow(
                         id = CharacterId.empty.value,
                         campaignId = header.campaignId.value,
                         name = header.name,
                         playerName = header.playerName,
                         info = info,
                         version = dmscreen.BuildInfo.version,
                         deleted = false
                       )
                     )
                   )
                   .returningGenerated(_.id)
               )
               .map(CharacterId.apply)
           })
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def upsert(
          header: NonPlayerCharacterHeader,
          info:   Json
        ): IO[DMScreenError, NonPlayerCharacterId] =
          (if (header.id != NonPlayerCharacterId.empty) {
             ctx
               .run(
                 qNonPlayerCharacters
                   .filter(v => !v.deleted && v.id == lift(header.id.value))
                   .updateValue(lift(NonPlayerCharacterRow.fromModel(header, info)))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qNonPlayerCharacters
                   .insertValue(
                     lift(
                       NonPlayerCharacterRow(
                         id = NonPlayerCharacterId.empty.value,
                         campaignId = header.campaignId.value,
                         name = header.name,
                         isActive = header.isActive,
                         info = info,
                         version = dmscreen.BuildInfo.version,
                         deleted = false
                       )
                     )
                   )
                   .returningGenerated(_.id)
               )
               .map(NonPlayerCharacterId.apply)
           })
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def upsert(
          header: StarshipHeader,
          info:   Json
        ): IO[DMScreenError, StarshipId] =
          (if (header.id != StarshipId.empty) {
             ctx
               .run(
                 qStarships
                   .filter(v => !v.deleted && v.id == lift(header.id.value))
                   .updateValue(lift(StarshipRow.fromModel(header, info)))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qStarships
                   .insertValue(
                     lift(
                       StarshipRow(
                         campaignId = header.campaignId.value,
                         id = StarshipId.empty.value,
                         name = header.name,
                         info = info,
                         version = dmscreen.BuildInfo.version,
                         deleted = false
                       )
                     )
                   )
                   .returningGenerated(_.id)
               )
               .map(StarshipId.apply)
           })
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def upsert(
          header: SceneHeader,
          info:   Json
        ): IO[DMScreenError, SceneId] = {
          (if (header.id != SceneId.empty) {
             ctx
               .run(
                 qScenes
                   .filter(v => !v.deleted && v.id == lift(header.id.value))
                   .updateValue(lift(SceneRow.fromModel(header, info)))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qScenes
                   .insertValue(
                     lift(
                       SceneRow(
                         id = SceneId.empty.value,
                         campaignId = header.campaignId.value,
                         name = header.name,
                         orderCol = header.orderCol,
                         isActive = header.isActive,
                         info = info,
                         version = dmscreen.BuildInfo.version,
                         deleted = false
                       )
                     )
                   )
                   .returningGenerated(_.id)
               )
               .map(SceneId.apply)
           })
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        }

        override def character(characterId: CharacterId): DMScreenTask[Option[Character]] =
          ctx
            .run(qCharacters.filter(v => !v.deleted && v.id == lift(characterId.value)))
            .map(_.headOption.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def nonPlayerCharacter(nonPlayerCharacterId: NonPlayerCharacterId)
          : DMScreenTask[Option[NonPlayerCharacter]] =
          ctx
            .run(qNonPlayerCharacters.filter(v => !v.deleted && v.id == lift(nonPlayerCharacterId.value)))
            .map(_.headOption.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

      }
    }

}
