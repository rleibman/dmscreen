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

import dmscreen.{EnvironmentBuilder, JsonPath, Replace, UserId}
import zio.*
import zio.test.*
import zio.json.*
import zio.json.ast.*

object StorageSpec extends ZIOSpecDefault {

  private val testCampaignId = CampaignId(1)
  private val testUser = UserId(1)

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("Testing Storage") {
      test("Should be able to store and retrieve a campaign") {
        for {
          service   <- ZIO.service[DND5eGameService]
          campaigns <- service.campaigns
          campaign  <- service.campaign(campaigns.head.id)
          newCampaignId <- service.insert(
            CampaignHeader(CampaignId.empty, testUser, "Test Campaign 2"),
            CampaignInfo(notes = "These are some notes").toJsonAST.getOrElse(Json.Null)
          )
          newCampaigns     <- service.campaigns
          afterNewCampaign <- service.campaign(newCampaignId)
          _ <- service.applyOperation(
            id = newCampaignId,
            operation = Replace(JsonPath("$.notes"), Json.Str("These are some updated notes"))
          )
          updatedCampaigns <- service.campaigns
          updatedCampaign  <- service.campaign(newCampaignId)
          _                <- service.delete(newCampaignId)
          deletedCampaigns <- service.campaigns
          deletedCampaign  <- service.campaign(newCampaignId)
        } yield {
//          val info = campaign.get.info.toOption.get
//          val updatedInfo = updatedCampaign.get.info.toOption.get
          assertTrue(
            campaigns.nonEmpty,
            campaign.isDefined,
//            info.notes.nonEmpty,
            newCampaignId.value > 0,
            newCampaigns.length == campaigns.length + 1,
            afterNewCampaign.isDefined,
            updatedCampaigns.length == newCampaigns.length,
//            updatedInfo.notes.contains("These are some updated notes"),
            deletedCampaigns.length == updatedCampaigns.length - 1,
            deletedCampaign.isEmpty
          )
        }
      }
    }.provideLayerShared(EnvironmentBuilder.withContainer)
  }

}
