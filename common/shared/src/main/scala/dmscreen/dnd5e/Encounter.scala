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
  
// 00000000-0000-0000-0000-000000000000 // empty
// fa4e264e-d2ca-4f8c-8b23-e071006f8e61  
  
  def empty: CombatantId = CombatantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
  
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

  def canTakeTurn: Boolean

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

  override def canTakeTurn: Boolean = !health.isDead

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

  override def canTakeTurn: Boolean = true

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
  override def canTakeTurn: Boolean = true

}

opaque type EncounterId = Long

object EncounterId {

  val empty: EncounterId = EncounterId(0)

  def apply(encounterId: Long): EncounterId = encounterId

  extension (encounterId: EncounterId) {

    def value: Long = encounterId

  }

}

enum EncounterDifficultyLevel {

  case Low
  case Moderate
  case High

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

enum EncounterTimeOfDay {

  case dawn, morning, noon, afternoon, dusk, evening, night, unimportant

}

case class EncounterInfo(
  combatants:           List[EncounterCombatant] = List.empty,
  currentTurnId:        CombatantId = CombatantId.empty,
  round:                Int = 0,
  notes:                String = "",
  timeOfDay:            EncounterTimeOfDay = EncounterTimeOfDay.unimportant,
  biome:                Biome = Biome.Unimportant,
  locationNotes:        String = "",
  initialDescription:   String = "",
  desiredDifficulty:    EncounterDifficultyLevel = EncounterDifficultyLevel.Moderate,
  treasure:             Treasure = Treasure(),
  generatedDescription: String = ""
) {

  def xpBudget(pcs: Seq[PlayerCharacter]): ThresholdRow =
    pcs.map(pc => thresholdTable(pc.info.totalLevel - 1)).foldLeft(ThresholdRow(0, 0, 0))(_ + _)

  def calculateDifficulty(
    pcs:  Seq[PlayerCharacter],
    npcs: Seq[NonPlayerCharacter]
  ): EncounterDifficultyLevel = {
    val playerXP: ThresholdRow = xpBudget(pcs)

    val theEnemyXP = enemyXP(npcs)

    if (theEnemyXP <= playerXP.low) EncounterDifficultyLevel.Low
    else if (theEnemyXP <= playerXP.moderate) EncounterDifficultyLevel.Moderate
    else EncounterDifficultyLevel.High
  }

  def enemyXP(fullNPCs: Seq[NonPlayerCharacter]): Long = {
    val monsterXP: Long = monsters
      .map { m =>
        if (m.monsterHeader.xp != 0) m.monsterHeader.xp.toDouble
        else (m.monsterHeader.cr.value + (m.monsterHeader.cr.value.toLong / 7)) * 100.0
      }.sum.toLong

    val npcXP: Long = fullNPCs
      .filter(npc =>
        npcs.exists(
          _.nonPlayerCharacterId == npc.id && Seq(
            RelationToPlayers.enemy,
            RelationToPlayers.unknown,
            RelationToPlayers.itsComplicated
          ).contains(npc.info.relationToPlayers)
        )
      ).map { npc =>
        npc.info.challengeRating
          .orElse(ChallengeRating.fromDouble(npc.info.totalLevel * 0.5)).fold(ChallengeRating.`1`.xp)(_.xp)
      }.sum

    monsterXP + npcXP
  }

  def monsters: List[MonsterCombatant] = combatants.collect { case m: MonsterCombatant => m }
  def pcs:      List[PlayerCharacterCombatant] = combatants.collect { case pc: PlayerCharacterCombatant => pc }
  def npcs:     List[NonPlayerCharacterCombatant] = combatants.collect { case npc: NonPlayerCharacterCombatant => npc }

}

private case class ThresholdRow(
  low:      Long,
  moderate: Long,
  high:     Long
) {

  def +(other: ThresholdRow): ThresholdRow = ThresholdRow(low + other.low, moderate + other.moderate, high + other.high)
  def xp(encounterDifficultyLevel: EncounterDifficultyLevel): Long =
    encounterDifficultyLevel match {
      case EncounterDifficultyLevel.Low      => low
      case EncounterDifficultyLevel.Moderate => moderate
      case EncounterDifficultyLevel.High     => high
    }

}
lazy private val thresholdTable = {
  Map(
    1  -> ThresholdRow(50, 75, 100),
    2  -> ThresholdRow(100, 150, 200),
    3  -> ThresholdRow(150, 225, 400),
    4  -> ThresholdRow(250, 375, 500),
    5  -> ThresholdRow(500, 750, 1100),
    6  -> ThresholdRow(600, 1000, 1400),
    7  -> ThresholdRow(750, 1300, 1700),
    8  -> ThresholdRow(1000, 1700, 2100),
    9  -> ThresholdRow(1300, 2000, 2600),
    10 -> ThresholdRow(1600, 2300, 3100),
    11 -> ThresholdRow(1900, 2900, 4100),
    12 -> ThresholdRow(2200, 3700, 4700),
    13 -> ThresholdRow(2600, 4200, 5400),
    14 -> ThresholdRow(2900, 4900, 6200),
    15 -> ThresholdRow(3300, 5400, 7800),
    16 -> ThresholdRow(3800, 6100, 9800),
    17 -> ThresholdRow(4500, 7200, 11700),
    18 -> ThresholdRow(5000, 8700, 14200),
    19 -> ThresholdRow(5500, 10700, 17200),
    20 -> ThresholdRow(6400, 13200, 22000)
  )
}

//lazy private val monsterMultiplier: Map[Int, Double] = Map(
//  1  -> 1,
//  2  -> 1.5,
//  3  -> 2,
//  4  -> 2,
//  5  -> 2,
//  6  -> 2,
//  7  -> 2.5,
//  8  -> 2.5,
//  9  -> 2.5,
//  10 -> 2.5,
//  11 -> 3,
//  12 -> 3,
//  13 -> 3,
//  14 -> 3,
//  15 -> 4
//)
//
//lazy private val pcMultiplier: Map[Int, Double] = Map(
//  1 -> 0.75,
//  2 -> 0.75,
//  3 -> 0.75,
//  4 -> 1,
//  5 -> 1,
//  6 -> 1,
//  7 -> 1.25,
//  8 -> 1.25
//)

object Encounter {}

case class Encounter(
  header:               EncounterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[EncounterId, EncounterHeader, EncounterInfo] {

  override def entityType: EntityType[EncounterId] = DND5eEntityType.encounter

}
