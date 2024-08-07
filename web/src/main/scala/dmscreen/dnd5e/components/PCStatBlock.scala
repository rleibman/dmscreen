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
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHS}

object PCStatBlock {

  import japgolly.scalajs.react.vdom.html_<^.*

  case class State(pc: Option[PlayerCharacter] = None)
  case class Props(pcId: PlayerCharacterId)

  case class Backend($ : BackendScope[Props, State]) {

    def loadState(id: PlayerCharacterId): Callback = {
      DND5eGraphQLRepository.live
        .playerCharacter(id)
        .map(pc => $.modState(_.copy(pc = pc)))
        .completeWith(_.get)
    }

    def profStr(proficiencyLevel: ProficiencyLevel): String =
      proficiencyLevel match {
        case ProficiencyLevel.none       => ""
        case ProficiencyLevel.half       => "½"
        case ProficiencyLevel.proficient => "*"
        case ProficiencyLevel.expert     => "**"
      }

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      DMScreenState.ctx.consume { dmScreenState =>
        val campaignState = dmScreenState.campaignState
          .map(_.asInstanceOf[DND5eCampaignState]).getOrElse(throw RuntimeException("No campaign"))
        state.pc.fold(EmptyVdom) { pc =>
          val info = pc.info

        Container.className("stat-block")(
          <.hr(^.className := "orange-border"),
          Container
            .className("creature-heading")(
              Header
                .size(SemanticSIZES.large).as("h1")(s"${pc.header.name}${pc.header.playerName.fold("")(n => s"($n)")}"),
              Header
                .size(SemanticSIZES.small).as("h2")(
                  s"${info.size.toString.capitalize} ${info.race.name}, ${info.alignment.name}"
                ),
              Header
                .size(SemanticSIZES.small).as("h2")(
                  info.classes.zipWithIndex.map {
                    (
                      cl,
                      i
                    ) =>
                      <.p(
                        s"${cl.characterClass.name} ${cl.subclass.fold("")(sc => s"(${sc.name})")} ${cl.level}"
                      )
                  }*
                )
            ),
          taperedLine,
          Container.className("top-stats")(
            Container
              .className("property-line")(
                Header.size(SemanticSIZES.small).as("h4")("Background"),
                <.p(info.background.map(_.name))
              ).when(info.background.isDefined),
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Armor Class"),
              <.p(info.armorClass)
            ),
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Max Hit points"),
              <.p(info.health.currentMax)
            ),
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Proficiency Bonus"),
              <.p(info.proficiencyBonusString)
            ),
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Spell DC"),
              <.p(info.spellDC.map(s => s"${s._1.abilityType.short} (${s._2})").mkString(" / "))
            ),
            Container.className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Initiative Bonus"),
              <.p(info.initiativeBonus)
            )
          ),
          Container.className("property-line")(
            Header.size(SemanticSIZES.small).as("h4")("Speed"),
            <.p(info.speeds.map(speed => s"${speed.value} (${speed.speedType.toString})").mkString(", "))
          ),
          Container
            .className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Senses"),
              <.p(info.senses.map(s => s"${s.sense} ${s.range}").mkString(", "))
            ).when(info.senses.nonEmpty),
          Container
            .className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Languages"),
              <.p(info.languages.map(_.name).mkString(", "))
            ).when(info.languages.nonEmpty),
          Container
            .className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Feats"),
              <.p(info.feats.map(_.name).mkString(", "))
            ).when(info.feats.nonEmpty),
          Container
            .className("property-line")(
              Header.size(SemanticSIZES.small).as("h4")("Conditions"),
              <.span(info.conditions.map(_.toString.capitalize).mkString(", "))
            ).when(info.conditions.nonEmpty),
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
          Container.className("abilities")(
            <.div(
              Header.size(SemanticSIZES.small).as("h4")("Passive Perception"),
              <.p(info.passivePerception)
            ),
            <.div(
              Header.size(SemanticSIZES.small).as("h4")("Passive Insight"),
              <.p(info.passiveInsight)
            ),
            <.div(
              Header.size(SemanticSIZES.small).as("h4")("Passive Investigation"),
              <.p(info.passivePerception)
            )
          ),
          taperedLine,
          Container.className("abilities")(
            <.table(
              <.tbody({
                val groupedSkills: List[List[Skill]] = info.skills.all
                  .filter(sk =>
                    sk.modifier(info.abilities) != 0 || sk.proficiencyLevel != ProficiencyLevel.none
                  ).grouped(2).toList
                groupedSkills.map { group =>
                  val sk1 = group.head
                  val sk2 = group.last
                  <.tr(
                    <.th(
                      ^.width := 25.pct,
                      Header
                        .size(SemanticSIZES.small).as("h4")(s"${profStr(sk1.proficiencyLevel)}${sk1.skillType.name}")
                    ),
                    <.td(^.width := 25.pct, sk1.modifierString(info.abilities)),
                    <.th(
                      ^.width := 25.pct,
                      Header
                        .size(SemanticSIZES.small).as("h4")(s"${profStr(sk2.proficiencyLevel)}${sk2.skillType.name}")
                    ),
                    <.td(^.width := 25.pct, sk2.modifierString(info.abilities))
                  )
                }
              }*)
            )
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
          taperedLine,
          info.notes.headOption.fold(EmptyVdom)(_ =>
            Container.className("notes")(
              Header.size(SemanticSIZES.medium).as("h3")("Notes"),
              <.p(
                ^.dangerouslySetInnerHtml := info.notes.trim.headOption.fold("Click here to add")(_ => info.notes)
              )
            )
          ),
          <.hr(^.className := "orange-border bottom")
        )
        }
      }
    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("PCStatBlock")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState($.props.pcId))
    .build

  def apply(
    pcId: PlayerCharacterId
  ): Unmounted[Props, State, Backend] =
    component(
      Props(pcId)
    )

}
