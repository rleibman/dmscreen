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
import dmscreen.dnd5e.ChallengeRating.`1/2`
import just.semver.SemVer
import zio.json.*
import zio.json.ast.*

enum Sense(val name: String) {

  case sight extends Sense("Sight")
  case blindsight extends Sense("Blindsight")
  case darkvision extends Sense("Darkvision")
  case tremorsense extends Sense("Tremorsense")
  case truesight extends Sense("Truesight")
  case scent extends Sense("Scent")
  case other extends Sense("Other")

}

case class SenseRange(
  sense: Sense,
  range: Int
)

opaque type MonsterId = Long

object MonsterId {

  def empty: MonsterId = MonsterId(0)

  def apply(monsterId: Long): MonsterId = monsterId

  extension (monsterId: MonsterId) {

    def value: Long = monsterId

  }

}

object ChallengeRating {

  def fromString(value: String): ChallengeRating = {
    value match {
      case "0.125" => `1/8`
      case "0.25"  => `1/4`
      case "0.5"   => `1/2`
      case "0"     => `0`
      case s       => valueOf(s)
    }
  }
  def fromDouble(value: Double): Option[ChallengeRating] = ChallengeRating.values.find(_.value == value)
  def fromXP(xp:        Long):   Option[ChallengeRating] = ChallengeRating.values.filter(_.xp <= xp).lastOption

}

enum ChallengeRating(
  val value: Double,
  val xp:    Long
) {

  case `0` extends ChallengeRating(0.0, 10)
  case `1/8` extends ChallengeRating(0.125, 25)
  case `1/4` extends ChallengeRating(0.25, 50)
  case `1/2` extends ChallengeRating(0.5, 100)
  case `1` extends ChallengeRating(1.0, 200)
  case `2` extends ChallengeRating(2.0, 450)
  case `3` extends ChallengeRating(3.0, 700)
  case `4` extends ChallengeRating(4.0, 1100)
  case `5` extends ChallengeRating(5.0, 1800)
  case `6` extends ChallengeRating(6.0, 2300)
  case `7` extends ChallengeRating(7.0, 2900)
  case `8` extends ChallengeRating(8.0, 3900)
  case `9` extends ChallengeRating(9.0, 5000)
  case `10` extends ChallengeRating(10.0, 5900)
  case `11` extends ChallengeRating(11.0, 7200)
  case `12` extends ChallengeRating(12.0, 8400)
  case `13` extends ChallengeRating(13.0, 10000)
  case `14` extends ChallengeRating(14.0, 11500)
  case `15` extends ChallengeRating(15.0, 13000)
  case `16` extends ChallengeRating(16.0, 15000)
  case `17` extends ChallengeRating(17.0, 18000)
  case `18` extends ChallengeRating(18.0, 20000)
  case `19` extends ChallengeRating(19.0, 22000)
  case `20` extends ChallengeRating(20.0, 25000)
  case `21` extends ChallengeRating(21.0, 33000)
  case `22` extends ChallengeRating(22.0, 41000)
  case `23` extends ChallengeRating(23.0, 50000)
  case `24` extends ChallengeRating(24.0, 62000)
  case `25` extends ChallengeRating(25.0, 75000)
  case `26` extends ChallengeRating(26.0, 90000)
  case `27` extends ChallengeRating(27.0, 105000)
  case `28` extends ChallengeRating(28.0, 120000)
  case `29` extends ChallengeRating(29.0, 135000)
  case `30` extends ChallengeRating(30.0, 155000)

}

enum MonsterType {

  case Aberration
  case Beast
  case Celestial
  case Construct
  case Dragon
  case Elemental
  case Fey
  case Fiend
  case Giant
  case Humanoid
  case Monstrosity
  case Ooze
  case Plant
  case Undead
  case Swarm
  case Unknown

}

enum Biome {

  case Arctic
  case Coastal
  case Desert
  case Forest
  case Grassland
  case Hill
  case Mountain
  case Swamp
  case Underdark
  case Underwater
  case Urban
  case Unknown
  case Unimportant

}

case class ActionDC(
  dcType:  AbilityType,
  dcValue: Int
)

case class ActionDamage(
  damageType: DamageType,
  damageDice: DiceRoll
)

enum ActionType {

  case Melee
  case Ranged
  case Spell
  case Ability

}

sealed trait Action {

  def name:        String
  def description: Option[String]

}

case class SingleAction(
  actionType:               ActionType,
  override val name:        String,
  override val description: Option[String],
  attackBonus:              Option[Int] = None,
  damage:                   Option[DiceRoll] = None,
  dc:                       Option[ActionDC] = None
) extends Action

case class MultiAction(
  override val name:        String,
  override val description: Option[String],
  actions:                  Seq[SingleAction]
) extends Action

object DamageType {

  val acid:        DamageType = DamageType("acid")
  val bludgeoning: DamageType = DamageType("bludgeoning")
  val cold:        DamageType = DamageType("cold")
  val fire:        DamageType = DamageType("fire")
  val force:       DamageType = DamageType("force")
  val lightning:   DamageType = DamageType("lightning")
  val necrotic:    DamageType = DamageType("necrotic")
  val piercing:    DamageType = DamageType("piercing")
  val poison:      DamageType = DamageType("poison")
  val psychic:     DamageType = DamageType("psychic")
  val radiant:     DamageType = DamageType("radiant")
  val slashing:    DamageType = DamageType("slashing")
  val thunder:     DamageType = DamageType("thunder")
  val values = Seq(
    acid,
    bludgeoning,
    cold,
    fire,
    force,
    lightning,
    necrotic,
    piercing,
    poison,
    psychic,
    radiant,
    slashing,
    thunder
  )

  def valueOf(name: String): DamageType = values.find(_.description.equalsIgnoreCase(name)).getOrElse(DamageType(name))

}

case class DamageType(description: String) {

  override def toString: String = description

}

case class SpecialAbility(
  name:        String,
  description: Option[String]
)

case class MonsterInfo(
  abilities:             Abilities,
  hitDice:               Option[DiceRoll] = None,
  languages:             Set[Language] = Set.empty,
  actions:               List[Action] = List.empty,
  reactions:             List[Action] = List.empty,
  legendaryActions:      List[Action] = List.empty,
  speeds:                List[Speed] = List.empty,
  senses:                List[SenseRange] = List.empty,
  conditionImmunities:   Seq[Condition] = Seq.empty,
  damageVulnerabilities: Seq[DamageType] = Seq.empty,
  damageResistances:     Seq[DamageType] = Seq.empty,
  damageImmunities:      Seq[DamageType] = Seq.empty,
  specialAbilities:      Seq[SpecialAbility] = Seq.empty,
  proficiencyBonus:      Int = 0,
  notes:                 String = ""
) {

  def initiativeBonus: Int = abilities.dexterity.modifier

}

case class MonsterHeader(
  id:               MonsterId,
  sourceId:         SourceId = SourceId("unknown"),
  name:             String,
  monsterType:      MonsterType,
  biome:            Option[Biome],
  alignment:        Option[Alignment],
  cr:               ChallengeRating,
  xp:               Long,
  armorClass:       Int,
  maximumHitPoints: Int,
  size:             CreatureSize,
  initiativeBonus:  Int
) extends HasId[MonsterId]

object Monster {

  def homeBrew: Monster =
    Monster(
      MonsterHeader(
        id = MonsterId.empty,
        sourceId = SourceId.homebrew,
        name = "",
        monsterType = MonsterType.Unknown,
        biome = None,
        alignment = None,
        cr = ChallengeRating.`1/2`,
        xp = 100,
        armorClass = 10,
        maximumHitPoints = 10,
        size = CreatureSize.medium,
        initiativeBonus = 0
      ),
      jsonInfo = MonsterInfo(
        abilities = Abilities()
      ).toJsonAST.toOption.get
    )

}

case class Monster(
  header:               MonsterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[MonsterId, MonsterHeader, MonsterInfo] {

  override val entityType: EntityType[MonsterId] = DND5eEntityType.monster

}

//Import monsters:
// - the srd, from https://github.com/5e-bits/5e-database?tab=readme-ov-file
// - A fairly complete list, but many monsters only have limited data in https://docs.google.com/spreadsheets/d/1-xWeXO3lh_aIUsFTLOfOCFQm-OKawrJHs3xsGikgOXA/edit#gid=1903101837
// - the more complete list, but with even less data in https://docs.google.com/spreadsheets/d/1sccR7HgoHGB0Mq24k5K-O-tqJZG8D3Z66Ig7UloukNY/edit#gid=2073157549
