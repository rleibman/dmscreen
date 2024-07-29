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

trait CharacterInfo {

  def health:                  Health
  def armorClass:              Int
  def classes:                 List[PlayerCharacterClass]
  def physicalCharacteristics: PhysicalCharacteristics
  def faith:                   Option[String]
  def overrideInitiative:      Option[Int]
  def currentXp:               Option[Long]
  def alignment:               Alignment
  def lifestyle:               Lifestyle
  def abilities:               Abilities
  def skills:                  Skills
  def background:              Option[Background]
  def race:                    Race
  def traits:                  Traits
  def inventory:               List[InventoryItem]
  def wallet:                  Wallet
  def feats:                   List[Feat]
  def conditions:              Set[Condition]
  def spellSlots:              List[SpellSlots]
  def pactMagic:               List[SpellSlots]
  def languages:               Set[Language]
  def actions:                 List[Action]
  def classSpells:             List[SpellHeader]
  def creatures:               List[Creature]
  def notes:                   String
  def speeds:                  List[Speed]
  def senses:                  List[SenseRange]

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

  def spellDC: Seq[(Ability, Int)] = {
    def calc(ability: Ability) = 8 + Math.floor((ability.value - 10) / 2.0).toInt + proficiencyBonus

    classes.map { clazz =>
      clazz match {
        case PlayerCharacterClass(CharacterClassId.artificer, _, _) |
            PlayerCharacterClass(CharacterClassId.`blood hunter`, _, _) |
            PlayerCharacterClass(CharacterClassId.wizard, _, _) |
            PlayerCharacterClass(CharacterClassId.fighter, Some(SubClass("eldritch knight")), _) |
            PlayerCharacterClass(CharacterClassId.rogue, Some(SubClass("arcane trickster")), _) =>
          Some((abilities.intelligence, calc(abilities.intelligence)))

        case PlayerCharacterClass(CharacterClassId.cleric, _, _) | PlayerCharacterClass(CharacterClassId.druid, _, _) |
            PlayerCharacterClass(CharacterClassId.ranger, _, _) | PlayerCharacterClass(CharacterClassId.monk, _, _) =>
          Some((abilities.wisdom, calc(abilities.wisdom)))

        case PlayerCharacterClass(CharacterClassId.bard, _, _) | PlayerCharacterClass(CharacterClassId.sorcerer, _, _) |
            PlayerCharacterClass(CharacterClassId.paladin, _, _) |
            PlayerCharacterClass(CharacterClassId.warlock, _, _) =>
          Some((abilities.charisma, calc(abilities.charisma)))

        case _ => None // Class/subclass is not a spellcaster
      }
    }.flatten
  }

}
