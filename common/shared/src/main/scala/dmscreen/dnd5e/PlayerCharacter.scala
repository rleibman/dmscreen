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
  name:       String,
  source:     ImportSource,
  playerName: Option[String] = None
) extends HasId[PlayerCharacterId]

case class PlayerCharacterInfo(
  health:                  Health,
  armorClass:              Int,
  classes:                 List[PlayerCharacterClass],
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
  size:                    CreatureSize = CreatureSize.medium,
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
) extends CharacterInfo {

  def initiativeTuple: (Int, Int, Int, Int, Int, Int) =
    (
      -initiativeBonus,
      -abilities.dexterity.value,
      -abilities.intelligence.value,
      -abilities.charisma.value,
      -abilities.strength.value,
      -abilities.constitution.value
    )

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
  header:               PlayerCharacterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[PlayerCharacterId, PlayerCharacterHeader, PlayerCharacterInfo] {

  override def entityType: EntityType[PlayerCharacterId] = DND5eEntityType.playerCharacter

}
