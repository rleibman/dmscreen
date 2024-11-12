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

import dmscreen.*
import dmscreen.dnd5e.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.^
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHS}

def taperedLine = {
  import japgolly.scalajs.react.vdom.svg_<^.{< => svgTag, ^ => svgAttr}

  svgTag.svg(
    svgAttr.height := "5",
    svgAttr.width  := "100%",
    ^.className    := "tapered-rule",
    svgTag.polyline(
      svgAttr.points := "0,0 600,2.5 0,5"
    )
  )
}

object MonsterStatBlock {

  import japgolly.scalajs.react.vdom.html_<^.*

  case class State(monster: Option[Monster] = None)
  case class Props(monsterId: MonsterId)

  case class Backend($ : BackendScope[Props, State]) {

    def loadState(monsterId: MonsterId): Callback = {
      DND5eGraphQLRepository.live
        .monster(monsterId)
        .map(m => $.modState(_.copy(monster = m)))
        .completeWith(_.get)
    }

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      state.monster.fold(EmptyVdom) { monster =>
        val info = monster.info

        Container.className("stat-block")(
          <.hr(^.className := "orange-border"),
          Container
            .className("creature-heading")(
              Header.size(SemanticSIZES.large).as("h1")(monster.header.name),
              Header.size(SemanticSIZES.small).as("h2")(s"${monster.header.size.toString.capitalize} ${monster.header.monsterType.toString.capitalize}, ${monster.header.alignment.fold("")(_.toString)}")
            ),
          taperedLine,
          Container.className("top-stats")(
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Armor Class"),
              <.p(monster.header.armorClass)
            ),
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Hit points"),
              <.p(s"${monster.header.maximumHitPoints} ${info.hitDice.fold("")(hd => s"(${hd.roll})")})")
            ),
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Speed"),
              <.p(info.speeds.map(speed => s"${speed.value} (${speed.speedType.toString})").mkString(", "))
            ),
            taperedLine,
            Container.className("abilities")(
              <.div(
                Header.size(SemanticSIZES.small).as("h4")("STR"),
                <.p(s"${info.abilities.strength.value} (${info.abilities.strength.modifierString})")
              ),
              <.div(
                Header.size(SemanticSIZES.small).as("h4")("DEX"),
                <.p(s"${info.abilities.dexterity.value} (${info.abilities.dexterity.modifierString})")
              ),
              <.div(
                Header.size(SemanticSIZES.small).as("h4")("CON"),
                <.p(s"${info.abilities.constitution.value} (${info.abilities.constitution.modifierString})")
              ),
              <.div(
                Header.size(SemanticSIZES.small).as("h4")("INT"),
                <.p(s"${info.abilities.intelligence.value} (${info.abilities.intelligence.modifierString})")
              ),
              <.div(
                Header.size(SemanticSIZES.small).as("h4")("WIS"),
                <.p(s"${info.abilities.wisdom.value} (${info.abilities.wisdom.modifierString})")
              ),
              <.div(
                Header.size(SemanticSIZES.small).as("h4")("CHA"),
                <.p(s"${info.abilities.charisma.value} (${info.abilities.charisma.modifierString})")
              )
            ),
            taperedLine,
            info.damageImmunities.headOption
              .fold(EmptyVdom)(_ =>
                Container.className("property-line")(
                  Header.size(SemanticSIZES.small).as("h4")("Damage Immunities"),
                  <.p(info.damageImmunities.map(_.description).mkString(", "))
                )
              ),
            info.damageVulnerabilities.headOption
              .fold(EmptyVdom)(_ =>
                Container.className("property-line")(
                  Header.size(SemanticSIZES.small).as("h4")("Damage Vulnelabilities"),
                  <.p(info.damageVulnerabilities.map(_.description).mkString(", "))
                )
              ),
            info.damageResistances.headOption
              .fold(EmptyVdom)(_ =>
                Container.className("property-line")(
                  Header.size(SemanticSIZES.small).as("h4")("Damage Resistances"),
                  <.p(info.damageResistances.map(_.description).mkString(", "))
                )
              ),
            info.conditionImmunities.headOption
              .fold(EmptyVdom)(_ =>
                Container.className("property-line")(
                  Header.size(SemanticSIZES.small).as("h4")("Condition Immunities"),
                  <.p(info.conditionImmunities.map(_.toString).mkString(", "))
                )
              ),
            info.senses.headOption
              .fold(EmptyVdom)(_ =>
                Container.className("property-line")(
                  Header.size(SemanticSIZES.small).as("h4")("Senses"),
                  <.p(info.senses.map(_.sense).mkString(", "))
                )
              ),
            info.languages.headOption
              .fold(EmptyVdom)(_ =>
                Container.className("property-line")(
                  Header.size(SemanticSIZES.small).as("h4")("Languages"),
                  <.p(info.languages.map(_.name).mkString(", "))
                )
              ),
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Challenge"),
              <.p(s"${monster.header.cr} (${monster.header.xp} XP)")
            ),
            taperedLine,
            info.actions.headOption.fold(EmptyVdom)(_ =>
              Container.className("actions")(
                (Header.size(SemanticSIZES.medium).as("h3")("Actions"): TagMod) +:
                  info.actions.map { action =>
                    Container.className("property-block")(
                      Header.size(SemanticSIZES.small).as("h4")(action.name),
                      <.p(action.description)
                    ): TagMod
                  }*
              )
            ),
            info.reactions.headOption.fold(EmptyVdom)(_ =>
              Container.className("actions")(
                (Header.size(SemanticSIZES.medium).as("h3")("Reactions"): TagMod) +:
                  info.reactions.map { action =>
                    Container.className("property-block")(
                      Header.size(SemanticSIZES.small).as("h4")(action.name),
                      <.p(action.description)
                    ): TagMod
                  }*
              )
            ),
            info.legendaryActions.headOption.fold(EmptyVdom)(_ =>
              Container.className("actions")(
                (Header.size(SemanticSIZES.medium).as("h3")("Legendary Actions"): TagMod) +:
                  info.legendaryActions.map { action =>
                    Container.className("property-block")(
                      Header.size(SemanticSIZES.small).as("h4")(action.name),
                      <.p(action.description)
                    ): TagMod
                  }*
              )
            ),
            info.specialAbilities.headOption.fold(EmptyVdom)(_ =>
              Container.className("actions")(
                (Header.size(SemanticSIZES.medium).as("h3")("Special Abilities"): TagMod) +:
                  info.specialAbilities.map { specialAbility =>
                    Container.className("property-block")(
                      Header.size(SemanticSIZES.small).as("h4")(specialAbility.name),
                      <.p(specialAbility.description)
                    ): TagMod
                  }*
              )
            )
          ),
          <.hr(^.className := "orange-border bottom")
        )
      }
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("MonsterStatBlock")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props.monsterId))
    .build

  def apply(
    monsterId: MonsterId
  ): Unmounted[Props, State, Backend] =
    component(
      Props(monsterId)
    )

}
