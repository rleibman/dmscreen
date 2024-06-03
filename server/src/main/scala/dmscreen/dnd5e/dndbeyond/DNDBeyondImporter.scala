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

package dmscreen.dnd5e.dndbeyond

import dmscreen.dnd5e.DND5eImporter
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenError, DMScreenServerEnvironment}
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.nio.file.*
import zio.prelude.NonEmptyList

import java.net.URI

object DNDBeyondImporter {

  def live = ZLayer.succeed(new DNDBeyondImporter)

}

class DNDBeyondImporter extends DND5eImporter[URI, URI, URI, URI] {

  override def importCampaign(campaignLink: URI): ZIO[Any, DMScreenError, DND5eCampaign] = ???

  override def importEncounter(encounterLink: URI): ZIO[Any, DMScreenError, Encounter] = ???

  override def importMonster(monsterLink: URI): ZIO[Any, DMScreenError, Monster] = ???

  extension (obj: Json.Obj) {

    def getObjOption(key:     String): Either[String, Option[Json.Obj]] = Right(obj.get(key).flatMap(_.asObject))
    def getArrOption(key:     String): Either[String, Option[Chunk[Json]]] = Right(obj.get(key).flatMap(_.asArray))
    def getStrOption(key:     String): Either[String, Option[String]] = Right(obj.get(key).flatMap(_.asString))
    def getBooleanOption(key: String): Either[String, Option[Boolean]] = Right(obj.get(key).flatMap(_.asBoolean))
    def getIntOption(key: String): Either[String, Option[Int]] =
      Right(obj.get(key).flatMap(_.asNumber).map(_.value.intValue))

    def getObj(key: String): Either[String, Json.Obj] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asObject.toRight(s"Key $key is not an object")
      } yield ret

    def getStr(key: String): Either[String, String] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asString.toRight(s"Key $key is not a String")
      } yield ret

    def getInt(key: String): Either[String, Int] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asNumber.toRight(s"Key $key is not an number")
      } yield ret.value.intValue

    def getBool(key: String): Either[String, Boolean] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asBoolean.toRight(s"Key $key is not an object")
      } yield ret

    def getArr(key: String): Either[String, Chunk[Json]] =
      for {
        got <- obj.get(key).toRight(s"No key $key")
        ret <- got.asArray.toRight(s"Key $key is not an object")
      } yield ret

  }
  def conditionId2Condition(id: Int): Condition = {
    id match {
      case 1  => Condition.blinded
      case 2  => Condition.charmed
      case 3  => Condition.deafened
      case 4  => Condition.frightened
      case 5  => Condition.grappled
      case 6  => Condition.incapacitated
      case 7  => Condition.invisible
      case 8  => Condition.paralyzed
      case 9  => Condition.petrified
      case 10 => Condition.poisoned
      case 11 => Condition.prone
      case 12 => Condition.restrained
      case 13 => Condition.stunned
      case 14 => Condition.unconscious
    }
  }

  def sizeId2CreatureSize(id: Int): CreatureSize =
    id match {
      case 1 => CreatureSize.tiny
      case 2 => CreatureSize.small
      case 3 => CreatureSize.medium
      case 4 => CreatureSize.large
      case 5 => CreatureSize.huge
      case 6 => CreatureSize.gargantuan
    }
  def alignmentId2Alignment(id: Int): Alignment =
    id match {
      case 1 => Alignment.lawfulGood
      case 2 => Alignment.neutralGood
      case 3 => Alignment.chaoticGood
      case 4 => Alignment.lawfulNeutral
      case 5 => Alignment.trueNeutral
      case 6 => Alignment.chaoticNeutral
      case 7 => Alignment.lawfulEvil
      case 8 => Alignment.neutralEvil
      case 9 => Alignment.chaoticEvil
    }

  def lifestyleId2Lifestyle(i: Int): Lifestyle =
    i match {
      case 1 => Lifestyle.wretched
      case 2 => Lifestyle.squalid
      case 3 => Lifestyle.poor
      case 4 => Lifestyle.modest
      case 5 => Lifestyle.comfortable
      case 6 => Lifestyle.wealthy
      case 7 => Lifestyle.aristocratic
    }

  private def listOfEither2EitherOfList[B](data: List[Either[String, B]]): Either[String, List[B]] = {
    data
      .foldLeft[Either[String, List[B]]](Right(List.empty)) {
        case (Right(acc), Right(b)) => Right(b :: acc)
        case (Right(_), Left(e))    => Left(e)
        case (Left(e), _)           => Left(e)
      }.map(_.reverse)
  }

  def statId2AbilityType(id: Int): AbilityType =
    id match {
      case 1 => AbilityType.strength
      case 2 => AbilityType.dexterity
      case 3 => AbilityType.constitution
      case 4 => AbilityType.intelligence
      case 5 => AbilityType.wisdom
      case 6 => AbilityType.charisma
    }

  override def importPlayerCharacter(uri: URI): ZIO[Any, DMScreenError, PlayerCharacter] = {
    def classObj2PlayerCharacterClass(obj: Json): Either[String, PlayerCharacterClass] =
      for {
        classObj   <- obj.asObject.toRight("Not an object")
        definition <- classObj.getObj("definition")
        name       <- definition.getStr("name")
        characterClassId <- CharacterClassId.values
          .find(s => name.equalsIgnoreCase(s.toString)).toRight(s"Unknown class $name")
        level              <- classObj.getInt("level")
        subclassDefinition <- classObj.getObj("subclassDefinition")
        subclassName       <- subclassDefinition.getStr("name")
      } yield PlayerCharacterClass(characterClassId, Option(Subclass(subclassName)), level)

    def statTuple(json: Json): Either[String, (AbilityType, Int)] = {
      for {
        obj         <- json.asObject.toRight("Not an object")
        abilityType <- obj.getInt("id").map(id => statId2AbilityType(id))
        value       <- obj.getIntOption("value")
      } yield (abilityType, value.getOrElse(0))
    }

    def json2PlayerCharacter(str: String): Either[DMScreenError, PlayerCharacter] =
      (for {
        json        <- str.fromJson[Json]
        d1          <- json.asObject.toRight("Not an object")
        data        <- d1.getObj("data")
        id          <- data.getInt("id").map(_.toString)
        player      <- data.getStr("username")
        name        <- data.getStr("name")
        gender      <- data.getStrOption("gender")
        faith       <- data.getStrOption("faith")
        age         <- data.getIntOption("age")
        hair        <- data.getStrOption("hair")
        eyes        <- data.getStrOption("eyes")
        skin        <- data.getStrOption("skin")
        height      <- data.getStrOption("height")
        weight      <- data.getIntOption("weight")
        inspiration <- data.getBool("inspiration")

        baseHitPoints       <- data.getInt("baseHitPoints")
        bonusHitPoints      <- data.getIntOption("bonusHitPoints")
        overrideHitPoints   <- data.getIntOption("overrideHitPoints")
        removedHitPoints    <- data.getIntOption("removedHitPoints")
        temporaryHitPoints  <- data.getIntOption("temporaryHitPoints")
        deathSaves          <- data.getObj("deathSaves")
        deathSavesSuccesses <- deathSaves.getInt("successCount")
        deathSavesFailures  <- deathSaves.getInt("failCount")
        deathSavesIsStable  <- deathSaves.getBool("isStabilized")
        currentXp           <- data.getInt("currentXp")
        alignment           <- data.getInt("alignmentId").map(alignmentId2Alignment)
        lifestyle           <- data.getInt("lifestyleId").map(lifestyleId2Lifestyle)
        inventoryArr        <- data.getArr("inventory").map(_.flatMap(_.asObject))
        inventory = inventoryArr
          .flatMap(
            _.get("definition").flatMap(
              _.asObject.flatMap(_.get("name").flatMap(_.asString.map(name => InventoryItem(name))))
            )
          ).toList
        statsObj         <- data.getArr("stats")
        bonusStatsObj    <- data.getArr("bonusStats")
        overrideStatsObj <- data.getArr("overrideStats")
        conditions <- data
          .getArr("conditions").map(_.flatMap(_.asNumber.map(n => conditionId2Condition(n.value.intValue))).toSet)
        stats         <- listOfEither2EitherOfList(statsObj.map(statTuple).toList)
        bonusStats    <- listOfEither2EitherOfList(bonusStatsObj.map(statTuple).toList)
        overrideStats <- listOfEither2EitherOfList(overrideStatsObj.map(statTuple).toList)
        backgroundOpt <- data.getObjOption("background")
        background <- backgroundOpt.fold(Left[String, Option[Background]](""))(
          _.getStrOption("name").map(_.map(Background.apply))
        )
        raceObj  <- data.getObj("race")
        raceName <- raceObj.getStr("baseRaceName")
        size <- raceObj
          .getIntOption("sizeId").map(_.map(sizeId2CreatureSize).getOrElse(CreatureSize.medium))
        notes          <- data.getObj("notes")
        notesBackstory <- notes.getStr("backstory")
        currencies     <- data.getObj("currencies")
        currencyGP     <- currencies.getInt("gp")
        currencyPP     <- currencies.getInt("pp")
        currencyEP     <- currencies.getInt("ep")
        currencySP     <- currencies.getInt("sp")
        currencyCP     <- currencies.getInt("cp")
        classes        <- data.getArr("classes")
        classesParsed <-
          listOfEither2EitherOfList(classes.map(classObj2PlayerCharacterClass).toList).flatMap { l =>
            NonEmptyList.fromIterableOption(l).toRight("You must supply at least one class")
          }
        feats <- {
          data
            .getArr("feats")
            .map(_.flatMap { o =>
              for {
                obj            <- o.asObject
                definitionJson <- obj.get("definition")
                definition     <- definitionJson.asObject
                nameJson       <- definition.get("name")
                name           <- nameJson.asString
              } yield Feat(name)
            }.toList)
        }
        spellSlotsArr <- data.getArr("spellSlots").map(_.toList)
        spellsArr <- data
          .getArr("classSpells").map(
            _.flatMap(_.asObject).flatMap(_.get("spells").flatMap(_.asArray)).flatten.flatMap(_.asObject).toList
          )
        deathSavesSuccesses <- deathSaves.getInt("failCount")
        deathSavesFailures  <- deathSaves.getInt("successCount")
        creatures           <- data.getArr("creatures")
        modifiers           <- data.getObj("modifiers")
        traitsObj           <- data.getObj("traits")
        personalityTraits   <- traitsObj.getStrOption("personalityTraits")
        ideals              <- traitsObj.getStrOption("ideals")
        bonds               <- traitsObj.getStrOption("bonds")
        flaws               <- traitsObj.getStrOption("flaws")
        appearance          <- traitsObj.getStrOption("appearance")
        raceModifiers <- modifiers
          .getArr("race").flatMap(chunk =>
            listOfEither2EitherOfList(chunk.map(_.asObject.toRight("Not an object")).toList)
          )
        classModifiers <- modifiers
          .getArr("class").flatMap(chunk =>
            listOfEither2EitherOfList(chunk.map(_.asObject.toRight("Not an object")).toList)
          )
        backgroundModifiers <- modifiers
          .getArr("background").flatMap(chunk =>
            listOfEither2EitherOfList(chunk.map(_.asObject.toRight("Not an object")).toList)
          )
        itemModifiers <- modifiers
          .getArr("item").flatMap(chunk =>
            listOfEither2EitherOfList(chunk.map(_.asObject.toRight("Not an object")).toList)
          )
        featModifiers <- modifiers
          .getArr("feat").flatMap(chunk =>
            listOfEither2EitherOfList(chunk.map(_.asObject.toRight("Not an object")).toList)
          )
        conditionModifiers <- modifiers
          .getArr("condition").flatMap(chunk =>
            listOfEither2EitherOfList(chunk.map(_.asObject.toRight("Not an object")).toList)
          )
      } yield {
        val allModifiers =
          (raceModifiers ++ classModifiers ++ backgroundModifiers ++ itemModifiers ++ featModifiers ++ conditionModifiers)
            .groupBy(_.get("type").flatMap(_.asString).getOrElse(""))

        val bonuses = allModifiers.filter(_._1 == "bonus").flatMap(_._2)
        val proficiencies = allModifiers.filter(_._1 == "proficiency").flatMap(_._2)
        val languages = allModifiers
          .filter(_._1 == "language")
          .flatMap(_._2.flatMap(_.get("subType").flatMap(_.asString.map(Language.fromName))))
          .toSet
        val resistances = allModifiers.filter(_._1 == "resistance").flatMap(_._2)
        val immunity = allModifiers.filter(_._1 == "immunity").flatMap(_._2)
        val vulnerability = allModifiers.filter(_._1 == "vulnerability").flatMap(_._2)
        val senses = {
          Seq(SenseRange(Sense.sight, 10560)) ++
            allModifiers
              .filter(_._1 == "set-base")
              .flatMap(_._2)
              .find(_.get("subType").flatMap(_.asString).contains("darkvision"))
              .flatMap(_.get("value").flatMap(_.asNumber).map(_.value.intValue))
              .map(SenseRange(Sense.darkvision, _)).toSeq ++
            allModifiers
              .filter(_._1 == "set-base")
              .flatMap(_._2)
              .find(_.get("subType").flatMap(_.asString).contains("blindsight"))
              .flatMap(_.get("value").flatMap(_.asNumber).map(_.value.intValue))
              .map(SenseRange(Sense.blindsight, _)).toSeq ++
            allModifiers
              .filter(_._1 == "set-base")
              .flatMap(_._2)
              .find(_.get("subType").flatMap(_.asString).contains("tremorsense"))
              .flatMap(_.get("value").flatMap(_.asNumber).map(_.value.intValue))
              .map(SenseRange(Sense.tremorsense, _)).toSeq ++
            allModifiers
              .filter(_._1 == "set-base")
              .flatMap(_._2)
              .find(_.get("subType").flatMap(_.asString).contains("truesight"))
              .flatMap(_.get("value").flatMap(_.asNumber).map(_.value.intValue))
              .map(SenseRange(Sense.truesight, _)).toSeq ++
            allModifiers
              .filter(_._1 == "set-base")
              .flatMap(_._2)
              .find(_.get("subType").flatMap(_.asString).contains("scent"))
              .flatMap(_.get("value").flatMap(_.asNumber).map(_.value.intValue))
              .map(SenseRange(Sense.scent, _))
        }

        val hitPoints = {
          val maxHP = overrideHitPoints.getOrElse(baseHitPoints + bonusHitPoints.getOrElse(0))
          val removedHP = removedHitPoints.getOrElse(0)
          val tempHP = temporaryHitPoints.getOrElse(0)
          val atZero = maxHP - removedHP + tempHP
          HitPoints(
            currentHitPoints =
              if (atZero <= 0)
                DeathSave(deathSavesSuccesses, deathSavesFailures, deathSavesIsStable)
              else
                maxHP - removedHP,
            maxHitPoints = maxHP,
            overrideMaxHitPoints = overrideHitPoints,
            temporaryHitPoints = temporaryHitPoints
          )
        }

        val abilities = {
          val profs: Set[Option[String]] = proficiencies.map(_.get("subType").flatMap(_.asString)).toSet
          val abs = stats
            .zip(bonusStats)
            .zip(overrideStats)
            .map { case ((stat, bonusStat), overrideStat) =>
              val abilityType = stat._1

              stat._1 -> Ability(
                abilityType = abilityType,
                value = stat._2 + bonusStat._2,
                overrideValue = if (overrideStat._2 == 0) None else Some(overrideStat._2),
                isProficient = profs.contains(Some(s"${abilityType.name.toLowerCase}-saving-throws"))
              )
            }
            .toMap

          Abilities(
            strength = abs.getOrElse(AbilityType.strength, Ability(AbilityType.strength, 10, None)),
            dexterity = abs.getOrElse(AbilityType.dexterity, Ability(AbilityType.dexterity, 10, None)),
            constitution = abs.getOrElse(AbilityType.constitution, Ability(AbilityType.constitution, 10, None)),
            intelligence = abs.getOrElse(AbilityType.intelligence, Ability(AbilityType.intelligence, 10, None)),
            wisdom = abs.getOrElse(AbilityType.wisdom, Ability(AbilityType.wisdom, 10, None)),
            charisma = abs.getOrElse(AbilityType.charisma, Ability(AbilityType.charisma, 10, None))
          )

        }

        val ac = {
          val baseUnarmoredAC =
            allModifiers
              .get("set")
              .toSeq
              .flatten
              .find(_.get("subType").flatMap(_.asString).contains("unarmored-armor-class"))
              .flatMap(_.get("value").flatMap(_.asNumber).map(_.value.intValue))
              .getOrElse(0) + 10 + abilities.dexterity.modifier
          val equiped = inventoryArr.filter(_.getBool("equipped").getOrElse(false))

          val equipmentBaseAC =
            equiped
              .map(_.getObj("definition").flatMap(_.getInt("armorClass")).getOrElse(0))
              .sum // This may not be right, it'll work for full plate, but not for half plate

          val modifyAC = bonuses
            .filter(_.get("subType").flatMap(_.asString).contains("armor-class"))
            .map(_.getInt("value").getOrElse(0))
            .sum

          scala.math.max(baseUnarmoredAC, equipmentBaseAC) + modifyAC
        }

        val skills = {
          import SkillType.*
          def hasProficiency(skillType: SkillType): Boolean = {
            proficiencies.exists { p =>
              val s = p.get("subType").flatMap(_.asString)
              s.contains(skillType.toString.toLowerCase)
            }
          }
          Skills(
            acrobatics = Skill(acrobatics, hasProficiency(acrobatics)),
            animalHandling = Skill(animalHandling, hasProficiency(animalHandling)),
            arcana = Skill(arcana, hasProficiency(arcana)),
            athletics = Skill(athletics, hasProficiency(athletics)),
            deception = Skill(deception, hasProficiency(deception)),
            history = Skill(history, hasProficiency(history)),
            insight = Skill(insight, hasProficiency(insight)),
            intimidation = Skill(intimidation, hasProficiency(intimidation)),
            investigation = Skill(investigation, hasProficiency(investigation)),
            medicine = Skill(medicine, hasProficiency(medicine)),
            nature = Skill(nature, hasProficiency(nature)),
            perception = Skill(perception, hasProficiency(perception)),
            performance = Skill(performance, hasProficiency(performance)),
            persuasion = Skill(persuasion, hasProficiency(persuasion)),
            religion = Skill(religion, hasProficiency(religion)),
            sleightOfHand = Skill(sleightOfHand, hasProficiency(sleightOfHand)),
            stealth = Skill(stealth, hasProficiency(stealth)),
            survival = Skill(survival, hasProficiency(survival))
          )
        }

        val spellSlots = spellSlotsArr.flatMap { o =>
          for {
            obj          <- o.asObject
            levelObj     <- obj.get("level")
            level        <- levelObj.asNumber.map(_.value.intValue)
            usedObj      <- obj.get("used")
            used         <- usedObj.asNumber.map(_.value.intValue)
            availableObj <- obj.get("available")
            available    <- availableObj.asNumber.map(_.value.intValue)
          } yield SpellSlots(level = level, used = used, available = available)
        }

        val classSpells = spellsArr.flatMap { o =>
          for {
            spellId <- o.get("id").flatMap(_.asString)
            name    <- o.get("name").flatMap(_.asString)
            // prepared
            // usesSpellSlot
            // castAtLevel
            // range
            // school
            // duration
            // castingTime
          } yield SpellHeader(SpellId(0), name)
        }

        PlayerCharacter(
          PlayerCharacterHeader(
            PlayerCharacterId(0),
            CampaignId(1),
            name,
            Some(player)
          ),
          PlayerCharacterInfo(
            hitPoints = hitPoints,
            armorClass = ac,
            source = DNDBeyondImportSource(uri),
            physicalCharacteristics = PhysicalCharacteristics(
              gender = gender,
              age = age,
              hair = hair,
              eyes = eyes,
              skin = skin,
              height = height,
              weight = weight,
              size = size
            ),
            faith = faith,
            inspiration = inspiration,
            currentXp = Option(currentXp),
            alignment = alignment,
            lifestyle = lifestyle,
            abilities = abilities,
            skills = skills,
            background = background,
            race = Race(raceName),
            traits = Traits(personalityTraits, ideals, bonds, flaws, appearance),
            inventory = inventory,
            wallet = Wallet(
              currencyGP,
              currencyPP,
              currencyEP,
              currencySP,
              currencyCP
            ),
            classes = classesParsed,
            feats = feats,
            conditions = conditions,
            spellSlots = spellSlots,
            languages = languages,
//  actions:                 List[Action] = List.empty,
            classSpells = classSpells,
//  creatures:               List[Creature] = List.empty,
            notes = notesBackstory,
            senses = senses
          ).toJsonAST.toOption.get
        )
      }).left.map(DMScreenError(_))

    for {
      pc <- Files
        .readAllBytes(zio.nio.file.Path(uri)).map(s => String(s.toArray, "UTF-8"))
        .mapBoth(
          e => DMScreenError("", Some(e)),
          s => json2PlayerCharacter(s)
        )
        .flatMap(ZIO.fromEither)
    } yield pc
  }

}
