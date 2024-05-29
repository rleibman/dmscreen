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
import dmscreen.{CampaignId, DMScreenEntity, EntityType, HasId}
import zio.json.ast.Json

import java.net.{URI, URL}

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
  playerName: Option[String]
) extends HasId[PlayerCharacterId]

sealed trait ImportSource

case object DMScreenSource extends ImportSource

sealed case class DNDBeyondImportSource(
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

enum CharacterClassId {

  case barbarian, bard, cleric, druid, fighter, monk, paladin, ranger, rogue, sorcerer, warlock, wizard, `blood hunter`

}

case class CharacterClass(
  id:      CharacterClassId,
  hitDice: String
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
  subclass:       Option[Subclass] = None,
  level:          Int = 1
)

case class Subclass(name: String)

case class Feat(name: String)

enum Lifestyle {

  case wretched, squalid, poor, modest, comfortable, wealthy, aristocratic

}

enum Alignment {

  case lawfulGood, neutralGood, chaoticGood, lawfulNeutral, trueNeutral, chaoticNeutral, lawfulEvil, neutralEvil,
    chaoticEvil

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

  // To determine an ability modifier without consulting the table,
  // subtract 10 from the ability score and then divide the total by 2 (round down).
  def modifier: Int = (overrideValue.getOrElse(value) - 10) / 2
  // Savings throws are calculated by adding the proficiency bonus to the modifier
  def savingThrow(proficiencyBonus: Int): Int = modifier + (if (isProficient) proficiencyBonus else 0)

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
    prone, restrained, stunned, unconscious

}

case class DeathSave(
  fails:        Int,
  successes:    Int,
  isStabilized: Boolean
)

case class SpellSlots(
  level:     Int,
  used:      Int,
  available: Int
)

case class Action(str: String)

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

  val all: Seq[Language] = List(
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
  val standard: Seq[Language] = List(common, dwarvish, elvish, giant, gnomish, goblin, halfling, orc)
  val exotic: Seq[Language] =
    List(abyssal, celestial, draconic, deep, infernal, primordial, auran, aquan, ignan, terran, sylvan, undercommon)
  val other: Seq[Language] = List(druidic, thievesCant)

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

case class Skill(
  skillType:   SkillType,
  proficiency: Boolean = false,
  advantage:   AdvantageDisadvantage = AdvantageDisadvantage.neither
) {

  def modifier(abilities: Abilities): Int = {
    abilities.byType(skillType.abilityType).modifier + (if (proficiency) 2 else 0)
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

case class HitPoints(
  currentHitPoints:     Either[DeathSave, Int],
  maxHitPoints:         Int,
  overrideMaxHitPoints: Option[Int] = None,
  temporaryHitPoints:   Option[Int] = None
)

case class PlayerCharacterInfo(
  hitPoints:               HitPoints,
  armorClass:              Int,
  source:                  ImportSource = DMScreenSource,
  physicalCharacteristics: PhysicalCharacteristics = PhysicalCharacteristics(),
  faith:                   Option[String] = None,
  inspiration:             Boolean = false,
  currentXp:               Option[Int] = None,
  alignment:               Alignment = Alignment.trueNeutral,
  lifestyle:               Lifestyle = Lifestyle.modest,
  abilities:               Abilities = Abilities(),
  skills:                  Skills = Skills(),
  background:              Option[Background] = None,
  race:                    Race = Race("Human"),
  traits:                  Traits = Traits(),
  inventory:               List[InventoryItem] = List.empty,
  wallet:                  Wallet = Wallet.empty,
  classes:                 List[PlayerCharacterClass],
  feats:                   List[Feat] = List.empty,
  conditions:              List[Condition] = List.empty,
  spellSlots:              List[SpellSlots] = List.empty,
  pactMagic:               List[SpellSlots] = List.empty,
  languages:               List[Language] = List(Language.common),
  actions:                 List[Action] = List.empty,
  classSpells:             List[SpellHeader] = List.empty,
  creatures:               List[Creature] = List.empty,
  notes:                   String = "",
  senses:                  Seq[SenseRange] = Seq(SenseRange(Sense.sight, 10560)) // Normal sight, 2 miles
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

}

case class PlayerCharacter(
  header:   PlayerCharacterHeader,
  jsonInfo: Json
) extends DMScreenEntity[PlayerCharacterId, PlayerCharacterHeader, PlayerCharacterInfo] {

  override def entityType: EntityType = DND5eEntityType.playerCharacter

}
