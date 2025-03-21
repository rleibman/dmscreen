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
import zio.*
import zio.json.ast.Json

import scala.reflect.ClassTag

enum OrderDirection {

  case asc, desc

  def toggle: OrderDirection = if (ordinal == OrderDirection.asc.ordinal) OrderDirection.desc else OrderDirection.asc

}

enum MonsterSearchOrder {

  case name, challengeRating, size, alignment, biome, monsterType, random

}

case class PlayerCharacterSearch(dndBeyondId: Option[DndBeyondId] = None)

case class MonsterSearch(
  name:            Option[String] = None,
  challengeRating: Option[ChallengeRating] = None,
  size:            Option[CreatureSize] = None,
  alignment:       Option[Alignment] = None,
  biome:           Option[Biome] = None,
  monsterType:     Option[MonsterType] = None,
  source:          Option[Source] = None,
  orderCol:        MonsterSearchOrder = MonsterSearchOrder.name,
  orderDir:        OrderDirection = OrderDirection.asc,
  page:            Int = 0,
  pageSize:        Int = 10
)

case class MonsterSearchResults(
  results: List[MonsterHeader] = List.empty,
  total:   Long = 0
)

case class FullMonsterSearchResults(
  results: List[Monster] = List.empty,
  total:   Long = 0
)

trait DND5eRepository[F[_]] extends GameSystemRepository[F] {

  // DND5e specific stuff

  def monster(monsterId: MonsterId): F[Option[Monster]]

  def deleteEntity[IDType](
    entityType: EntityType[IDType],
    id:         IDType,
    softDelete: Boolean = true
  ): F[Unit]

  def playerCharacters(
    campaignId: CampaignId,
    search:     PlayerCharacterSearch = PlayerCharacterSearch()
  ): F[Seq[PlayerCharacter]]

  def playerCharacter(
    playerCharacterId: PlayerCharacterId
  ): F[Option[PlayerCharacter]]

  def nonPlayerCharacter(
    nonPlayerCharacterId: NonPlayerCharacterId
  ): F[Option[NonPlayerCharacter]]

  def scenes(campaignId: CampaignId): F[Seq[Scene]]

  def scene(id: SceneId): F[Option[Scene]]

  def nonPlayerCharacters(campaignId: CampaignId): F[Seq[NonPlayerCharacter]]

  def encounters(campaignId: CampaignId): F[Seq[Encounter]]

  def encounter(
    campaignId:  CampaignId,
    encounterId: EncounterId
  ): F[Option[Encounter]]
  // Stuff that's generic to all campaigns

  def randomTables(tableTypeOpt: Option[RandomTableType]): F[Seq[RandomTable]]

  def randomTable(id: RandomTableId): F[Option[RandomTable]]

  def bestiary(search: MonsterSearch): F[MonsterSearchResults]

  def fullBestiary(search: MonsterSearch): F[FullMonsterSearchResults]

  def sources: F[Seq[Source]]

  def classes: F[Seq[CharacterClass]]

  def races: F[Seq[Race]]

  def backgrounds: F[Seq[Background]]

  def subClasses(characterClass: CharacterClassId): F[Seq[SubClass]]

  def spells: F[Seq[Spell]]

  def upsert(
    header: PlayerCharacterHeader,
    info:   Json
  ): F[PlayerCharacterId]
  def upsert(
    header: NonPlayerCharacterHeader,
    info:   Json
  ): F[NonPlayerCharacterId]
  def upsert(
    header: MonsterHeader,
    info:   Json
  ): F[MonsterId]
  def upsert(
    header: SpellHeader,
    info:   Json
  ): F[SpellId]
  def upsert(
    header: EncounterHeader,
    info:   Json
  ): F[EncounterId]
  def upsert(
    header: SceneHeader,
    info:   Json
  ): F[SceneId]

  def npcsForScene(campaignId: CampaignId): F[Map[SceneId, Seq[NonPlayerCharacterId]]]
  def addNpcToScene(
    sceneId: SceneId,
    npcId:   NonPlayerCharacterId
  ): F[Unit]
  def removeNpcFromScene(
    sceneId: SceneId,
    npcId:   NonPlayerCharacterId
  ): F[Unit]

}
