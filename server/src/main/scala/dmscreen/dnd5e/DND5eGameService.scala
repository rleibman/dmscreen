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

import dmscreen.{DMScreenEnvironment, DMScreenError, GameService}

import java.net.URL
import zio.*

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

case class Source(
  name:     String,
  nickName: String,
  url:      String
)

trait DND5eGameService extends GameService {

  def campaigns: ZIO[DMScreenEnvironment, DMScreenError, Seq[CampaignHeader]]

  def campaign(campaignId: CampaignId): ZIO[DMScreenEnvironment, DMScreenError, Option[Campaign]]

  def playerCharacters(campaignId: CampaignId): ZIO[DMScreenEnvironment, DMScreenError, Seq[PlayerCharacter]]

  def nonPlayerCharacters(campaignId: CampaignId): ZIO[DMScreenEnvironment, DMScreenError, Seq[NonPlayerCharacter]]

  // Stuff that's generic to all campaigns

  def bestiary(search: MonsterSearch): ZIO[DMScreenEnvironment, DMScreenError, Seq[Monster]]

  def sources: ZIO[DMScreenEnvironment, DMScreenError, Seq[Source]]

  def classes: ZIO[DMScreenEnvironment, DMScreenError, Seq[CharacterClass]]

  def races: ZIO[DMScreenEnvironment, DMScreenError, Seq[Race]]

  def backgrounds: ZIO[DMScreenEnvironment, DMScreenError, Seq[Background]]

  def subClasses(characterClass: CharacterClassId): ZIO[DMScreenEnvironment, DMScreenError, Seq[Subclass]]

}

trait EncounterRunner {

  def encounter: Encounter

}
