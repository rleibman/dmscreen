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
  cr:               Double,
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
