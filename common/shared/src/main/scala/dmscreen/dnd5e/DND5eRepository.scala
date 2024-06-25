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

  def toggle: OrderDirection = if (this == OrderDirection.asc) OrderDirection.desc else OrderDirection.asc

}

enum MonsterSearchOrder {

  case name, challengeRating, size, alignment, biome, monsterType, random

}

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
  results: Seq[Monster],
  total:   Long
)

trait DND5eRepository extends GameRepository {

  def campaigns: IO[DMScreenError, Seq[CampaignHeader]]

  def campaign(campaignId: CampaignId): IO[DMScreenError, Option[DND5eCampaign]]

  def scene(sceneId: SceneId): IO[DMScreenError, Option[Scene]]

  def applyOperations[IDType](
    entityType: EntityType,
    id:         IDType,
    operations: DMScreenEvent*
  ): IO[DMScreenError, Unit]

  def deleteEntity[IDType](
    entityType: EntityType,
    id:         IDType,
    softDelete: Boolean = true
  ): IO[DMScreenError, Unit]

  def playerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[PlayerCharacter]]

  def scenes(campaignId: CampaignId): IO[DMScreenError, Seq[Scene]]

  def playerCharacter(playerCharacterId: PlayerCharacterId): IO[DMScreenError, Option[PlayerCharacter]]

  def nonPlayerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[NonPlayerCharacter]]

  def encounters(campaignId: CampaignId): IO[DMScreenError, Seq[Encounter]]

  // Stuff that's generic to all campaigns

  def bestiary(search: MonsterSearch): IO[DMScreenError, MonsterSearchResults]

  def sources: IO[DMScreenError, Seq[Source]]

  def classes: IO[DMScreenError, Seq[CharacterClass]]

  def races: IO[DMScreenError, Seq[Race]]

  def backgrounds: IO[DMScreenError, Seq[Background]]

  def subClasses(characterClass: CharacterClassId): IO[DMScreenError, Seq[SubClass]]

  def spells: IO[DMScreenError, Seq[Spell]]

  def upsert(
    campaignHeader: CampaignHeader,
    info:           Json
  ): IO[DMScreenError, CampaignId]
  def upsert(
    playerCharacterHeader: PlayerCharacterHeader,
    info:                  Json
  ): IO[DMScreenError, PlayerCharacterId]
  def upsert(
    nonPlayerCharacterHeader: NonPlayerCharacterHeader,
    info:                     Json
  ): IO[DMScreenError, NonPlayerCharacterId]
  def upsert(
    monsterHeader: MonsterHeader,
    info:          Json
  ): IO[DMScreenError, MonsterId]
  def upsert(
    spellHeader: SpellHeader,
    info:        Json
  ): IO[DMScreenError, SpellId]
  def upsert(
    encounterHeader: EncounterHeader,
    info:            Json
  ): IO[DMScreenError, EncounterId]
  def upsert(
    sceneHeader: SceneHeader,
    info:        Json
  ): IO[DMScreenError, SceneId]

}

trait EncounterRunner {

  def encounter: Encounter

}
