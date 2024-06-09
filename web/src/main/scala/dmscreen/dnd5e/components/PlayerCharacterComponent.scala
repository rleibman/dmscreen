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

import dmscreen.DMScreenState
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.{CtorType, *}
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticSIZES
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import zio.json.*
import zio.prelude.*

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object PlayerCharacterComponent {

  case class State(
    playerCharacter: PlayerCharacter,
    dialogOpen:      Boolean = false
  )
  case class Props(
    playerCharacter: PlayerCharacter,
    onSave:          PlayerCharacter => Callback = _ => Callback.empty,
    onDelete:        PlayerCharacter => Callback = _ => Callback.empty,
    onSync:          PlayerCharacter => Callback = _ => Callback.empty
  )

  case class Backend($ : BackendScope[Props, State]) {

    def profStr(proficiencyLevel: ProficiencyLevel): String =
      proficiencyLevel match {
        case ProficiencyLevel.none       => ""
        case ProficiencyLevel.half       => "½"
        case ProficiencyLevel.proficient => "*"
        case ProficiencyLevel.expert     => "**"
      }

    extension (b: BackendScope[Props, State]) {

      // This is doing json-info/info-json way too much, we need to change it to just store the info in the object
      // And then every so often accumulate the changes and send 'em to the server
      def modPCInfo(fn: PlayerCharacterInfo => PlayerCharacterInfo): Callback = {
        b.modState { s =>
          s.playerCharacter.info.fold(
            _ => s, // TODO Do something with the error
            oldInfo => s.copy(playerCharacter = s.playerCharacter.copy(jsonInfo = fn(oldInfo).toJsonAST.toOption.get)) // do something with the error
          )
        }
      }

    }

    def render(
      p: Props,
      s: State
    ): VdomElement = {
      DMScreenState.ctx.consume { dmScreenState =>
        s.playerCharacter.info.fold(
          e => <.div(s"Could not parse character info: ${e.getMessage}"),
          { pc =>

            def allBackgrounds =
              dmScreenState.dnd5e.backgrounds ++ pc.background.fold(Seq.empty)(bk =>
                Seq(
                  dmScreenState.dnd5e.backgrounds
                    .find(_.name.equalsIgnoreCase(bk.name)).getOrElse(Background(name = bk.name))
                )
              )

            <.div(
              ^.className := "characterCard",
              Button("Delete").size(SemanticSIZES.tiny).compact(true),
              Button("Sync").size(SemanticSIZES.tiny).compact(true).when(pc.source != DMScreenSource), // Only if the character originally came from a synchable source
              <.div(
                ^.className := "characterHeader",
                <.h2(EditableText(s.playerCharacter.header.name)),
                <.span(EditableText(s.playerCharacter.header.playerName.getOrElse("")))
              ),
              <.div(
                ^.className := "characterDetails",
                ^.minHeight := 180.px,
                <.table(
                  ^.width := "auto",
                  <.tbody(
                    <.tr(
                      <.td(
                        ^.colSpan := 2,
                        EditableComponent(
                          viewComponent = pc.classes.headOption.fold(<.div("Click to add Classes"))(_ =>
                            pc.classes.zipWithIndex.map {
                              (
                                cl,
                                i
                              ) =>
                                <.div(
                                  ^.key := s"characterClass_$i",
                                  s"${cl.characterClass.name} ${cl.subclass.fold("")(sc => s"(${sc.name})")} ${cl.level}"
                                )
                            }.toVdomArray
                          ),
                          editComponent = PlayerCharacterClassEditor(
                            pc.classes,
                            onChange = classes => $.modPCInfo(info => info.copy(classes = classes))
                          ),
                          modalTitle = "Classes/Subclasses/Levels",
                          onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                        )
                      )
                    ),
                    <.tr(
                      <.th("Background"),
                      <.td(
                        Dropdown
                          .placeholder("Choose")
                          .clearable(true)
                          .compact(true)
                          .allowAdditions(true)
                          .search(true)
                          .options(
                            allBackgrounds
                              .map(background =>
                                DropdownItemProps()
                                  .setValue(background.name)
                                  .setText(background.name),
                              ).toJSArray
                          )
                          .onChange(
                            (
                              _,
                              changedData
                            ) =>
                              $.modPCInfo(info =>
                                info.copy(background = changedData.value match {
                                  case s: String if s.isEmpty => None
                                  case s: String =>
                                    dmScreenState.dnd5e.backgrounds.find(_.name == s).orElse(Some(Background(s)))
                                  case _ => throw new RuntimeException("Unexpected value")
                                })
                              )
                          )
                          .value(pc.background.fold("")(_.name))
                      )
                    ),
                    <.tr(
                      <.th("AC"),
                      <.td(
                        EditableComponent(
                          viewComponent = <.div(pc.armorClass),
                          editComponent = ArmorClassEditor(
                            pc.armorClass,
                            onChange = armorClass => $.modPCInfo(info => info.copy(armorClass = armorClass))
                          ),
                          modalTitle = "Armor Class",
                          onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                        )
                      ) // AC Editor
                    ),
                    <.tr(
                      <.th("Proficiency Bonus"),
                      <.td(pc.proficiencyBonusString) // Automatic
                    ),
                    <.tr(
                      <.th("Initiative"),
                      <.td(pc.initiativeBonusString) // Override
                    ),
                    <.tr(
                      <.td(^.colSpan := 2, Checkbox.fitted(true).label("Inspiration").toggle(true)) // TODO onChange
                    )
                  )
                )
              ),
              <.div(
                ^.className       := "characterDetails",
                ^.backgroundColor := pc.hitPoints.lifeColor,
                EditableComponent(
                  viewComponent = <.table(
                    <.thead(
                      <.tr(<.th("HP"), <.th("Temp HP"))
                    ),
                    <.tbody(
                      pc.hitPoints.currentHitPoints match {
                        case ds: DeathSave =>
                          <.tr(
                            <.td(s"0/${pc.hitPoints.currentMax}", if (ds.isStabilized) " (stabilized)" else ""),
                            <.td(pc.hitPoints.temporaryHitPoints.toString)
                          )
                        case i: Int =>
                          <.tr(
                            <.td(s"$i/${pc.hitPoints.currentMax}"),
                            <.td(pc.hitPoints.temporaryHitPoints.toString)
                          )
                      }
                    )
                  ),
                  editComponent = HitPointsEditor(
                    pc.hitPoints,
                    onChange = hitPoints => $.modPCInfo(info => info.copy(hitPoints = hitPoints))
                  ),
                  modalTitle = "Hit Points",
                  onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                )
              ),
              <.div(
                ^.className := "characterDetails",
                EditableComponent(
                  viewComponent = <.table(
                    <.thead(
                      <.tr(
                        <.th(^.width := "16.667%", "Str"),
                        <.th(^.width := "16.666%", "Dex"),
                        <.th(^.width := "16.666%", "Con"),
                        <.th(^.width := "16.666%", "Int"),
                        <.th(^.width := "16.666%", "Wis"),
                        <.th(^.width := "16.666%", "Cha")
                      )
                    ),
                    <.tbody(
                      <.tr(
                        <.td(s"${pc.abilities.strength.overridenValue}"),
                        <.td(s"${pc.abilities.dexterity.overridenValue}"),
                        <.td(s"${pc.abilities.constitution.overridenValue}"),
                        <.td(s"${pc.abilities.intelligence.overridenValue}"),
                        <.td(s"${pc.abilities.wisdom.overridenValue}"),
                        <.td(s"${pc.abilities.charisma.overridenValue}")
                      ),
                      <.tr(
                        <.td(s"(${pc.abilities.strength.modifierString})"),
                        <.td(s"(${pc.abilities.dexterity.modifierString})"),
                        <.td(s"(${pc.abilities.constitution.modifierString})"),
                        <.td(s"(${pc.abilities.intelligence.modifierString})"),
                        <.td(s"(${pc.abilities.wisdom.modifierString})"),
                        <.td(s"(${pc.abilities.charisma.modifierString})")
                      )
                    ),
                    <.thead(
                      <.tr(<.th(^.colSpan := 6, <.div("Saving Throws"))),
                      <.tr(
                        <.th("Str"),
                        <.th("Dex"),
                        <.th("Con"),
                        <.th("Int"),
                        <.th("Wis"),
                        <.th("Cha")
                      )
                    ),
                    <.tbody(
                      <.tr(
                        <.td(s"${pc.abilities.strength.savingThrowString(pc.proficiencyBonus)}"),
                        <.td(s"${pc.abilities.dexterity.savingThrowString(pc.proficiencyBonus)}"),
                        <.td(s"${pc.abilities.constitution.savingThrowString(pc.proficiencyBonus)}"),
                        <.td(s"${pc.abilities.intelligence.savingThrowString(pc.proficiencyBonus)}"),
                        <.td(s"${pc.abilities.wisdom.savingThrowString(pc.proficiencyBonus)}"),
                        <.td(s"${pc.abilities.charisma.savingThrowString(pc.proficiencyBonus)}")
                      )
                    )
                  ),
                  editComponent = AbilitiesEditor(
                    pc.abilities,
                    onChange = abilities => $.modPCInfo(info => info.copy(abilities = abilities))
                  ),
                  modalTitle = "Abilities",
                  onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Passive"),
                <.table(
                  <.thead(
                    <.tr(
                      <.th("Perception"),
                      <.th("Investigation"),
                      <.th("Insight")
                    )
                  ),
                  <.tbody(
                    <.tr(
                      <.td(pc.passivePerception),
                      <.td(pc.passiveInvestigation),
                      <.td(pc.passiveInsight)
                    )
                  )
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Conditions"),
                EditableComponent(
                  viewComponent = <.span(
                    pc.conditions.headOption
                      .fold("Click to change")(_ => pc.conditions.map(_.toString.capitalize).mkString(", "))
                  ),
                  editComponent = ConditionsEditor(
                    pc.conditions,
                    onChange = conditions => $.modPCInfo(info => info.copy(conditions = conditions))
                  ),
                  modalTitle = "Conditions",
                  onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Speed"),
                EditableComponent(
                  editComponent = SpeedsEditor(
                    pc.speeds,
                    speeds => $.modPCInfo(info => info.copy(speeds = speeds))
                  ),
                  viewComponent = pc.speeds.headOption.fold(<.div("Click to add")) { _ =>
                    <.table(
                      <.thead(
                        <.tr(
                          pc.speeds.map(sp => <.th(sp.speedType.toString)).toVdomArray
                        )
                      ),
                      <.tbody(
                        <.tr(
                          pc.speeds.map(sp => <.th(sp.value.toString)).toVdomArray
                        )
                      )
                    )
                  },
                  modalTitle = "Speeds",
                  onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Skills"),
                EditableComponent(
                  viewComponent = <.table(
                    ^.minHeight := 330.px,
                    <.tbody(
                      <.tr(
                        <.th(^.width := 23.px, s"${profStr(pc.skills.acrobatics.proficiencyLevel)}Acrobatics"),
                        <.td(^.width := "50%", pc.skills.acrobatics.modifierString(pc.abilities)),
                        <.th(^.width := 23.px, s"${profStr(pc.skills.medicine.proficiencyLevel)}Medicine"),
                        <.td(^.width := "50%", pc.skills.medicine.modifierString(pc.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(pc.skills.animalHandling.proficiencyLevel)}Animal H."),
                        <.td(pc.skills.animalHandling.modifierString(pc.abilities)),
                        <.th(s"${profStr(pc.skills.nature.proficiencyLevel)}Nature"),
                        <.td(pc.skills.nature.modifierString(pc.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(pc.skills.arcana.proficiencyLevel)}Arcana"),
                        <.td(pc.skills.arcana.modifierString(pc.abilities)),
                        <.th(s"${profStr(pc.skills.perception.proficiencyLevel)}Perception"),
                        <.td(pc.skills.perception.modifierString(pc.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(pc.skills.athletics.proficiencyLevel)}Athletics"),
                        <.td(pc.skills.athletics.modifierString(pc.abilities)),
                        <.th(s"${profStr(pc.skills.performance.proficiencyLevel)}Perf."),
                        <.td(pc.skills.performance.modifierString(pc.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(pc.skills.deception.proficiencyLevel)}Deception"),
                        <.td(pc.skills.deception.modifierString(pc.abilities)),
                        <.th(s"${profStr(pc.skills.persuasion.proficiencyLevel)}Persuasion"),
                        <.td(pc.skills.persuasion.modifierString(pc.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(pc.skills.history.proficiencyLevel)}History"),
                        <.td(pc.skills.history.modifierString(pc.abilities)),
                        <.th(s"${profStr(pc.skills.religion.proficiencyLevel)}Religion"),
                        <.td(pc.skills.religion.modifierString(pc.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(pc.skills.insight.proficiencyLevel)}Insight"),
                        <.td(pc.skills.insight.modifierString(pc.abilities)),
                        <.th(s"${profStr(pc.skills.sleightOfHand.proficiencyLevel)}Sleight of H."),
                        <.td(pc.skills.sleightOfHand.modifierString(pc.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(pc.skills.intimidation.proficiencyLevel)}Intim."),
                        <.td(pc.skills.intimidation.modifierString(pc.abilities)),
                        <.th(s"${profStr(pc.skills.stealth.proficiencyLevel)}Stealth"),
                        <.td(pc.skills.stealth.modifierString(pc.abilities))
                      ),
                      <.tr(
                        <.th(s"${profStr(pc.skills.investigation.proficiencyLevel)}Invest."),
                        <.td(pc.skills.investigation.modifierString(pc.abilities)),
                        <.th(s"${profStr(pc.skills.survival.proficiencyLevel)}Survival"),
                        <.td(pc.skills.survival.modifierString(pc.abilities))
                      )
                    )
                  ),
                  editComponent = SkillsEditor(
                    pc.skills,
                    pc.abilities,
                    onChange = skills => $.modPCInfo(info => info.copy(skills = skills))
                  ),
                  modalTitle = "Skills",
                  onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Languages"),
                EditableComponent(
                  viewComponent =
                    pc.languages.headOption.fold("Click to add")(_ => pc.languages.map(_.name).mkString(", ")),
                  editComponent =
                    LanguageEditor(pc.languages, languages => $.modPCInfo(info => info.copy(languages = languages))),
                  modalTitle = "Languages",
                  onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Feats"),
                EditableComponent(
                  viewComponent = pc.feats.headOption.fold("Click to add")(_ => pc.feats.map(_.name).mkString(", ")),
                  editComponent = FeatsEditor(pc.feats, feats => $.modPCInfo(info => info.copy(feats = feats))),
                  modalTitle = "Feats",
                  onModeChange = mode => $.modState(_.copy(dialogOpen = mode == EditableComponent.Mode.edit))
                )
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Senses"),
                "Darkvision 60ft"
              ),
              <.div(
                ^.className := "characterDetails",
                <.div(^.className := "sectionTitle", "Notes"),
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed ut cursus sapien, sed sollicitudin nibh. Morbi vitae purus eu diam tempor efficitur. Etiam nec sem est. Curabitur et sem pharetra, tristique libero vel, venenatis ante. Curabitur mattis egestas erat. Ut finibus suscipit augue a iaculis. Ut congue dui eget malesuada ullamcorper. Phasellus nec nunc blandit, viverra metus sed, placerat enim. Suspendisse vel nibh volutpat, sagittis est ut, feugiat leo. Phasellus suscipit et erat id sollicitudin. In vel posuere odio. Donec vestibulum nec felis et feugiat. Morbi lacus orci, finibus at risus sed, vestibulum pretium lorem. Vivamus suscipit diam id dignissim maximus. Curabitur et consectetur elit, vel ornare risus."
              )
            )
          }
        )
      }
    }

  }

//  import scala.language.unsafeNulls
//
//  given Reusability[Props] = Reusability.by((_: Props).playerCharacter)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("playerCharacterComponent")
    .initialStateFromProps(p => State(p.playerCharacter))
    .renderBackend[Backend]
    .componentDidMount($ => Callback.empty)
    .shouldComponentUpdatePure($ => ! $.nextState.dialogOpen)
    .build

  def apply(
    playerCharacter: PlayerCharacter,
    onSave:          PlayerCharacter => Callback = _ => Callback.empty,
    onDelete:        PlayerCharacter => Callback = _ => Callback.empty,
    onSync:          PlayerCharacter => Callback = _ => Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(playerCharacter, onSave, onDelete, onSync))

}
