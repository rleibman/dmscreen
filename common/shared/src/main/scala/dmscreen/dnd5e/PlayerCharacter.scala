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

import dmscreen.{CampaignId, DMScreenEntity, EntityType, HasId}
import zio.json.ast.Json

import java.net.{URI, URL}

opaque type PlayerCharacterId = Long

object PlayerCharacterId {

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

sealed case class DNDBeyondImportSource(
  uri: URI
) extends ImportSource

case class Traits(
  personalityTraits: String,
  ideals:            String,
  bonds:             String,
  flaws:             String,
  appearance:        String
)

case class Race(name: String)

opaque type CharacterClassId = String

object CharacterClassId {

  def apply(characterClassId: String): CharacterClassId = characterClassId

  extension (characterClassId: CharacterClassId) {

    def value: String = characterClassId

  }

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
  name:     CharacterClassId,
  subclass: Subclass,
  level:    Int
)

case class Subclass(name: String)

case class Feat(name: String)

enum Lifestyle {

  case wretched, squalid, poor, modest, comfortable, wealthy, aristocratic

}

enum Alignment {

  case LawfulGood, NeutralGood, ChaoticGood, LawfulNeutral, TrueNeutral, ChaoticNeutral, LawfulEvil, NeutralEvil,
    ChaoticEvil

}

enum AbilityType(
  val name:    String,
  val short:   String,
  val shorter: String
) {

  case Strength extends AbilityType("Strength", "Str", "S")
  case Dexterity extends AbilityType("Dexterity", "Dex", "D")
  case Constitution extends AbilityType("Constitution", "Con", "C")
  case Intelligence extends AbilityType("Intelligence", "Int", "I")
  case Wisdom extends AbilityType("Wisdom", "Wis", "W")
  case Charisma extends AbilityType("Charisma", "Cha", "Ch")

}

case class Ability(
  abilityType: AbilityType,
  value:       Int,
  bonus:       Int,
  tempValue:   Option[Int],
  tempBonus:   Option[Int]
)

case class Background(name: String)

case class InventoryItem(name: String)

enum Condition {

  case Blinded, Charmed, Deafened, Frightened, Grappled, Incapacitated, Invisible, Paralyzed, Petrified, Poisoned,
    Prone, Restrained, Stunned, Unconscious

}

case class DeathSave(
  fails:     Int,
  successes: Int
)

case class SpellSlot(str: String)

case class Options(str: String)

case class Choices(str: String)

case class Actions(str: String)

case class Modifiers(str: String)

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

enum CreatureSize {

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
  size:   CreatureSize
)

case class PlayerCharacterInfo(
  id:                      Long,
  source:                  ImportSource,
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
  classes:                 List[PlayerCharacterClass],
  feats:                   List[Feat],
  conditions:              List[Condition],
  deathSaves:              DeathSave,
  adjustmentXp:            String,
  spellSlots:              List[SpellSlot],
  pactMagic:               List[SpellSlot],
  languages:               List[String],
  options:                 Options,
  choices:                 Choices,
  actions:                 Actions,
  modifiers:               Modifiers,
  classSpells:             List[SpellId],
  creatures:               List[Creature],
  notes:                   String,
  senses:                  Seq[SenseRange]
)

case class PlayerCharacter(
  header:   PlayerCharacterHeader,
  jsonInfo: Json
) extends DMScreenEntity[PlayerCharacterId, PlayerCharacterHeader, PlayerCharacterInfo] {

  override def entityType: EntityType = DND5eEntityType.playerCharacter

}
