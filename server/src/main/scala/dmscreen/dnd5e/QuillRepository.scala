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

import dmscreen.DMScreenSchema.{*, given}
import dmscreen.{*, given}
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

object DND5eSchema {

  given MappedEncoding[Long, MonsterId] = MappedEncoding[Long, MonsterId](MonsterId.apply)
  given MappedEncoding[MonsterId, Long] = MappedEncoding[MonsterId, Long](_.value)
  given MappedEncoding[Long, SceneId] = MappedEncoding[Long, SceneId](SceneId.apply)
  given MappedEncoding[SceneId, Long] = MappedEncoding[SceneId, Long](_.value)
  given MappedEncoding[Long, PlayerCharacterId] = MappedEncoding[Long, PlayerCharacterId](PlayerCharacterId.apply)
  given MappedEncoding[PlayerCharacterId, Long] = MappedEncoding[PlayerCharacterId, Long](_.value)
  given MappedEncoding[Long, NonPlayerCharacterId] =
    MappedEncoding[Long, NonPlayerCharacterId](NonPlayerCharacterId.apply)
  given MappedEncoding[NonPlayerCharacterId, Long] = MappedEncoding[NonPlayerCharacterId, Long](_.value)
  given MappedEncoding[Long, EncounterId] = MappedEncoding[Long, EncounterId](EncounterId.apply)
  given MappedEncoding[EncounterId, Long] = MappedEncoding[EncounterId, Long](_.value)
  given MappedEncoding[String, SourceId] = MappedEncoding[String, SourceId](SourceId.apply)
  given MappedEncoding[SourceId, String] = MappedEncoding[SourceId, String](_.value)
  given MappedEncoding[ChallengeRating, String] = MappedEncoding[ChallengeRating, String](_.toString)
  given MappedEncoding[String, ChallengeRating] = MappedEncoding[String, ChallengeRating](ChallengeRating.fromString)
  given MappedEncoding[Alignment, String] = MappedEncoding[Alignment, String](_.toString)
  given MappedEncoding[String, Alignment] = MappedEncoding[String, Alignment](Alignment.valueOf)
  given MappedEncoding[MonsterType, String] = MappedEncoding[MonsterType, String](_.toString)
  given MappedEncoding[String, MonsterType] = MappedEncoding[String, MonsterType](MonsterType.valueOf)
  given MappedEncoding[CreatureSize, String] = MappedEncoding[CreatureSize, String](_.toString)
  given MappedEncoding[String, CreatureSize] = MappedEncoding[String, CreatureSize](CreatureSize.valueOf)
  given MappedEncoding[Biome, String] = MappedEncoding[Biome, String](_.toString)
  given MappedEncoding[String, Biome] = MappedEncoding[String, Biome](Biome.valueOf)
  given MappedEncoding[EncounterStatus, String] = MappedEncoding[EncounterStatus, String](_.toString)
  given MappedEncoding[String, EncounterStatus] = MappedEncoding[String, EncounterStatus](EncounterStatus.valueOf)

  inline def qScenes =
    quote {
      querySchema[DBObject[Scene]](
        "DND5eScene",
        _.value.header.id         -> "id",
        _.value.header.campaignId -> "campaignId",
        _.value.header.name       -> "name",
        _.value.header.orderCol   -> "orderCol",
        _.value.header.isActive   -> "isActive",
        _.value.jsonInfo          -> "info",
        _.value.version           -> "version",
        _.deleted                 -> "deleted"
      )
    }

  inline def qPlayerCharacters =
    quote {
      querySchema[DBObject[PlayerCharacter]](
        "DND5ePlayerCharacter",
        _.value.header.id         -> "id",
        _.value.header.campaignId -> "campaignId",
        _.value.header.name       -> "name",
        _.value.header.playerName -> "playerName",
        _.value.header.source     -> "source",
        _.value.jsonInfo          -> "info",
        _.value.version           -> "version",
        _.deleted                 -> "deleted"
      )
    }

  inline def qNonPlayerCharacters =
    quote {
      querySchema[DBObject[NonPlayerCharacter]](
        "DND5eNonPlayerCharacter",
        _.value.header.id         -> "id",
        _.value.header.campaignId -> "campaignId",
        _.value.header.name       -> "name",
        _.value.jsonInfo          -> "info",
        _.value.version           -> "version",
        _.deleted                 -> "deleted"
      )
    }

  inline def qMonsters =
    quote {
      querySchema[DBObject[Monster]](
        "DND5eMonster",
        _.value.header.id               -> "id",
        _.value.header.sourceId         -> "sourceId",
        _.value.header.name             -> "name",
        _.value.header.monsterType      -> "monsterType",
        _.value.header.biome            -> "biome",
        _.value.header.alignment        -> "alignment",
        _.value.header.cr               -> "cr",
        _.value.header.xp               -> "xp",
        _.value.header.armorClass       -> "armorClass",
        _.value.header.maximumHitPoints -> "hitPoints", // Note different name
        _.value.header.size             -> "size",
        _.value.header.initiativeBonus  -> "initiativeBonus",
        _.value.jsonInfo                -> "info",
        _.value.version                 -> "version",
        _.deleted                       -> "deleted"
      )
    }

  inline def qEncounters =
    quote {
      querySchema[DBObject[Encounter]](
        "DND5eEncounter",
        _.value.header.id         -> "id",
        _.value.header.campaignId -> "campaignId",
        _.value.header.sceneId    -> "sceneId",
        _.value.header.name       -> "name",
        _.value.header.status     -> "status",
        _.value.header.orderCol   -> "orderCol",
        _.value.jsonInfo          -> "info",
        _.value.version           -> "version",
        _.deleted                 -> "deleted"
      )
    }

}

object QuillRepository {

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

        import DND5eSchema.{*, given}

        override def monster(monsterId: MonsterId): DMScreenTask[Option[Monster]] =
          ctx
            .run(qMonsters.filter(v => !v.deleted && v.value.header.id == lift(monsterId)))
            .map(_.headOption.map(_.value))
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
          val q0: Quoted[EntityQuery[DBObject[PlayerCharacter]]] =
            qPlayerCharacters.filter(v => !v.deleted && v.value.header.campaignId == lift(campaignId))
          val q1: Quoted[EntityQuery[DBObject[PlayerCharacter]]] =
            search.dndBeyondId.fold(q0)(dndBeyondId =>
              q0.filter(_.value.header.source == lift(DNDBeyondImportSource(dndBeyondId).asInstanceOf[ImportSource]))
            )

          ctx
            .run(q1)
            .map(_.map(_.value))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        }

        override def scenes(campaignId: CampaignId): IO[DMScreenError, Seq[Scene]] =
          ctx
            .run(
              qScenes
                .filter(v => !v.deleted && v.value.header.campaignId == lift(campaignId)).sortBy(
                  _.value.header.orderCol
                )
            )
            .map(_.map(_.value))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def nonPlayerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[NonPlayerCharacter]] =
          ctx
            .run(qNonPlayerCharacters.filter(v => !v.deleted && v.value.header.campaignId == lift(campaignId)))
            .map(_.map(_.value))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def encounters(campaignId: CampaignId): IO[DMScreenError, Seq[Encounter]] =
          ctx
            .run(
              qEncounters
                .filter(v => !v.deleted && v.value.header.campaignId == lift(campaignId))
                .map(_.value)
            )
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def bestiary(search: MonsterSearch): IO[DMScreenError, MonsterSearchResults] = {
          val q0: Quoted[EntityQuery[DBObject[Monster]]] = qMonsters.filter(v => !v.deleted)
          val q1: Quoted[EntityQuery[DBObject[Monster]]] =
            search.name.fold(q0)(n => q0.filter(_.value.header.name like lift(s"%$n%")))
          val q2: Quoted[EntityQuery[DBObject[Monster]]] =
            search.challengeRating.fold(q1)(n => q1.filter(_.value.header.cr == lift(n)))
          val q3: Quoted[EntityQuery[DBObject[Monster]]] =
            search.monsterType.fold(q2)(n => q2.filter(_.value.header.monsterType == lift(n)))
          val q4: Quoted[EntityQuery[DBObject[Monster]]] =
            search.biome.fold(q3)(n => q3.filter(_.value.header.biome.contains(lift(n))))
          val q5: Quoted[EntityQuery[DBObject[Monster]]] =
            search.alignment.fold(q4)(n => q4.filter(_.value.header.alignment.contains(lift(n))))
          val q6: Quoted[EntityQuery[DBObject[Monster]]] =
            search.size.fold(q5)(n => q5.filter(_.value.header.size == lift(n)))

          // If sort is random, then it's a bit more complex, for now, we do it the simple (but non-performing) way, read this
          // https://jan.kneschke.de/projects/mysql/order-by-rand/
          // ^^ use the stored procedure

          val limited = quote(
            q6
              .drop(lift(search.page * search.pageSize))
              .take(lift(search.pageSize))
          )

          val sorted: Quoted[Query[DBObject[Monster]]] = (search.orderCol, search.orderDir) match {
            case (MonsterSearchOrder.challengeRating, OrderDirection.asc) =>
              quote(limited.sortBy(r => r.value.header.cr)(Ord.asc))
            case (MonsterSearchOrder.challengeRating, OrderDirection.desc) =>
              quote(limited.sortBy(r => r.value.header.cr)(Ord.desc))
            case (MonsterSearchOrder.size, OrderDirection.asc) =>
              quote(limited.sortBy(r => r.value.header.size)(Ord.asc))
            case (MonsterSearchOrder.size, OrderDirection.desc) =>
              quote(limited.sortBy(r => r.value.header.size)(Ord.desc))
            case (MonsterSearchOrder.alignment, OrderDirection.asc) =>
              quote(limited.sortBy(r => r.value.header.alignment)(Ord.asc))
            case (MonsterSearchOrder.alignment, OrderDirection.desc) =>
              quote(limited.sortBy(r => r.value.header.alignment)(Ord.desc))
            case (MonsterSearchOrder.biome, OrderDirection.asc) =>
              quote(limited.sortBy(r => r.value.header.biome)(Ord.asc))
            case (MonsterSearchOrder.biome, OrderDirection.desc) =>
              quote(limited.sortBy(r => r.value.header.biome)(Ord.desc))
            case (MonsterSearchOrder.monsterType, OrderDirection.asc) =>
              quote(limited.sortBy(r => r.value.header.monsterType)(Ord.asc))
            case (MonsterSearchOrder.monsterType, OrderDirection.desc) =>
              quote(limited.sortBy(r => r.value.header.monsterType)(Ord.desc))
            case (MonsterSearchOrder.random, OrderDirection.asc) =>
              quote(limited.sortBy(_ => infix"RAND()"))
            case (_, OrderDirection.asc) =>
              quote(limited.sortBy(r => r.value.header.name)(Ord.asc))
            case (_, OrderDirection.desc) =>
              quote(limited.sortBy(r => r.value.header.name)(Ord.desc))
          }

          (for {
            monsters <- ctx.run(sorted)
            total    <- ctx.run(q6.size)
          } yield MonsterSearchResults(
            results = monsters.map(_.value.header),
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
                        .filter(_.value.header.id == lift(id.asInstanceOf[PlayerCharacterId])).update(_.deleted -> true)
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
                        .filter(_.value.header.id == lift(id.asInstanceOf[NonPlayerCharacterId])).update(
                          _.deleted -> true
                        )
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
                  a <- ctx.run(
                    qMonsters.filter(_.value.header.id == lift(id.asInstanceOf[MonsterId])).update(_.deleted -> true)
                  )
                  b <- ctx.run(rawQuery)
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.scene =>
                val total = for {
                  a <- ctx
                    .run(
                      qScenes
                        .filter(_.value.header.id == lift(id.asInstanceOf[SceneId]))
                        .update(_.deleted -> true)
                    )
                  b <- ctx.run(
                    qEncounters
                      .filter(_.value.header.sceneId.exists(_ == lift(id.asInstanceOf[SceneId])))
                      .update(_.deleted -> true)
                  )
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.encounter =>
                ctx
                  .run(
                    qEncounters
                      .filter(_.value.header.id == lift(id.asInstanceOf[EncounterId])).update(_.deleted -> true)
                  )
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
                  a <- ctx.run(
                    qPlayerCharacters.filter(_.value.header.id == lift(id.asInstanceOf[PlayerCharacterId])).delete
                  )
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
                    qNonPlayerCharacters.filter(_.value.header.id == lift(id.asInstanceOf[NonPlayerCharacterId])).delete
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
                  a <- ctx.run(qMonsters.filter(_.value.header.id == lift(id.asInstanceOf[MonsterId])).delete)
                  b <- ctx.run(rawQuery)
                } yield a + b
                ctx.transaction(total)
              case DND5eEntityType.scene =>
                ctx.run(qScenes.filter(_.value.header.id == lift(id.asInstanceOf[SceneId])).delete)
              case DND5eEntityType.encounter =>
                ctx
                  .run(qEncounters.filter(_.value.header.id == lift(id.asInstanceOf[EncounterId])).delete)
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
                   .filter(v => !v.deleted && v.value.header.id == lift(header.id))
                   .updateValue(lift(DBObject(PlayerCharacter(header, info))))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qPlayerCharacters
                   .insertValue(lift(DBObject(PlayerCharacter(header, info))))
                   .returningGenerated(_.value.header.id)
               )
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
                   .filter(v => !v.deleted && v.value.header.id == lift(header.id))
                   .updateValue(lift(DBObject(NonPlayerCharacter(header, info))))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qNonPlayerCharacters
                   .insertValue(lift(DBObject(NonPlayerCharacter(header, info))))
                   .returningGenerated(_.value.header.id)
               )
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
                   .filter(v => !v.deleted && v.value.header.id == lift(header.id))
                   .updateValue(lift(DBObject(Monster(header, info))))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qMonsters
                   .insertValue(lift(DBObject(Monster(header, info))))
                   .returningGenerated(_.value.header.id)
               )
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
                   .filter(v => !v.deleted && v.value.header.id == lift(header.id))
                   .updateValue(lift(DBObject(Encounter(header, info))))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qEncounters
                   .insertValue(lift(DBObject(Encounter(header, info))))
                   .returningGenerated(_.value.header.id)
               )

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
                   .filter(v => !v.deleted && v.value.header.id == lift(header.id))
                   .updateValue(lift(DBObject(Scene(header, info))))
               )
               .as(header.id)
           } else {
             ctx
               .run(
                 qScenes
                   .insertValue(lift(DBObject(Scene(header, info))))
                   .returningGenerated(_.value.header.id)
               )
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
                .filter(v =>
                  !v.deleted && v.value.header.campaignId == lift(campaignId) && v.value.header.id == lift(encounterId)
                )
                .map(_.value)
            )
            .map(_.headOption)
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        }

        override def playerCharacter(playerCharacterId: PlayerCharacterId): DMScreenTask[Option[PlayerCharacter]] =
          ctx
            .run(qPlayerCharacters.filter(v => !v.deleted && v.value.header.id == lift(playerCharacterId)))
            .map(_.headOption.map(_.value))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        override def nonPlayerCharacter(nonPlayerCharacterId: NonPlayerCharacterId)
          : DMScreenTask[Option[NonPlayerCharacter]] =
          ctx
            .run(qNonPlayerCharacters.filter(v => !v.deleted && v.value.header.id == lift(nonPlayerCharacterId)))
            .map(_.headOption.map(_.value))
            .provideLayer(dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

      }
    }

}
