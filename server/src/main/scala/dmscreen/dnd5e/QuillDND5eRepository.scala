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
    dmUserId:   Long,
    gameSystem: String,
    info:       Json,
    version:    String
  ) {

    def toModel: DND5eCampaign =
      DND5eCampaign(
        header = CampaignHeader(id = CampaignId(id), dmUserId = UserId(dmUserId), name = name),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
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
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
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
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  private case class EncounterRow(
    id:         Long,
    campaignId: Long,
    sceneId:    Option[Long],
    name:       String,
    status:     String,
    order:      Int,
    info:       Json,
    version:    String
  ) {

    def toModel: Encounter =
      Encounter(
        header = EncounterHeader(
          id = EncounterId(id),
          campaignId = CampaignId(campaignId),
          sceneId = sceneId.map(SceneId.apply),
          name = name,
          status = EncounterStatus.valueOf(status),
          order = order
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  private case class MonsterRow(
    id:          Long,
    name:        String,
    monsterType: String,
    biome:       Option[String],
    alignment:   Option[String],
    cr:          Double,
    xp:          Long,
    armorClass:  Int,
    hitPoints:   Int,
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
          cr = ChallengeRating.fromDouble(cr).getOrElse(ChallengeRating.`0`),
          xp = xp,
          armorClass = armorClass,
          maximumHitPoints = hitPoints,
          size = CreatureSize.valueOf(size)
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
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
        cachedSubclasses        <- readFromResource[Map[CharacterClassId, Seq[SubClass]]]("/data/subclasses.json")
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
            .run(qCampaigns.map(a => (a.id, a.dmUserId, a.name, a.gameSystem)))
            .map(
              _.map(t =>
                CampaignHeader(
                  id = CampaignId(t._1),
                  dmUserId = UserId(t._2),
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

        override def encounters(campaignId: CampaignId): IO[DMScreenError, Seq[Encounter]] =
          ctx
            .run(
              qEncounters
                .filter(_.campaignId == lift(campaignId.value)).map(a =>
                  (a.id, a.campaignId, a.sceneId, a.name, a.status, a.order, a.info)
                )
            )
            .map(
              _.map(t =>
                Encounter(
                  header = EncounterHeader(
                    id = EncounterId(t._1),
                    campaignId = CampaignId(t._2),
                    sceneId = t._3.map(i => SceneId(i)),
                    name = t._4,
                    status = EncounterStatus.valueOf(t._5),
                    order = t._6
                  ),
                  jsonInfo = t._7
                )
              )
            )
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        override def bestiary(search: MonsterSearch): IO[DMScreenError, MonsterSearchResults] = {
          val q0: Quoted[EntityQuery[MonsterRow]] = qMonsters
          val q1: Quoted[EntityQuery[MonsterRow]] = search.name.fold(q0)(n => q0.filter(_.name like lift(s"%$n%")))
          val q2: Quoted[EntityQuery[MonsterRow]] =
            search.challengeRating.fold(q1)(n => q1.filter(_.cr == lift(n.value)))
          val q3: Quoted[EntityQuery[MonsterRow]] =
            search.monsterType.fold(q2)(n => q2.filter(_.monsterType == lift(n.toString)))
          val q4: Quoted[EntityQuery[MonsterRow]] =
            search.biome.fold(q3)(n => q3.filter(_.biome.contains(lift(n.toString))))
          val q5: Quoted[EntityQuery[MonsterRow]] =
            search.alignment.fold(q4)(n => q4.filter(_.alignment.contains(lift(n.toString))))
          val q6: Quoted[EntityQuery[MonsterRow]] = search.size.fold(q5)(n => q5.filter(_.size == lift(n.toString)))

          (for {
            monsters <- ctx.run(q6.take(lift(search.pageSize)).drop(lift(search.page * search.pageSize)))
            total    <- ctx.run(q6.size)
          } yield MonsterSearchResults(
            results = monsters.map(_.toModel),
            total = total
          ))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
        }

        override def sources: IO[DMScreenError, Seq[Source]] = ZIO.succeed(cachedSources)

        override def classes: IO[DMScreenError, Seq[CharacterClass]] = ZIO.succeed(chachedCharacterClasses)

        override def races: IO[DMScreenError, Seq[Race]] = ZIO.succeed(cachedRaces)

        override def backgrounds: IO[DMScreenError, Seq[Background]] = ZIO.succeed(cachedBackgrounds)

        override def subClasses(characterClass: CharacterClassId): IO[DMScreenError, Seq[SubClass]] = {
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
          operations: DMScreenEvent*
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
          operations: DMScreenEvent*
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
                  case _                 => ???
                }.unit
            }.provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)

        def applyOperationsPlayerCharacter(
          playerCharacterId: PlayerCharacterId,
          operations:        DMScreenEvent*
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
                  case _                 => ???
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
                        dmUserId = header.dmUserId.value,
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
        ): IO[DMScreenError, NonPlayerCharacterId] = {
          if (header.id != NonPlayerCharacterId.empty) {
            ZIO.fail(DMScreenError("Can't insert an NPC with an id"))
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
                        info = info,
                        version = dmscreen.BuildInfo.version
                      )
                    )
                  )
                  .returningGenerated(_.id)
              )
              .map(NonPlayerCharacterId.apply)
              .provideLayer(dataSourceLayer)
              .mapError(RepositoryError.apply)

          }
        }

        override def insert(
          header: MonsterHeader,
          info:   Json
        ): IO[DMScreenError, MonsterId] = {
          if (header.id != MonsterId.empty) {
            ZIO.fail(DMScreenError("Can't insert a monster character with an id"))
          } else {
            ctx
              .run(
                qMonsters
                  .insertValue(
                    lift(
                      MonsterRow(
                        id = MonsterId.empty.value,
                        name = header.name,
                        monsterType = header.monsterType.toString,
                        biome = header.biome.map(_.toString),
                        alignment = header.alignment.map(_.toString),
                        cr = header.cr.value,
                        xp = header.xp,
                        armorClass = header.armorClass,
                        hitPoints = header.maximumHitPoints,
                        size = header.size.toString,
                        info = info,
                        version = dmscreen.BuildInfo.version
                      )
                    )
                  )
                  .returningGenerated(_.id)
              )
              .map(MonsterId.apply)
              .provideLayer(dataSourceLayer)
              .mapError(RepositoryError.apply)

          }
        }

        override def insert(
          header: SpellHeader,
          info:   Json
        ): IO[DMScreenError, SpellId] = ???

        override def insert(
          header: EncounterHeader,
          info:   Json
        ): IO[DMScreenError, EncounterId] = {
          if (header.id != EncounterId.empty) {
            ZIO.fail(DMScreenError("Can't insert an encounter with an id"))
          } else {
            ctx
              .run(
                qEncounters
                  .insertValue(
                    lift(
                      EncounterRow(
                        id = EncounterId.empty.value,
                        campaignId = header.campaignId.value,
                        sceneId = header.sceneId.map(_.value),
                        name = header.name,
                        status = header.status.toString,
                        order = header.order,
                        info = info,
                        version = dmscreen.BuildInfo.version
                      )
                    )
                  )
                  .returningGenerated(_.id)
              )
              .map(EncounterId.apply)
              .provideLayer(dataSourceLayer)
              .mapError(RepositoryError.apply)

          }

        }

      }
    }

}
