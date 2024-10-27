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

package dmscreen.sta.foundry

import dmscreen.sta.*
import dmscreen.{CampaignId, DMScreenError, sta}
import zio.*
import zio.json.*
import zio.json.ast.*
import zio.nio.file.Files
import cats.implicits.*

import java.net.URI

object FoundryImporter {

  def live: ULayer[FoundryImporter] = ZLayer.succeed(new FoundryImporter)

}

class FoundryImporter extends STAImporter[URI, URI] {

  extension (obj: Json.Obj) {

    private def getObjOption(key: String): Either[String, Option[Json.Obj]] =
      Right(
        obj
          .get(key).flatMap(j =>
            j match {
              case Json.Null => None
              case _         => j.asObject
            }
          )
      )
    private def getArrOption(key: String): Either[String, Option[Chunk[Json]]] =
      Right(
        obj
          .get(key).flatMap(j =>
            j match {
              case Json.Null => None
              case _         => j.asArray
            }
          )
      )
    private def getStrOption(key: String): Either[String, Option[String]] =
      Right(
        obj
          .get(key).flatMap(j =>
            j match {
              case Json.Null => None
              case _         => j.asString
            }
          )
      )
    private def getBooleanOption(key: String): Either[String, Option[Boolean]] =
      Right(
        obj
          .get(key).flatMap(j =>
            j match {
              case Json.Null => None
              case _         => j.asBoolean
            }
          )
      )
    private def getIntOption(key: String): Either[String, Option[Int]] =
      Right(
        obj
          .get(key).flatMap(j =>
            j match {
              case Json.Null => None
              case _         => j.asNumber.map(_.value.intValue)
            }
          )
      )

    private def getObj(key: String): Either[String, Json.Obj] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asObject.toRight(s"Key $key is not an object")
      } yield ret

    private def getStr(key: String): Either[String, String] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asString.toRight(s"Key $key is not a String")
      } yield ret

    private def getInt(key: String): Either[String, Int] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asNumber.toRight(s"Key $key is not an number")
      } yield ret.value.intValue

    private def getBoolean(key: String): Either[String, Boolean] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asBoolean.toRight(s"Key $key is not an object")
      } yield ret

    private def getArr(key: String): Either[String, Chunk[Json]] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asArray.toRight(s"Key $key is not an object")
      } yield ret

  }

  given JsonDecoder[AttributeRating] =
    JsonDecoder[Json.Obj].mapOrFail(json =>
      for {
        strValue <- json.getStr("value")
        intValue <- strValue.toIntOption.toRight(s"value $strValue is not an int")
        rating <- intValue match {
          case v: Int if v >= 7 && v <= 12 => Right(v.asInstanceOf[AttributeRating]) // Ugly cast
          case v: Int                      => Left("AttributeRating must be between 7 and 12, not $v")
        }
      } yield rating.asInstanceOf[AttributeRating]
    )
  given JsonDecoder[DepartmentRating] =
    JsonDecoder[Json.Obj].mapOrFail(json =>
      for {
        strValue <- json.getStr("value")
        intValue <- strValue.toIntOption.toRight(s"value $strValue is not an int")
        rating <- intValue match {
          case v: Int if v >= 0 && v <= 5 => Right(v.asInstanceOf[DepartmentRating]) // Ugly cast
          case v: Int                     => Left("DepartmentRating must be between 0 and 5, not $v")
        }
      } yield rating.asInstanceOf[DepartmentRating]
    )

  def json2Character(str: String): Either[DMScreenError, Character] = {
    (for {
      json   <- str.fromJson[Json.Obj]
      name   <- json.getStrOption("name")
      _      <- json.getStr("type").flatMap(t => if (t == "character") Right(t) else Left("Not a character"))
      system <- json.getObj("system")
      notes  <- system.getStrOption("notes").map(_.getOrElse(""))
      attributes <- for {
        attributesObj <- system.getObj("attributes")
        controlObj    <- attributesObj.getObj("control")
        control       <- controlObj.as[AttributeRating]
        daringObj     <- attributesObj.getObj("daring")
        daring        <- daringObj.as[AttributeRating]
        fitnessObj    <- attributesObj.getObj("fitness")
        fitness       <- fitnessObj.as[AttributeRating]
        insightObj    <- attributesObj.getObj("insight")
        insight       <- insightObj.as[AttributeRating]
        presenceObj   <- attributesObj.getObj("presence")
        presence      <- presenceObj.as[AttributeRating]
        reasonObj     <- attributesObj.getObj("reason")
        reason        <- reasonObj.as[AttributeRating]
      } yield Attributes(
        control = control,
        daring = daring,
        fitness = fitness,
        insight = insight,
        presence = presence,
        reason = reason
      )
      determination <- for {
        determinationObj <- system.getObj("determination")
        value            <- determinationObj.getInt("value")
        max              <- determinationObj.getInt("max")
      } yield Determination(value, max)
      departments <- for {
        departmentsObj <- system.getObj("disciplines")
        commandObj     <- departmentsObj.getObj("command")
        command        <- commandObj.as[DepartmentRating]
        connObj        <- departmentsObj.getObj("conn")
        conn           <- connObj.as[DepartmentRating]
        engineeringObj <- departmentsObj.getObj("engineering")
        engineering    <- engineeringObj.as[DepartmentRating]
        securityObj    <- departmentsObj.getObj("security")
        security       <- securityObj.as[DepartmentRating]
        scienceObj     <- departmentsObj.getObj("science")
        science        <- scienceObj.as[DepartmentRating]
        medicineObj    <- departmentsObj.getObj("medicine")
        medicine       <- medicineObj.as[DepartmentRating]
      } yield Departments(
        command = command,
        conn = conn,
        engineering = engineering,
        security = security,
        science = science,
        medicine = medicine
      )
      _ <- system.getStrOption("milestones")
      rank <- system
        .getStrOption("rank").flatMap(opt =>
          Right(opt.flatMap(str => Rank.values.find(_.toString.equalsIgnoreCase(str))))
        )
      reputation <- system.getInt("reputation")
      stress <- for {
        stressObj <- system.getObj("stress")
        value     <- stressObj.getInt("value")
        max       <- stressObj.getInt("max")
      } yield Stress(value, max)
      traits      <- system.getStrOption("traits").map(opt => opt.map(Trait.apply))
      environment <- system.getStrOption("environment")
      lineage <- system
        .getStrOption("species").map(
          _.map(str =>
            Lineage(LineageType.values.find(_.name.equalsIgnoreCase(str)).getOrElse(LineageType.other(str)))
          ).toSeq
        )
      upbringing <- system.getStrOption("upbringing")
      items      <- system.getArr("items").flatMap(_.toList.map(s => s.asObject.toRight("Not an object")).sequence)
      values <- items
        .filter(a => a.getStr("type") == Right("value")).map(obj => obj.getStr("name").map(EthicalValue.apply)).sequence
      focuses <- items
        .filter(a => a.getStr("type") == Right("focus")).map(obj => obj.getStr("name").map(Focus.apply)).sequence
      inventoryItems <- items
        .filter(a => a.getStr("type") == Right("item")).map(itemObj =>
          for {
            name        <- itemObj.getStr("name")
            systemObj   <- itemObj.getObj("system")
            description <- systemObj.getStr("description")
            quantity    <- systemObj.getInt("quantity")
          } yield InventoryItem(name = name, quantity = quantity, description = description)
        ).sequence
      weapons <- items
        .filter(a => a.getStr("type") == Right("characterweapon")).map(weaponObj =>
          for {
            name        <- weaponObj.getStr("name")
            systemObj   <- weaponObj.getObj("system")
            description <- systemObj.getStr("description")
            range <- systemObj
              .getStr("range").map(str =>
                WeaponRange.values.find(_.toString.equalsIgnoreCase(str)).getOrElse(WeaponRange.Melee)
              )
            damage       <- systemObj.getInt("damage")
            hands        <- systemObj.getInt("hands")
            qualitiesObj <- systemObj.getObj("qualities")
            qualities <- for {
              area         <- qualitiesObj.getBoolean("area")
              intense      <- qualitiesObj.getBoolean("intense")
              knockdown    <- qualitiesObj.getBoolean("knockdown")
              accurate     <- qualitiesObj.getBoolean("accurate")
              charge       <- qualitiesObj.getBoolean("charge")
              cumbersome   <- qualitiesObj.getBoolean("cumbersome")
              deadly       <- qualitiesObj.getBoolean("deadly")
              debilitating <- qualitiesObj.getBoolean("debilitating")
              grenade      <- qualitiesObj.getBoolean("grenade")
              inaccurate   <- qualitiesObj.getBoolean("inaccurate")
              nonlethal    <- qualitiesObj.getBoolean("nonlethal")
              hidden       <- qualitiesObj.getInt("hiddenx")
              piercing     <- qualitiesObj.getInt("piercingx")
              vicious      <- qualitiesObj.getInt("viciousx")
            } yield WeaponQualities(
              area,
              intense,
              knockdown,
              accurate,
              charge,
              cumbersome,
              deadly,
              debilitating,
              grenade,
              inaccurate,
              nonlethal,
              hidden,
              piercing,
              vicious
            )
          } yield CharacterWeapon(name, description, damage, range, hands, qualities)
        ).sequence
      talents <- items
        .filter(a => a.getStr("type") == Right("talent")).map(talentObj =>
          for {
            name        <- talentObj.getStr("name")
            systemObj   <- talentObj.getObj("system")
            description <- systemObj.getStr("description")
          } yield Talent(name, description)
        ).sequence
    } yield {
      import dmscreen.sta.given
      Character(
        CharacterHeader(
          id = CharacterId.empty,
          campaignId = CampaignId(1),
          name = name
        ),
        CharacterInfo(
          notes = notes,
          talents = talents,
          lineage = lineage,
          attributes = attributes,
          determination = determination,
          departments = departments,
          rank = rank,
          reputation = reputation,
          stress = stress,
          traits = traits.toSeq,
          values = values,
          focuses = focuses,
          pastimes = Seq.empty,
          inventoryItems = inventoryItems,
          weapons = weapons
        ).toJsonAST.toOption.get
      )
    }).left.map(DMScreenError(_))
  }

  override def importCharacter(uri: URI): ZIO[Any, DMScreenError, sta.Character] = {
    for {
      c <- Files
        .readAllBytes(zio.nio.file.Path(uri)).map(s => String(s.toArray, "UTF-8"))
        .mapBoth(
          e => DMScreenError("", Some(e)),
          s => json2Character(s)
        )
        .flatMap(ZIO.fromEither)
    } yield c
  }

  override def importStarship(uri: URI): ZIO[Any, DMScreenError, Starship] = ???

}
