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

package dmscreen.pages

import dmscreen.components.EditableText
import dmscreen.dnd5e.{DND5eGraphQLRepository, DND5eUI}
import dmscreen.{*, given}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Generic.UnmountedRaw
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import org.scalajs.dom.{StorageEvent, window}
import zio.json.*

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object HomePage extends DMScreenPage {

  case class State(
    hideArchived:      Boolean = true,
    confirmDeleteOpen: Boolean = false,
    deleteMe:          Option[CampaignId] = None,
    campaigns:         Seq[CampaignHeader] = Seq.empty,
    newCampaign:       Option[CampaignHeader] = None
  )

  class Backend($ : BackendScope[Unit, State]) {

    def loadState: Callback = {
      DMScreenGraphQLRepository.live.campaigns
        .map { campaigns =>
          $.modState(_.copy(campaigns = campaigns))
        }.completeWith(_.get)
    }

    def ConfirmDelete(
      open:      Boolean,
      onConfirm: => Callback = Callback.empty,
      onCancel:  => Callback = Callback.empty
    ): UnmountedRaw = {
      case class ConfirmDeleteState(
        deleteString: String = ""
      )
      case class ConfirmDeleteProps(
        open:      Boolean,
        onConfirm: Callback,
        onCancel:  Callback
      )
      ScalaFnComponent
        .withHooks[ConfirmDeleteProps]
        .useState(ConfirmDeleteState())
        .render {
          (
            props,
            state
          ) =>
            Modal
              .open(props.open)
              .closeIcon(true).onClose(
                (
                  _,
                  _
                ) => props.onCancel
              )(
                Modal.Header("Confirm"),
                Modal.Content(
                  "Are you sure you want to delete this campaign? please write 'DELETE' in the box below",
                  Input.value(state.value.deleteString).onChange {
                    (
                      _,
                      data
                    ) =>
                      val newVal = data.value match {
                        case s: String => s
                        case _ => state.value.deleteString
                      }
                      state.modState(s => s.copy(deleteString = newVal))
                  }
                ),
                Modal.Actions(
                  Button
                    .secondary(true)("Cancel").onClick(
                      (
                        _,
                        _
                      ) => onCancel
                    ),
                  Button
                    .primary(true)
                    .disabled(state.value.deleteString != "DELETE")("Ok").onClick(
                      (
                        _,
                        _
                      ) => onConfirm
                    )
                )
              )
        }
        .apply(ConfirmDeleteProps(open, onConfirm, onCancel))
    }

    def render(state: State): VdomNode = {

      DMScreenState.ctx.consume { dmScreenState =>
        def modCampaignHeader(campaignHeader: CampaignHeader): Callback = {
          $.modState(
            s =>
              s.copy(campaigns = s.campaigns.map { c =>
                if (c.id == campaignHeader.id) campaignHeader else c
              }),
            // after setting it locally, persist it.
            (for {
              campaign <- DMScreenGraphQLRepository.live.campaign(campaignHeader.id)
              _ <- DMScreenGraphQLRepository.live
                .upsert(campaignHeader, campaign.get.jsonInfo).when(campaign.isDefined)
            } yield Callback.empty).completeWith(_.get)
          )
        }

        VdomArray(
          Header.as("h1")(
            s"Welcome ${dmScreenState.user.fold("")(_.name)}",
//            Button.onClick {
//              (
//                _,
//                _
//              ) =>
//                import sttp.client4.*
//
//                val request = basicRequest.get(uri"/api/test")
//
//                dmscreen.util.ApiClient
//                  .apiCall[String, String](
//                    request
//                  ).map { response =>
//                    response.body match {
//                      case Left(e)    => Callback.log(s"Error: $e")
//                      case Right(str) => Callback.log(s"Success: $str")
//                    }
//                  }
//                  .completeWith(_.get)
//
//            }("Test Refresh"),
            Checkbox
              .checked(state.hideArchived)
              .onClick(
                (
                  _,
                  d
                ) => $.modState(_.copy(hideArchived = d.checked.getOrElse(false)))
              ),
            Label.size(SemanticSIZES.big)("Hide archived campaigns")
          ),
          ConfirmDelete(
            open = state.confirmDeleteOpen,
            onConfirm = {
              val async =
                for {
                  _ <- AsyncCallback
                    .traverse(state.deleteMe)(id =>
                      DMScreenGraphQLRepository.live
                        .deleteCampaign(state.deleteMe.get)
                    )
                } yield Callback(window.localStorage.removeItem("campaignId")) >> loadState >> dmScreenState
                  .onSelectCampaign(None) >> $.modState(
                  _.copy(confirmDeleteOpen = false, deleteMe = None)
                )
              async.completeWith(_.get)
            },
            onCancel = $.modState(_.copy(confirmDeleteOpen = false, deleteMe = None))
          ),
          state.newCampaign.fold(EmptyVdom) { newCampaign =>
            Modal
              .withKey("addCampaignModal")
              .open(true)
              .closeIcon(true).onClose(
                (
                  _,
                  _
                ) => $.modState(_.copy(newCampaign = None))
              )(
                Modal.Header("Add Campaign"),
                Modal.Content(
                  Form(
                    Form.Input.label("Name").value(newCampaign.name).onChange {
                      (
                        _,
                        data
                      ) =>
                        val newVal: String = data.value match {
                          case s: String => s
                          case _ => newCampaign.name
                        }
                        $.modState(s => s.copy(newCampaign = s.newCampaign.map(_.copy(name = newVal))))
                    },
                    Form
                      .Select(
                        GameSystem.values
                          .map(value => DropdownItemProps().setValue(value.ordinal).setText(value.name)).toJSArray
                      )
                      .label("Game System")
                      .value(newCampaign.gameSystem.ordinal)
                      .onChange {
                        (
                          _,
                          data
                        ) =>
                          val newVal: GameSystem = data.value match {
                            case i: Double => GameSystem.fromOrdinal(i.toInt)
                            case _ => newCampaign.gameSystem
                          }
                          $.modState(s => s.copy(newCampaign = s.newCampaign.map(_.copy(gameSystem = newVal))))
                      }
                  )
                ),
                Modal.Actions(
                  Button
                    .secondary(true)("Cancel").onClick(
                      (
                        _,
                        _
                      ) => $.modState(_.copy(newCampaign = None))
                    ),
                  Button.primary(true)("Ok").onClick {
                    (
                      _,
                      _
                    ) =>
                      (for {
                        _ <- DMScreenGraphQLRepository.live
                          .upsert(newCampaign, CampaignInfo(notes = "").toJsonAST.toOption.get)
                        reloaded <- DMScreenGraphQLRepository.live.campaigns
                      } yield $.modState(_.copy(newCampaign = None, campaigns = reloaded))).completeWith(_.get)
                  }
                )
              )
          },
          Table
            .inverted(DND5eUI.tableInverted)
            .color(DND5eUI.tableColor)
            .withKey("campaignTable")(
              Table.Header(
                Table
                  .Row(
                    Table.HeaderCell("Status"),
                    Table.HeaderCell("Campaign Name"),
                    Table.HeaderCell("Game System"),
                    Table.HeaderCell( /*Actions*/ )
                  )
              ),
              Table.Body(
                state.campaigns.collect {
                  case campaign if !(state.hideArchived && campaign.campaignStatus == CampaignStatus.archived) =>
                    Table.Row.withKey(s"Campaign${campaign.id.value}")(
                      Table.Cell(
                        // Enhancement, might want to color the table differently if it's the active campaign
                        if (dmScreenState.campaignState.exists(_.campaignHeader.id == campaign.id)) "Current"
                        else "Not Current"
                      ),
                      Table.Cell(
                        EditableText(
                          value = s"${campaign.name}${
                              if (campaign.campaignStatus == CampaignStatus.archived) " (Archived)" else ""
                            }",
                          allowEditing = true,
                          onChange = name => modCampaignHeader(campaign.copy(name = name))
                        )
                      ),
                      Table.Cell(campaign.gameSystem.name),
                      Table.Cell(
                        Button
                          .title("Delete Campaign").icon(true)(Icon.name(SemanticICONS.`trash`)).onClick(
                            (
                              _,
                              _
                            ) => $.modState(_.copy(confirmDeleteOpen = true, deleteMe = Some(campaign.id)))
                          ),
                        Button
                          .title("Make this the current campaign").icon(true)(
                            Icon.name(SemanticICONS.`check circle outline`)
                          ).onClick {
                            (
                              _,
                              _
                            ) =>
                              window.localStorage.setItem("campaignId", s"${campaign.id.value}")
                              dmScreenState.onSelectCampaign(Some(campaign))
                          }.when(dmScreenState.campaignState.fold(true)(_.campaignHeader.id != campaign.id)),
                        Button
                          .title("Take a snapshot of this campaign").icon(true)(
                            Icon.name(SemanticICONS.`camera`)
                          ).onClick(
                            (
                              _,
                              _
                            ) =>
                              DMScreenGraphQLRepository.live
                                .snapshotCampaign(campaign.id)
                                .map(_ => dmScreenState.onSelectCampaign(Some(campaign)))
                                .completeWith(_.get)
                          ),
                        Button
                          .title("Archive this campaign")
                          .icon(true)(Icon.name(SemanticICONS.archive))
                          .onClick {
                            (
                              _,
                              _
                            ) => modCampaignHeader(campaign.copy(campaignStatus = CampaignStatus.archived))
                          }.when(
                            campaign.campaignStatus != CampaignStatus.archived
                          )
                      )
                    )
                }*
              ),
              Table.Footer(
                Table.Row(
                  Table.HeaderCell.colSpan(4)(
                    Button("Add Campaign").onClick(
                      (
                        _,
                        _
                      ) =>
                        $.modState(
                          _.copy(newCampaign = Some(CampaignHeader.empty(dmScreenState.user.fold(UserId.empty)(_.id))))
                        )
                    )
                  )
                )
              )
            )
        )
      }
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("router")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState)
    .build

  def apply(
  ): Unmounted[Unit, State, Backend] = component()

}
