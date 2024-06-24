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

import dmscreen.{BuildInfo, CampaignId, DMScreenEntity, EntityType, HasId}
import just.semver.SemVer
import zio.json.ast.Json

opaque type CreatureId = Long

object CreatureId {

  def empty: CreatureId = CreatureId(0)

  def apply(creatureId: Long): CreatureId = creatureId

  extension (creatureId: CreatureId) {

    def value: Long = creatureId

  }

}

sealed abstract class EncounterCreature(
) {

  def id: CreatureId

  def notes: String

  def initiativeBonus: Int

  def initiative: Int

  def otherMarkers: List[Marker]

}

case class MonsterEncounterCreature(
  override val id:              CreatureId,
  monsterHeader:                MonsterHeader,
  override val notes:           String,
  hitPoints:                    HitPoints,
  armorClass:                   Int,
  override val initiative:      Int,
  conditions:                   Set[Condition],
  override val otherMarkers:    List[Marker],
  name:                         String,
  override val initiativeBonus: Int
) extends EncounterCreature()

/** Used for things like Hunter's Mark, Hex, Hidden, concentration, etc.
  */
case class Marker(name: String)

case class PlayerCharacterEncounterCreature(
  override val id:              CreatureId,
  playerCharacterId:            PlayerCharacterId,
  override val notes:           String,
  override val initiative:      Int,
  override val otherMarkers:    List[Marker],
  override val initiativeBonus: Int
) extends EncounterCreature() {}

opaque type EncounterId = Long

object EncounterId {

  val empty: EncounterId = EncounterId(0)

  def apply(encounterId: Long): EncounterId = encounterId

  extension (encounterId: EncounterId) {

    def value: Long = encounterId

  }

}

enum EncounterDifficulty {

  case Easy
  case Medium
  case Hard
  case Deadly

}

enum EncounterStatus(val name: String) {

  case active extends EncounterStatus("Active")
  case planned extends EncounterStatus("Planned")
  case archived extends EncounterStatus("Archived")

}

case class EncounterHeader(
  id:         EncounterId,
  campaignId: CampaignId,
  name:       String,
  status:     EncounterStatus,
  sceneId:    Option[SceneId],
  order:      Int
) extends HasId[EncounterId]

case class EncounterInfo(
  creatures:   List[EncounterCreature],
  currentTurn: Int = 0,
  round:       Int = 0,
  notes:       String = ""
) {

  lazy val difficulty: EncounterDifficulty = EncounterDifficulty.Hard // TODO calculate
  lazy val xp = creatures.collect { case m: MonsterEncounterCreature =>
    m.monsterHeader.xp
  }.sum

  def monsters: List[MonsterEncounterCreature] = creatures.collect { case m: MonsterEncounterCreature => m }

}

case class Encounter(
  header:               EncounterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[EncounterId, EncounterHeader, EncounterInfo] {

  override def entityType: EntityType = DND5eEntityType.encounter

}
