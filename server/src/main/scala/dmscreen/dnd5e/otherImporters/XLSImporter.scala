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

package dmscreen.dnd5e.otherImporters

import dmscreen.*
import dmscreen.dnd5e.srd.SRDImporter
import dmscreen.dnd5e.{*, given}
import dmscreen.util.*
import zio.json.*
import zio.json.ast.*
import zio.stream.ZStream
import zio.{Scope, ULayer, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

import java.io.File

object XLSImporter {

  val live: ULayer[XLSImporter] = ZLayer.succeed(XLSImporter())

}

class XLSImporter extends DND5eImporter[File, File, File, File] {

  override def importCampaign(campaignLink: File): ZIO[Any, DMScreenError, Campaign] = ???

  override def importPlayerCharacter(playerCharacterLink: File): ZIO[Any, DMScreenError, PlayerCharacter] = ???

  override def importEncounter(encounterLink: File): ZIO[Any, DMScreenError, Encounter] = ???

  override def importMonsters(monsterLink: File): ZStream[Any, DMScreenError, Monster] = {
    given JsonDecoder[String | Int] = JsonDecoder[String].orElse(JsonDecoder[Int].map(_.toString))

    extension (json: Json.Obj) {
      private def getValue[T: JsonDecoder](name: String): Either[String, T] =
        json.fields.find(_._1 == name) match {
          case Some(value) => value._2.as[T]
          case None        => Left(s"Missing field '$name'")
        }
    }
    given JsonDecoder[Monster] =
      JsonDecoder[Json.Obj].mapOrFail { json =>
        for {
          name <- json.getValue[String]("Creature")
          size <- json
            .getValue[String]("Size").map(t =>
              CreatureSize.values.find(_.toString.equalsIgnoreCase(t)).getOrElse(CreatureSize.unknown)
            )
          monsterType <- json
            .getValue[String]("Type").map(t =>
              MonsterType.values.find(_.toString.equalsIgnoreCase(t)).getOrElse(MonsterType.Unknown)
            )
          ac <- json.getValue[String | Int]("AC").map {
            case t: String if t.isEmpty => 0
            case t: String              => t.toInt
            case t: Int                 => t
          }
          maximumHitPoints <- json.getValue[String | Int]("HP").map {
            case t: String if t.isEmpty => 0
            case t: String              => t.toInt
            case t: Int                 => t
          }
          walking <- json.getValue[String | Int]("Walking")
          burrow  <- json.getValue[String | Int]("Burrow")
          flying  <- json.getValue[String | Int]("Flying")
          swim    <- json.getValue[String | Int]("Swim")
          cr <- json.getValue[String | Int]("CR").map {
            case t: String => ChallengeRating.values.find(_.toString.equalsIgnoreCase(t))
            case t: Int    => ChallengeRating.fromDouble(t.toDouble)
          }
          source <- json.getValue[String]("Source")
          biome  <- json.getValue[String]("Biome").map(t => Biome.values.find(_.toString.equalsIgnoreCase(t)))
        } yield {
          Monster(
            MonsterHeader(
              id = MonsterId.empty,
              sourceId = SourceId.other(source),
              name = name, // String,
              monsterType = monsterType, // MonsterType,
              biome = biome, // Option[Biome],
              alignment = None, // Option[Alignment],
              cr = cr.getOrElse(ChallengeRating._0), // ChallengeRating,
              xp = 0, // Int,
              armorClass = ac, // Int,
              maximumHitPoints = maximumHitPoints, // Int,
              size = size, // CreatureSize,
              initiativeBonus = 0
            ),
            jsonInfo = MonsterInfo(
              abilities = Abilities()
//              hitDice = hitDice,
//              speeds = speeds,
//              abilities = abilities,
//              languages = languages,
//              actions = actions,
//              reactions = reactions,
//              senses = senses,
//              damageVulnerabilities = damageVulnerabilities,
//              damageResistances = damageResistances,
//              damageImmunities = damageImmunities,
//              conditionImmunities = conditionImmunities,
//              proficiencyBonus = proficiencyBonus
            ).toJsonAST.toOption.get
          )
        }
      }

    jsonStream[Monster](monsterLink)
      .mapError(e => DMScreenError(s"Error importing monsters from $monsterLink: $e", Some(e)))
  }

}

object MonsterImport extends ZIOAppDefault {

  def run: ZIO[ZIOAppArgs & Scope, Throwable, Unit] = {
    val file = File("/home/rleibman/projects/dmscreen/common/shared/src/main/resources/5e-SRD-Monsters.json")

    ZIO
      .serviceWithZIO[SRDImporter](
        _.importMonsters(file)
          .tap(m => zio.Console.printLine(m.header.name)).take(500).runCollect.map(_.foreach(data => println(data)))
      ).provideLayer(SRDImporter.live)

  }

}
