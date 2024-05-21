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
        } yield assertTrue(
          campaigns.nonEmpty,
          campaign.isDefined,
          campaign.get.info.notes.nonEmpty,
          newCampaignId.value > 0,
          newCampaigns.length == campaigns.length + 1,
          afterNewCampaign.isDefined,
          updatedCampaigns.length == newCampaigns.length,
          updatedCampaign.map(_.info.notes).contains("These are some updated notes"),
          deletedCampaigns.length == updatedCampaigns.length - 1,
          deletedCampaign.isEmpty
        )
      }
    }.provideLayerShared(EnvironmentBuilder.withContainer)
  }

}
