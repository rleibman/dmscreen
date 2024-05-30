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

import dmscreen.*
import io.getquill.extras.*
import io.getquill.jdbczio.Quill
import io.getquill.{query as qquery, *}
import just.semver.SemVer
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.nio.file.*

import java.sql.SQLException
import javax.sql.DataSource
import scala.annotation.nowarn
import scala.reflect.ClassTag

object QuillDND5eRepository {

  private case class CampaignRow(
    id:         Long,
    name:       String,
    dm:         Long,
    gameSystem: String,
    info:       Json,
    version:    String
  ) {

    def toModel: DND5eCampaign =
      DND5eCampaign(
        header = CampaignHeader(id = CampaignId(id), dm = UserId(dm), name = name),
        jsonInfo = info,
        version = SemVer.unsafeParse(version)
      )

  }

  private case class PlayerCharacterRow(
    id:         Long,
    campaignId: Long,
    name:       String,
    playerName: Option[String],
    info:       Json,
    version:    String
  ) {

    def toModel: PlayerCharacter =
      PlayerCharacter(
        header = PlayerCharacterHeader(
          id = PlayerCharacterId(id),
          campaignId = CampaignId(campaignId),
          name = name,
          playerName = playerName
        ),
        jsonInfo = info,
        version = SemVer.unsafeParse(version)
      )

  }

  private case class NonPlayerCharacterRow(
    id:         Long,
    campaignId: Long,
    name:       String,
    info:       Json,
    version:    String
  ) {

    def toModel: NonPlayerCharacter =
      NonPlayerCharacter(
        header = NonPlayerCharacterHeader(
          id = NonPlayerCharacterId(id),
          campaignId = CampaignId(campaignId),
          name = name
        ),
        jsonInfo = info,
        version = SemVer.unsafeParse(version)
      )

  }

  private case class EncounterRow(
    id:         Long,
    campaignId: Long,
    name:       String,
    info:       Json,
    version:    String
  ) {

    def toModel: Encounter =
      Encounter(
        header = EncounterHeader(
          id = EncounterId(id),
          campaignId = CampaignId(campaignId),
          name = name
        ),
        jsonInfo = info,
        version = SemVer.unsafeParse(version)
      )

  }

  private case class MonsterRow(
    id:          Long,
    name:        String,
    monsterType: String,
    biome:       Option[String],
    alignment:   Option[String],
    cr:          Double,
    xp:          Int,
    ac:          Int,
    hp:          Int,
    size:        String,
    info:        Json,
    version:     String
  ) {

    def toModel: Monster =
      Monster(
        header = MonsterHeader(
          id = MonsterId(id),
          name = name,
          monsterType = MonsterType.valueOf(monsterType),
          biome = biome.map(Biome.valueOf),
          alignment = alignment.map(Alignment.valueOf),
          cr = cr,
          xp = xp,
          ac = ac,
          hp = hp,
          size = CreatureSize.valueOf(size)
        ),
        jsonInfo = info,
        version = SemVer.unsafeParse(version)
      )

  }

  private def readFromResource[A: JsonDecoder](resourceName: String): IO[DMScreenError, A] = {
    getClass.getResource(".")
    val uri = getClass.getResource(resourceName).nn.toURI.nn
    Files
      .readAllBytes(Path(uri)).map(bytes => new String(bytes.toArray, "UTF-8"))
      .mapBoth(
        e => DMScreenError("", Some(e)),
        _.fromJson[A].left.map(DMScreenError(_))
      )
      .flatMap(ZIO.fromEither)

  }

  def db: ZLayer[ConfigurationService, DMScreenError, DND5eRepository] =
    ZLayer.fromZIO {
      for {
        config                  <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
        chachedCharacterClasses <- readFromResource[Seq[CharacterClass]]("/data/classes.json")
        cachedSources           <- readFromResource[Seq[Source]]("/data/sources.json")
        cachedSubclasses        <- readFromResource[Map[CharacterClassId, Seq[Subclass]]]("/data/subclasses.json")
        cachedRaces             <- readFromResource[Seq[Race]]("/data/races.json")
        cachedBackgrounds       <- readFromResource[Seq[Background]]("/data/backgrounds.json")

      } yield new DND5eRepository() {

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

        inline private def qCampaigns =
          quote {
            querySchema[CampaignRow]("campaign")
          }

        inline private def qPlayerCharacters =
          quote {
            querySchema[PlayerCharacterRow]("playerCharacter")
          }

        inline private def qNonPlayerCharacters =
          quote {
            querySchema[NonPlayerCharacterRow]("nonPlayerCharacter")
          }

        inline private def qMonsters =
          quote {
            querySchema[MonsterRow]("monster")
          }

        inline private def qEncounters =
          quote {
            querySchema[EncounterRow]("encounter")
          }

        override def campaigns: IO[DMScreenError, Seq[CampaignHeader]] =
          ctx
            .run(qCampaigns.map(a => (a.id, a.dm, a.name, a.gameSystem)))
            .map(
              _.map(t =>
                CampaignHeader(
                  id = CampaignId(t._1),
                  dm = UserId(t._2),
                  name = t._3,
                  gameSystem = GameSystem.valueOf(t._4)
                )
              )
            )
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def playerCharacter(playerCharacterId: PlayerCharacterId): IO[DMScreenError, Option[PlayerCharacter]] =
          ctx
            .run(qPlayerCharacters.filter(_.id == lift(playerCharacterId.value)))
            .map(_.headOption.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def campaign(campaignId: CampaignId): IO[DMScreenError, Option[DND5eCampaign]] =
          ctx
            .run(qCampaigns.filter(_.id == lift(campaignId.value)))
            .map(_.headOption.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def playerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[PlayerCharacter]] =
          ctx
            .run(qPlayerCharacters.filter(_.campaignId == lift(campaignId.value)))
            .map(_.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def nonPlayerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[NonPlayerCharacter]] =
          ctx
            .run(qNonPlayerCharacters.filter(_.campaignId == lift(campaignId.value)))
            .map(_.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def encounters(campaignId: CampaignId): IO[DMScreenError, Seq[EncounterHeader]] =
          ctx
            .run(qEncounters.filter(_.campaignId == lift(campaignId.value)).map(a => (a.id, a.campaignId, a.name)))
            .map(
              _.map(t =>
                EncounterHeader(
                  id = EncounterId(t._1),
                  campaignId = CampaignId(t._2),
                  name = t._3
                )
              )
            )
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def encounter(encounterId: EncounterId): IO[DMScreenError, Seq[Encounter]] =
          ctx
            .run(qEncounters.filter(_.id == lift(encounterId.value)))
            .map(_.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def bestiary(search: MonsterSearch): IO[DMScreenError, Seq[Monster]] =
          ctx
            .run(qMonsters)
            .map(_.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def sources: IO[DMScreenError, Seq[Source]] = ZIO.succeed(cachedSources)

        override def classes: IO[DMScreenError, Seq[CharacterClass]] = ZIO.succeed(chachedCharacterClasses)

        override def races: IO[DMScreenError, Seq[Race]] = ZIO.succeed(cachedRaces)

        override def backgrounds: IO[DMScreenError, Seq[Background]] = ZIO.succeed(cachedBackgrounds)

        override def subClasses(characterClass: CharacterClassId): IO[DMScreenError, Seq[Subclass]] = {
          ZIO.succeed(cachedSubclasses.get(characterClass).toSeq.flatten)
        }

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

        @nowarn // We're using ClassTag to inject compile info to runtime so we can match on the IDType, but it still produces a warning
        override def applyOperations[IDType](
          entityType: EntityType,
          id:         IDType,
          operations: DMScreenOperation*
        ): IO[DMScreenError, Unit] = {
          entityType match {
            case DND5eEntityType.campaign => applyOperationsCampaign(CampaignId(id.asInstanceOf[Long]), operations*)
            case DND5eEntityType.playerCharacter =>
              applyOperationsPlayerCharacter(PlayerCharacterId(id.asInstanceOf[Long]), operations*)
            case _ => ZIO.fail(DMScreenError("Can't apply operation to an id $id"))
          }
        }

        def applyOperationsCampaign(
          campaignId: CampaignId,
          operations: DMScreenOperation*
        ): IO[DMScreenError, Unit] =
          ctx
            .transaction {
              // UPDATE t SET json_col = JSON_SET(json_col, '$.name', 'Knut') WHERE id = 123
              ZIO
                .foreach(operations) {
                  case Add(path, value) =>
                    ctx
                      .run(
                        qCampaigns
                          .filter(_.id == lift(campaignId.value)).update(a =>
                            a.info -> jsonInsert(a.info, lift(path.value), lift(value))
                          )
                      )
                  case Remove(path) =>
                    ctx
                      .run(
                        qCampaigns
                          .filter(_.id == lift(campaignId.value)).update(a =>
                            a.info -> jsonRemove(a.info, lift(path.value))
                          )
                      )
                  case Replace(path, value) =>
                    ctx
                      .run(
                        qCampaigns
                          .filter(_.id == lift(campaignId.value)).update(a =>
                            a.info -> jsonReplace(a.info, lift(path.value), lift(value))
                          )
                      )
                  case Move(from, path) => ??? // Currently Not supported, but probably just read, then a delete followed by an insert
                  case Copy(from, path) => ??? // Currently Not supported, but probably just a read followed by an insert
                  case Test(path, value) => ??? // Currently Not supported
                }.unit
            }.provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        def applyOperationsPlayerCharacter(
          playerCharacterId: PlayerCharacterId,
          operations:        DMScreenOperation*
        ): IO[DMScreenError, Unit] =
          ctx
            .transaction {
              // UPDATE t SET json_col = JSON_SET(json_col, '$.name', 'Knut') WHERE id = 123
              ZIO
                .foreach(operations) {
                  case Add(path, value) =>
                    ctx
                      .run(
                        qPlayerCharacters
                          .filter(_.id == lift(playerCharacterId.value)).update(a =>
                            a.info -> jsonInsert(a.info, lift(path.value), lift(value))
                          )
                      )
                  case Remove(path) =>
                    ctx
                      .run(
                        qPlayerCharacters
                          .filter(_.id == lift(playerCharacterId.value)).update(a =>
                            a.info -> jsonRemove(a.info, lift(path.value))
                          )
                      )
                  case Replace(path, value) =>
                    ctx
                      .run(
                        qPlayerCharacters
                          .filter(_.id == lift(playerCharacterId.value)).update(a =>
                            a.info -> jsonReplace(a.info, lift(path.value), lift(value))
                          )
                      )
                  case Move(from, path) => ??? // Currently Not supported, but probably just read, then a delete followed by an insert
                  case Copy(from, path) => ??? // Currently Not supported, but probably just a read followed by an insert
                  case Test(path, value) => ??? // Currently Not supported
                }.unit
            }.provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def deleteEntity[IDType](
          entityType: EntityType,
          id:         IDType,
          softDelete: Boolean = true
        ): IO[DMScreenError, Unit] = {
          // TODO implement soft deletes
          entityType match {
            case DND5eEntityType.campaign =>
              ctx
                .run(qCampaigns.filter(_.id == lift(id.asInstanceOf[CampaignId].value)).delete)
                .unit
                .provideLayer(dataSourceLayer)
                .mapError(RepositoryError.apply)
            case DND5eEntityType.playerCharacter =>
              ctx
                .run(qPlayerCharacters.filter(_.id == lift(id.asInstanceOf[PlayerCharacterId].value)).delete)
                .unit
                .provideLayer(dataSourceLayer)
                .mapError(RepositoryError.apply)
            case _ => ZIO.fail(DMScreenError(s"Don't know how to delete ${entityType.name}"))
          }
        }

        override def spells: IO[DMScreenError, Seq[Spell]] = ???

        override def insert(
          header: CampaignHeader,
          info:   Json
        ): IO[DMScreenError, CampaignId] =
          if (header.id != CampaignId.empty) {
            ZIO.fail(DMScreenError("Can't insert a campaign with an id"))
          } else {
            ctx
              .run(
                qCampaigns
                  .insertValue(
                    lift(
                      CampaignRow(
                        id = CampaignId.empty.value,
                        name = header.name,
                        dm = header.dm.value,
                        gameSystem = header.gameSystem.toString,
                        info = info,
                        version = dmscreen.BuildInfo.version
                      )
                    )
                  )
                  .returningGenerated(_.id)
              )
              .map(CampaignId.apply)
              .provideLayer(dataSourceLayer)
              .mapError(RepositoryError.apply)
          }

        override def insert(
          header: PlayerCharacterHeader,
          info:   Json
        ): IO[DMScreenError, PlayerCharacterId] =
          if (header.id != PlayerCharacterId.empty) {
            ZIO.fail(DMScreenError("Can't insert a player character with an id"))
          } else {
            ctx
              .run(
                qPlayerCharacters
                  .insertValue(
                    lift(
                      PlayerCharacterRow(
                        id = PlayerCharacterId.empty.value,
                        campaignId = header.campaignId.value,
                        name = header.name,
                        playerName = header.playerName,
                        info = info,
                        version = dmscreen.BuildInfo.version
                      )
                    )
                  )
                  .returningGenerated(_.id)
              )
              .map(PlayerCharacterId.apply)
              .provideLayer(dataSourceLayer)
              .mapError(RepositoryError.apply)
          }

        override def insert(
          header: NonPlayerCharacterHeader,
          info:   Json
        ): IO[DMScreenError, NonPlayerCharacterId] = ???

        override def insert(
          header: MonsterHeader,
          info:   Json
        ): IO[DMScreenError, MonsterId] = ???

        override def insert(
          header: SpellHeader,
          info:   Json
        ): IO[DMScreenError, SpellId] = ???

        override def insert(
          header: EncounterHeader,
          info:   Json
        ): IO[DMScreenError, EncounterId] = ???

      }
    }

}
