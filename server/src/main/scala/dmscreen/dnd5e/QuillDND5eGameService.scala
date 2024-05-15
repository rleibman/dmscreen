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

import dmscreen.{ConfigurationError, ConfigurationService, DMScreenEnvironment, DMScreenError, UserId}
import io.getquill.extras.*
import io.getquill.jdbczio.Quill
import io.getquill.{query as qquery, *}
import zio.*
import zio.json.*
import zio.json.ast.Json

import javax.sql.DataSource

object QuillDND5eGameService {

  private case class CampaignRow(
    id:       Long,
    name:     String,
    dm:       Long,
    gameType: String,
    info:     Json
  ) {

    def toCampaign: Campaign = {
      val decoder = summon[JsonDecoder[CampaignInfo]]

      decoder
        .fromJsonAST(info).fold(
          msg => throw new RuntimeException(msg),
          info => Campaign(CampaignHeader(id = CampaignId(id), dm = UserId(dm), name = name), info)
        )
    }

  }

  def db: ZLayer[ConfigurationService, ConfigurationError, DND5eGameService] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
      } yield new DND5eGameService() {

        private object ctx extends MysqlZioJdbcContext(MysqlEscape)

        import ctx.*

        private val dataSourceLayer = Quill.DataSource.fromDataSource(config.dataSource)

        given MappedEncoding[Json, String] = MappedEncoding[Json, String](_.toJson)

        given MappedEncoding[String, Json] =
          MappedEncoding[String, Json](s =>
            Json.decoder
              .decodeJson(s).fold(
                msg => throw new RuntimeException(msg),
                identity
              )
          )

        inline private def qCampaigns =
          quote {
            querySchema[CampaignRow]("campaign")
          }

        override def campaigns: ZIO[DMScreenEnvironment, DMScreenError, Seq[CampaignHeader]] =
          ctx
            .run(qCampaigns.map(a => (a.id, a.dm, a.name)))
            .map(_.map(t => CampaignHeader(id = CampaignId(t._1), dm = UserId(t._2), name = t._3)))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def campaign(campaignId: CampaignId): ZIO[DMScreenEnvironment, DMScreenError, Option[Campaign]] =
          ctx
            .run(qCampaigns.filter(_.id == lift(campaignId.value)))
            .map(_.headOption.map(_.toCampaign))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def playerCharacters(campaignId: CampaignId)
          : ZIO[DMScreenEnvironment, DMScreenError, Seq[PlayerCharacter]] = ???

        override def nonPlayerCharacters(campaignId: CampaignId)
          : ZIO[DMScreenEnvironment, DMScreenError, Seq[NonPlayerCharacter]] = ???

        override def encounters(campaignId: CampaignId): ZIO[DMScreenEnvironment, DMScreenError, Seq[EncounterHeader]] =
          ???

        override def encounter(encounterId: EncounterId): ZIO[DMScreenEnvironment, DMScreenError, Seq[Encounter]] = ???

        override def bestiary(search: MonsterSearch): ZIO[DMScreenEnvironment, DMScreenError, Seq[Monster]] = ???

        override def sources: ZIO[DMScreenEnvironment, DMScreenError, Seq[Source]] = ???

        override def classes: ZIO[DMScreenEnvironment, DMScreenError, Seq[CharacterClass]] = ???

        override def races: ZIO[DMScreenEnvironment, DMScreenError, Seq[Race]] = ???

        override def backgrounds: ZIO[DMScreenEnvironment, DMScreenError, Seq[Background]] = ???

        override def subClasses(characterClass: CharacterClassId)
          : ZIO[DMScreenEnvironment, DMScreenError, Seq[Subclass]] = ???

        override def insert(
          campaignHeader: CampaignHeader,
          info:           Json
        ): ZIO[DMScreenEnvironment, DMScreenError, CampaignId] = ???

        override def applyOperation(
          campaignId: CampaignId,
          operation:  Operation
        ): ZIO[DMScreenEnvironment, DMScreenError, Unit] = ???

        override def delete(campaignId: CampaignId): ZIO[DMScreenEnvironment, DMScreenError, Unit] = ???
      }
    }

}