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

package dmscreen.dnd5e.components

import dmscreen.dnd5e.*
import dmscreen.dnd5e.components.ConditionsEditor.{Backend, Props, State}
import dmscreen.dnd5e.components.HitPointsEditor.State
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHS}
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

object OtherMarkersEditor {

  case class State(
    otherMarkers:     List[Marker],
    otherMarkerInput: String = ""
  )

  case class Props(
    otherMarkers: List[Marker],
    onChange:     List[Marker] => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      Table
        .compact(true)(
          Table.Body(
            state.otherMarkers.zipWithIndex.map(
              (
                marker,
                index
              ) =>
                Table.Row(
                  Table.Cell(marker.name),
                  Table.Cell(
                    Button
                      .compact(true)
                      .size(SemanticSIZES.tiny)
                      .title("Delete Marker")
                      .icon(true)(Icon.name(SemanticICONS.trash))
                      .onClick(
                        (
                          _,
                          _
                        ) =>
                          $.modState(
                            s => s.copy(otherMarkers = s.otherMarkers.patch(index, Nil, 1)),
                            $.state.flatMap(s => props.onChange(s.otherMarkers))
                          )
                      )
                  )
                )
            )*
          ),
          Table.Footer(
            Table.Row(
              Table.Cell(
                Input
                  .id("otherMarkerInput")
                  .value(state.otherMarkerInput)
                  .onChange(
                    (
                      _,
                      data
                    ) => {
                      val newVal = data.value match {
                        case s: String => s
                        case _ => state.otherMarkerInput
                      }
                      $.modState(_.copy(otherMarkerInput = newVal))
                    }
                  )
              ),
              Table.Cell(
                Button
                  .compact(true)
                  .size(SemanticSIZES.tiny)
                  .title("Add Marker")
                  .icon(true)(Icon.name(SemanticICONS.plus))
                  .disabled(state.otherMarkerInput.trim.isEmpty)
                  .onClick(
                    (
                      _,
                      _
                    ) =>
                      $.modState(
                        s => s.copy(otherMarkers = s.otherMarkers :+ Marker(s.otherMarkerInput), otherMarkerInput = ""),
                        $.state.flatMap(s => props.onChange(s.otherMarkers))
                      )
                  )
              )
            )
          )
        )
    }

  }
  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props) => "") // make sure the props are ignored for re-rendering the component

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("OtherMarkersEditor")
    .initialStateFromProps(p => State(p.otherMarkers))
    .renderBackend[Backend]
    .componentDidMount($ => Callback.empty)
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    otherMarkers: List[Marker],
    onChange:     List[Marker] => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(otherMarkers = otherMarkers, onChange = onChange))

}
