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

trait DND5eZIORepository extends DND5eRepository[DMScreenTask]

object QuillRepository {

  private object PlayerCharacterRow {

    def fromModel(
      header:   PlayerCharacterHeader,
      jsonInfo: Json
    ): PlayerCharacterRow = {
      PlayerCharacterRow(
        id = header.id.value,
        campaignId = header.campaignId.value,
        name = header.name,
        source = header.source,
        playerName = header.playerName,
        info = jsonInfo,
        version = dmscreen.BuildInfo.version,
        deleted = false
      )
    }

  }

  private case class PlayerCharacterRow(
    id:         Long,
    campaignId: Long,
    name:       String,
    playerName: Option[String],
    info:       Json,
    source:     ImportSource,
    version:    String,
    deleted:    Boolean
  ) {

    def toModel: PlayerCharacter =
      PlayerCharacter(
        header = PlayerCharacterHeader(
          id = PlayerCharacterId(id),
          campaignId = CampaignId(campaignId),
          name = name,
          source = source,
          playerName = playerName
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
    info:       Json,
    version:    String,
    deleted:    Boolean
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

  private object EncounterRow {

    def fromModel(
      header:   EncounterHeader,
      jsonInfo: Json
    ): EncounterRow = {
      EncounterRow(
        id = header.id.value,
        campaignId = header.campaignId.value,
        sceneId = header.sceneId.map(_.value),
        name = header.name,
        status = header.status.toString,
        orderCol = header.orderCol,
        info = jsonInfo,
        version = dmscreen.BuildInfo.version,
        deleted = false
      )
    }

  }

  private case class EncounterRow(
    id:         Long,
    campaignId: Long,
    sceneId:    Option[Long],
    name:       String,
    status:     String,
    orderCol:   Int,
    info:       Json,
    version:    String,
    deleted:    Boolean
  ) {

    def toModel: Encounter =
      Encounter(
        header = EncounterHeader(
          id = EncounterId(id),
          campaignId = CampaignId(campaignId),
          sceneId = sceneId.map(SceneId.apply),
          name = name,
          status = EncounterStatus.valueOf(status),
          orderCol = orderCol
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  private object MonsterRow {

    def fromModel(
      header:   MonsterHeader,
      jsonInfo: Json
    ): MonsterRow = {
      MonsterRow(
        id = header.id.value,
        sourceId = header.sourceId.value,
        name = header.name,
        monsterType = header.monsterType.toString,
        biome = header.biome.map(_.toString),
        alignment = header.alignment.map(_.toString),
        cr = header.cr.value,
        xp = header.xp,
        armorClass = header.armorClass,
        hitPoints = header.maximumHitPoints,
        size = header.size.toString,
        initiativeBonus = header.initiativeBonus,
        info = jsonInfo,
        version = dmscreen.BuildInfo.version,
        deleted = false
      )
    }

  }

  private case class MonsterRow(
    id:              Long,
    sourceId:        String,
    name:            String,
    monsterType:     String,
    biome:           Option[String],
    alignment:       Option[String],
    cr:              Double,
    xp:              Long,
    armorClass:      Int,
    hitPoints:       Int,
    size:            String,
    info:            Json,
    initiativeBonus: Int,
    version:         String,
    deleted:         Boolean
  ) {

    def toModel: Monster =
      Monster(
        header = MonsterHeader(
          id = MonsterId(id),
          sourceId = SourceId(sourceId),
          name = name,
          monsterType = MonsterType.valueOf(monsterType),
          biome = biome.map(Biome.valueOf),
          alignment = alignment.map(Alignment.valueOf),
          cr = ChallengeRating.fromDouble(cr).getOrElse(ChallengeRating.`0`),
          xp = xp,
          armorClass = armorClass,
          maximumHitPoints = hitPoints,
          size = CreatureSize.valueOf(size),
          initiativeBonus = initiativeBonus
        ),
        jsonInfo = info,
        version = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
      )

  }

  private object MonsterHeaderRow {

    def fromModel(
      header: MonsterHeader
    ): MonsterHeaderRow = {
      MonsterHeaderRow(
        id = header.id.value,
        sourceId = header.sourceId.value,
        name = header.name,
        monsterType = header.monsterType.toString,
        biome = header.biome.map(_.toString),
        alignment = header.alignment.map(_.toString),
        cr = header.cr.value,
        xp = header.xp,
        armorClass = header.armorClass,
        hitPoints = header.maximumHitPoints,
        size = header.size.toString,
        initiativeBonus = header.initiativeBonus,
        deleted = false
      )
    }

  }

  private case class MonsterHeaderRow(
    id:              Long,
    sourceId:        String,
    name:            String,
    monsterType:     String,
    biome:           Option[String],
    alignment:       Option[String],
    cr:              Double,
    xp:              Long,
    armorClass:      Int,
    hitPoints:       Int,
    size:            String,
    initiativeBonus: Int,
    deleted:         Boolean
  ) {

    def toModel: MonsterHeader =
      MonsterHeader(
        id = MonsterId(id),
        sourceId = SourceId(sourceId),
        name = name,
        monsterType = MonsterType.valueOf(monsterType),
        biome = biome.map(Biome.valueOf),
        alignment = alignment.map(Alignment.valueOf),
        cr = ChallengeRating.fromDouble(cr).getOrElse(ChallengeRating.`0`),
        xp = xp,
        armorClass = armorClass,
        maximumHitPoints = hitPoints,
        size = CreatureSize.valueOf(size),
        initiativeBonus = initiativeBonus
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

  def db: ZLayer[ConfigurationService, DMScreenError, DND5eZIORepository] =
    ZLayer.fromZIO {
      for {
        config                  <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
        chachedCharacterClasses <- readFromResource[Seq[CharacterClass]]("/data/classes.json")
        cachedSources           <- readFromResource[Seq[Source]]("/data/sources.json")
        cachedSubclasses        <- readFromResource[Map[CharacterClassId, Seq[SubClass]]]("/data/subclasses.json")
        cachedRaces             <- readFromResource[Seq[Race]]("/data/races.json")
        cachedBackgrounds       <- readFromResource[Seq[Background]]("/data/backgrounds.json")

      } yield new DND5eZIORepository() {

        private object ctx extends MysqlZioJdbcContext(MysqlEscape)

        import ctx.*

        private val dataSourceLayer: TaskLayer[DataSource] = Quill.DataSource.fromDataSource(config.dataSource)

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
            querySchema[SceneRow]("DND5eScene")
          }
        inline private def qPlayerCharacters =
          quote {
            querySchema[PlayerCharacterRow]("DND5ePlayerCharacter")
          }

        inline private def qNonPlayerCharacters =
          quote {
            querySchema[NonPlayerCharacterRow]("DND5eNonPlayerCharacter")
          }

        inline private def qMonsters =
          quote {
            querySchema[MonsterRow]("DND5eMonster")
          }

        inline private def qMonsterHeaders =
          quote {
            querySchema[MonsterHeaderRow]("DND5eMonster")
          }

        inline private def qEncounters =
          quote {
            querySchema[EncounterRow]("DND5eEncounter")
          }

        override def monster(monsterId: MonsterId): DMScreenTask[Option[Monster]] =
          ctx
            .run(qMonsters.filter(v => !v.deleted && v.id == lift(monsterId.value)))
            .map(_.headOption.map(_.toModel))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        private given Decoder[ImportSource] =
          decoder(
            (
              index,
              row,
              session
            ) => row.getString(index).fromJson[ImportSource].toOption.get
          )

        private given Encoder[ImportSource] =
          encoder(
            java.sql.Types.VARCHAR,
            (
              index,
              value,
              row
            ) => row.setString(index, value.toJson)
          )

        override def playerCharacters(
          campaignId: CampaignId,
          search:     PlayerCharacterSearch
        ): DMScreenTask[Seq[PlayerCharacter]] = {
          val q0: Quoted[EntityQuery[PlayerCharacterRow]] =
            qPlayerCharacters.filter(v => !v.deleted && v.campaignId == lift(campaignId.value))
          val q1: Quoted[EntityQuery[PlayerCharacterRow]] =
            search.dndBeyondId.fold(q0)(dndBeyondId =>
              q0.filter(_.source == lift(DNDBeyondImportSource(dndBeyondId).asInstanceOf[ImportSource]))
            )

          ctx
            .run(q1)
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

        override def encounters(campaignId: CampaignId): IO[DMScreenError, Seq[Encounter]] =
          ctx
            .run(
              qEncounters
                .filter(v => !v.deleted && v.campaignId == lift(campaignId.value)).map(a =>
                  (a.id, a.campaignId, a.sceneId, a.name, a.status, a.orderCol, a.info)
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
                    orderCol = t._6
                  ),
                  jsonInfo = t._7
                )
              )
            )
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def bestiary(search: MonsterSearch): IO[DMScreenError, MonsterSearchResults] = {
          val q0: Quoted[EntityQuery[MonsterHeaderRow]] = qMonsterHeaders.filter(v => !v.deleted)
          val q1: Quoted[EntityQuery[MonsterHeaderRow]] =
            search.name.fold(q0)(n => q0.filter(_.name like lift(s"%$n%")))
          val q2: Quoted[EntityQuery[MonsterHeaderRow]] =
            search.challengeRating.fold(q1)(n => q1.filter(_.cr == lift(n.value)))
          val q3: Quoted[EntityQuery[MonsterHeaderRow]] =
            search.monsterType.fold(q2)(n => q2.filter(_.monsterType == lift(n.toString)))
          val q4: Quoted[EntityQuery[MonsterHeaderRow]] =
            search.biome.fold(q3)(n => q3.filter(_.biome.contains(lift(n.toString))))
          val q5: Quoted[EntityQuery[MonsterHeaderRow]] =
            search.alignment.fold(q4)(n => q4.filter(_.alignment.contains(lift(n.toString))))
          val q6: Quoted[EntityQuery[MonsterHeaderRow]] =
            search.size.fold(q5)(n => q5.filter(_.size == lift(n.toString)))

          // If sort is random, then it's a bit more complex, for now, we do it the simple (but non-performing) way, read this
          // https://jan.kneschke.de/projects/mysql/order-by-rand/
          // ^^ use the stored procedure

          val q7 = quote(
            q6
              .drop(lift(search.page * search.pageSize))
              .take(lift(search.pageSize))
          )

          val q8: Quoted[Query[MonsterHeaderRow]] = (search.orderCol, search.orderDir) match {
            case (MonsterSearchOrder.challengeRating, OrderDirection.asc) =>
              quote(q7.sortBy(r => r.cr)(Ord.asc))
            case (MonsterSearchOrder.challengeRating, OrderDirection.desc) =>
              quote(q7.sortBy(r => r.cr)(Ord.desc))
            case (MonsterSearchOrder.size, OrderDirection.asc) =>
              quote(q7.sortBy(r => r.size)(Ord.asc))
            case (MonsterSearchOrder.size, OrderDirection.desc) =>
              quote(q7.sortBy(r => r.size)(Ord.desc))
            case (MonsterSearchOrder.alignment, OrderDirection.asc) =>
              quote(q7.sortBy(r => r.alignment)(Ord.asc))
            case (MonsterSearchOrder.alignment, OrderDirection.desc) =>
              quote(q7.sortBy(r => r.alignment)(Ord.desc))
            case (MonsterSearchOrder.biome, OrderDirection.asc) =>
              quote(q7.sortBy(r => r.biome)(Ord.asc))
            case (MonsterSearchOrder.biome, OrderDirection.desc) =>
              quote(q7.sortBy(r => r.biome)(Ord.desc))
            case (MonsterSearchOrder.monsterType, OrderDirection.asc) =>
              quote(q7.sortBy(r => r.monsterType)(Ord.asc))
            case (MonsterSearchOrder.monsterType, OrderDirection.desc) =>
              quote(q7.sortBy(r => r.monsterType)(Ord.desc))
            case (MonsterSearchOrder.random, OrderDirection.asc) =>
              quote(q7.sortBy(_ => infix"RAND()"))
            case (_, OrderDirection.asc) =>
              quote(q7.sortBy(r => r.name)(Ord.asc))
            case (_, OrderDirection.desc) =>
              quote(q7.sortBy(r => r.name)(Ord.desc))
          }

          (for {
            monsters <- ctx.run(q8)
            total    <- ctx.run(q6.size)
          } yield MonsterSearchResults(
            results = monsters.map(_.toModel),
            total = total
          ))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        }

        override def sources: IO[DMScreenError, Seq[Source]] = ZIO.succeed(cachedSources)

        override def classes: IO[DMScreenError, Seq[CharacterClass]] = ZIO.succeed(chachedCharacterClasses)

        override def races: IO[DMScreenError, Seq[Race]] = ZIO.succeed(cachedRaces)

        override def backgrounds: IO[DMScreenError, Seq[Background]] = ZIO.succeed(cachedBackgrounds)

        override def subClasses(characterClass: CharacterClassId): IO[DMScreenError, Seq[SubClass]] = {
          ZIO.succeed(cachedSubclasses.get(characterClass).toSeq.flatten)
        }

        val jsonInsert = quote {
          (
            doc:   Json,
            path:  String,
            value: Json
          ) => sql"JSON_INSERT($doc, $path, $value)".as[Json]
        }
        val jsonReplace = quote {
          (
            doc:   Json,
            path:  String,
            value: Json
          ) => sql"JSON_REPLACE($doc, $path, $value)".as[Json]
        }
        val jsonRemove = quote {
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
//                  c <- ctx.run(qEncounters.filter(_.campaignId == lift(idVal)).update(_.deleted -> true))
//                  d <- ctx.run(qPlayerCharacters.filter(_.campaignId == lift(idVal)).update(_.deleted -> true))
//                  e <- ctx.run(qNonPlayerCharacters.filter(_.campaignId == lift(idVal)).update(_.deleted -> true))
//                } yield a + b + c + d + e
//                ctx.transaction(total)
              case DND5eEntityType.playerCharacter =>
                val valId = id.asInstanceOf[PlayerCharacterId].value
                val rawQuery =
                  quote {
                    sql"""
                    UPDATE DND5eEncounter
                    SET info = JSON_REMOVE(
                            info,
                            SUBSTRING_INDEX(
                                    JSON_UNQUOTE(
                                            JSON_SEARCH(
                                                    info,
                                                    'one',
                                                    '#$valId',
                                                    NULL,
                                                    '$$.combatants[*].PlayerCharacterCombatant.playerCharacterId'
                                            )
                                    ),
                                    '.',
                                    2
                            )
                               )
                    WHERE JSON_SEARCH(info, 'one', '#$valId', NULL, '$$.combatants[*].PlayerCharacterCombatant.playerCharacterId') IS NOT NULL
                  """.as[Action[Int]]
                  }
                val total = for {
                  a <- ctx
                    .run(
                      qPlayerCharacters
                        .filter(_.id == lift(id.asInstanceOf[PlayerCharacterId].value)).update(_.deleted -> true)
                    )

                  b <- ctx.run(rawQuery)
                } yield a + b
                ctx
                  .transaction(total)
              case DND5eEntityType.nonPlayerCharacter =>
                val valId = id.asInstanceOf[NonPlayerCharacterId].value
                val rawQuery = quote {
                  sql"""
                    UPDATE DND5eEncounter
                    SET info = JSON_REMOVE(
                            info,
                            SUBSTRING_INDEX(
                                    JSON_UNQUOTE(
                                            JSON_SEARCH(
                                                    info,
                                                    'one',
                                                    '#$valId',
                                                    NULL,
                                                    '$$.combatants[*].NonPlayerCharacterCombatant.nonPlayerCharacterId'
                                            )
                                    ),
                                    '.',
                                    2
                            )
                               )
                    WHERE JSON_SEARCH(info, 'one', '#$valId', NULL, '$$.combatants[*].NonPlayerCharacterCombatant.nonPlayerCharacterId') IS NOT NULL
                  """.as[Action[Long]]
                }
                val total = for {
                  a <- ctx
                    .run(
                      qNonPlayerCharacters
                        .filter(_.id == lift(id.asInstanceOf[NonPlayerCharacterId].value)).update(_.deleted -> true)
                    )
                  b <- ctx.run(rawQuery)
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.monster =>
                val valId = id.asInstanceOf[MonsterId].value
                val rawQuery = quote {
                  sql"""
                    UPDATE DND5eEncounter
                    SET info = JSON_REMOVE(
                            info,
                            SUBSTRING_INDEX(
                                    JSON_UNQUOTE(
                                            JSON_SEARCH(
                                                    info,
                                                    'one',
                                                    '#$valId',
                                                    NULL,
                                                    '$$.combatants[*].MonsterCombatant.monsterHeader.id'
                                            )
                                    ),
                                    '.',
                                    2
                            )
                               )
                    WHERE JSON_SEARCH(info, 'one', '#$valId', NULL, '$$.combatants[*].MonsterCombatant.monsterHeader.id') IS NOT NULL
                  """.as[Action[Long]]
                }
                val total = for {
                  a <- ctx.run(qMonsters.filter(_.id == lift(valId)).update(_.deleted -> true))
                  b <- ctx.run(rawQuery)
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.scene =>
                val total = for {
                  a <- ctx
                    .run(
                      qScenes
                        .filter(_.id == lift(id.asInstanceOf[SceneId].value))
                        .update(_.deleted -> true)
                    )
                  b <- ctx.run(
                    qEncounters
                      .filter(_.sceneId.exists(_ == lift(id.asInstanceOf[SceneId].value)))
                      .update(_.deleted -> true)
                  )
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.encounter =>
                ctx
                  .run(qEncounters.filter(_.id == lift(id.asInstanceOf[EncounterId].value)).update(_.deleted -> true))
              case _ => ZIO.fail(DMScreenError(s"Don't know how to delete ${entityType.name}"))
            }
          } else {
            // Hard delete's are a bit easier, because we have ON DELETE CASCADE, so we don't have to delete anything else except for the main class
            entityType match {
//              case CampaignEntityType =>
//                ctx
//                  .run(qCampaigns.filter(_.id == lift(id.asInstanceOf[CampaignId].value)).delete)
              case DND5eEntityType.playerCharacter =>
                val valId = id.asInstanceOf[PlayerCharacterId].value
                val rawQuery = quote {
                  sql"""
                    UPDATE DND5eEncounter
                    SET info = JSON_REMOVE(
                            info,
                            SUBSTRING_INDEX(
                                    JSON_UNQUOTE(
                                            JSON_SEARCH(
                                                    info,
                                                    'one',
                                                    '#$valId',
                                                    NULL,
                                                    '$$.combatants[*].PlayerCharacterCombatant.playerCharacterId'
                                            )
                                    ),
                                    '.',
                                    2
                            )
                               )
                    WHERE JSON_SEARCH(info, 'one', '#$valId', NULL, '$$.combatants[*].PlayerCharacterCombatant.playerCharacterId') IS NOT NULL
                  """.as[Action[Long]]
                }
                val total = for {
                  a <- ctx.run(qPlayerCharacters.filter(_.id == lift(id.asInstanceOf[PlayerCharacterId].value)).delete)
                  b <- ctx.run(rawQuery)
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.nonPlayerCharacter =>
                val valId = id.asInstanceOf[NonPlayerCharacterId].value
                val rawQuery = quote {
                  sql"""
                    UPDATE DND5eEncounter
                    SET info = JSON_REMOVE(
                            info,
                            SUBSTRING_INDEX(
                                    JSON_UNQUOTE(
                                            JSON_SEARCH(
                                                    info,
                                                    'one',
                                                    '#$valId',
                                                    NULL,
                                                    '$$.combatants[*].NonPlayerCharacterCombatant.nonPlayerCharacterId'
                                            )
                                    ),
                                    '.',
                                    2
                            )
                               )
                    WHERE JSON_SEARCH(info, 'one', '#$valId', NULL, '$$.combatants[*].NonPlayerCharacterCombatant.nonPlayerCharacterId') IS NOT NULL
                  """.as[Action[Long]]
                }
                val total = for {
                  a <- ctx.run(
                    qNonPlayerCharacters.filter(_.id == lift(id.asInstanceOf[NonPlayerCharacterId].value)).delete
                  )
                  b <- ctx.run(rawQuery)
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.monster =>
                val valId = id.asInstanceOf[MonsterId].value
                val rawQuery = quote {
                  sql"""
                    UPDATE DND5eEncounter
                    SET info = JSON_REMOVE(
                            info,
                            SUBSTRING_INDEX(
                                    JSON_UNQUOTE(
                                            JSON_SEARCH(
                                                    info,
                                                    'one',
                                                    '#$valId',
                                                    NULL,
                                                    '$$.combatants[*].MonsterCombatant.monsterHeader.id'
                                            )
                                    ),
                                    '.',
                                    2
                            )
                               )
                    WHERE JSON_SEARCH(info, 'one', '#$valId', NULL, '$$.combatants[*].MonsterCombatant.monsterHeader.id') IS NOT NULL
                  """.as[Action[Long]]
                }
                val total = for {
                  a <- ctx.run(qMonsters.filter(_.id == lift(id.asInstanceOf[MonsterId].value)).delete)
                  b <- ctx.run(rawQuery)
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.scene =>
                ctx.run(qScenes.filter(_.id == lift(id.asInstanceOf[SceneId].value)).delete)
              case DND5eEntityType.encounter =>
                ctx
                  .run(qEncounters.filter(_.id == lift(id.asInstanceOf[EncounterId].value)).delete)
              case _ => ZIO.fail(DMScreenError(s"Don't know how to delete ${entityType.name}"))
            }
          }

        }.unit
          .provideLayer(dataSourceLayer)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def spells: IO[DMScreenError, Seq[Spell]] = ???

        override def upsert(
          header: PlayerCharacterHeader,
          info:   Json
        ): IO[DMScreenError, PlayerCharacterId] =
          (if (header.id != PlayerCharacterId.empty) {
             ctx
               .run(
                 qPlayerCharacters
                   .filter(v => !v.deleted && v.id == lift(header.id.value))
                   .updateValue(lift(PlayerCharacterRow.fromModel(header, info)))
               )
               .as(header.id)
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
                         source = header.source,
                         playerName = header.playerName,
                         info = info,
                         version = dmscreen.BuildInfo.version,
                         deleted = false
                       )
                     )
                   )
                   .returningGenerated(_.id)
               )
               .map(PlayerCharacterId.apply)
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
          header: MonsterHeader,
          info:   Json
        ): IO[DMScreenError, MonsterId] =
          (if (header.id != MonsterId.empty) {
             ctx
               .run(
                 qMonsters
                   .filter(v => !v.deleted && v.id == lift(header.id.value))
                   .updateValue(lift(MonsterRow.fromModel(header, info)))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qMonsters
                   .insertValue(
                     lift(
                       MonsterRow(
                         id = MonsterId.empty.value,
                         sourceId = header.sourceId.value,
                         name = header.name,
                         monsterType = header.monsterType.toString,
                         biome = header.biome.map(_.toString),
                         alignment = header.alignment.map(_.toString),
                         cr = header.cr.value,
                         xp = header.xp,
                         armorClass = header.armorClass,
                         hitPoints = header.maximumHitPoints,
                         size = header.size.toString,
                         initiativeBonus = header.initiativeBonus,
                         info = info,
                         version = dmscreen.BuildInfo.version,
                         deleted = false
                       )
                     )
                   )
                   .returningGenerated(_.id)
               )
               .map(MonsterId.apply)
           })
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def upsert(
          header: SpellHeader,
          info:   Json
        ): IO[DMScreenError, SpellId] = ???

        override def upsert(
          header: EncounterHeader,
          info:   Json
        ): IO[DMScreenError, EncounterId] =
          (if (header.id != EncounterId.empty) {
             ctx
               .run(
                 qEncounters
                   .filter(v => !v.deleted && v.id == lift(header.id.value))
                   .updateValue(lift(EncounterRow.fromModel(header, info)))
               )
               .as(header.id)
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
                         orderCol = header.orderCol,
                         info = info,
                         version = dmscreen.BuildInfo.version,
                         deleted = false
                       )
                     )
                   )
                   .returningGenerated(_.id)
               )
               .map(EncounterId.apply)

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

        override def encounter(
          campaignId:  CampaignId,
          encounterId: EncounterId
        ): DMScreenTask[Option[Encounter]] = {
          ctx
            .run(
              qEncounters
                .filter(v => !v.deleted && v.campaignId == lift(campaignId.value) && v.id == lift(encounterId.value))
                .map(a => (a.id, a.campaignId, a.sceneId, a.name, a.status, a.orderCol, a.info))
            )
            .map(
              _.headOption.map(t =>
                Encounter(
                  header = EncounterHeader(
                    id = EncounterId(t._1),
                    campaignId = CampaignId(t._2),
                    sceneId = t._3.map(i => SceneId(i)),
                    name = t._4,
                    status = EncounterStatus.valueOf(t._5),
                    orderCol = t._6
                  ),
                  jsonInfo = t._7
                )
              )
            )
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        }

        override def playerCharacter(playerCharacterId: PlayerCharacterId): DMScreenTask[Option[PlayerCharacter]] =
          ctx
            .run(qPlayerCharacters.filter(v => !v.deleted && v.id == lift(playerCharacterId.value)))
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
