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
import dmscreen.dnd5e.components.HealthEditor.State
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

import java.util.UUID

object FeatsEditor {

  case class State(
    feats: Map[String, Feat]
  )

  case class Props(
    feats:    List[Feat],
    onChange: List[Feat] => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      Table
        .inverted(DND5eUI.tableInverted)
        .color(DND5eUI.tableColor)(
          Table.Body(state.feats.toList.zipWithIndex.map {
            case (
                  (id, feat),
                  i
                ) =>
              Table.Row
                .withKey(id)(
                  Table.Cell(
                    Input
                      .value(feat.name)
                      .onChange(
                        (
                          _,
                          data
                        ) => {
                          val newVal = data.value match {
                            case s: String => s
                            case _ => feat.name
                          }
                          $.modState(
                            s => s.copy(feats = s.feats + (id -> Feat(newVal))),
                            $.state.flatMap(s => props.onChange(s.feats.values.toList.filter(_.name.trim.nonEmpty)))
                          )
                        }
                      )
                  ),
                  Table.Cell(
                    Button
                      .title("Delete this feat")
                      .icon(true).onClick {
                        (
                          _,
                          _
                        ) =>
                          $.modState(
                            s => s.copy(feats = s.feats.removed(id)),
                            $.state.flatMap(s => props.onChange(s.feats.values.toList))
                          )
                      }(Icon.name(SemanticICONS.delete))
                  )
                )
          }*),
          Table.Footer(
            Table.Row(
              Table.Cell.colSpan(4)(
                Button
                  .title("Add a feat")
                  .icon(true).onClick(
                    (
                      _,
                      _
                    ) =>
                      $.modState(
                        s => s.copy(feats = s.feats + (UUID.randomUUID().toString -> Feat(""))),
                        $.state.flatMap(s => props.onChange(s.feats.values.toList.filter(_.name.trim.nonEmpty)))
                      )
                  )(Icon.name(SemanticICONS.add))
              )
            )
          )
        )

    }

  }

  import scala.language.unsafeNulls

  given Ordering[Feat] = Ordering.by(_.name)
  given Reusability[State] = Reusability.by((s: State) => s.feats.mkString)
  given Reusability[Props] = Reusability.by((_: Props) => "") // make sure the props are ignored for re-rendering the component

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("FeatsEditor")
    .initialStateFromProps(p => State(p.feats.map(UUID.randomUUID().toString -> _).toMap))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    feats:    List[Feat],
    onChange: List[Feat] => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(feats = feats, onChange = onChange))

}
