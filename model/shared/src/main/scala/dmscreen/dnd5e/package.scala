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

import dmscreen.DiceRoll

import java.net.URI
import scala.collection.SortedMap

sealed trait ImportSource

object ImportSource {

  given CanEqual[ImportSource, ImportSource] = CanEqual.derived

}

case object DMScreenSource extends ImportSource

opaque type DndBeyondId = String

object DndBeyondId {

  private val dndBeyondIdRegex = "^[1-9][0-9]{8}$".r

  def apply(dndBeyondId: String): Either[String, DndBeyondId] =
    if (dndBeyondIdRegex.matches(dndBeyondId)) Right(dndBeyondId) else Left(s"Invalid DnD Beyond Id: $dndBeyondId")
  def apply(dndBeyondId: Long): DndBeyondId = dndBeyondId.toString

  extension (dndBeyondId: DndBeyondId) {

    def value: String = dndBeyondId

  }

}

sealed case class DNDBeyondImportSource(
  dndBeyondId: DndBeyondId
) extends ImportSource

sealed case class FifthEditionCharacterSheetImportSource(
  uri: URI
) extends ImportSource

case class Traits(
  personalityTraits: Option[String] = None,
  ideals:            Option[String] = None,
  bonds:             Option[String] = None,
  flaws:             Option[String] = None,
  appearance:        Option[String] = None
)

case class Race(name: String)

enum CharacterClassId(val name: String) {

  case artificer extends CharacterClassId("Artificer")
  case barbarian extends CharacterClassId("Barbarian")
  case bard extends CharacterClassId("Bard")
  case cleric extends CharacterClassId("Cleric")
  case druid extends CharacterClassId("Druid")
  case fighter extends CharacterClassId("Fighter")
  case monk extends CharacterClassId("Monk")
  case paladin extends CharacterClassId("Paladin")
  case ranger extends CharacterClassId("Ranger")
  case rogue extends CharacterClassId("Rogue")
  case sorcerer extends CharacterClassId("Sorcerer")
  case warlock extends CharacterClassId("Warlock")
  case wizard extends CharacterClassId("Wizard")
  case `blood hunter` extends CharacterClassId("Blood Hunter")
  case unknown extends CharacterClassId("Unknown")

}

object CharacterClassId {

  given CanEqual[CharacterClassId, CharacterClassId] = CanEqual.derived

}

case class CharacterClass(
  id:      CharacterClassId,
  hitDice: DiceRoll
)

opaque type SourceId = String

object SourceId {

  def apply(sourceId: String): SourceId = sourceId

  val homebrew:                SourceId = "homebrew"
  val systemReferenceDocument: SourceId = "srd"
  def other(sourceId: String): SourceId = sourceId

  extension (value: SourceId) {

    def value: String = value

  }

}

object Source {

  val homebrew: Source = Source("Homebrew", SourceId.homebrew, None)
  val systemReferenceDocument: Source = Source(
    "System Reference Document",
    SourceId.systemReferenceDocument,
    Some("https://dnd.wizards.com/resources/systems-reference-document")
  )

}

case class Source(
  name: String,
  id:   SourceId,
  url:  Option[String]
)

case class PlayerCharacterClass(
  characterClass: CharacterClassId,
  subclass:       Option[SubClass] = None,
  level:          Int = 1
)

case class SubClass(name: String)

case class Feat(name: String)

enum Lifestyle {

  case wretched, squalid, poor, modest, comfortable, wealthy, aristocratic, unknown

}

enum Alignment(
  val name:        String,
  val description: String
) {

  case lawfulGood extends Alignment(
        "Lawful Good",
        "Acts with compassion and honor, values law, tradition, and doing what is right. A classic hero who believes in justice and helping others through order."
      )

  case neutralGood extends Alignment(
        "Neutral Good",
        "Does the best good they can without regard for or against rules. Guided by conscience, they strive to help others without being bound by laws or chaos."
      )

  case chaoticGood extends Alignment(
        "Chaotic Good",
        "Values personal freedom and kindness. Rebels against oppression and fights for the downtrodden, even if it means breaking the rules."
      )

  case lawfulNeutral extends Alignment(
        "Lawful Neutral",
        "Follows a strict code, tradition, or set of laws, regardless of moral considerations. Believes order is the foundation of society."
      )

  case trueNeutral extends Alignment(
        "True Neutral",
        "Maintains balance and avoids taking sides. May act selfishly or helpfully depending on the situation but avoids extremes of good, evil, law, or chaos."
      )

  case chaoticNeutral extends Alignment(
        "Chaotic Neutral",
        "Follows personal whims and acts unpredictably. Values freedom above all, unconcerned with morality or order. Neither good nor evil."
      )

  case lawfulEvil extends Alignment(
        "Lawful Evil",
        "Uses structure and law to advance selfish or malevolent goals. Manipulative and calculating, they respect power and hierarchy but exploit others mercilessly. Evil characters are real assholes and should not be portrayed sympathetically"
      )

  case neutralEvil extends Alignment(
        "Neutral Evil",
        "Primarily self-serving. Will harm or help others as it suits their interests. Lacks compassion or respect for rules but isn't driven by chaos. Evil characters are real assholes and should not be portrayed sympathetically"
      )

  case chaoticEvil extends Alignment(
        "Chaotic Evil",
        "Driven by cruelty, destruction, and personal gratification. Rejects authority, thrives on violence, and acts with no regard for others. Evil characters are real assholes and should not be portrayed sympathetically. These characters have no desire for redemption, only power, cruelty, and chaos."
      )

  case unaligned extends Alignment(
        "Unaligned",
        "Lacks a moral or ethical framework. Often applies to animals or simple-minded creatures without the capacity for alignment-based decisions."
      )

  case unknown extends Alignment(
        "Unknown",
        "The character’s alignment is not defined or has yet to be revealed. May behave inconsistently or conceal their true nature."
      )

}

object AbilityType {

  given CanEqual[AbilityType, AbilityType] = CanEqual.derived

}

enum AbilityType(
  val name:    String,
  val short:   String,
  val shorter: String
) {

  case strength extends AbilityType("Strength", "Str", "S")
  case dexterity extends AbilityType("Dexterity", "Dex", "D")
  case constitution extends AbilityType("Constitution", "Con", "C")
  case intelligence extends AbilityType("Intelligence", "Int", "I")
  case wisdom extends AbilityType("Wisdom", "Wis", "W")
  case charisma extends AbilityType("Charisma", "Cha", "Ch")

}

case class Ability(
  abilityType:   AbilityType,
  value:         Int,
  overrideValue: Option[Int] = None,
  isProficient:  Boolean = false
) {

  def overridenValue: Int = overrideValue.getOrElse(value)

  // To determine an ability modifier without consulting the table,
  // subtract 10 from the ability score and then divide the total by 2 (round down).
  def modifier: Int = (overrideValue.getOrElse(value) - 10) / 2
  // Savings throws are calculated by adding the proficiency bonus to the modifier
  def savingThrow(proficiencyBonus: Int): Int = modifier + (if (isProficient) proficiencyBonus else 0)

  def modifierString: String = {
    val m = modifier
    (if (m <= 0) ""
     else "+") + m.toString
  }
  def savingThrowString(proficiencyBonus: Int): String = {
    val s = savingThrow(proficiencyBonus)
    (if (s <= 0) ""
     else "+") + s.toString
  }

}

case class Abilities(
  strength: Ability = Ability(
    abilityType = AbilityType.strength,
    value = 10
  ),
  dexterity: Ability = Ability(
    abilityType = AbilityType.dexterity,
    value = 10
  ),
  constitution: Ability = Ability(
    abilityType = AbilityType.constitution,
    value = 10
  ),
  intelligence: Ability = Ability(
    abilityType = AbilityType.intelligence,
    value = 10
  ),
  wisdom: Ability = Ability(
    abilityType = AbilityType.wisdom,
    value = 10
  ),
  charisma: Ability = Ability(
    abilityType = AbilityType.charisma,
    value = 10
  )
) {

  given Ordering[AbilityType] = Ordering.by(_.ordinal)

  val all: SortedMap[AbilityType, Ability] = SortedMap(
    AbilityType.strength     -> strength,
    AbilityType.dexterity    -> dexterity,
    AbilityType.constitution -> constitution,
    AbilityType.intelligence -> intelligence,
    AbilityType.wisdom       -> wisdom,
    AbilityType.charisma     -> charisma
  )

  def byType(abilityType: AbilityType): Ability =
    abilityType match {
      case AbilityType.strength     => strength
      case AbilityType.dexterity    => dexterity
      case AbilityType.constitution => constitution
      case AbilityType.intelligence => intelligence
      case AbilityType.wisdom       => wisdom
      case AbilityType.charisma     => charisma
    }

}

case class Background(name: String)

case class InventoryItem(
  name:     String,
  quantity: Int = 1
)

enum Condition {

  case blinded, charmed, deafened, frightened, grappled, incapacitated, invisible, paralyzed, petrified, poisoned,
    prone, restrained, stunned, unconscious, exhaustion

}

object DeathSave {

  val empty: DeathSave = DeathSave(0, 0, true)

}

case class DeathSave(
  fails:        Int,
  successes:    Int,
  isStabilized: Boolean = true
)

case class SpellSlots(
  level: Int,
  used:  Int,
  total: Int
)

case class Creature(
  name:         String,
  creatureType: MonsterId
)

object Wallet {

  val empty: Wallet = Wallet(0, 0, 0, 0, 0)

}
case class Wallet(
  pp: Long,
  gp: Long,
  ep: Long,
  sp: Long,
  cp: Long
) {

  def +(other: Wallet): Wallet = Wallet(pp + other.pp, gp + other.gp, ep + other.ep, sp + other.sp, cp + other.cp)

}

enum CreatureSize {

  case tiny, small, medium, large, huge, gargantuan, unknown

}

case class PhysicalCharacteristics(
  gender: Option[String] = None,
  age:    Option[Int] = None,
  hair:   Option[String] = None,
  eyes:   Option[String] = None,
  skin:   Option[String] = None,
  height: Option[String] = None,
  weight: Option[Int] = None,
  size:   CreatureSize = CreatureSize.medium
)

object Language {

  val common:      Language = Language("Common")
  val dwarvish:    Language = Language("Dwarvish")
  val elvish:      Language = Language("Elvish")
  val giant:       Language = Language("Giant")
  val gnomish:     Language = Language("Gnomish")
  val goblin:      Language = Language("Goblin")
  val halfling:    Language = Language("Halfling")
  val orc:         Language = Language("Orc")
  val abyssal:     Language = Language("Abyssal")
  val celestial:   Language = Language("Celestial")
  val draconic:    Language = Language("Draconic")
  val deep:        Language = Language("eech  Deep Speech")
  val infernal:    Language = Language("Infernal")
  val primordial:  Language = Language("Primordial")
  val auran:       Language = Language("Auran")
  val aquan:       Language = Language("Aquan")
  val ignan:       Language = Language("Ignan")
  val terran:      Language = Language("Terran")
  val sylvan:      Language = Language("Sylvan")
  val undercommon: Language = Language("Undercommon")
  val druidic:     Language = Language("Druidic")
  val thievesCant: Language = Language("Thieves' Cant")

  val all: Set[Language] = Set(
    common,
    dwarvish,
    elvish,
    giant,
    gnomish,
    goblin,
    halfling,
    orc,
    abyssal,
    celestial,
    draconic,
    deep,
    infernal,
    primordial,
    auran,
    aquan,
    ignan,
    terran,
    sylvan,
    undercommon,
    druidic,
    thievesCant
  )
  val standard: Set[Language] = Set(common, dwarvish, elvish, giant, gnomish, goblin, halfling, orc)
  val exotic: Set[Language] =
    Set(abyssal, celestial, draconic, deep, infernal, primordial, auran, aquan, ignan, terran, sylvan, undercommon)
  val other: Set[Language] = Set(druidic, thievesCant)

  def fromName(str: String): Language = all.find(_.name.equalsIgnoreCase(str)).getOrElse(Language(str))

}

enum SkillType(
  val name:        String,
  val abilityType: AbilityType
) {

  case acrobatics extends SkillType("Acrobatics", AbilityType.dexterity)
  case animalHandling extends SkillType("Animal Handling", AbilityType.wisdom)
  case arcana extends SkillType("Arcana", AbilityType.intelligence)
  case athletics extends SkillType("Athletics", AbilityType.strength)
  case deception extends SkillType("Deception", AbilityType.charisma)
  case history extends SkillType("History", AbilityType.intelligence)
  case insight extends SkillType("Insight", AbilityType.wisdom)
  case intimidation extends SkillType("Intimidation", AbilityType.charisma)
  case investigation extends SkillType("Investigation", AbilityType.intelligence)
  case medicine extends SkillType("Medicine", AbilityType.wisdom)
  case nature extends SkillType("Nature", AbilityType.intelligence)
  case perception extends SkillType("Perception", AbilityType.wisdom)
  case performance extends SkillType("Performance", AbilityType.charisma)
  case persuasion extends SkillType("Persuasion", AbilityType.charisma)
  case religion extends SkillType("Religion", AbilityType.intelligence)
  case sleightOfHand extends SkillType("Sleigh of Hand", AbilityType.dexterity)
  case stealth extends SkillType("Stealth", AbilityType.dexterity)
  case survival extends SkillType("Survival", AbilityType.wisdom)

}

enum AdvantageDisadvantage {

  case neither, advantage, disadvantage

}

object ProficiencyLevel {

  given CanEqual[ProficiencyLevel, ProficiencyLevel] = CanEqual.derived

}
enum ProficiencyLevel {

  case none, half, proficient, expert

  def profStr: String =
    this match {
      case ProficiencyLevel.none       => ""
      case ProficiencyLevel.half       => "½"
      case ProficiencyLevel.proficient => "*"
      case ProficiencyLevel.expert     => "**"
    }

}

case class Skill(
  skillType:        SkillType,
  proficiencyLevel: ProficiencyLevel = ProficiencyLevel.none,
  advantage:        AdvantageDisadvantage = AdvantageDisadvantage.neither
) {

  def modifier(abilities: Abilities): Int = {
    val base = abilities.byType(skillType.abilityType).modifier
    proficiencyLevel match {
      case ProficiencyLevel.none       => base
      case ProficiencyLevel.half       => Math.floor(base / 2.0).toInt
      case ProficiencyLevel.proficient => base + 2
      case ProficiencyLevel.expert     => base * 2
    }

  }

  def modifierString(abilities: Abilities): String = {
    val m = modifier(abilities)
    (if (m <= 0) ""
     else "+") + m.toString
  }

}

case class Skills(
  acrobatics:     Skill = Skill(SkillType.acrobatics),
  animalHandling: Skill = Skill(SkillType.animalHandling),
  arcana:         Skill = Skill(SkillType.arcana),
  athletics:      Skill = Skill(SkillType.athletics),
  deception:      Skill = Skill(SkillType.deception),
  history:        Skill = Skill(SkillType.history),
  insight:        Skill = Skill(SkillType.insight),
  intimidation:   Skill = Skill(SkillType.intimidation),
  investigation:  Skill = Skill(SkillType.investigation),
  medicine:       Skill = Skill(SkillType.medicine),
  nature:         Skill = Skill(SkillType.nature),
  perception:     Skill = Skill(SkillType.perception),
  performance:    Skill = Skill(SkillType.performance),
  persuasion:     Skill = Skill(SkillType.persuasion),
  religion:       Skill = Skill(SkillType.religion),
  sleightOfHand:  Skill = Skill(SkillType.sleightOfHand),
  stealth:        Skill = Skill(SkillType.stealth),
  survival:       Skill = Skill(SkillType.survival)
) {

  def all =
    List(
      acrobatics,
      animalHandling,
      arcana,
      athletics,
      deception,
      history,
      insight,
      intimidation,
      investigation,
      medicine,
      nature,
      perception,
      performance,
      persuasion,
      religion,
      sleightOfHand,
      stealth,
      survival
    )

}

case class Language(name: String)

final private def deadColor(transparency:              Double) = s"hsl(0, 100%, 25%, $transparency)"
final private def deadButStabilizedColor(transparency: Double) = s"hsl(300, 100%, 25%, $transparency)"

case class Health(
  deathSave:            DeathSave,
  currentHitPoints:     Int,
  maxHitPoints:         Int,
  overrideMaxHitPoints: Option[Int] = None,
  temporaryHitPoints:   Int = 0
) {

  def currentMax: Int = overrideMaxHitPoints.getOrElse(maxHitPoints)
  def lifeColor(transparency: Double = 0.8): String =
    if (currentHitPoints <= 0)
      if (deathSave.isStabilized) deadButStabilizedColor(transparency) else deadColor(transparency)
    else s"hsl(${Math.min((currentHitPoints * 120.0) / currentMax, 120)}, 85%, 50%, $transparency)"

  def isDead: Boolean = currentHitPoints <= 0

}

enum SpeedType {

  case walk, fly, climb, swim, burrow

}

case class Speed(
  speedType: SpeedType,
  value:     Int
)
