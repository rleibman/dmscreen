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

package dmscreen.dnd5e.srd

import dmscreen.DMScreenError
import dmscreen.dnd5e.{*, given}
import dmscreen.util.*
import zio.json.*
import zio.json.ast.*
import zio.stream.ZStream
import zio.{Scope, ULayer, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

import java.io.File

object SRDImporter {

  val live: ULayer[SRDImporter] = ZLayer.succeed(SRDImporter())

}

class SRDImporter extends DND5eImporter[File, File, File, File] {

  override def importCampaign(campaignLink: File): ZIO[Any, DMScreenError, DND5eCampaign] = ???

  override def importPlayerCharacter(playerCharacterLink: File): ZIO[Any, DMScreenError, PlayerCharacter] = ???

  override def importEncounter(encounterLink: File): ZIO[Any, DMScreenError, Encounter] = ???

  override def importMonsters(monsterLink: File): ZStream[Any, DMScreenError, Monster] = {
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
          name <- json.getValue[String]("name")
          size <- json
            .getValue[String]("size").flatMap(s =>
              CreatureSize.values.find(a => s.equalsIgnoreCase(a.toString)).toRight(s"Invalid size: $s")
            )
          monsterType <- json
            .getValue[String]("type").flatMap(s =>
              MonsterType.values.find(a => s.equalsIgnoreCase(a.toString)).toRight(s"Invalid monsterType: $s")
            )
          alignment <- json
            .getEitherOption[String]("alignment").map(
              _.flatMap(s => Alignment.values.find(a => s.equalsIgnoreCase(a.name) || s.equalsIgnoreCase(a.toString)))
            )
          armorClass <- json
            .getValue[Json.Arr]("armor_class").flatMap(arr =>
              arr.elements.map(o => o.asObject.toRight("Not an object").flatMap(_.getValue[Int]("value"))).head
            )
          maximumHitPoints <- json.getValue[Int]("hit_points")
          hitDice          <- json.getEitherOption[String]("hit_points_roll")
          speeds <- json
            .getEitherOption[Json.Obj]("speed").map {
              _.map { spObj =>

                val walk = spObj
                  .getOption[String]("walk").flatMap(str =>
                    str.replaceAll("ft.", "").trim.toIntOption.map(Speed(SpeedType.walk, _))
                  )
                val fly = spObj
                  .getOption[String]("fly").flatMap(str =>
                    str.replaceAll("ft.", "").trim.toIntOption.map(Speed(SpeedType.fly, _))
                  )
                val burrow = spObj
                  .getOption[String]("burrow").flatMap(str =>
                    str.replaceAll("ft.", "").trim.toIntOption.map(Speed(SpeedType.burrow, _))
                  )
                val climb = spObj
                  .getOption[String]("climb").flatMap(str =>
                    str.replaceAll("ft.", "").trim.toIntOption.map(Speed(SpeedType.climb, _))
                  )
                val swim = spObj
                  .getOption[String]("swim").flatMap(str =>
                    str.replaceAll("ft.", "").trim.toIntOption.map(Speed(SpeedType.swim, _))
                  )

                walk.toSeq ++ fly.toSeq ++ burrow.toSeq ++ climb.toSeq ++ swim.toSeq

              }.toSeq.flatten
            }
          abilities <- {
            for {
              strength     <- json.getValue[Int]("strength")
              dexterity    <- json.getValue[Int]("dexterity")
              constitution <- json.getValue[Int]("constitution")
              intelligence <- json.getValue[Int]("intelligence")
              wisdom       <- json.getValue[Int]("wisdom")
              charisma     <- json.getValue[Int]("charisma")
            } yield Abilities(
              strength = Ability(AbilityType.strength, strength, None),
              dexterity = Ability(AbilityType.dexterity, dexterity, None),
              constitution = Ability(AbilityType.constitution, constitution, None),
              intelligence = Ability(AbilityType.intelligence, intelligence, None),
              wisdom = Ability(AbilityType.wisdom, wisdom, None),
              charisma = Ability(AbilityType.charisma, charisma, None)
            )
          }
          cr        <- json.getValue[Double]("challenge_rating")
          xp        <- json.getValue[Long]("xp")
          languages <- json.getValue[String]("languages").map(_.split(",").map(Language(_)).toSeq)
          //          "damage_vulnerabilities": [],
          //          "damage_resistances": [],
          //          "damage_immunities": [
          // proficiencies
          // hit dice
          // actions
          // reactions
          senses <- json.getEitherOption[Json.Obj]("senses").map {
            _.map { s =>
              val sight = s
                .getOption[String]("sight").map(str =>
                  SenseRange(Sense.sight, str.replaceAll("ft.*", "").trim.toInt)
                ).orElse(Option(SenseRange(Sense.sight, 10560)))
              val blindsight = s
                .getOption[String]("blindsight").map(str =>
                  SenseRange(Sense.blindsight, str.replaceAll("ft.*", "").trim.toInt)
                )
              val darkvision = s
                .getOption[String]("darkvision").map(str =>
                  SenseRange(Sense.darkvision, str.replaceAll("ft.*", "").trim.toInt)
                )
              val tremorsense = s
                .getOption[String]("tremorsense").map(str =>
                  SenseRange(Sense.tremorsense, str.replaceAll("ft.*", "").trim.toInt)
                )
              val truesight = s
                .getOption[String]("truesight").map(str =>
                  SenseRange(Sense.truesight, str.replaceAll("ft.*", "").trim.toInt)
                )
              val scent =
                s.getOption[String]("scent").map(str => SenseRange(Sense.scent, str.replaceAll("ft.", "").trim.toInt))
            sight.toSeq ++ blindsight.toSeq ++ darkvision.toSeq ++ tremorsense.toSeq ++ truesight.toSeq ++ scent.toSeq
            }.toSeq.flatten
          }

        } yield {
          Monster(
            MonsterHeader(
              id = MonsterId.empty,
              name = name, // String,
              monsterType = monsterType, // MonsterType,
              biome = None, // Option[Biome],
              alignment = alignment, // Option[Alignment],
              cr = ChallengeRating.fromDouble(cr).getOrElse(ChallengeRating.`0`),
              xp = xp, // Int,
              armorClass = armorClass, // Int,
              maximumHitPoints = maximumHitPoints, // Int,
              size = size // CreatureSize,
            ),
            jsonInfo = MonsterInfo(
              hitDice = hitDice,
              speeds = speeds, // :    Seq[Speed],
              abilities = abilities, // : Abilities,
              languages = languages, // : Seq[Language],
              actions = Seq.empty, // :   Seq[String],
              reactions = Seq.empty, // : String,
              senses = senses // :    Seq[SenseRange]
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
        _.importMonsters(file).take(5).runCollect.map(_.foreach(data => println(data)))
      ).provideLayer(SRDImporter.live)

  }

}
