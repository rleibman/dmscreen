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

import java.util.UUID

opaque type CombatantId = UUID

object CombatantId {

  def create: CombatantId = CombatantId(UUID.randomUUID)

  def apply(combatantId: UUID):   CombatantId = combatantId
  def apply(combatantId: String): CombatantId = UUID.fromString(combatantId)

  extension (combatantId: CombatantId) {

    def value: UUID = combatantId

  }

}

sealed abstract class EncounterCombatant(
) {

  def name: String

  def id: CombatantId

  def notes: String

  def initiativeBonus: Int

  def initiative: Int

  def otherMarkers: List[Marker]

  def isNPC: Boolean

}

final case class MonsterCombatant(
  override val id:              CombatantId,
  monsterHeader:                MonsterHeader,
  override val name:            String,
  override val notes:           String = "",
  health:                       Health,
  armorClass:                   Int,
  override val initiative:      Int = 0,
  conditions:                   Set[Condition] = Set.empty,
  override val otherMarkers:    List[Marker] = List.empty,
  override val initiativeBonus: Int
) extends EncounterCombatant() {

  val isNPC = true

}

/** Used for things like Hunter's Mark, Hex, Hidden, concentration, etc.
  */
case class Marker(name: String)

final case class NonPlayerCharacterCombatant(
  override val id:              CombatantId,
  override val name:            String,
  nonPlayerCharacterId:         NonPlayerCharacterId,
  override val notes:           String,
  override val initiative:      Int,
  override val initiativeBonus: Int,
  override val otherMarkers:    List[Marker] = List.empty
) extends EncounterCombatant() {

  val isNPC = true

}

final case class PlayerCharacterCombatant(
  override val id:              CombatantId,
  override val name:            String,
  playerCharacterId:            PlayerCharacterId,
  override val notes:           String,
  override val initiative:      Int,
  override val initiativeBonus: Int,
  override val otherMarkers:    List[Marker] = List.empty
) extends EncounterCombatant() {

  val isNPC = false

}

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
  orderCol:   Int
) extends HasId[EncounterId]

case class EncounterInfo(
  combatants:  List[EncounterCombatant] = List.empty,
  currentTurn: Int = 0,
  round:       Int = 0,
  notes:       String = ""
) {

  lazy val xp = combatants.collect { case m: MonsterCombatant =>
    m.monsterHeader.xp
  }.sum

  def monsters: List[MonsterCombatant] = combatants.collect { case m: MonsterCombatant => m }
  def npcs:     List[NonPlayerCharacterCombatant] = combatants.collect { case npc: NonPlayerCharacterCombatant => npc }

}

private case class ThresholdRow(
  easy:   Long,
  medium: Long,
  hard:   Long,
  deadly: Long
) {

  def +(other: ThresholdRow): ThresholdRow =
    ThresholdRow(easy + other.easy, medium + other.medium, hard + other.hard, deadly + other.deadly)

}
lazy private val thresholdTable = {
  Map(
    1  -> ThresholdRow(25, 50, 75, 100),
    2  -> ThresholdRow(50, 100, 150, 200),
    3  -> ThresholdRow(75, 150, 225, 400),
    4  -> ThresholdRow(125, 250, 375, 500),
    5  -> ThresholdRow(250, 500, 750, 1100),
    6  -> ThresholdRow(300, 600, 900, 1400),
    7  -> ThresholdRow(350, 750, 1100, 1700),
    8  -> ThresholdRow(450, 900, 1400, 2100),
    9  -> ThresholdRow(550, 1100, 1600, 2400),
    10 -> ThresholdRow(600, 1200, 1900, 2800),
    11 -> ThresholdRow(800, 1600, 2400, 3600),
    12 -> ThresholdRow(1000, 2000, 3000, 4500),
    13 -> ThresholdRow(1100, 2200, 3400, 5100),
    14 -> ThresholdRow(1250, 2500, 3800, 5700),
    15 -> ThresholdRow(1400, 2800, 4300, 6400),
    16 -> ThresholdRow(1600, 3200, 4800, 7200),
    17 -> ThresholdRow(2000, 3900, 5900, 8800),
    18 -> ThresholdRow(2100, 4200, 6300, 9500),
    19 -> ThresholdRow(2400, 4900, 7300, 10900),
    20 -> ThresholdRow(2800, 5700, 8500, 12700)
  )
}

lazy private val monsterMultiplier: Map[Int, Double] = Map(
  1  -> 1,
  2  -> 1.5,
  3  -> 2,
  4  -> 2,
  5  -> 2,
  6  -> 2,
  7  -> 2.5,
  8  -> 2.5,
  9  -> 2.5,
  10 -> 2.5,
  11 -> 3,
  12 -> 3,
  13 -> 3,
  14 -> 3,
  15 -> 4
)

lazy private val pcMultiplier: Map[Int, Double] = Map(
  1 -> 0.75,
  2 -> 0.75,
  3 -> 0.75,
  4 -> 1,
  5 -> 1,
  6 -> 1,
  7 -> 1.25,
  8 -> 1.25
)

case class Encounter(
  header:               EncounterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[EncounterId, EncounterHeader, EncounterInfo] {

  def calculateDifficulty(pcs: Seq[PlayerCharacter]): EncounterDifficulty = {
    val pcMultiplierThreshold = pcMultiplier.getOrElse(pcs.size, 1.25)
    val thresholdXP: ThresholdRow =
      pcs.map(pc => thresholdTable(pc.info.totalLevel - 1)).foldLeft(ThresholdRow(0, 0, 0, 0))(_ + _)

    val adjustedThresholdXP: ThresholdRow = thresholdXP.copy(
      easy = (thresholdXP.easy * pcMultiplierThreshold).toLong,
      medium = (thresholdXP.medium * pcMultiplierThreshold).toLong,
      hard = (thresholdXP.hard * pcMultiplierThreshold).toLong,
      deadly = (thresholdXP.deadly * pcMultiplierThreshold).toLong
    )
    val monsterXP:   Long = info.monsters.map(_.monsterHeader.xp).sum
    val mMultiplier: Double = monsterMultiplier.getOrElse(info.monsters.size, 4)
    val adjustedXP = (monsterXP * mMultiplier).toLong
    if (adjustedXP <= adjustedThresholdXP.easy) EncounterDifficulty.Easy
    else if (adjustedXP <= adjustedThresholdXP.medium) EncounterDifficulty.Medium
    else if (adjustedXP <= adjustedThresholdXP.hard) EncounterDifficulty.Hard
    else EncounterDifficulty.Deadly
  }

  override def entityType: EntityType[EncounterId] = DND5eEntityType.encounter

}
