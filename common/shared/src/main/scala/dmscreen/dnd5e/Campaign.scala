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

import dmscreen.UserId
import dmscreen.dnd5e.GameSystem.dnd5e

opaque type CampaignId = Long

object CampaignId {

  val empty: CampaignId = CampaignId(0)

  def apply(campaignId: Long): CampaignId = campaignId

  extension (campaignId: CampaignId) {

    def value: Long = campaignId

  }

}

enum GameSystem {

  case dnd5e, pathfinder2e, starTrekAdventures

}

case class CampaignHeader(
  id:         CampaignId,
  dm:         UserId,
  name:       String,
  gameSystem: GameSystem = dnd5e
)

case class Scene(
  name:       String,
  isActive:   Boolean,
  notes:      String,
  npcs:       List[NonPlayerCharacterId] = List.empty,
  encounters: List[EncounterId] = List.empty
)

case class CampaignInfo(
  notes:  String,
  scenes: List[Scene] = List.empty
)

case class Campaign(
  header: CampaignHeader,
  info:   CampaignInfo
)
