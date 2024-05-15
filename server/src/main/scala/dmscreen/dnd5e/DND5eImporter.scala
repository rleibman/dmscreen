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

import dmscreen.{DMScreenEnvironment, DMScreenError}

import java.net.URL
import zio.*

trait DND5eImporter[CampaignLink, PlayerCharacterLink, EncounterLink, MonsterLink] {

  def importCampaign(campaignLink: CampaignLink): ZIO[DMScreenEnvironment, DMScreenError, Campaign]

  def importPlayerCharacter(playerCharacterLink: PlayerCharacterLink)
    : ZIO[DMScreenEnvironment, DMScreenError, PlayerCharacter]

  def importEncounter(encounterLink: EncounterLink): ZIO[DMScreenEnvironment, DMScreenError, Encounter]

  def importMonster(monsterLink: MonsterLink): ZIO[DMScreenEnvironment, DMScreenError, Monster]

}

class DNDBeyondImporter extends DND5eImporter[URL, URL, URL, URL] {

  override def importCampaign(campaignLink: URL): ZIO[DMScreenEnvironment, DMScreenError, Campaign] = ???

  override def importPlayerCharacter(playerCharacterLink: URL)
    : ZIO[DMScreenEnvironment, DMScreenError, PlayerCharacter] = ???

  override def importEncounter(encounterLink: URL): ZIO[DMScreenEnvironment, DMScreenError, Encounter] = ???

  override def importMonster(monsterLink: URL): ZIO[DMScreenEnvironment, DMScreenError, Monster] = ???

}