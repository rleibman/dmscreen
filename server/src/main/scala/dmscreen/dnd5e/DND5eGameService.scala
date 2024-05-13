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
