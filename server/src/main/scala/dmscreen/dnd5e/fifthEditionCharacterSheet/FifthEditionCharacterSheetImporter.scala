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

package dmscreen.dnd5e.fifthEditionCharacterSheet

import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenError}
import zio.json.*
import zio.nio.file.Files
import zio.prelude.NonEmptyList
import zio.{ULayer, ZIO, ZLayer}

import java.io.StringReader
import java.net.URI
import javax.xml.parsers.SAXParserFactory
import scala.xml.factory.XMLLoader
import scala.xml.{Elem, XML}

object FifthEditionCharacterSheetImporter {

  val live: ULayer[FifthEditionCharacterSheetImporter] = ZLayer.succeed(FifthEditionCharacterSheetImporter())

}

class FifthEditionCharacterSheetImporter extends DND5eImporter[URI, URI, URI, URI] {

  extension (str: String) {

    def splitWith(delimiter: Char): Seq[String] = {
      // Use foldLeft to split the string based on the delimiter
      val splitSeqs = str.foldLeft(Seq(Seq[Char]())) {
        (
          acc,
          ch
        ) =>
          if (ch == delimiter) acc :+ Seq()
          else acc.init :+ (acc.last :+ ch)
      }

      // Convert the sequences of characters back to strings
      val strings = splitSeqs.map(_.mkString)

      // If the string ends with the delimiter, ensure we add an empty string at the end
      if (str.nonEmpty && str.last == delimiter) strings :+ ""
      else strings
    }

  }

  override def importCampaign(uri: URI): ZIO[Any, DMScreenError, DND5eCampaign] = ???

  override def importEncounter(uri: URI): ZIO[Any, DMScreenError, Encounter] = ???

  override def importMonster(uri: URI): ZIO[Any, DMScreenError, Monster] = ???

  override def importPlayerCharacter(uri: URI): ZIO[Any, DMScreenError, PlayerCharacter] = {
    def parseCharacterElem(elem: Elem): Either[DMScreenError, PlayerCharacter] = {
      val child = elem.child
      val version = child.collect { case elem2: Elem if elem2.label == "version" => elem2.text }.head
      val initMiscMod = child.collect { case elem2: Elem if elem2.label == "initMiscMod" => elem2.text }.head
      val improvedInitiative = child.collect {
        case elem2: Elem if elem2.label == "improvedInitiative" => elem2.text
      }.head
      val hitPoints = {
        val currentHealth =
          child.collect { case elem2: Elem if elem2.label == "currentHealth" => elem2.text }.head.toInt
        HitPoints(
          currentHitPoints = if (currentHealth <= 0) {
            DeathSave(
              fails = child
                .collect {
                  case elem2: Elem if elem2.label == "deathSaveFailures" => elem2.text
                }.head.toInt,
              successes = child
                .collect {
                  case elem2: Elem if elem2.label == "deathSaveSuccesses" => elem2.text
                }.head.toInt
            )
          } else currentHealth,
          maxHitPoints = child.collect { case elem2: Elem if elem2.label == "maxHealth" => elem2.text }.head.toInt,
          temporaryHitPoints =
            child.collectFirst { case elem2: Elem if elem2.label == "currentTempHP" => elem2.text }.map(_.toInt)
        )
      }
//      val maxDex = child.collect { case elem2: Elem if elem2.label == "maxDex" => elem2.text }.head
//      val proficiencyBonus = child.collect {case elem2: Elem if elem2.label == "proficiencyBonus" => elem2.text}.head
//      val miscSpellAttackBonus = child.collect {case elem2: Elem if elem2.label == "miscSpellAttackBonus" => elem2.text}.head
//      val miscSpellDCBonus = child.collect {case elem2: Elem if elem2.label == "miscSpellDCBonus" => elem2.text}.head
//      val castingStatCode = child.collect {case elem2: Elem if elem2.label == "castingStatCode" => elem2.text}.head
//      val offenseAbilityDisplay = child.collect {case elem2: Elem if elem2.label == "offenseAbilityDisplay" => elem2.text}.head
//      val baseSpeed = child.collect { case elem2: Elem if elem2.label == "baseSpeed" => elem2.text }.head //TODO use this
//      val speedMiscMod = child.collect { case elem2: Elem if elem2.label == "speedMiscMod" => elem2.text }.head
//      val movementMode = child.collect { case elem2: Elem if elem2.label == "movementMode" => elem2.text }.head
//      val raceCode = child.collect { case elem2: Elem if elem2.label == "raceCode" => elem2.text }.head
//      val subraceCode = child.collect { case elem2: Elem if elem2.label == "subraceCode" => elem2.text }.head
//      val backgroundCode = child.collect { case elem2: Elem if elem2.label == "backgroundCode" => elem2.text }.head
//      val pagePosition0 = child.collect { case elem2: Elem if elem2.label == "pagePosition0" => elem2.text }.head
//      val pagePosition1 = child.collect { case elem2: Elem if elem2.label == "pagePosition1" => elem2.text }.head
//      val pagePosition2 = child.collect { case elem2: Elem if elem2.label == "pagePosition2" => elem2.text }.head
//      val pagePosition3 = child.collect { case elem2: Elem if elem2.label == "pagePosition3" => elem2.text }.head
//      val pagePosition4 = child.collect { case elem2: Elem if elem2.label == "pagePosition4" => elem2.text }.head
//      val unarmoredDefense = child.collect {case elem2: Elem if elem2.label == "unarmoredDefense" => elem2.text}.head
//      val featCode = child.collect { case elem2: Elem if elem2.label == "featCode" => elem2.text }.head
      val classData = child.collect { case elem2: Elem if elem2.label == "classData" => elem2.text }.head

      val classDataParsed = classData.splitWith('⊟').map(_.splitWith('⊠').map(_.splitWith('⊡')))
      // classDataParsed also has feats, resources, sourcery points and other things I don't quite understand yet

      val classes = NonEmptyList
        .fromIterableOption(classDataParsed.head.map { data =>
          PlayerCharacterClass(
            characterClass = CharacterClassId.values
              .find(_.toString.equalsIgnoreCase(data(0).trim)).getOrElse(CharacterClassId.unknown),
            subclass = if (data(1).isEmpty) None else Some(Subclass(data(1).trim)),
            level = data(2).trim.toInt
          )
        }).getOrElse(throw new Exception("No classes found"))

//      val multiclassFeatures = child.collect {case elem2: Elem if elem2.label == "multiclassFeatures" => elem2.text}.head
//      val weaponList = child.collect { case elem2: Elem if elem2.label == "weaponList" => elem2.text }.head
      val abilities = child.collect {
        case elem2: Elem if elem2.label == "abilityScores" =>
          elem2.text.splitWith(8864.toChar) match {
            case Seq(
                  str:     String,
                  dex:     String,
                  con:     String,
                  int:     String,
                  wis:     String,
                  cha:     String,
                  strProf: String,
                  dexProf: String,
                  conProf: String,
                  intProf: String,
                  wisProf: String,
                  chaProf: String,
                  strOv:   String,
                  dexOv:   String,
                  conOv:   String,
                  intOv:   String,
                  wisOv:   String,
                  chaOv:   String,
                  extra:   String // Who the heck knows what this is?
                ) =>
              def ov(str: String) = str.toIntOption.flatMap(v => if (v <= 0) None else Some(v))
              Abilities(
                strength = Ability(AbilityType.strength, str.toInt, ov(strOv), strProf.toBoolean),
                dexterity = Ability(AbilityType.dexterity, dex.toInt, ov(dexOv), dexProf.toBoolean),
                constitution = Ability(AbilityType.constitution, con.toInt, ov(conOv), conProf.toBoolean),
                intelligence = Ability(AbilityType.intelligence, int.toInt, ov(intOv), intProf.toBoolean),
                wisdom = Ability(AbilityType.wisdom, wis.toInt, ov(wisOv), wisProf.toBoolean),
                charisma = Ability(AbilityType.charisma, cha.toInt, ov(chaOv), chaProf.toBoolean)
              )
            case _ => Abilities()
          }
      }.head

      val armorClass = {
        child.collectFirst { case elem2: Elem if elem2.label == "armorBonus" => elem2.text }.map(_.toInt).getOrElse(0) +
          child
            .collectFirst { case elem2: Elem if elem2.label == "shieldBonus" => elem2.text }.map(_.toInt).getOrElse(0) +
          child
            .collectFirst { case elem2: Elem if elem2.label == "miscArmorBonus" => elem2.text }.map(_.toInt).getOrElse(
              0) +
          abilities.dexterity.modifier
      }

      val skillInfo = child.collect { case elem2: Elem if elem2.label == "skillInfo" => elem2.text }.head
      val skills = {
        /*
          Somewhere in there she has proficiency in acrobatics, animal handling, history investigation and perception, the order seems to be:
          Athletics, Acrobatics, Sleight of Hand, Stealth, Arcana, History, Investigation, Nature, Religion, Animal Handling, Insight, Medicine, Perception, Survival, Deception, Intimidation, Performance, Persuasion
          //I think
          //Skill Proficiency
          //Miscellaneous Bonus
          //Double Proficiency
          //Half Proficiency
          false⊠true⊠false⊠false⊠false⊠true⊠true⊠false⊠false⊠true⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠true⊠false⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠0⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false⊠false
         */
        import SkillType.*
        val list = skillInfo.splitWith(8864.toChar)
        val proficiencies = list.take(18).map(_.trim.toBoolean)
        Skills(
          athletics = Skill(athletics, proficiencies(0)),
          acrobatics = Skill(acrobatics, proficiencies(1)),
          sleightOfHand = Skill(sleightOfHand, proficiencies(2)),
          stealth = Skill(stealth, proficiencies(3)),
          arcana = Skill(arcana, proficiencies(4)),
          history = Skill(history, proficiencies(5)),
          investigation = Skill(investigation, proficiencies(6)),
          nature = Skill(nature, proficiencies(7)),
          religion = Skill(religion, proficiencies(8)),
          animalHandling = Skill(animalHandling, proficiencies(9)),
          insight = Skill(insight, proficiencies(10)),
          medicine = Skill(medicine, proficiencies(11)),
          perception = Skill(perception, proficiencies(12)),
          survival = Skill(survival, proficiencies(13)),
          deception = Skill(deception, proficiencies(14)),
          intimidation = Skill(intimidation, proficiencies(15)),
          performance = Skill(performance, proficiencies(16)),
          persuasion = Skill(persuasion, proficiencies(17))
        )
      }

//      val spellList = child.collect { case elem2: Elem if elem2.label == "spellList" => elem2.text }.head
      val noteList = child.collect { case elem2: Elem if elem2.label == "noteList" => elem2.text }.head

      val noteListParsed = noteList.splitWith(8864.toChar)
      val raceFeatures = noteListParsed(0)
      val armorProficiencies = noteListParsed(1)
      val weaponProficiencies = noteListParsed(2)
      val gameProficiencies = noteListParsed(3)
      val languages = noteListParsed(4).split("\n").map(s => Language.fromName(s.trim))
      val inventory = noteListParsed(5).split("\n").map(InventoryItem(_))
      val extranotes = noteListParsed(6)
      // val classes = noteListParsed(7) //ignore, we get better info from <classData>
      val race = Race(noteListParsed(8).trim)
      val background = Some(Background(noteListParsed(9).trim))
      val alignment =
        Alignment.values.find(_.toString.equalsIgnoreCase(noteListParsed(10).trim)).getOrElse(Alignment.unaligned)
      val personalityTraits = noteListParsed(11)
      val ideals = noteListParsed(12)
      val bonds = noteListParsed(13)
      val flaws = noteListParsed(14)
      val name = noteListParsed(15)
      //      val characterClass2 = noteListParsed(16) //ignore, we get better info from <classData>
      val wallet = Wallet(
        cp = noteListParsed(17).trim.toLong,
        sp = noteListParsed(18).trim.toLong,
        ep = noteListParsed(19).trim.toLong,
        gp = noteListParsed(20).trim.toLong,
        pp = noteListParsed(21).trim.toLong
      )
      val currentXp = noteListParsed(22).trim.toLongOption.getOrElse(0L)

      //      val hitDiceList = child.collect { case elem2: Elem if elem2.label == "hitDiceList" => elem2.text }.head
      //      val classResource = child.collect { case elem2: Elem if elem2.label == "classResource" => elem2.text }.head

      Right(
        PlayerCharacter(
          header = PlayerCharacterHeader(id = PlayerCharacterId.empty, campaignId = CampaignId(1), name = name),
          jsonInfo = PlayerCharacterInfo(
            hitPoints = hitPoints,
            armorClass = armorClass,
            classes = classes,
            source = FifthEditionCharacterSheetImportSource(uri),
//physicalCharacteristics = ,
//faith = ,
//inspiration = ,
            currentXp = if (currentXp == 0L) None else Some(currentXp),
            alignment = alignment,
//lifestyle = ,
            abilities = abilities,
            skills = skills,
            background = background,
            race = race,
//traits = ,
            inventory = inventory.toList,
            wallet = wallet,
//feats = ,
//conditions = ,
//spellSlots = ,
//pactMagic = ,
            languages = languages.toList,
//actions = ,
//classSpells = ,
//creatures = ,
            notes = extranotes
//senses = , // Normal sight, 2 miles
          ).toJsonAST.toOption.get
        )
      )
    }

    def xml2PlayerCharacter(s: String): Either[DMScreenError, PlayerCharacter] = {
      val customXML: XMLLoader[Elem] = XML.withSAXParser {
        val factory = SAXParserFactory.newInstance()
        factory.setFeature("http://xml.org/sax/features/validation", false)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory.newSAXParser()
      }
      val root: Elem = customXML.load(StringReader(s))
      root.collect {
        case elem: Elem if elem.label == "character" => parseCharacterElem(elem)
      }.head
    }

    for {
      pc <- Files
        .readAllBytes(zio.nio.file.Path(uri)).map(s => String(s.toArray, "UTF-8"))
        .mapBoth(
          e => DMScreenError("", Some(e)),
          s => xml2PlayerCharacter(s)
        )
        .flatMap(ZIO.fromEither)
    } yield pc

  }

}
