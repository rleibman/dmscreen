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

import _root_.components.{Confirm, Toast}
import caliban.ScalaJSClientAdapter.*
import caliban.client.CalibanClientError.DecodingError
import caliban.client.Operations.RootQuery
import caliban.client.scalajs.DND5eClient.{
  Background as CalibanBackground,
  Campaign as CalibanCampaign,
  CampaignHeader as CalibanCampaignHeader,
  CampaignStatus as CalibanCampaignStatus,
  CharacterClass as CalibanCharacterClass,
  DiceRoll as CalibanDiceRoll,
  Encounter as CalibanEncounter,
  EncounterHeader as CalibanEncounterHeader,
  GameSystem as CalibanGameSystem,
  PlayerCharacter as CalibanPlayerCharacter,
  PlayerCharacterHeader as CalibanPlayerCharacterHeader,
  Queries,
  Scene as CalibanScene,
  SceneHeader as CalibanSceneHeader
}
import caliban.client.{ScalarDecoder, SelectionBuilder}
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

  case class State(dmScreenState: DMScreenState = DMScreenState())

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

    private val defaultCssFiles: Seq[String] = Seq("css/sui-dmscreen.css")

    def DynamicStylesheet(ui: Option[GameUI]): TagMod =
      ScalaFnComponent
        .withHooks[Option[GameUI]]
        .render { gameUI =>
          val cssFiles = ui.fold(defaultCssFiles)(_.cssFiles)
          cssFiles.map(file => <.link(^.href := file, ^.rel := "stylesheet", ^.`type` := "text/css")).toVdomArray
        }.apply(ui)

    def render(state: State): VdomNode = {
      DMScreenState.ctx.provide(state.dmScreenState) {
        <.div(
          ^.key    := "contentDiv",
          ^.height := 100.pct,
          DynamicStylesheet(state.dmScreenState.campaignState.map(_.gameUI)),
          Confirm.render(),
          Toast.render(),
          AppRouter.router(
            state.dmScreenState.campaignState.map(_.campaignHeader.id),
            state.dmScreenState.campaignState.map(_.gameUI)
          )()
        )
      }
    }

    def loadState(argCampaignId: Option[CampaignId] = None): Callback = {
      // If the campaign is in the URL, use it, otherwise use the one in the session storage if it exist, otherwise, for now use 1
      // But in the future, we just shouldn't show the other tabs and only show the home tab
      val id = argCampaignId
        .orElse(
          Option(window.sessionStorage.getItem("currentCampaignId"))
            .flatMap(_.toLongOption)
            .map(CampaignId.apply)
        )
        .getOrElse(CampaignId(1)) // Change this, we'll need to load campaigns from the server and select the first one)

      val ajax = for {
        oldState <- $.state.asAsyncCallback
        _ <- AsyncCallback.pure(window.sessionStorage.setItem("currentCampaignId", id.value.toString)) // Store the current campaign Id in the session storage for next time
        _ <- Callback.log(s"Loading campaign data (campaign = $id) from server...").asAsyncCallback
        campaignOpt <- GraphQLRepository.live.campaign(id) // First load the campaign, this will allow us to ask for the GameSystem-specific data
        campaignState <- AsyncCallback.traverseOption(campaignOpt) { c =>
          c.header.gameSystem match {
            case GameSystem.dnd5e              => DND5eCampaignState.load(c)
            case GameSystem.starTrekAdventures => STACampaignState.load(c)
            case _ =>
              AsyncCallback.throwException(RuntimeException(s"Unsupported game system ${c.header.gameSystem}"))
          }
        }
        _ <- Callback.log(s"Campaign data (${campaignOpt.fold("")(_.header.name)}) loaded from server").asAsyncCallback
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

      ajax.completeWith(_.get) >>
        // We need to add the methods to the state
        $.modState(s =>
          s.copy(dmScreenState =
            s.dmScreenState.copy(
              onSelectCampaign = campaign => loadState(campaign.map(_.id)),
              onModifyCampaignState = (
                newState,
                log
              ) =>
                $.modState(
                  s =>
                    s.copy(
                      dmScreenState = s.dmScreenState.copy(
                        campaignState = Some(newState),
                        campaignLog = log.trim.headOption.map(_ => log.trim).toSeq ++ s.dmScreenState.campaignLog
                      )
                    ),
                  startSaveTicker.when(interval.isEmpty).map(_ => ())
                ),
              onForceSave = saveAll
            )
          )
        )
    }

  }

  import dmscreen.dnd5e.components.given

  given Reusability[CampaignId] = Reusability.long.contramap(_.value)
  given Reusability[UserId] = Reusability.long.contramap(_.value)
  given Reusability[GameSystem] = Reusability.int.contramap(_.ordinal)
  given Reusability[CampaignStatus] = Reusability.int.contramap(_.ordinal)
  given Reusability[DialogMode] = Reusability.int.contramap(_.ordinal)
  given Reusability[Json] = Reusability.string.contramap(_.toString)
  given Reusability[CampaignHeader] = Reusability.by(c => (c.id, c.campaignStatus, c.name, c.gameSystem, c.dmUserId))
  given Reusability[Campaign] = Reusability.by(c => (c.header, c.jsonInfo))
  given rsr: Reusability[Seq[Race]] = Reusability.list[Race].contramap(_.toList)
  given rsb: Reusability[Seq[Background]] = Reusability.list[Background].contramap(_.toList)
  given rsc: Reusability[Seq[CharacterClass]] = Reusability.list[CharacterClass].contramap(_.toList)

  given Reusability[CampaignState] =
    Reusability[CampaignState] {
      case (s1: DND5eCampaignState, s2: DND5eCampaignState) =>
        Reusability.by((s: DND5eCampaignState) => (s.campaign, s.races, s.backgrounds, s.classes)).test(s1, s2)
      case (s1: STACampaignState, s2: STACampaignState) =>
        Reusability.by((s: STACampaignState) => s.campaign).test(s1, s2)
      case _ => false

    }

  // Note, we don't want to update while a dialog is open
  given Reusability[DMScreenState] =
    Reusability.by((s: DMScreenState) => (s.dialogMode == DialogMode.closed, s.campaignState))
  given Reusability[State] = Reusability.by((s: State) => s.dmScreenState)

  private val component = ScalaComponent
    .builder[Unit]("content")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState())
    .configure(Reusability.shouldComponentUpdate)
    .componentWillUnmount($ =>
      $.backend.stopSaveTicker >> Callback.log("Closing down operationStream")
//        >>
//        $.state.dmScreenState.operationStream.fold(Callback.empty)(_.close())
    )
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
