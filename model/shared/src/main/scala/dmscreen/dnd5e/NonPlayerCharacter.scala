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

import dmscreen.*
import just.semver.SemVer
import zio.json.*
import zio.json.ast.Json

case class StatBlock()

opaque type NonPlayerCharacterId = Long

object NonPlayerCharacterId {

  val empty: NonPlayerCharacterId = 0L

  def apply(value: Long): NonPlayerCharacterId = value

  extension (npcId: NonPlayerCharacterId) {

    def value: Long = npcId

  }

  given CanEqual[NonPlayerCharacterId, NonPlayerCharacterId] = CanEqual.derived

}

enum RelationToPlayers {

  case neutral, enemy, ally, unknown, itsComplicated

}

case class NonPlayerCharacterHeader(
  id:         NonPlayerCharacterId,
  campaignId: CampaignId,
  name:       String
) extends HasId[NonPlayerCharacterId]

case class NonPlayerCharacterInfo(
  health:                             Health,
  armorClass:                         Int,
  classes:                            List[PlayerCharacterClass],
  physicalCharacteristics:            PhysicalCharacteristics = PhysicalCharacteristics(),
  faith:                              Option[String] = None,
  overrideInitiative:                 Option[Int] = None,
  currentXp:                          Option[Long] = None,
  alignment:                          Alignment = Alignment.trueNeutral,
  lifestyle:                          Lifestyle = Lifestyle.modest,
  abilities:                          Abilities = Abilities(),
  skills:                             Skills = Skills(),
  background:                         Option[Background] = None,
  race:                               Race = Race("Human"),
  size:                               CreatureSize = CreatureSize.medium,
  inventory:                          List[InventoryItem] = List.empty,
  wallet:                             Wallet = Wallet.empty,
  feats:                              List[Feat] = List.empty,
  conditions:                         Set[Condition] = Set.empty,
  spellSlots:                         List[SpellSlots] = List.empty,
  pactMagic:                          List[SpellSlots] = List.empty,
  languages:                          Set[Language] = Set(Language.common),
  actions:                            List[Action] = List.empty,
  classSpells:                        List[SpellHeader] = List.empty,
  creatures:                          List[Creature] = List.empty,
  speeds:                             List[Speed] = List(Speed(SpeedType.walk, 30)),
  senses:                             List[SenseRange] = List(SenseRange(Sense.sight, 10560)), // Normal sight, 2 miles
  hair:                               String = "",
  skin:                               String = "",
  eyes:                               String = "",
  height:                             String = "",
  weight:                             String = "",
  age:                                String = "",
  gender:                             String = "",
  override val conditionImmunities:   Seq[Condition] = Seq.empty,
  override val damageVulnerabilities: Seq[DamageType] = Seq.empty,
  override val damageResistances:     Seq[DamageType] = Seq.empty,
  override val damageImmunities:      Seq[DamageType] = Seq.empty,
  notes:                              String = "",
  rollplayInfo:                       RollplayInfo = RollplayInfo(),
  monster:                            Option[MonsterId] = None,
  challengeRating:                    Option[ChallengeRating] = None,
  relationToPlayers:                  RelationToPlayers = RelationToPlayers.unknown, // TODO add this to editor
  traits:                             Traits = Traits(),
  organizations:                      String = "",
  allies:                             String = "",
  enemies:                            String = "",
  backstory:                          String = ""
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
  ): NonPlayerCharacterInfo = this.copy()

}

object RollplayInfo {

  given CanEqual[RollplayInfo, RollplayInfo] = CanEqual.derived

}

case class RollplayInfo(
  occupation:     String = "",
  personality:    String = "",
  ideal:          String = "",
  bond:           String = "",
  flaw:           String = "",
  characteristic: String = "",
  speech:         String = "",
  hobby:          String = "",
  fear:           String = "",
  currently:      String = "",
  nickname:       String = "",
  weapon:         String = "",
  rumor:          String = "",
  raisedBy:       String = "",
  parent1:        String = "",
  parent2:        String = "",
  siblingCount:   Int = 0,
  childhood:      String = "",
  children:       String = "",
  spouse:         String = ""
) {

  def isEmpty: Boolean = this == RollplayInfo()

}

case class NonPlayerCharacter(
  header:               NonPlayerCharacterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[NonPlayerCharacterId, NonPlayerCharacterHeader, NonPlayerCharacterInfo] {

  override val entityType: EntityType[NonPlayerCharacterId] = DND5eEntityType.nonPlayerCharacter

}
