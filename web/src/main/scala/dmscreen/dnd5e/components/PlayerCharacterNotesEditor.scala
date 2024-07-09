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
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.reactQuill.components.ReactQuill
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

object PlayerCharacterNotesEditor {

  case class State(
    notes: String,
    // Enhancement: all the below
    personalityTraits: String = "",
    ideals:            String = "",
    bonds:             String = "",
    flaws:             String = ""
  )

  case class Props(
    notes:             String,
    personalityTraits: String = "",
    ideals:            String = "",
    bonds:             String = "",
    flaws:             String = "",
    onChange: (String, String, String, String, String) => Callback = (
      _,
      _,
      _,
      _,
      _
    ) => Callback.empty
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      ReactQuill
        .value(state.notes).onChange(
          (
            newValue,
            _,
            _,
            _
          ) => $.modState(_.copy(notes = newValue), $.state.flatMap(s => props.onChange(s.notes.trim, "", "", "", "")))
        )
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props).notes)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("PlayerCharacterNotesEditor")
    .initialStateFromProps(p => State(p.notes))
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    notes:             String,
    personalityTraits: String = "",
    ideals:            String = "",
    bonds:             String = "",
    flaws:             String = "",
    onChange: (String, String, String, String, String) => Callback = (
      _,
      _,
      _,
      _,
      _
    ) => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(notes, personalityTraits, ideals, bonds, flaws, onChange))

}
