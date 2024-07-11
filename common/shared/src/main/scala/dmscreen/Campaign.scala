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

package dmscreen

import just.semver.SemVer
import zio.json.ast.Json
import zio.json.*
import zio.{IO, ZIO}

opaque type CampaignId = Long

object CampaignId {

  val empty: CampaignId = CampaignId(0)

  def apply(campaignId: Long): CampaignId = campaignId

  extension (campaignId: CampaignId) {

    def value: Long = campaignId

  }

}

enum GameSystem(val name: String) {

  case dnd5e extends GameSystem("Dungeons and Dragons (5e)")
  case starTrekAdventures extends GameSystem("Star Trek Adventures (1e)")
  case pathfinder2e extends GameSystem("Pathfinder (2e)")
  case starfinder extends GameSystem("Starfinder")
  case callOfCthulhu extends GameSystem("Call of Cthulhu")
  case savageWorlds extends GameSystem("Savage Worlds")
  case fateCore extends GameSystem("Fate Core")
  case fateAccelerated extends GameSystem("Fate Accelerated")
  case dnd3_5 extends GameSystem("Dungeons and Dragons (3.5)")
  case dnd4e extends GameSystem("Dungeons and Dragons (4e)")
  case pathfinder1e extends GameSystem("Pathfinder (1e)")

}

enum CampaignStatus {

  case active, archived

}

object CampaignHeader {

  def empty(userId: UserId): CampaignHeader =
    CampaignHeader(CampaignId.empty, userId, "", GameSystem.dnd5e, CampaignStatus.active)

}

case object CampaignEntityType extends EntityType[CampaignId] {

  val name = "Campaign"
  def createId(id: Long): CampaignId = CampaignId(id)

}

case class CampaignHeader(
  id:             CampaignId,
  dmUserId:       UserId,
  name:           String,
  gameSystem:     GameSystem,
  campaignStatus: CampaignStatus
) extends HasId[CampaignId]

case class Campaign(
  override val header:   CampaignHeader,
  override val jsonInfo: Json,
  override val version:  SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[CampaignId, CampaignHeader, CampaignInfo] {

  override val entityType: EntityType[CampaignId] = CampaignEntityType

}

case class CampaignInfo(
  notes: String
)
