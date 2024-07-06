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

import dmscreen.dnd5e.CreatureSize.medium
import dmscreen.{CampaignId, DMScreenEntity, DiceRoll, EntityType, HasId}
import just.semver.SemVer
import zio.json.ast.Json

import java.net.URI
import scala.collection.SortedMap

opaque type PlayerCharacterId = Long

object PlayerCharacterId {

  val empty: PlayerCharacterId = PlayerCharacterId(0)

  def apply(playerCharacterId: Long): PlayerCharacterId = playerCharacterId

  extension (playerCharacterId: PlayerCharacterId) {

    def value: Long = playerCharacterId

  }

}

final case class PlayerCharacterHeader(
  id:         PlayerCharacterId,
  campaignId: CampaignId,
  name:       String, // TODO make it optional
  playerName: Option[String] = None
) extends HasId[PlayerCharacterId]

sealed trait ImportSource

case object DMScreenSource extends ImportSource

sealed case class DNDBeyondImportSource(
  uri: URI
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

case class CharacterClass(
  id:      CharacterClassId,
  hitDice: DiceRoll
)

opaque type SourceId = String

object SourceId {

  def apply(sourceId: String): SourceId = sourceId

  extension (sourceId: SourceId) {

    def value: String = sourceId

  }

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

  case wretched, squalid, poor, modest, comfortable, wealthy, aristocratic

}

enum Alignment(val name: String) {

  case lawfulGood extends Alignment("Lawful Good")
  case neutralGood extends Alignment("Neutral Good")
  case chaoticGood extends Alignment("Chaotic Good")
  case lawfulNeutral extends Alignment("Lawful Neutral")
  case trueNeutral extends Alignment("True Neutral")
  case chaoticNeutral extends Alignment("Chaotic Neutral")
  case lawfulEvil extends Alignment("Lawful Evil")
  case neutralEvil extends Alignment("Neutral Evil")
  case chaoticEvil extends Alignment("Chaotic Evil")
  case unaligned extends Alignment("Unaligned")

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

case class DeathSave(
  fails:        Int,
  successes:    Int,
  isStabilized: Boolean = false
)

case class SpellSlots(
  level:     Int,
  used:      Int,
  available: Int
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
)

enum CreatureSize {

  case tiny, small, medium, large, huge, gargantuan

}

case class PhysicalCharacteristics(
  gender: Option[String] = None,
  age:    Option[Int] = None,
  hair:   Option[String] = None,
  eyes:   Option[String] = None,
  skin:   Option[String] = None,
  height: Option[String] = None,
  weight: Option[Int] = None,
  size:   CreatureSize = medium
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

enum SkillType(val abilityType: AbilityType) {

  case acrobatics extends SkillType(AbilityType.dexterity)
  case animalHandling extends SkillType(AbilityType.wisdom)
  case arcana extends SkillType(AbilityType.intelligence)
  case athletics extends SkillType(AbilityType.strength)
  case deception extends SkillType(AbilityType.charisma)
  case history extends SkillType(AbilityType.intelligence)
  case insight extends SkillType(AbilityType.wisdom)
  case intimidation extends SkillType(AbilityType.charisma)
  case investigation extends SkillType(AbilityType.intelligence)
  case medicine extends SkillType(AbilityType.wisdom)
  case nature extends SkillType(AbilityType.intelligence)
  case perception extends SkillType(AbilityType.wisdom)
  case performance extends SkillType(AbilityType.charisma)
  case persuasion extends SkillType(AbilityType.charisma)
  case religion extends SkillType(AbilityType.intelligence)
  case sleightOfHand extends SkillType(AbilityType.dexterity)
  case stealth extends SkillType(AbilityType.dexterity)
  case survival extends SkillType(AbilityType.wisdom)

}

enum AdvantageDisadvantage {

  case neither, advantage, disadvantage

}

enum ProficiencyLevel {

  case none, half, proficient, expert

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
)

case class Language(name: String)

object DeadDead

final private val deadColor = "hsl(0, 100%, 25%, 0.8)"

case class HitPoints(
  currentHitPoints:     DeathSave | Int, // TODO or DeadDead
  maxHitPoints:         Int,
  overrideMaxHitPoints: Option[Int] = None,
  temporaryHitPoints:   Int = 0
) {

  def currentMax: Int = overrideMaxHitPoints.getOrElse(maxHitPoints)
  def lifeColor: String =
    currentHitPoints match {
      case DeathSave(_, _, _) => deadColor
      case i: Int => if (i <= 0) deadColor else s"hsl(${(i * 120.0) / currentMax}, 85%, 50%, 0.8)"
    }

  def isDead: Boolean =
    currentHitPoints match {
      case DeathSave(_, _, _) => true
      case i: Int => i <= 0
    }

}

enum SpeedType {

  case walk, fly, climb, swim, burrow

}

case class Speed(
  speedType: SpeedType,
  value:     Int
)

case class PlayerCharacterInfo(
  hitPoints:               HitPoints,
  armorClass:              Int,
  classes:                 List[PlayerCharacterClass],
  source:                  ImportSource = DMScreenSource,
  physicalCharacteristics: PhysicalCharacteristics = PhysicalCharacteristics(),
  faith:                   Option[String] = None,
  inspiration:             Boolean = false,
  overrideInitiative:      Option[Int] = None,
  currentXp:               Option[Long] = None,
  alignment:               Alignment = Alignment.trueNeutral,
  lifestyle:               Lifestyle = Lifestyle.modest,
  abilities:               Abilities = Abilities(),
  skills:                  Skills = Skills(),
  background:              Option[Background] = None,
  race:                    Race = Race("Human"),
  traits:                  Traits = Traits(),
  inventory:               List[InventoryItem] = List.empty,
  wallet:                  Wallet = Wallet.empty,
  feats:                   List[Feat] = List.empty,
  conditions:              Set[Condition] = Set.empty,
  spellSlots:              List[SpellSlots] = List.empty,
  pactMagic:               List[SpellSlots] = List.empty,
  languages:               Set[Language] = Set(Language.common),
  actions:                 List[Action] = List.empty,
  classSpells:             List[SpellHeader] = List.empty,
  creatures:               List[Creature] = List.empty,
  notes:                   String = "",
  speeds:                  List[Speed] = List(Speed(SpeedType.walk, 30)),
  senses:                  List[SenseRange] = List(SenseRange(Sense.sight, 10560)) // Normal sight, 2 miles
) {

  /** This method will make a copy of this character info, but will auto calculate some of the fields based on the
    * options passed in.
    */
  def autoCalculate(
    spellSlots:       Boolean = false,
    armorClass:       Boolean = false,
    hitPoints:        Boolean = false,
    abilityModifiers: Boolean = false,
    skillModifiers:   Boolean = false,
    level:            Boolean = false
  ): PlayerCharacterInfo = this.copy()

  def totalLevel: Int = classes.map(_.level).sum

  def proficiencyBonus: Int = {
    val level = totalLevel
    Math.ceil((level / 4.0) + 1).toInt
  }

  def proficiencyBonusString: String = {
    val m = proficiencyBonus
    (if (m <= 0) ""
     else "+") + m.toString
  }

  def initiativeBonus: Int = abilities.dexterity.modifier

  def overridenInitiative: Int = overrideInitiative.getOrElse(abilities.dexterity.modifier)

  def initiativeBonusString: String = {
    val m = overridenInitiative
    (if (m <= 0) ""
     else "+") + m.toString
  }

  def passivePerception: Int = skills.perception.modifier(abilities) + 10

  def passiveInvestigation: Int = skills.investigation.modifier(abilities) + 10

  def passiveInsight: Int = skills.insight.modifier(abilities) + 10

}

case class PlayerCharacter(
  header:               PlayerCharacterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[PlayerCharacterId, PlayerCharacterHeader, PlayerCharacterInfo] {

  override def entityType: EntityType = DND5eEntityType.playerCharacter

}
