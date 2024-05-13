/*
 * Copyright 2020 Roberto Leibman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dmscreen.dnd5e

opaque type MonsterId = Long

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
  monsterType:    MonsterType,
  biome:          Biome,
  alignment:      Alignment,
  cr:             Double,
  xp:             Int,
  ac:             Int,
  hp:             Int,
  walkingSpeed:   Option[Int],
  burrowingSpeed: Option[Int],
  climbingSpeed:  Option[Int],
  flyingSpeed:    Option[Int],
  swimmingSpeed:  Option[Int],
  abilities:      List[Ability],
  languages:      String,
  challenge:      String,
  traits:         String,
  actions:        String,
  reactions:      String
)

case class MonsterHeader(id: MonsterId)

case class Monster(
  header: MonsterHeader,
  info:   MonsterInfo
)
