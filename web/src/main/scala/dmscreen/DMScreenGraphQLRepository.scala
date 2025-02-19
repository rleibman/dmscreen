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

import auth.UserId
import caliban.client.scalajs.DMScreenClient.{
  Campaign as CalibanCampaign,
  CampaignHeader as CalibanCampaignHeader,
  CampaignHeaderInput,
  CampaignLogEntry as CalibanCampaignLogEntry,
  CampaignStatus as CalibanCampaignStatus,
  GameSystem as CalibanGameSystem,
  Mutations,
  Queries
}
import caliban.client.scalajs.{DMScreenClient, given}
import caliban.client.{ArgEncoder, SelectionBuilder}
import dmscreen.*
import japgolly.scalajs.react.callback.AsyncCallback
import zio.*
import zio.json.*
import zio.json.ast.Json

object DMScreenGraphQLRepository {

  trait ExtendedRepository extends DMScreenRepository[AsyncCallback] {}

  val live: ExtendedRepository = new ExtendedRepository {
    private val calibanClient = caliban.ScalaJSClientAdapter("dmscreen")

    override def campaigns: AsyncCallback[Seq[CampaignHeader]] = {
      val sb = (
        CalibanCampaignHeader.id ~
          CalibanCampaignHeader.dmUserId ~
          CalibanCampaignHeader.name ~
          CalibanCampaignHeader.gameSystem ~
          CalibanCampaignHeader.campaignStatus
      ).map {
        case (
              id:             Long,
              dmUserId:       Long,
              name:           String,
              gameSystem:     CalibanGameSystem,
              campaignStatus: CalibanCampaignStatus
            ) =>
          CampaignHeader(
            id = CampaignId(id),
            dmUserId = UserId(dmUserId),
            name = name,
            gameSystem = GameSystem.valueOf(gameSystem.value),
            campaignStatus = CampaignStatus.valueOf(campaignStatus.value)
          )
      }

      calibanClient.asyncCalibanCall(Queries.campaigns(sb)).map(_.toSeq.flatten)
    }

    override def campaign(campaignId: CampaignId): AsyncCallback[Option[Campaign]] = {
      val sb: SelectionBuilder[CalibanCampaign, Campaign] = (CalibanCampaign.header(
        CalibanCampaignHeader.id ~ CalibanCampaignHeader.name ~ CalibanCampaignHeader.dmUserId ~ CalibanCampaignHeader.gameSystem ~ CalibanCampaignHeader.campaignStatus
      ) ~ CalibanCampaign.jsonInfo).map {
        (
          id:             Long,
          name:           String,
          dmUserId:       Long,
          system:         CalibanGameSystem,
          campaignStatus: CalibanCampaignStatus,
          info:           Json
        ) =>
          Campaign(
            CampaignHeader(
              CampaignId(id),
              UserId(dmUserId),
              name,
              GameSystem.valueOf(system.value),
              CampaignStatus.valueOf(campaignStatus.value)
            ),
            info
          )
      }

      calibanClient.asyncCalibanCall(Queries.campaign(campaignId.value)(sb))
    }

    private val campaignLogSB: SelectionBuilder[CalibanCampaignLogEntry, CampaignLogEntry] = (
      CalibanCampaignLogEntry.campaignId ~
        CalibanCampaignLogEntry.message ~
        CalibanCampaignLogEntry.timestamp
    ).map {
      (
        campaignId,
        message,
        timestamp
      ) =>
        CampaignLogEntry(
          campaignId = CampaignId(campaignId),
          message = message,
          timestamp = timestamp
        )
    }

    override def upsert(
      campaignHeader: CampaignHeader,
      info:           Json
    ): AsyncCallback[CampaignId] = {
      val headerInput = CampaignHeaderInput(
        id = campaignHeader.id.value,
        dmUserId = campaignHeader.dmUserId.value,
        name = campaignHeader.name,
        gameSystem = CalibanGameSystem.values.find(_.value == campaignHeader.gameSystem.toString).get,
        campaignStatus = CalibanCampaignStatus.values.find(_.value == campaignHeader.campaignStatus.toString).get
      )

      val sb = Mutations.upsertCampaign(headerInput, info, dmscreen.BuildInfo.version)
      calibanClient.asyncCalibanCall(sb).map(_.fold(CampaignId.empty)(CampaignId.apply))
    }

    override def campaignLogs(
      campaignId: CampaignId,
      maxNum:     Int
    ): AsyncCallback[Seq[CampaignLogEntry]] = {
      calibanClient.asyncCalibanCall(Queries.campaignLogs(campaignId.value, maxNum)(campaignLogSB)).map(_.toSeq.flatten)
    }

    override def campaignLog(
      campaignId: CampaignId,
      message:    String
    ): AsyncCallback[Unit] = {
      val sb = Mutations.campaignLog(campaignId.value, message)
      calibanClient.asyncCalibanCall(sb).map(_ => ())
    }

    override def deleteCampaign(
      id:         CampaignId,
      softDelete: Boolean = true
    ) = {
      val sb = Mutations.deleteCampaign(id.asInstanceOf[Long], softDelete)
      calibanClient.asyncCalibanCall(sb).map(_.get)
    }
  }

}
