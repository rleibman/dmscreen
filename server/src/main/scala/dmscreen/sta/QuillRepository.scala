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

import dmscreen.sta.STARepository
import dmscreen.{CampaignId, ConfigurationService, DMScreenError, DMScreenEvent, DMScreenTask, EntityType}
import io.getquill.jdbczio.Quill
import io.getquill.{MappedEncoding, MysqlEscape, MysqlZioJdbcContext}
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.nio.file.*

import javax.sql.DataSource

trait STAZIORepository extends STARepository[DMScreenTask]

object QuillRepository {

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

        override def scene(sceneId: SceneId): DMScreenTask[Option[Scene]] = ???

        override def applyOperations[IDType](
          entityType: EntityType[IDType],
          id:         IDType,
          operations: DMScreenEvent*
        ): DMScreenTask[Unit] = ???

        override def deleteEntity[IDType](
          entityType: EntityType[IDType],
          id:         IDType,
          softDelete: Boolean
        ): DMScreenTask[Unit] = ???

        override def characters(campaignId: CampaignId): DMScreenTask[Seq[Character]] = ???

        override def scenes(campaignId: CampaignId): DMScreenTask[Seq[Scene]] = ???

        override def character(characterId: CharacterId): DMScreenTask[Option[Character]] = ???

        override def nonPlayerCharacters(campaignId: CampaignId): DMScreenTask[Seq[NonPlayerCharacter]] = ???

        override def encounters(campaignId: CampaignId): DMScreenTask[Seq[Encounter]] = ???

        override def upsert(
          header: CharacterHeader,
          info:   Json
        ): DMScreenTask[CharacterId] = ???

        override def upsert(
          header: StarshipHeader,
          info:   Json
        ): DMScreenTask[StarshipId] = ???

        override def upsert(
          header: NonPlayerCharacterHeader,
          info:   Json
        ): DMScreenTask[NonPlayerCharacterId] = ???

        override def upsert(
          header: SceneHeader,
          info:   Json
        ): DMScreenTask[SceneId] = ???

        override def upsert(
          header: EncounterHeader,
          info:   Json
        ): DMScreenTask[EncounterId] = ???

        override def starships(campaignId: CampaignId): DMScreenTask[Seq[Starship]] = ???
      }
    }

}
