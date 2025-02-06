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

object LanguageEditor {

  case class State(
    languages: Map[Int, Language]
  )

  case class Props(
    languages: Seq[Language],
    onChange:  Set[Language] => Callback
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      Table
        .inverted(DND5eUI.tableInverted)
        .color(DND5eUI.tableColor)(
          Table.Body(state.languages.toList.zipWithIndex.map {
            (
              lang,
              i
            ) =>
              Table.Row.withKey(lang._1.toString)(
                Table.Cell(
                  Input
                    .value(lang._2.name)
                    .onChange(
                      (
                        _,
                        data
                      ) => {
                        val newVal = data.value match {
                          case s: String => s
                          case _ => lang._2.name
                        }
                        $.modState(
                          s => s.copy(languages = s.languages + (lang._1 -> Language(newVal))),
                          $.state.flatMap(s => props.onChange(s.languages.values.filter(_.name.trim.nonEmpty).toSet))
                        )
                      }
                    )
                ),
                Table.Cell(
                  Button
                    .title("Delete this language")
                    .icon(true).onClick {
                      (
                        _,
                        _
                      ) =>
                        $.modState(
                          s => s.copy(languages = s.languages.filter(_ != lang)),
                          $.state.flatMap(s => props.onChange(s.languages.values.toSet))
                        )
                    }(Icon.name(SemanticICONS.delete))
                )
              )
          }*),
          Table.Footer(
            Table.Row(
              Table.Cell.colSpan(4)(
                Button
                  .title("Add a language")
                  .icon(true).onClick(
                    (
                      _,
                      _
                    ) =>
                      $.modState(
                        s => s.copy(languages = s.languages + (UUID.randomUUID().toString.hashCode() -> Language(""))),
                        $.state.flatMap(s => props.onChange(s.languages.values.filter(_.name.trim.nonEmpty).toSet))
                      )
                  )(Icon.name(SemanticICONS.add))
              )
            )
          )
        )

    }

  }

  import scala.language.unsafeNulls

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("LanguageEditor")
    .initialStateFromProps(p => State(p.languages.map(l => l.name.hashCode() -> l).toMap))
    .renderBackend[Backend]
    .build

  def apply(
    languages: Set[Language],
    onChange:  Set[Language] => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(languages = languages.toSeq, onChange = onChange))

}
