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

import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.*
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.components.{Accordion, Button, List as SList, Table, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHS}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesAccordionAccordionTitleMod.*

object ReferencePage {

  case class State(active: Int)

  class Backend($ : BackendScope[Unit, State]) {

    def loadState(): Callback = {
      for {
        _ <- Callback.log("Loading ReferencePage state")
      } yield ()
    }

    def render(state: State): VdomNode = {
      <.div(
        <.h1("Quick Reference (SRD)"),
        <.h2("Actions In combat"),
        <.div(
          """When you take your action on your turn, you can take one of the actions presented here, an action you gained from your class or a special feature, or an action that you improvise.
            |Many monsters have action options of their own in their stat blocks. When you describe an action not detailed elsewhere in the rules, the GM tells you whether that action is possible and what kind of roll you need to make, if any, to determine success or failure.""".stripMargin
        ),
        Accordion.Accordion
          .styled(true)
          .fluid(true)(
            Accordion
              .Title("Attack").active(state.active == 1).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 1), Callback.log("Attack clicked"))
              ),
            Accordion.Content.active(state.active == 1)(
              """The most common action to take in combat is the Attack action, whether you are swinging a sword, firing an arrow from a bow, or brawling with your fists.
                |With this action, you make one melee or ranged attack.
                |See the "Making an Attack" section for the rules that govern attacks.  Certain features, such as the Extra Attack feature of the fighter, allow you to make more than one attack with this action.""".stripMargin
            ),
            Accordion
              .Title("Cast a Spell").active(state.active == 2).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 2))
              ),
            Accordion.Content.active(state.active == 2)(
              """Spellcasters such as wizards and clerics, as well as many monsters, have access to spells and can use them to great effect in combat.
                |Each spell has a casting time, which specifies whether the caster must use an action, a reaction, minutes, or even hours to cast the spell. Casting a spell is, therefore, not necessarily an action.
                |Most spells do have a casting time of 1 action, so a spellcaster often uses his or her action in combat to cast such a spell.""".stripMargin
            ),
            Accordion
              .Title("Dash").active(state.active == 3).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 3))
              ),
            Accordion.Content.active(state.active == 3)(
              """When you take the Dash action, you gain extra movement for the current turn. The increase equals your speed, after applying any modifiers.
                |With a speed of 30 feet, for example, you can move up to 60 feet on your turn if you dash.
                |Any increase or decrease to your speed changes this additional movement by the same amount.
                |If your speed of 30 feet is reduced to 15 feet, for instance, you can move up to 30 feet this turn if you dash.""".stripMargin
            ),
            Accordion
              .Title("Disengage").active(state.active == 4).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 4))
              ),
            Accordion.Content.active(state.active == 4)(
              """If you take the Disengage action, your movement doesn't provoke opportunity attacks for the rest of the turn."""
            ),
            Accordion
              .Title("Dodge").active(state.active == 5).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 5))
              ),
            Accordion.Content.active(state.active == 5)("""When you take the Dodge action, you focus entirely on avoiding attacks.
                |Until the start of your next turn, any attack roll made against you has disadvantage if you can see the attacker, and you make Dexterity saving throws with advantage.
                |You lose this benefit if you are incapacitated or if your speed drops to 0.""".stripMargin),
            Accordion
              .Title("Help").active(state.active == 6).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 6))
              ),
            Accordion.Content.active(state.active == 6)(
              """You can lend your aid to another creature in the completion of a task.
                |When you take the Help action, the creature you aid gains advantage on the next ability check it makes to perform the task you are helping with, provided that it makes the check before the start of your next turn.
                |Alternatively, you can aid a friendly creature in attacking a creature within 5 feet of you.
                |You feint, distract the target, or in some other way team up to make your ally's attack more effective.
                |If your ally attacks the target before your next turn, the first attack roll is made with advantage.""".stripMargin
            ),
            Accordion
              .Title("Hide").active(state.active == 7).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 7))
              ),
            Accordion.Content.active(state.active == 7)(
              """When you take the Hide action, you make a Dexterity (Stealth) check in an attempt to hide, following the rules for hiding.
                |If you succeed, you gain certain benefits, as described in the "Unseen Attackers and Targets" section later in this chapter.""".stripMargin
            ),
            Accordion
              .Title("Ready").active(state.active == 8).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 8))
              ),
            Accordion.Content.active(state.active == 8)(
              """Sometimes you want to get the jump on a foe or wait for a particular circumstance before you act.
              |To do so, you can take the Ready action on your turn, which lets you act using your reaction before the start of your next turn.
              |First, you decide what perceivable circumstance will trigger your reaction. Then, you choose the action you will take in response to that trigger, or you choose to move up to your speed in response to it.
              |Examples include "If the cultist steps on the trapdoor, I'll pull the lever that opens it," and "If the goblin steps next to me, I move away."
              |When the trigger occurs, you can either take your reaction right after the trigger finishes or ignore the trigger. Remember that you can take only one reaction per round.
              |When you ready a spell, you cast it as normal but hold its energy, which you release with your reaction when the trigger occurs.
              |To be readied, a spell must have a casting time of 1 action, and holding onto the spell's magic requires concentration. If your concentration is broken, the spell dissipates without taking effect.
              |For example, if you are concentrating on the web spell and ready magic missile, your web spell ends, and if you take damage before you release magic missile with your reaction, your concentration might be broken.""".stripMargin
            ),
            Accordion
              .Title("Search").active(state.active == 9).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 9))
              ),
            Accordion.Content.active(state.active == 9)(
              """When you take the Search action, you devote your attention to finding something.
                |Depending on the nature of your search, the GM might have you make a Wisdom (Perception) check or an Intelligence (Investigation) check.""".stripMargin
            ),
            Accordion
              .Title("Use an Object").active(state.active == 10).onClick(
                (
                  _,
                  _
                ) => $.modState(s => s.copy(active = 10))
              ),
            Accordion.Content.active(state.active == 10)(
              """You normally interact with an object while doing something else, such as when you draw a sword as part of an attack.
                |When an object requires your action for its use, you take the Use an Object action. This action is also useful when you want to interact with more than one object on your turn.""".stripMargin
            )
          ),
        <.h2("Alignment"),
        <.h2("Backgrounds"),
        <.h2("Classes"),
        <.h2("Coins"),
        <.h2("Combat Rules"),
        <.h2("Conditions"),
        <.h2("Equipment"),
        <.h2("Movement"),
        <.h2("Proficiency Bonus"),
        <.h2("Races"),
        <.h2("Resting"),
        <.h2("Time")
      )
    }

  }

  private val component = ScalaComponent
    .builder[Unit]("ReferencePage")
    .initialState(State(active = 0))
    .renderBackend[Backend]
    .componentDidMount(_.backend.loadState())
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
