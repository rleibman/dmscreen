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

import dmscreen.{DMScreenError, DMScreenOperation, GameService}
import zio.*
import zio.json.ast.Json

import scala.reflect.ClassTag

enum OrderDirection {

  case asc, desc

}

enum MonsterSearchOrder {

  case name, challengeRating, size, alignment, environment, monsterType, source

}

case class MonsterSearch(
  name:            Option[String],
  challengeRating: Option[Double],
  size:            Option[String],
  alignment:       Option[String],
  environment:     Option[String],
  monsterType:     Option[MonsterType],
  source:          Option[Source],
  order:           MonsterSearchOrder = MonsterSearchOrder.name,
  orderDir:        OrderDirection = OrderDirection.asc,
  page:            Int = 0,
  pageSize:        Int = 25
)

trait DND5eGameService extends GameService {

  def campaigns: IO[DMScreenError, Seq[CampaignHeader]]

  def campaign(campaignId: CampaignId): IO[DMScreenError, Option[Campaign]]

  def applyOperation[IDType: ClassTag](
    id:        IDType,
    operation: DMScreenOperation
  )(using ev:  ClassTag[IDType]
  ): IO[DMScreenError, Unit]

  def delete(
    campaignId: CampaignId,
    softDelete: Boolean = true
  ): IO[DMScreenError, Unit]

  def playerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[PlayerCharacter]]

  def nonPlayerCharacters(campaignId: CampaignId): IO[DMScreenError, Seq[NonPlayerCharacter]]

  def encounters(campaignId: CampaignId): IO[DMScreenError, Seq[EncounterHeader]]

  def encounter(encounterId: EncounterId): IO[DMScreenError, Seq[Encounter]]

  // Stuff that's generic to all campaigns

  def bestiary(search: MonsterSearch): IO[DMScreenError, Seq[Monster]]

  def sources: IO[DMScreenError, Seq[Source]]

  def classes: IO[DMScreenError, Seq[CharacterClass]]

  def races: IO[DMScreenError, Seq[Race]]

  def backgrounds: IO[DMScreenError, Seq[Background]]

  def subClasses(characterClass: CharacterClassId): IO[DMScreenError, Seq[Subclass]]

  def spells: IO[DMScreenError, Seq[Spell]]

  def insert(
    campaignHeader: CampaignHeader,
    info:           Json
  ): IO[DMScreenError, CampaignId]
  def insert(
    playerCharacterHeader: PlayerCharacterHeader,
    info:                  Json
  ): IO[DMScreenError, PlayerCharacterId]
  def insert(
    nonPlayerCharacterHeader: NonPlayerCharacterHeader,
    info:                     Json
  ): IO[DMScreenError, NonPlayerCharacterId]
  def insert(
    monsterHeader: MonsterHeader,
    info:          Json
  ): IO[DMScreenError, MonsterId]
  def insert(
    spellHeader: SpellHeader,
    info:        Json
  ): IO[DMScreenError, SpellId]
  def insert(
    encounterHeader: EncounterHeader,
    info:            Json
  ): IO[DMScreenError, EncounterId]

  def deletePlayerCharacter(
    playerCharacterId: PlayerCharacterId,
    softDelete:        Boolean = false
  ): IO[DMScreenError, Unit]
  def deleteNonPlayerCharacter(
    nonPlayerCharacterId: NonPlayerCharacterId,
    softDelete:           Boolean = false
  ): IO[DMScreenError, Unit]
  def deleteMonster(
    monsterId:  MonsterId,
    softDelete: Boolean = false
  ): IO[DMScreenError, Unit]
  def deleteSpell(
    spellId:    SpellId,
    softDelete: Boolean = false
  ): IO[DMScreenError, Unit]
  def deleteEncounter(
    encounterId: EncounterId,
    softDelete:  Boolean = false
  ): IO[DMScreenError, Unit]

}

trait EncounterRunner {

  def encounter: Encounter

}
