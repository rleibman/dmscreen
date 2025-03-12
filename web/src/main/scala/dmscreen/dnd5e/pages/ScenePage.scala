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

package dmscreen.dnd5e.pages

import dmscreen.components.EditableText
import dmscreen.dnd5e.components.PCEditComponent
import dmscreen.dnd5e.pages.EncounterPage.State
import dmscreen.dnd5e.pages.NPCPage.State
import dmscreen.dnd5e.{*, given}
import dmscreen.{CampaignId, DMScreenState, DMScreenTab}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, Reusability, ScalaComponent, ScalaFnComponent}
import net.leibman.dmscreen.reactQuill.components.ReactQuill
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.{List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHS}
import zio.json.*

import scala.util.Random

object ScenePage extends DMScreenTab {

  case class State(
    npcs:         Seq[NonPlayerCharacter] = Seq.empty,
    scenes:       Seq[Scene] = Seq.empty,
    sceneNpcMap:  Map[SceneId, Seq[NonPlayerCharacterId]] = Map.empty,
    editingScene: Option[Scene] = None
  )

  class Backend($ : BackendScope[CampaignId, State]) {

    def loadState(campaignId: CampaignId): Callback = {
      (for {
        scenes      <- DND5eGraphQLRepository.live.scenes(campaignId)
        npcs        <- DND5eGraphQLRepository.live.nonPlayerCharacters(campaignId)
        sceneNpcMap <- DND5eGraphQLRepository.live.npcsForScene(campaignId)
      } yield $.modState(_.copy(npcs = npcs, scenes = scenes, sceneNpcMap = sceneNpcMap))).completeWith(_.get)

    }

    def render(state: State): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>
          def modScene(
            scene: Scene,
            log:   String
          ): Callback = {
            $.modState(
              s => s.copy(scenes = s.scenes.map(s => if (s.header.id == scene.header.id) scene else s)),
              dmScreenState.onModifyCampaignState(
                campaignState.copy(changeStack = campaignState.changeStack.logSceneChanges(scene)),
                log
              )
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
                    Form
                      .Field(
                        Label("Non Player Characters"),
                        state.npcs.map { npc =>
                          <.div(
                            ^.key := s"${npc.header.id.value}_${scene.header.id.value}",
                            <.span(
                              Checkbox
                                .checked(
                                  state.sceneNpcMap.exists(t => t._1 == scene.id && t._2.contains(npc.header.id))
                                )
                                .onChange {
                                  (
                                    _,
                                    changedData
                                  ) =>
                                    (if (changedData.checked.getOrElse(false))
                                       DND5eGraphQLRepository.live.addNpcToScene(scene.header.id, npc.header.id)
                                     else
                                       DND5eGraphQLRepository.live.removeNpcFromScene(scene.header.id, npc.header.id))
                                      .map(_ =>
                                        $.modState(
                                          s =>
                                            s.copy(
                                              sceneNpcMap = s.sceneNpcMap
                                                .updated(
                                                  scene.id,
                                                  if (changedData.checked.getOrElse(false))
                                                    s.sceneNpcMap.getOrElse(scene.id, Seq.empty) :+ npc.header.id
                                                  else
                                                    s.sceneNpcMap
                                                      .getOrElse(scene.id, Seq.empty).filterNot(_ == npc.header.id)
                                                )
                                            ),
                                          dmScreenState.log(s"Added ${npc.header.name} to scene ${scene.header.name}")
                                        )
                                      ).completeWith(_.get)
                                }
                            ),
                            <.span(
                              s"${npc.header.name} (${(npc.info.race.name +: npc.info.classes.map(_.characterClass.name)).distinct.mkString(", ")})"
                            )
                          )
                        }.toVdomArray
                      ).when(state.npcs.nonEmpty),
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
          val activeSceneId = state.scenes.find(_.header.isActive).map(_.header.id).getOrElse(SceneId.empty)
          VdomArray(
            state.editingScene.fold(EmptyVdom)(EditModal),
            Table
              .inverted(DND5eUI.tableInverted)
              .color(DND5eUI.tableColor)
              .withKey("SceneTable")(
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
                            val orderCol = state.scenes.size
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

                            DND5eGraphQLRepository.live
                              .upsert(header = newScene.header, info = newScene.jsonInfo)
                              .map(id =>
                                $.modState(
                                  s =>
                                    s.copy(
                                      scenes = s.scenes :+ newScene.copy(header = newScene.header.copy(id = id)),
                                      editingScene = Some(newScene.copy(header = newScene.header.copy(id = id)))
                                    ),
                                  dmScreenState.log(s"Added a new scene")
                                )
                              )
                              .completeWith(_.get)
                          }
                        )
                        .icon(true)(Icon.name(SemanticICONS.`plus circle`))
                    )
                  ),
                  Table.Row.withKey("headerCells")(
                    Table.HeaderCell.width(SemanticWIDTHS.`1`)("Active"),
                    Table.HeaderCell("Name"),
                    Table.HeaderCell("")
                  )
                ),
                Table.Body.withKey("SceneTableBody")(
                  state.scenes
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
                                modScene(
                                  scene = scene.copy(header = scene.header.copy(isActive = d.checked.getOrElse(false))),
                                  ""
                                )
                            }
                        ),
                        Table.Cell(
                          <.p(
                            ^.fontWeight.bold,
                            ^.fontSize.larger,
                            EditableText(
                              value = scene.header.name,
                              onChange =
                                newValue => modScene(scene.copy(header = scene.header.copy(name = newValue)), "")
                            )
                          ),
                          <.p(
                            ^.dangerouslySetInnerHtml := scene.info.notes.trim
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
                                  onConfirm = DND5eGraphQLRepository.live
                                    .deleteEntity(entityType = DND5eEntityType.scene, id = scene.header.id)
                                    .map(_ =>
                                      $.modState(
                                        s =>
                                          s.copy(
                                            scenes = s.scenes
                                              .filterNot(_.header.id == scene.header.id)
                                              .sortBy(_.header.orderCol)
                                              .zipWithIndex
                                              .map(
                                                (
                                                  scene,
                                                  index
                                                ) => scene.copy(header = scene.header.copy(orderCol = index))
                                              )
                                          ),
                                        dmScreenState.log(s"Deleted scene ${scene.header.name}")
                                      )
                                    )
                                    .completeWith(_.get)
                                )
                            )(Icon.name(SemanticICONS.trash)).title(
                              "Delete Scene"
                            ),
                          Button()
                            .compact(true).size(SemanticSIZES.mini).icon(true).onClick {
                              (
                                _,
                                _
                              ) =>
                                val changedScenes: Seq[(Scene, Option[Scene])] = state.scenes
                                  .map { s =>
                                    if (s.header.orderCol == scene.header.orderCol)
                                      (
                                        s.copy(header = s.header.copy(orderCol = s.header.orderCol - 1)),
                                        Option(s)
                                      )
                                    else if (s.header.orderCol == scene.header.orderCol - 1)
                                      (
                                        s.copy(header = s.header.copy(orderCol = s.header.orderCol + 1)),
                                        Option(s)
                                      )
                                    else
                                      (s, None)
                                  }

                                val changedScenesOnly = changedScenes.flatMap(_._2)

                                $.modState(
                                  state => {
                                    state.copy(
                                      scenes = changedScenes.map(_._1)
                                    )
                                  },
                                  $.state.flatMap(_ =>
                                    dmScreenState.onModifyCampaignState(
                                      // enhancement, we only need to add the scenes that changed, but for now this is easier to write
                                      campaignState
                                        .copy(changeStack =
                                          campaignState.changeStack.logSceneChanges(changedScenesOnly*)
                                        ),
                                      ""
                                    )
                                  )
                                )
                            }(Icon.name(SemanticICONS.`arrow up`)).title(
                              "Move Up"
                            ).disabled(!(scene.header.orderCol > 0)),
                          Button()
                            .compact(true).size(SemanticSIZES.mini).icon(true).onClick {
                              (
                                _,
                                _
                              ) =>

                                val changedScenes: Seq[(Scene, Option[Scene])] = state.scenes
                                  .map { s =>
                                    if (s.header.orderCol == scene.header.orderCol)
                                      (
                                        s.copy(header = s.header.copy(orderCol = s.header.orderCol + 1)),
                                        Option(s)
                                      )
                                    else if (s.header.orderCol == scene.header.orderCol + 1)
                                      (
                                        s.copy(header = s.header.copy(orderCol = s.header.orderCol - 1)),
                                        Option(s)
                                      )
                                    else
                                      (s, None)
                                  }

                                val changedScenesOnly = changedScenes.flatMap(_._2)

                                $.modState(
                                  state => {
                                    state.copy(
                                      scenes = changedScenes.map(_._1)
                                    )
                                  },
                                  $.state.flatMap(_ =>
                                    dmScreenState.onModifyCampaignState(
                                      campaignState
                                        .copy(changeStack =
                                          campaignState.changeStack.logSceneChanges(changedScenesOnly*)
                                        ),
                                      ""
                                    )
                                  )
                                )
                            }(Icon.name(SemanticICONS.`arrow down`)).title(
                              "Move Down"
                            ).disabled(!(scene.header.orderCol < state.scenes.size - 1))
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
    .builder[CampaignId]("router")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props))
    .build

  def apply(campaignId: CampaignId): Unmounted[CampaignId, State, Backend] = component(campaignId)

}
