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

import dmscreen.{DMScreenError, GameService}

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

  def campaign(campaignId: CampaignId): ZIO[DND5eEnvironment, DMScreenError, Campaign]

  def playerCharacters(campaignId: CampaignId): ZIO[DND5eEnvironment, DMScreenError, List[PlayerCharacter]]

  def nonPlayerCharacters(campaignId: CampaignId): ZIO[DND5eEnvironment, DMScreenError, List[NonPlayerCharacter]]

  // Stuff that's generic to all campaigns

  def bestiary(search: MonsterSearch): ZIO[DND5eEnvironment, DMScreenError, List[Monster]]

  def sources: ZIO[DND5eEnvironment, DMScreenError, List[Source]]

  def classes: ZIO[DND5eEnvironment, DMScreenError, List[CharacterClass]]

  def races: ZIO[DND5eEnvironment, DMScreenError, List[Race]]

  def backgrounds: ZIO[DND5eEnvironment, DMScreenError, List[Background]]

  def subClasses(characterClass: CharacterClass): ZIO[DND5eEnvironment, DMScreenError, List[Subclass]]

}

trait EncounterRunner {

  def encounter: Encounter

}

object DND5eGameService {

  def db: ULayer[DND5eEnvironment] =
    ZLayer.succeed(
      new DND5eGameService() {
        override def campaign(campaignId: CampaignId): ZIO[DND5eEnvironment, DMScreenError, Campaign] = ???

        override def playerCharacters(campaignId: CampaignId)
          : ZIO[DND5eEnvironment, DMScreenError, List[PlayerCharacter]] = ???

        override def nonPlayerCharacters(campaignId: CampaignId)
          : ZIO[DND5eEnvironment, DMScreenError, List[NonPlayerCharacter]] = ???

        override def bestiary(search: MonsterSearch): ZIO[DND5eEnvironment, DMScreenError, List[Monster]] = ???

        override def sources: ZIO[DND5eEnvironment, DMScreenError, List[Source]] = ???

        override def classes: ZIO[DND5eEnvironment, DMScreenError, List[CharacterClass]] = ???

        override def races: ZIO[DND5eEnvironment, DMScreenError, List[Race]] = ???

        override def backgrounds: ZIO[DND5eEnvironment, DMScreenError, List[Background]] = ???

        override def subClasses(characterClass: CharacterClass): ZIO[DND5eEnvironment, DMScreenError, List[Subclass]] =
          ???
      }
    )

}
