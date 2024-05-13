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

import dmscreen.DMScreenError

import java.net.URL
import zio.*

trait DND5eImporter[CampaignLink, PlayerCharacterLink, EncounterLink, MonsterLink] {

  def importCampaign(campaignLink: CampaignLink): ZIO[DND5eEnvironment, DMScreenError, Campaign]

  def importPlayerCharacter(playerCharacterLink: PlayerCharacterLink)
    : ZIO[DND5eEnvironment, DMScreenError, PlayerCharacter]

  def importEncounter(encounterLink: EncounterLink): ZIO[DND5eEnvironment, DMScreenError, Encounter]

  def importMonster(monsterLink: MonsterLink): ZIO[DND5eEnvironment, DMScreenError, Monster]

}

class DNDBeyondImporter extends DND5eImporter[URL, URL, URL, URL] {

  override def importCampaign(campaignLink: URL) = ???

  override def importPlayerCharacter(playerCharacterLink: URL) = ???

  override def importEncounter(encounterLink: URL) = ???

  override def importMonster(monsterLink: URL) = ???

}
