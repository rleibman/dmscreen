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

import caliban.ScalaJSClientAdapter.*
import caliban.client.CalibanClientError.DecodingError
import caliban.client.Operations.RootQuery
import caliban.client.scalajs.DND5eClient.{Queries, Background as CalibanBackground, Campaign as CalibanCampaign, CampaignHeader as CalibanCampaignHeader, CampaignStatus as CalibanCampaignStatus, CharacterClass as CalibanCharacterClass, DiceRoll as CalibanDiceRoll, Encounter as CalibanEncounter, EncounterHeader as CalibanEncounterHeader, GameSystem as CalibanGameSystem, PlayerCharacter as CalibanPlayerCharacter, PlayerCharacterHeader as CalibanPlayerCharacterHeader, Scene as CalibanScene, SceneHeader as CalibanSceneHeader}
import caliban.client.{ScalarDecoder, SelectionBuilder}
import _root_.components.{Confirm, Toast}
import dmscreen.dnd5e.*
import dmscreen.sta.STACampaignState
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.TimerSupport
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom.*
import zio.json.*
import zio.json.ast.Json

import java.net.URI
import java.util.UUID
import scala.scalajs.js
import scala.scalajs.js.UndefOr

object Content {

  private val connectionId = UUID.randomUUID().toString

  case class State(dmScreenState: DMScreenState)

  class Backend($ : BackendScope[Unit, State]) {

    private var interval: js.UndefOr[js.timers.SetIntervalHandle] = js.undefined

    private def saveAll: Callback = {
      // Here's where the magic happens, we need to check if anything needs to be saved, if it does, we save it
      // Pick up the change stack from the state
      // We might want to stop the ticker while we do the save
      val ajax = for {
        state <- $.state.map(_.dmScreenState).asAsyncCallback
        state <- AsyncCallback.traverse(state.campaignState)(_.saveChanges())
        _     <- stopSaveTicker.asAsyncCallback
      } yield $.modState(s => s.copy(dmScreenState = s.dmScreenState.copy(campaignState = state.headOption)))

      ajax.completeWith(_.get)

    }

    private def startSaveTicker: Callback =
      Callback {
        interval = js.timers.setInterval(ClientConfiguration.live.saveCheckIntervalMS)(saveAll.runNow())
      }

    def stopSaveTicker =
      Callback {
        interval.foreach(js.timers.clearInterval)
        interval = js.undefined
      }

    def render(state: State): VdomNode = {
      DMScreenState.ctx.provide(state.dmScreenState) {
        <.div(
          ^.key    := "contentDiv",
          ^.height := 100.pct,
          Confirm.render(),
          Toast.render(),
          AppRouter.router(state.dmScreenState.campaignState.map(_.gameUI))()
        )
      }
    }

    def initialize(argCampaignId: Option[CampaignId] = None): Callback = {
      Callback.log(s"Selected campaign $argCampaignId") >>
        argCampaignId.fold(Callback.empty) { id =>
          // TODO this needs to be moved somewhere that's DND5e specific, so that we can have different game systems
          val ajax = for {
            oldState <- $.state.asAsyncCallback
            currentCampaignId <- AsyncCallback.pure {
              // If the campaign is in the URL, use it, otherwise use the one in the session storage if it exist, otherwise, for now use 1
              // But in the future, we just shouldn't show the other tabs and only show the home tab
              argCampaignId
                .orElse(
                  Option(window.sessionStorage.getItem("currentCampaignId"))
                    .flatMap(_.toLongOption).map(CampaignId.apply)
                )
                .getOrElse(CampaignId(1)) // Change this, we'll need to load campaigns from the server and select the first one)
            }
            // Store the current campaign Id in the session storage for next time
            _ <- AsyncCallback.pure(
              window.sessionStorage.setItem("currentCampaignId", currentCampaignId.value.toString)
            )
            _ <- Callback.log(s"Loading campaign data (campaign = $currentCampaignId) from server...").asAsyncCallback
            // First load the campaign, this will allow us to ask for the GameSystem-specific data
            campaignOpt <- GraphQLRepository.live.campaign(currentCampaignId)
            campaignState <- AsyncCallback.traverseOption(campaignOpt) { c =>
              c.header.gameSystem match {
                case GameSystem.dnd5e              => DND5eCampaignState.load(c)
                case GameSystem.starTrekAdventures => STACampaignState.load(c)
                case _ =>
                  AsyncCallback.throwException(RuntimeException(s"Unsupported game system ${c.header.gameSystem}"))
              }
            }
            _ <- Callback
              .log(s"Campaign data (${campaignOpt.fold("")(_.header.name)}) loaded from server").asAsyncCallback
          } yield $.modState { s =>
            s.copy(dmScreenState =
              s.dmScreenState
                .copy(
                  campaignState = campaignState,
                  changeDialogMode =
                    newMode => $.modState(s => s.copy(dmScreenState = s.dmScreenState.copy(dialogMode = newMode)))
                )
            )
          }
          for {
            _          <- Callback.log("Initializing Content Component")
            modedState <- ajax.completeWith(_.get)
          } yield modedState
        } >>
        // This needs to be initalized regardless
        $.modState(s =>
          s.copy(dmScreenState =
            s.dmScreenState.copy(
              onSelectCampaign = campaign => initialize(campaign.map(_.id)),
              onModifyCampaignState = (
                newState,
                log
              ) =>
                // if the ticker is on, do nothing, otherwise start it
                // ENHANCEMENT persist campaign log
                $.modState(
                  s =>
                    s.copy(
                      dmScreenState = s.dmScreenState.copy(
                        campaignState = Some(newState),
                        campaignLog = log.trim.headOption.map(_ => log.trim).toSeq ++ s.dmScreenState.campaignLog
                      )
                    ),
                  if (interval.isEmpty) {
                    startSaveTicker
                  } else {
                    Callback.empty
                  }
                )
            )
          )
        )
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("content")
    .initialState {
      // Get from window.sessionStorage anything that we may need later to fill in the state.
      State(dmScreenState = DMScreenState())
    }
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.initialize())
    .shouldComponentUpdatePure { $ =>
      $.nextState.dmScreenState.dialogMode == DialogMode.closed
    }
    .componentWillUnmount($ =>
      $.backend.stopSaveTicker >> Callback.log("Closing down operationStream")
//        >>
//        $.state.dmScreenState.operationStream.fold(Callback.empty)(_.close())
    )
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
