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

import java.net.URL

opaque type PlayerCharacterId = Long

case class PlayerCharacterHeader(id: PlayerCharacterId)

sealed trait ImportSource

case class DNDBeyondImportSource(
  url: URL
) extends ImportSource

case class Traits(
  personalityTraits: String,
  ideals:            String,
  bonds:             String,
  flaws:             String,
  appearance:        String
)

case class Race(name: String)

case class CharacterClass(name: String)

case class Subclass(name: String)

case class Feat()

case class Lifestyle()

enum Alignment {

  case LawfulGood, NeutralGood, ChaoticGood, LawfulNeutral, TrueNeutral, ChaoticNeutral, LawfulEvil, NeutralEvil,
    ChaoticEvil

}

enum AbilityType(name: String) {

  case Strength extends AbilityType("Strength")
  case Dexterity extends AbilityType("Dexterity")
  case Constitution extends AbilityType("Constitution")
  case Intelligence extends AbilityType("Intelligence")
  case Wisdom extends AbilityType("Wisdom")
  case Charisma extends AbilityType("Charisma")

}

case class Ability(abilityType: AbilityType)

case class Background(name: String)

case class InventoryItem(name: String)

enum Condition {

  case Blinded, Charmed, Deafened, Frightened, Grappled, Incapacitated, Invisible, Paralyzed, Petrified, Poisoned,
    Prone, Restrained, Stunned, Unconscious

}

case class DeathSave()

case class SpellSlot()

case class Options()

case class Choices()

case class Actions()

case class Modifiers()

case class Spell()

case class Creature(
  name:         String,
  creatureType: MonsterId
)

case class Wallet(
  pp: Long,
  gp: Long,
  ep: Long,
  sp: Long,
  cp: Long
)

enum Size {

  case Tiny, Small, Medium, Large, Huge, Gargantuan

}

case class PhysicalCharacteristics(
  gender: String,
  age:    Int,
  hair:   String,
  eyes:   String,
  skin:   String,
  height: String,
  weight: Int,
  size:   Size
)

case class PlayerCharacterInfo(
  id:                      Long,
  source:                  ImportSource,
  name:                    String,
  physicalCharacteristics: PhysicalCharacteristics,
  faith:                   String,
  inspiration:             Boolean,
  baseHitPoints:           Int,
  bonusHitPoints:          String,
  overrideHitPoints:       String,
  removedHitPoints:        Int,
  temporaryHitPoints:      Int,
  currentXp:               Int,
  armorClass:              Int,
  alignment:               Alignment,
  lifestyle:               Lifestyle,
  abilities:               List[Ability],
  background:              Background,
  race:                    Race,
  traits:                  Traits,
  inventory:               List[InventoryItem],
  wallet:                  Wallet,
  classes:                 List[CharacterClass],
  feats:                   List[Feat],
  conditions:              List[Condition],
  deathSaves:              DeathSave,
  adjustmentXp:            String,
  spellSlots:              List[SpellSlot],
  pactMagic:               List[SpellSlot],
  options:                 Options,
  choices:                 Choices,
  actions:                 Actions,
  modifiers:               Modifiers,
  classSpells:             List[Spell],
  creatures:               List[Creature],
  notes:                   String
)

case class PlayerCharacter(
  header: PlayerCharacterHeader,
  info:   PlayerCharacterInfo
)
