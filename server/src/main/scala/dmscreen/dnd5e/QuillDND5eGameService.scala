package dmscreen.dnd5e

import dmscreen.{ConfigurationError, ConfigurationService, DMScreenEnvironment, DMScreenError}
import io.getquill.extras.*
import io.getquill.jdbczio.Quill
import io.getquill.{query as qquery, *}
import zio.*
import zio.json.*
import zio.json.ast.Json

import javax.sql.DataSource

object QuillDND5eGameService {

  private case class CampaignRow(
    id:   Long,
    name: String,
    info: Json
  ) {

    def toCampaign: Campaign = {
      given JsonDecoder[CampaignInfo] = JsonDecoder.derived[CampaignInfo] //Move this to a more global scope
      val decoder = summon[JsonDecoder[CampaignInfo]]

      decoder
        .fromJsonAST(info).fold(
          msg => throw new RuntimeException(msg),
          info => Campaign(CampaignHeader(CampaignId(id), name), info)
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

        given MappedEncoding[Json, String] = MappedEncoding[Json, String](_.asString.getOrElse(""))

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
            .run(qCampaigns.map(a => (a.id, a.name)))
            .map(_.map(t => CampaignHeader(CampaignId(t._1), t._2)))
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

        override def bestiary(search: MonsterSearch): ZIO[DMScreenEnvironment, DMScreenError, Seq[Monster]] = ???

        override def sources: ZIO[DMScreenEnvironment, DMScreenError, Seq[Source]] = ???

        override def classes: ZIO[DMScreenEnvironment, DMScreenError, Seq[CharacterClass]] = ???

        override def races: ZIO[DMScreenEnvironment, DMScreenError, Seq[Race]] = ???

        override def backgrounds: ZIO[DMScreenEnvironment, DMScreenError, Seq[Background]] = ???

        override def subClasses(characterClass: CharacterClassId)
          : ZIO[DMScreenEnvironment, DMScreenError, Seq[Subclass]] = ???
      }
    }

}
