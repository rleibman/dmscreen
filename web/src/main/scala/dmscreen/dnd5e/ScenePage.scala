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

import dmscreen.dnd5e.NPCPage.State
import dmscreen.dnd5e.components.{EditableText, PlayerCharacterComponent}
import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, Reusability, ScalaComponent, ScalaFnComponent}
import net.leibman.dmscreen.reactQuill.components.ReactQuill
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES}
import zio.json.*

import scala.util.Random

object ScenePage extends DMScreenTab {

  case class State(editingScene: Option[Scene] = None)
  case class Props(
  )

  class Backend($ : BackendScope[Props, State]) {

    def render(state: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          def modScene(
            scene: Scene,
            log:   String
          ): Callback = {
            dmScreenState.onModifyCampaignState(
              campaignState.copy(
                scenes = campaignState.scenes.map(s => if (s.header.id == scene.header.id) scene else s),
                changeStack = campaignState.changeStack.logSceneChanges(scene.header.id)
              ),
              log
            )
          }

          def EditModal(scene: Scene) = {
            val doClose = $.modState(_.copy(editingScene = None))

            Modal
              .onClose(
                (
                  _,
                  _
                ) => doClose
              )
              .withKey("editModal")
              .open(true)(
                Modal.Header(Header.as("h1")(s"Scene Details"), Header.as("h2")(scene.header.name)),
                Modal.Content(
                  Form(
                    ReactQuill
                      .defaultValue(scene.info.notes)
                      .onChange(
                        (
                          newValue,
                          _,
                          _,
                          _
                        ) =>
                          modScene(scene.copy(jsonInfo = scene.info.copy(notes = newValue).toJsonAST.toOption.get), "")
                      )
                  )
                ),
                Modal.Actions(
                  Button("Close").onClick(
                    (
                      _,
                      _
                    ) => doClose
                  )
                )
              )
          }

          val campaign = campaignState.campaign
          val activeSceneId = campaignState.scenes.find(_.header.isActive).map(_.header.id).getOrElse(SceneId.empty)
          VdomArray(
            state.editingScene.fold(EmptyVdom)(EditModal),
            Table.withKey("SceneTable")(
              Table.Header.withKey("SceneTableHeader")(
                Table.Row.withKey("title")(
                  Table.HeaderCell.colSpan(2)(Header.as("h1")("Scenes")),
                  Table.HeaderCell(
                    Button
                      .title("Add Scene")
                      .compact(true)
                      .onClick(
                        (
                          _,
                          _
                        ) => {
                          val orderCol = campaignState.scenes.size
                          val newScene = Scene(
                            header = SceneHeader(
                              id = SceneId.empty,
                              campaignId = campaign.header.id,
                              name = s"Scene #${orderCol + 1}",
                              orderCol = orderCol,
                              isActive = false
                            ),
                            jsonInfo = SceneInfo().toJsonAST.toOption.get
                          )

                          GraphQLRepository.live
                            .upsert(header = newScene.header, info = newScene.jsonInfo)
                            .map(id =>
                              dmScreenState
                                .onModifyCampaignState(
                                  campaignState.copy(scenes =
                                    campaignState.scenes :+ newScene.copy(header = newScene.header.copy(id = id))
                                  ),
                                  "Added a new scene"
                                )
                            )
                            .completeWith(_.get)
                        }
                      )
                      .icon(true)(Icon.name(SemanticICONS.`plus circle`))
                  )
                ),
                Table.Row.withKey("headerCells")(
                  Table.HeaderCell("Active"),
                  Table.HeaderCell("Name"),
                  Table.HeaderCell("")
                )
              ),
              Table.Body.withKey("SceneTableBody")(
                campaignState.scenes
                  .sortBy(_.header.orderCol)
                  .map(scene => {
                    Table.Row.withKey(s"scene${scene.header.id.value}")(
                      Table.Cell(
                        Radio
                          .name("activeScene") // This is required so that the radio buttons work correctly
                          .checked(activeSceneId == scene.header.id)
                          .value(scene.header.id.value.toDouble)
                          .onChange {
                            (
                              _,
                              d
                            ) =>
                              val newScenes = campaignState.scenes.map(s =>
                                s.copy(header =
                                  s.header
                                    .copy(isActive = s.header.id == scene.header.id && d.checked.getOrElse(false))
                                )
                              )
                              val changedSceneIds = newScenes
                                .zip(campaignState.scenes)
                                .filter(_.header.isActive != _.header.isActive)
                                .map(_._1.header.id)
                              dmScreenState.onModifyCampaignState(
                                campaignState.copy(
                                  scenes = newScenes,
                                  changeStack = campaignState.changeStack.logSceneChanges(changedSceneIds*)
                                ),
                                s"Made scene ${scene.header.name} active"
                              )
                          }
                      ),
                      Table.Cell(
                        EditableText(
                          value = scene.header.name,
                          onChange = newValue => modScene(scene.copy(header = scene.header.copy(name = newValue)), "")
                        )
                      ),
                      Table.Cell(
                        Button()
                          .compact(true).size(SemanticSIZES.mini).icon(true).onClick(
                            (
                              _,
                              _
                            ) => $.modState(_.copy(editingScene = Some(scene)))
                          )(Icon.name(SemanticICONS.`edit`)).title(
                            "Edit Scene"
                          ),
                        Button()
                          .compact(true).size(SemanticSIZES.mini).icon(true).onClick(
                            (
                              _,
                              _
                            ) =>
                              _root_.components.Confirm.confirm(
                                question = s"Are you sure you want to delete this scene (${scene.header.name})?",
                                onConfirm = GraphQLRepository.live
                                  .deleteEntity(entityType = DND5eEntityType.scene, id = scene.header.id)
                                  .map(_ =>
                                    dmScreenState.onModifyCampaignState(
                                      campaignState
                                        .copy(scenes =
                                          campaignState.scenes
                                            .filterNot(_.header.id == scene.header.id) // Remove the delete scene
                                            .sortBy(_.header.orderCol) // Reorder the remaining scenes
                                            .zipWithIndex
                                            .map(
                                              (
                                                scene,
                                                index
                                              ) => scene.copy(header = scene.header.copy(orderCol = index))
                                            )
                                        ),
                                      s"Deleted scene ${scene.header.name}"
                                    )
                                  )
                                  .completeWith(_.get)
                              )
                          )(Icon.name(SemanticICONS.trash)).title(
                            "Delete Scene"
                          ),
                        Button()
                          .compact(true).size(SemanticSIZES.mini).icon(true).onClick(
                            (
                              _,
                              _
                            ) =>
                              dmScreenState.onModifyCampaignState(
                                {

                                  val changedScenes: List[(Scene, List[SceneId])] = campaignState.scenes.map(s =>
                                    if (s.header.orderCol == scene.header.orderCol)
                                      (
                                        s.copy(header = s.header.copy(orderCol = s.header.orderCol - 1)),
                                        List(s.header.id)
                                      )
                                    else if (s.header.orderCol == scene.header.orderCol - 1)
                                      (
                                        s.copy(header = s.header.copy(orderCol = s.header.orderCol + 1)),
                                        List(s.header.id)
                                      )
                                    else
                                      (s, List.empty)
                                  )

                                  campaignState.copy(
                                    scenes = changedScenes.map(_._1),
                                    changeStack =
                                      campaignState.changeStack.logSceneChanges(changedScenes.flatMap(_._2)*)
                                  )
                                },
                                ""
                              )
                          )(Icon.name(SemanticICONS.`arrow up`)).title(
                            "Move Up"
                          ).disabled(!(scene.header.orderCol > 0)),
                        Button()
                          .compact(true).size(SemanticSIZES.mini).icon(true).onClick(
                            (
                              _,
                              _
                            ) =>
                              dmScreenState.onModifyCampaignState(
                                {
                                  val changedScenes: List[(Scene, List[SceneId])] = campaignState.scenes.map(s =>
                                    if (s.header.orderCol == scene.header.orderCol)
                                      (
                                        s.copy(header = s.header.copy(orderCol = s.header.orderCol + 1)),
                                        List(s.header.id)
                                      )
                                    else if (s.header.orderCol == scene.header.orderCol + 1)
                                      (
                                        s.copy(header = s.header.copy(orderCol = s.header.orderCol - 1)),
                                        List(s.header.id)
                                      )
                                    else
                                      (s, List.empty)
                                  )

                                  campaignState.copy(
                                    scenes = changedScenes.map(_._1),
                                    changeStack =
                                      campaignState.changeStack.logSceneChanges(changedScenes.flatMap(_._2)*)
                                  )
                                },
                                ""
                              )
                          )(Icon.name(SemanticICONS.`arrow down`)).title(
                            "Move Down"
                          ).disabled(!(scene.header.orderCol < campaignState.scenes.size - 1))
                      )
                    )
                  })*
              )
            )
          )
        }
      }
    }

  }

  private val component = ScalaComponent
    .builder[Props]("router")
    .initialStateFromProps(_ => State())
    .renderBackend[Backend]
    .build

  def apply(
  ): Unmounted[Props, State, Backend] = component(Props())

}
