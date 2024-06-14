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

import caliban.client.scalajs.DND5eClient.{
  Background as CalibanBackground,
  CampaignHeader as CalibanCampaignHeader,
  CharacterClass as CalibanCharacterClass,
  DND5eCampaign as CalibanDND5eCampaign,
  GameSystem as CalibanGameSystem,
  PlayerCharacter as CalibanPlayerCharacter,
  PlayerCharacterHeader as CalibanPlayerCharacterHeader,
  Queries
}
import caliban.ScalaJSClientAdapter.*
import caliban.client.CalibanClientError.DecodingError
import caliban.client.Operations.RootQuery
import caliban.client.{ScalarDecoder, SelectionBuilder}
import components.*
import dmscreen.dnd5e.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.TimerSupport
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom.*

import java.net.URI
import java.util.UUID
import zio.json.*
import zio.json.ast.Json

object Content {

  private val connectionId = UUID.randomUUID().toString

  case class State(dmScreenState: DMScreenState)

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State): VdomElement = {
      DMScreenState.ctx.provide(s.dmScreenState) {
        <.div(
          ^.key    := "contentDiv",
          ^.height := 100.pct,
          Confirm.render(),
          Toast.render(),
          AppRouter.router()
        )
      }
    }

    def refresh(initial: Boolean): Callback = {
      val params = URL(window.location.href).searchParams

      val ajax = for {
        oldState <- $.state.asAsyncCallback
        currentCampaignId <- AsyncCallback.pure {

          // If the campaign is in the URL, use it, otherwise use the one in the session storage if it exist, otherwise, for now use 1
          // But in the future, we just shouldn't show the other tabs and only show the home tab
          Option(params.get("campaignId"))
            .orElse(Option(window.sessionStorage.getItem("currentCampaignId")))
            .fold(
              Some(CampaignId(1)) // Change this, we'll need to load campaigns from the server and select the first one)
            )(str => Some(CampaignId(str.toInt)))
        }
        // Store the current campaign Id in the session storage for next time
        _ <- AsyncCallback.pure(window.sessionStorage.setItem("currentCampaignId", currentCampaignId.value.toString))
        _ <- Callback.log("Loading campaign data from server...").asAsyncCallback
        backgrounds <- {
          val sb = Queries.backgrounds(CalibanBackground.name.map(Background.apply))
          asyncCalibanCall(sb).map(_.toSeq.flatten)
        }
        classes <- {
          val sb = Queries.classes(
            (CalibanCharacterClass.id ~ CalibanCharacterClass.hitDice).map(
              (
                id,
                hd
              ) =>
                CharacterClass(
                  CharacterClassId.values.find(_.name.equalsIgnoreCase(id)).getOrElse(CharacterClassId.unknown),
                  hd
                )
            )
          )
          asyncCalibanCall(sb).map(_.toSeq.flatten)
        }
        campaign <- currentCampaignId.fold(AsyncCallback.pure(None: Option[(DND5eCampaign, List[PlayerCharacter])])) {
          id =>
            // TODO move the sb declarations to common code
            val campaignSB: SelectionBuilder[CalibanDND5eCampaign, DND5eCampaign] = (CalibanDND5eCampaign.header(
              CalibanCampaignHeader.id ~ CalibanCampaignHeader.name ~ CalibanCampaignHeader.dmUserId ~ CalibanCampaignHeader.gameSystem
            ) ~ CalibanDND5eCampaign.jsonInfo).map {
              (
                id:       Long,
                name:     String,
                dmUserId: Long,
                system:   CalibanGameSystem,
                info:     Json
              ) =>
                DND5eCampaign(
                  CampaignHeader(CampaignId(id), UserId(dmUserId), name, GameSystem.valueOf(system.value)),
                  info
                )
            }
            val playerCharacterSB: SelectionBuilder[CalibanPlayerCharacter, PlayerCharacter] =
              (CalibanPlayerCharacter.header(
                CalibanPlayerCharacterHeader.campaignId ~
                  CalibanPlayerCharacterHeader.id ~
                  CalibanPlayerCharacterHeader.name ~
                  CalibanPlayerCharacterHeader.playerName
              ) ~ CalibanPlayerCharacter.jsonInfo).map {
                (
                  campaignId: Long,
                  pcId:       Long,
                  name:       String,
                  playerName: Option[String],
                  info:       Json
                ) =>
                  PlayerCharacter(
                    PlayerCharacterHeader(
                      id = PlayerCharacterId(pcId),
                      campaignId = CampaignId(campaignId),
                      name = name,
                      playerName = playerName
                    ),
                    info
                  )
              }

            val totalSB =
              (Queries.campaign(id.value)(campaignSB) ~ Queries.playerCharacters(id.value)(playerCharacterSB)).map {
                (
                  cOpt,
                  pcsOpt
                ) =>
                  for {
                    c   <- cOpt
                    pcs <- pcsOpt
                  } yield (c, pcs)
              }
            asyncCalibanCall(totalSB)
        }
        _ <- Callback.log(s"Campaign data (${campaign.fold("")(_._1.header.name)}) loaded from server").asAsyncCallback

        // TODO use asyncCalibanCall to get all the pieces of data needed to create a new global state
      } yield $.modState { s =>
        import scala.language.unsafeNulls
        val copy = if (initial) {
          // Special stuff needs to be done on initalization:
          // - Create the userStream and connect to it
          // - Set all methods that will be used by users of DMState to update pieces of the state, think of these as application level methods
          // - Set the global User
          s.copy()
        } else {
          s
        }

        val newCampaignState = campaign.map(
          (
            c,
            pcs
          ) =>
            s.dmScreenState.campaignState.fold(
              // Completely brand new
              DND5eCampaignState(c, pcs)
            ) { case oldCampaignState: DND5eCampaignState =>
              oldCampaignState.copy(campaign = c, pcs = pcs)
            }
        )

        copy.copy(dmScreenState =
          s.dmScreenState
            .copy(
              campaignState = newCampaignState,
              dnd5e = s.dmScreenState.dnd5e.copy(
                backgrounds = backgrounds,
                classes = classes
              ),
              onModifyCampaignState = newState =>
                $.modState(s => s.copy(dmScreenState = s.dmScreenState.copy(campaignState = Some(newState))))
            )
        )
      }
      for {
        _ <- Callback.log(
          if (initial) "Initializing Content Component" else "Refreshing Content Component"
        )
        modedState <- ajax.completeWith(_.get)
      } yield modedState
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("content")
    .initialState {
      // Get from window.sessionStorage anything that we may need later to fill in the state.
      State(dmScreenState = DMScreenState())
    }
    .renderBackend[Backend]
    .componentDidMount(_.backend.refresh(initial = true))
    .componentWillUnmount($ =>
      Callback.log("Closing down operationStream")
//        >>
//        $.state.dmScreenState.operationStream.fold(Callback.empty)(_.close())
    )
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
