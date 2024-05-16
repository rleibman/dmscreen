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

import dmscreen.DMScreenTab

object PlayersTab extends DMScreenTab {

  case class State(pcs: Seq[PlayerCharacter] = Seq.empty)

  class Backend($ : BackendScope[Unit, State]) {

    def render(s: State) = {
      <.div(
        s.pcs.map { pc =>
          <.div(
            pc.info.name,
            pc.info.alignment,
            pc.info.race.name,
            pc.info.faith,
            pc.info.classes.map(c => s"${c.name}: ${c.subclass} (l ${c.level})"),
            mkString(", "),
            pc.info.abilities.map(a => <.div(^.className = "shortAbility", s"${a.abilityType.short}${a.value}")),
            <.div(^.className = "hitPoints", s"${pc.info.baseHitPoints}"),
            pc.info.armorClass,
            pc.info.background,
            pc.info.conditions,
            pc.info.deathSaves,
            pc.info.feats.map(f => s"${f.name}"),
            pc.info.inspiration,
            pc.info.modifiers,
            pc.info.notes,
            pc.info.languages.mkString(","),
            pc.info.traits
          )

        }
      )
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("router")
    .initialState {
      State()
    }
    .renderBackend[Backend]
    .componentDidMount(
      // _.backend.refresh(initial = true)()
      $ => Callback.empty
    )
    .componentWillUnmount($ =>
      // TODO close down streams here
      Callback.empty
    )
    .build

  def apply(pcs: Seq[PlayerCharacter]): Unmounted[Unit, State, Backend] = component()

}
