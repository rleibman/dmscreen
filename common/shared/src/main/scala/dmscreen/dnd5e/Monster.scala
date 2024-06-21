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
import zio.json.ast.Json

opaque type MonsterId = Long

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

object MonsterId {

  def empty: MonsterId = MonsterId(0)

  def apply(monsterId: Long): MonsterId = monsterId

  extension (monsterId: MonsterId) {

    def value: Long = monsterId

  }

}

object ChallengeRating {

  def fromDouble(value: Double): Option[ChallengeRating] = ChallengeRating.values.find(_.value == value)

}

enum ChallengeRating(val value: Double) {

  case `0` extends ChallengeRating(0.0)
  case `1/8` extends ChallengeRating(0.125)
  case `1/4` extends ChallengeRating(0.25)
  case `1/2` extends ChallengeRating(0.5)
  case `1` extends ChallengeRating(1.0)
  case `2` extends ChallengeRating(2.0)
  case `3` extends ChallengeRating(3.0)
  case `4` extends ChallengeRating(4.0)
  case `5` extends ChallengeRating(5.0)
  case `6` extends ChallengeRating(6.0)
  case `7` extends ChallengeRating(7.0)
  case `8` extends ChallengeRating(8.0)
  case `9` extends ChallengeRating(9.0)
  case `10` extends ChallengeRating(10.0)
  case `11` extends ChallengeRating(11.0)
  case `12` extends ChallengeRating(12.0)
  case `13` extends ChallengeRating(13.0)
  case `14` extends ChallengeRating(14.0)
  case `15` extends ChallengeRating(15.0)
  case `16` extends ChallengeRating(16.0)
  case `17` extends ChallengeRating(17.0)
  case `18` extends ChallengeRating(18.0)
  case `19` extends ChallengeRating(19.0)
  case `20` extends ChallengeRating(20.0)
  case `21` extends ChallengeRating(21.0)
  case `22` extends ChallengeRating(22.0)
  case `23` extends ChallengeRating(23.0)
  case `24` extends ChallengeRating(24.0)
  case `25` extends ChallengeRating(25.0)
  case `26` extends ChallengeRating(26.0)
  case `27` extends ChallengeRating(27.0)
  case `28` extends ChallengeRating(28.0)
  case `29` extends ChallengeRating(29.0)
  case `30` extends ChallengeRating(30.0)

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

}

case class MonsterInfo(
  abilities: Abilities,
  hitDice:   Option[String] = None,
  speeds:    Seq[Speed] = Seq.empty,
  languages: Seq[Language] = Seq.empty,
  actions:   Seq[String] = Seq.empty,
  reactions: Seq[String] = Seq.empty,
  senses:    Seq[SenseRange] = Seq.empty
) {

  def initiativeBonus: Int = abilities.dexterity.modifier

}

case class MonsterHeader(
  id:               MonsterId,
  name:             String,
  monsterType:      MonsterType,
  biome:            Option[Biome],
  alignment:        Option[Alignment],
  cr:               ChallengeRating,
  xp:               Long,
  armorClass:       Int,
  maximumHitPoints: Int,
  size:             CreatureSize
) extends HasId[MonsterId]

case class Monster(
  header:               MonsterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[MonsterId, MonsterHeader, MonsterInfo] {

  override val entityType: EntityType = DND5eEntityType.monster

}

//Import monsters:
// - the srd, from https://github.com/5e-bits/5e-database?tab=readme-ov-file
// - A fairly complete list, but many monsters only have limited data in https://docs.google.com/spreadsheets/d/1-xWeXO3lh_aIUsFTLOfOCFQm-OKawrJHs3xsGikgOXA/edit#gid=1903101837
// - the more complete list, but with even less data in https://docs.google.com/spreadsheets/d/1sccR7HgoHGB0Mq24k5K-O-tqJZG8D3Z66Ig7UloukNY/edit#gid=2073157549
