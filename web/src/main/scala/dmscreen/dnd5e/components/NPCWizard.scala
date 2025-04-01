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

import components.Confirm
import dmscreen.*
import dmscreen.components.EditableComponent
import dmscreen.components.EditableComponent.EditingMode
import dmscreen.dnd5e.{*, given}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.html_<^.{<, *}
import net.leibman.dmscreen.react.mod.CSSProperties
import net.leibman.dmscreen.semanticUiReact.components.{Confirm as SConfirm, List as SList, *}
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticCOLORS, SemanticICONS, SemanticWIDTHS}
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesCheckboxCheckboxMod.CheckboxProps
import net.leibman.dmscreen.semanticUiReact.distCommonjsModulesDropdownDropdownItemMod.DropdownItemProps
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings
import net.leibman.dmscreen.semanticUiReact.semanticUiReactStrings.*
import org.scalajs.dom.HTMLInputElement
import zio.json.*
import zio.json.ast.Json

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.util.Random

object NPCWizard {

  def hair: String =
    Random.shuffle(Seq("Blond", "Brown", "Black", "Red", "Gray", "White", "None", "Blue", "Green", "Purple")).head
  def skin: String =
    Random
      .shuffle(
        Seq("Light", "Dark", "Tan", "Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet", "White")
      ).head
  def eyes: String =
    Random
      .shuffle(
        Seq("Black", "Brown", "Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet", "Silver", "White")
      ).head
  def height(size: CreatureSize): String = {
    size match {
      case CreatureSize.tiny       => f"${Random.between(0.5, 2)}%1.2f"
      case CreatureSize.small      => f"${Random.between(3.5, 4)}%1.2f"
      case CreatureSize.medium     => f"${Random.between(5.0, 8)}%1.2f"
      case CreatureSize.large      => f"${Random.between(8.0, 16)}%1.2f"
      case CreatureSize.huge       => Random.between(12, 32).toString
      case CreatureSize.gargantuan => Random.between(18, 64).toString
      case _                       => ""
    }
  }
  def weight(size: CreatureSize): String = {
    size match {
      case CreatureSize.tiny       => f"${Random.between(0.5, 8)}%1.2f"
      case CreatureSize.small      => f"${Random.between(8.0, 60)}%1.2f"
      case CreatureSize.medium     => Random.between(60, 500).toString
      case CreatureSize.large      => Random.between(500, 4000).toString
      case CreatureSize.huge       => Random.between(4000, 32000).toString
      case CreatureSize.gargantuan => Random.between(32000, 250000).toString
      case _                       => ""
    }
  }
  def age:    String = Random.between(1, 400).toString
  def gender: String = Random.shuffle(Seq("Male", "Female", "Other")).head

  case class Props(
    campaign:       Campaign,
    selectedScenes: Set[SceneId] = Set.empty,
    onCancel:       Callback,
    onSaved:        NonPlayerCharacter => Callback,
    npcId:          Option[NonPlayerCharacterId] = None
  )

  case class State(
    header:      NonPlayerCharacterHeader,
    info:        NonPlayerCharacterInfo,
    currentStep: WizardStepType = WizardStepType.scenes,
    editingMode: EditableComponent.EditingMode = EditableComponent.EditingMode.view,
    //
    scenes:         List[Scene] = List.empty,
    selectedScenes: Set[SceneId] = Set.empty,
    //
    monsters:        List[MonsterHeader] = List.empty,
    monsterSearch:   MonsterSearch = MonsterSearch(pageSize = 8),
    monsterCount:    Long = 0,
    isMonster:       Boolean = false,
    selectedMonster: Option[MonsterHeader] = None,
    //
    races:       Set[Race] = Set.empty,
    classes:     List[CharacterClass] = List.empty,
    backgrounds: Set[Background] = Set.empty,

    // generating step
    generating: Boolean = false
  )

  enum WizardStepType(val name: String) {

    case scenes extends WizardStepType("Scenes")
    case race extends WizardStepType("NPC Race")
    case npcClass extends WizardStepType("NPC Class")
    case stats extends WizardStepType("Stats")
    case personalCharacteristics extends WizardStepType("Personal Characteristics")
    case other extends WizardStepType("Other")

    case confirmWizard extends WizardStepType("Confirm")

  }

  case class Backend($ : BackendScope[Props, State]) {

    def onSelectMonster(header: MonsterHeader): Callback = {
      DND5eGraphQLRepository.live
        .monster(header.id)
        .map { monsterOpt =>

          val newInfo = monsterOpt.map(monster =>
            NonPlayerCharacterInfo(
              race = Race(monster.header.name),
              size = monster.header.size,
              health = Health(
                deathSave = DeathSave.empty,
                currentHitPoints = monster.header.maximumHitPoints,
                maxHitPoints = monster.header.maximumHitPoints
              ),
              armorClass = monster.header.armorClass,
              abilities = monster.info.abilities,
              senses = monster.info.senses,
              speeds = monster.info.speeds,
              languages = monster.info.languages,
              alignment = monster.header.alignment.getOrElse(Alignment.trueNeutral),
              actions = monster.info.actions ++ monster.info.reactions ++ monster.info.legendaryActions,
              conditionImmunities = monster.info.conditionImmunities,
              damageVulnerabilities = monster.info.damageVulnerabilities,
              damageResistances = monster.info.damageResistances,
              damageImmunities = monster.info.damageImmunities,
              monster = Some(monster.header.id),
              challengeRating = Some(monster.header.cr),
              classes = List.empty
            )
          )
          newInfo.fold(
            Callback.throwException(new RuntimeException(s"Could not find monster with an id of ${header.id}"))
          ) { info =>
            $.modState(_.copy(info = info, selectedMonster = Some(header), monsterSearch = MonsterSearch()))
          }
        }
        .completeWith(_.get)

    }

    def loadState(): Callback = {
      for {
        oldState    <- $.state.asAsyncCallback
        props       <- $.props.asAsyncCallback
        scenes      <- DND5eGraphQLRepository.live.scenes(props.campaign.id)
        monsters    <- DND5eGraphQLRepository.live.bestiary(oldState.monsterSearch)
        races       <- DND5eGraphQLRepository.live.races
        classes     <- DND5eGraphQLRepository.live.classes
        backgrounds <- DND5eGraphQLRepository.live.backgrounds
        npc <- AsyncCallback
          .traverse(props.npcId)(id => DND5eGraphQLRepository.live.nonPlayerCharacter(id)).map(_.flatten.headOption)
        selectedMonster <- AsyncCallback
          .traverse(npc.flatMap(_.info.monster))(id => DND5eGraphQLRepository.live.monster(id)).map(
            _.flatten.headOption
          )
      } yield $.modState(s =>
        s.copy(
          header = npc.fold(s.header)(_.header),
          info = npc.fold(s.info)(_.info),
          isMonster = selectedMonster.isDefined,
          selectedMonster = selectedMonster.map(_.header),
          scenes = scenes.toList,
          monsters = monsters.results,
          monsterCount = monsters.total,
          races = races.toSet,
          classes = classes.toList,
          backgrounds = backgrounds.toSet
        )
      )
    }.completeWith(_.get)

    private def Scenes(state: State): VdomNode =
      Form(
        <.div(
          ^.className := "responsive-three-column-grid",
          VdomArray(
            state.scenes.map(scene =>
              Checkbox
                .withKey(scene.header.name)
                .label(scene.header.name)
                .checked(state.selectedScenes.contains(scene.header.id))
                .onChange {
                  (
                    _,
                    data
                  ) =>
                    if (data.checked.getOrElse(false))
                      $.modState(s => s.copy(selectedScenes = s.selectedScenes + scene.header.id))
                    else
                      $.modState(s => s.copy(selectedScenes = s.selectedScenes - scene.header.id))
                }
            )*
          )
        )
      )

    private def RaceTab(state: State): VdomNode = {
      def modMonsterSearch(fn: MonsterSearch => MonsterSearch): Callback =
        $.modState(
          s => s.copy(monsterSearch = fn(s.monsterSearch)),
          $.state.flatMap(s =>
            DND5eGraphQLRepository.live
              .bestiary(s.monsterSearch)
              .map(monsters => $.modState(_.copy(monsters = monsters.results, monsterCount = monsters.total)))
              .completeWith(_.get)
          )
        )

      println(s"Total Monster Count: ${state.monsterCount}")
      println(s"Total Monster Pages: ${state.monsterCount / state.monsterSearch.pageSize.toDouble}")

      Form(
        Form.Group(
          Radio
            .radioGroup("raceOrMonster")
            .label("Player Race")
            .checked(!state.isMonster)
            .onChange {
              (
                _,
                data
              ) =>
                $.modState(_.copy(isMonster = !data.checked.getOrElse(true)))
            }
        ),
        Form.Group(
          Form.Dropdown
            .disabled(state.isMonster)
            .placeholder("Choose Race")
            .clearable(true)
            .compact(true)
            .allowAdditions(true)
            .selection(true)
            .search(true)
            .options(
              state.races.toList
                .sortBy(_.name)
                .map(race =>
                  DropdownItemProps()
                    .setValue(race.name)
                    .setText(race.name)
                ).toJSArray
            )
            .onChange(
              (
                _,
                changedData
              ) =>
                $.modState { s =>
                  val race = changedData.value match {
                    case str: String if str.isEmpty => s.info.race
                    case str: String                => s.races.find(_.name == str).getOrElse(Race(str))
                    case _ => throw RuntimeException("Unexpected value")
                  }

                  s.copy(info = s.info.copy(race = race), races = s.races + race)
                }
            )
            .value(state.info.race.name)
        ),
        Form.Group(
          Radio
            .radioGroup("raceOrMonster")
            .label("Monster Race").checked(state.isMonster)
            .onChange {
              (
                _,
                data
              ) => $.modState(_.copy(isMonster = data.checked.getOrElse(false)))
            }
        ),
        Form.Group(
          state.selectedMonster.map(_.name).getOrElse("")
        ),
        Form.Group(
          Form.Field(
            Label("Type"),
            Dropdown
              .fluid(true)
              .disabled(!state.isMonster)
              .search(false)
              .clearable(true)
              .placeholder("All")
              .options(
                MonsterType.values
                  .map(s =>
                    DropdownItemProps().setValue(s.toString.capitalize).setText(s.toString.capitalize)
                  ).toJSArray
              )
              .onChange {
                (
                  _,
                  data
                ) =>
                  val newVal = data.value match {
                    case s: String if s.trim.isEmpty => None
                    case s: String                   => MonsterType.values.find(_.toString.equalsIgnoreCase(s))
                    case _ => None
                  }

                  modMonsterSearch(_.copy(monsterType = newVal))
              }
              .value(state.monsterSearch.monsterType.fold("")(_.toString.capitalize))
          ),
          Form.Field(
            Label("Biome"),
            Form
              .Dropdown()
              .disabled(!state.isMonster)
              .fluid(true)
              .search(false)
              .clearable(true)
              .placeholder("All")
              .options(
                Biome.values
                  .map(s =>
                    DropdownItemProps().setValue(s.toString.capitalize).setText(s.toString.capitalize)
                  ).toJSArray
              )
              .onChange {
                (
                  _,
                  data
                ) =>
                  val newVal = data.value match {
                    case s: String if s.trim.isEmpty => None
                    case s: String                   => Biome.values.find(_.toString.equalsIgnoreCase(s))
                    case _ => None
                  }

                  modMonsterSearch(_.copy(biome = newVal))
              }
              .value(state.monsterSearch.biome.fold("")(_.toString.capitalize))
          ),
          Form.Field(
            Label("Aligment"),
            Form
              .Dropdown()
              .disabled(!state.isMonster)
              .fluid(true)
              .search(false)
              .clearable(true)
              .placeholder("All")
              .options(
                Alignment.values
                  .map(s => DropdownItemProps().setValue(s.name).setText(s.name)).toJSArray
              )
              .onChange {
                (
                  _,
                  data
                ) =>
                  val newVal = data.value match {
                    case s: String if s.trim.isEmpty => None
                    case s: String                   => Alignment.values.find(_.name.equalsIgnoreCase(s))
                    case _ => None
                  }

                  modMonsterSearch(_.copy(alignment = newVal))
              }
              .value(state.monsterSearch.alignment.fold("")(_.name))
          ),
          Form.Field(
            Label("Size"),
            Form
              .Dropdown()
              .disabled(!state.isMonster)
              .fluid(true)
              .search(false)
              .clearable(true)
              .placeholder("All")
              .options(
                CreatureSize.values
                  .map(s =>
                    DropdownItemProps().setValue(s.toString.capitalize).setText(s.toString.capitalize)
                  ).toJSArray
              )
              .onChange {
                (
                  _,
                  data
                ) =>
                  val newVal = data.value match {
                    case s: String if s.trim.isEmpty => None
                    case s: String                   => CreatureSize.values.find(_.toString.equalsIgnoreCase(s))
                    case _ => None
                  }

                  modMonsterSearch(_.copy(size = newVal))
              }
              .value(state.monsterSearch.size.fold("")(_.toString.capitalize))
          ),
          Form.Input
            .disabled(!state.isMonster)
            .onChange(
              (
                _,
                data
              ) => modMonsterSearch(_.copy(name = data.value.toOption.map(_.asInstanceOf[String])))
            )
        ),
        Form
          .Group(
            Table
              .inverted(DND5eUI.tableInverted)
              .color(DND5eUI.tableColor)(
                Table.Body(
                  state.monsters.map { header =>
                    Table.Row.withKey(header.id.value.toString)(
                      Table.Cell(
                        s"${header.name} (${header.monsterType.toString.capitalize}, CR: ${header.cr.name})"
                      ),
                      Table.Cell(
                        Button
                          .icon(true)
                          .color(SemanticCOLORS.violet)
                          .onClick(
                            (
                              _,
                              _
                            ) => onSelectMonster(header)
                          )(Icon.name(SemanticICONS.`check circle outline`))
                      )
                    )
                  }*
                ),
                Table.Body(
                  Table
                    .Row(
                      Table.Cell.colSpan(2)(
                        Pagination(state.monsterCount / state.monsterSearch.pageSize.toDouble)
                          .set("size", tiny)
                          .onPageChange {
                            (
                              _,
                              data
                            ) =>
                              val newVal = data.activePage match {
                                case s: String => s.toInt
                                case d: Double => d.toInt
                                case _: Unit   => 1
                              }

                              modMonsterSearch(_.copy(page = newVal - 1))
                          }
                          .activePage(state.monsterSearch.page + 1)
                      )
                    ).when(state.monsters.nonEmpty)
                )
              )
          ).when(state.isMonster)
      )

    }

    def doModeChange(newMode: EditableComponent.EditingMode): Callback = $.modState(s => s.copy(editingMode = newMode))

    private def NPCClass(state: State): VdomNode = {
      Form(
        Form.Group(
          Label("Class"),
          Form.Field(
            EditableComponent(
              view = state.info.classes.headOption.fold(<.div("Click to add Classes"))(_ =>
                state.info.classes.zipWithIndex.map {
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
              edit = CharacterClassEditor(
                state.info.classes,
                onChange = classes => $.modState(s => s.copy(info = s.info.copy(classes = classes)))
              ),
              title = "Classes/Subclasses/Levels",
              onEditingModeChange = doModeChange
            )
          )
        ),
        Form.Group(
          Label("Background"),
          Dropdown
            .placeholder("Choose")
            .clearable(true)
            .compact(true)
            .allowAdditions(true)
            .search(true)
            .options(
              state.backgrounds.toList
                .sortBy(_.name)
                .map(background =>
                  DropdownItemProps()
                    .setValue(background.name)
                    .setText(background.name)
                ).toJSArray
            )
            .onChange {
              (
                _,
                changedData
              ) =>
                $.modState { s =>
                  val background: Option[Background] = changedData.value match {
                    case str: String if str.isEmpty => None
                    case str: String                => s.backgrounds.find(_.name == str).orElse(Some(Background(str)))
                    case _ => throw RuntimeException("Unexpected value")
                  }

                  s.copy(info = s.info.copy(background = background), backgrounds = s.backgrounds ++ background)
                }
            }
            .value(state.info.background.fold("")(_.name))
        )
      )
    }

    private def Stats(state: State): VdomNode = {
      Form(
        Form.Group(
          Label("AC"),
          Form.Field(
            EditableComponent(
              view = <.div(state.info.armorClass),
              edit = ArmorClassEditor(
                state.info.armorClass,
                onChange = armorClass => $.modState(s => s.copy(info = state.info.copy(armorClass = armorClass)))
              ),
              title = "Armor Class",
              onEditingModeChange = doModeChange
            )
          )
        ),
        Form.Group(
          Label("Alignment"),
          Form
            .Dropdown()
            .fluid(true)
            .search(false)
            .clearable(true)
            .placeholder("All")
            .options(
              Alignment.values
                .map(s => DropdownItemProps().setValue(s.name).setText(s.name)).toJSArray
            )
            .onChange {
              (
                _,
                data
              ) =>
                val newVal: Option[Alignment] = data.value match {
                  case s: String if s.trim.isEmpty => None
                  case s: String                   => Alignment.values.find(_.name.equalsIgnoreCase(s))
                  case _ => None
                }

                $.modState(s => s.copy(info = state.info.copy(alignment = newVal.getOrElse(Alignment.trueNeutral))))
            }
            .value(state.info.alignment.name)
        ),
        Form.Group(
          <.div(
            ^.backgroundColor := state.info.health.lifeColor(),
            EditableComponent(
              view = <.table(
                <.thead(
                  <.tr(<.th("HP"), <.th("Temp HP"))
                ),
                <.tbody(
                  <.tr(
                    <.td(
                      s"${state.info.health.currentHitPoints}/${state.info.health.currentMax}",
                      " (stabilized)".when(
                        state.info.health.currentHitPoints <= 0 && state.info.health.deathSave.isStabilized
                      )
                    ),
                    <.td(state.info.health.temporaryHitPoints.toString)
                  )
                )
              ),
              edit = HealthEditor(
                state.info.health,
                onChange = hitPoints => $.modState(s => s.copy(info = s.info.copy(health = hitPoints)))
              ),
              title = "Hit Points",
              onEditingModeChange = doModeChange
            )
          )
        ),
        Form.Group(
          EditableComponent(
            view = <.table(
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
                  <.td(s"${state.info.abilities.strength.overridenValue}"),
                  <.td(s"${state.info.abilities.dexterity.overridenValue}"),
                  <.td(s"${state.info.abilities.constitution.overridenValue}"),
                  <.td(s"${state.info.abilities.intelligence.overridenValue}"),
                  <.td(s"${state.info.abilities.wisdom.overridenValue}"),
                  <.td(s"${state.info.abilities.charisma.overridenValue}")
                ),
                <.tr(
                  <.td(s"(${state.info.abilities.strength.modifierString})"),
                  <.td(s"(${state.info.abilities.dexterity.modifierString})"),
                  <.td(s"(${state.info.abilities.constitution.modifierString})"),
                  <.td(s"(${state.info.abilities.intelligence.modifierString})"),
                  <.td(s"(${state.info.abilities.wisdom.modifierString})"),
                  <.td(s"(${state.info.abilities.charisma.modifierString})")
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
                  <.td(s"${state.info.abilities.strength.savingThrowString(state.info.proficiencyBonus)}"),
                  <.td(s"${state.info.abilities.dexterity.savingThrowString(state.info.proficiencyBonus)}"),
                  <.td(s"${state.info.abilities.constitution.savingThrowString(state.info.proficiencyBonus)}"),
                  <.td(s"${state.info.abilities.intelligence.savingThrowString(state.info.proficiencyBonus)}"),
                  <.td(s"${state.info.abilities.wisdom.savingThrowString(state.info.proficiencyBonus)}"),
                  <.td(s"${state.info.abilities.charisma.savingThrowString(state.info.proficiencyBonus)}")
                )
              )
            ),
            edit = AbilitiesEditor(
              state.info.abilities,
              onChange = abilities => $.modState(s => s.copy(info = s.info.copy(abilities = abilities)))
            ),
            title = "Abilities",
            onEditingModeChange = doModeChange
          )
        ),
        Form.Group(
          Label("Speed"),
          Form.Field(
            EditableComponent(
              edit = SpeedsEditor(
                state.info.speeds,
                speeds => $.modState(s => s.copy(info = s.info.copy(speeds = speeds)))
              ),
              view = state.info.speeds.headOption.fold(<.div("Click to add")) { _ =>
                <.table(
                  <.thead(
                    <.tr(
                      state.info.speeds
                        .map(sp => <.th(^.key := sp.speedType.toString, sp.speedType.toString)).toVdomArray
                    )
                  ),
                  <.tbody(
                    <.tr(
                      state.info.speeds.map(sp => <.th(^.key := sp.speedType.toString, sp.value.toString)).toVdomArray
                    )
                  )
                )
              },
              title = "Speeds",
              onEditingModeChange = doModeChange
            )
          )
        ),
        Form.Group(
          Label("Skills"),
          Form.Field(
            EditableComponent(
              view = <.table(
                ^.minHeight := 330.px,
                <.tbody(
                  <.tr(
                    <.th(^.width := 23.px, s"${state.info.skills.acrobatics.proficiencyLevel.profStr}Acrobatics"),
                    <.td(^.width := "50%", state.info.skills.acrobatics.modifierString(state.info.abilities)),
                    <.th(^.width := 23.px, s"${state.info.skills.medicine.proficiencyLevel.profStr}Medicine"),
                    <.td(^.width := "50%", state.info.skills.medicine.modifierString(state.info.abilities))
                  ),
                  <.tr(
                    <.th(s"${state.info.skills.animalHandling.proficiencyLevel.profStr}Animal H."),
                    <.td(state.info.skills.animalHandling.modifierString(state.info.abilities)),
                    <.th(s"${state.info.skills.nature.proficiencyLevel.profStr}Nature"),
                    <.td(state.info.skills.nature.modifierString(state.info.abilities))
                  ),
                  <.tr(
                    <.th(s"${state.info.skills.arcana.proficiencyLevel.profStr}Arcana"),
                    <.td(state.info.skills.arcana.modifierString(state.info.abilities)),
                    <.th(s"${state.info.skills.perception.proficiencyLevel.profStr}Perception"),
                    <.td(state.info.skills.perception.modifierString(state.info.abilities))
                  ),
                  <.tr(
                    <.th(s"${state.info.skills.athletics.proficiencyLevel.profStr}Athletics"),
                    <.td(state.info.skills.athletics.modifierString(state.info.abilities)),
                    <.th(s"${state.info.skills.performance.proficiencyLevel.profStr}Perf."),
                    <.td(state.info.skills.performance.modifierString(state.info.abilities))
                  ),
                  <.tr(
                    <.th(s"${state.info.skills.deception.proficiencyLevel.profStr}Deception"),
                    <.td(state.info.skills.deception.modifierString(state.info.abilities)),
                    <.th(s"${state.info.skills.persuasion.proficiencyLevel.profStr}Persuasion"),
                    <.td(state.info.skills.persuasion.modifierString(state.info.abilities))
                  ),
                  <.tr(
                    <.th(s"${state.info.skills.history.proficiencyLevel.profStr}History"),
                    <.td(state.info.skills.history.modifierString(state.info.abilities)),
                    <.th(s"${state.info.skills.religion.proficiencyLevel.profStr}Religion"),
                    <.td(state.info.skills.religion.modifierString(state.info.abilities))
                  ),
                  <.tr(
                    <.th(s"${state.info.skills.insight.proficiencyLevel.profStr}Insight"),
                    <.td(state.info.skills.insight.modifierString(state.info.abilities)),
                    <.th(s"${state.info.skills.sleightOfHand.proficiencyLevel.profStr}Sleight of H."),
                    <.td(state.info.skills.sleightOfHand.modifierString(state.info.abilities))
                  ),
                  <.tr(
                    <.th(s"${state.info.skills.intimidation.proficiencyLevel.profStr}Intim."),
                    <.td(state.info.skills.intimidation.modifierString(state.info.abilities)),
                    <.th(s"${state.info.skills.stealth.proficiencyLevel.profStr}Stealth"),
                    <.td(state.info.skills.stealth.modifierString(state.info.abilities))
                  ),
                  <.tr(
                    <.th(s"${state.info.skills.investigation.proficiencyLevel.profStr}Invest."),
                    <.td(state.info.skills.investigation.modifierString(state.info.abilities)),
                    <.th(s"${state.info.skills.survival.proficiencyLevel.profStr}Survival"),
                    <.td(state.info.skills.survival.modifierString(state.info.abilities))
                  )
                )
              ),
              edit = SkillsEditor(
                state.info.skills,
                state.info.abilities,
                onChange = skills => $.modState(s => s.copy(info = s.info.copy(skills = skills)))
              ),
              title = "Skills",
              onEditingModeChange = doModeChange
            )
          )
        )
      )
    }

    private def PersonalCharacteristics(state: State): VdomNode =
      Form(
        Form.Group(
          Label("Size"),
          Form.Dropdown
            .placeholder("Choose").compact(true).options(
              CreatureSize.values
                .map(s =>
                  DropdownItemProps()
                    .setValue(s.ordinal)
                    .setText(s.toString.capitalize)
                ).toJSArray
            ).onChange {
              (
                _,
                changedData
              ) =>
                val newVal = changedData.value match {
                  case s: String => s.toInt
                  case s: Double => s.toInt
                  case _ => throw RuntimeException("Unexpected value")
                }

                $.modState(s => s.copy(info = s.info.copy(size = CreatureSize.fromOrdinal(newVal))))
            }.value(state.info.size.ordinal)
        ),
        Form.Group(
          Label("Languages"),
          Form.Field(
            EditableComponent(
              view = state.info.languages.headOption.fold("Click to add")(_ =>
                state.info.languages.map(_.name).mkString(", ")
              ),
              edit = LanguageEditor(
                state.info.languages,
                languages => $.modState(s => s.copy(info = s.info.copy(languages = languages)))
              ),
              title = "Languages",
              onEditingModeChange = doModeChange
            )
          )
        ),
        Form.Group(
          Label("Feats"),
          Form.Field(
            EditableComponent(
              view = state.info.feats.headOption.fold("Click to add")(_ => state.info.feats.map(_.name).mkString(", ")),
              edit = FeatsEditor(state.info.feats, feats => $.modState(s => s.copy(info = s.info.copy(feats = feats)))),
              title = "Feats",
              onEditingModeChange = doModeChange
            )
          )
        ),
        Form.Group(
          Label("Senses"),
          Form.Field(
            EditableComponent(
              view = <.div(state.info.senses.headOption.fold("Click to add") { _ =>
                state.info.senses.map(s => s"${s.sense} ${s.range}").mkString(", ")
              }),
              edit =
                SensesEditor(state.info.senses, senses => $.modState(s => s.copy(info = s.info.copy(senses = senses)))),
              title = "Senses",
              onEditingModeChange = doModeChange
            )
          )
        ),
        Form.Group(
          Form.Input
            .label("hair")
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s => s.copy(info = s.info.copy(hair = data.value.toOption.fold("")(_.asInstanceOf[String]))))
            )
            .value(state.info.hair),
          Form.Button
            .icon(true)(Icon.name(SemanticICONS.`shuffle`))
            .onClick(
              (
                _,
                _
              ) => $.modState(s => s.copy(info = s.info.copy(hair = hair)))
            )
        ),
        Form.Group(
          Form.Input
            .label("Skin")
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s => s.copy(info = s.info.copy(skin = data.value.toOption.fold("")(_.asInstanceOf[String]))))
            )
            .value(state.info.skin),
          Form.Button
            .icon(true)(Icon.name(SemanticICONS.`shuffle`)).onClick(
              (
                _,
                _
              ) => $.modState(s => s.copy(info = s.info.copy(skin = skin)))
            )
        ),
        Form.Group(
          Form.Input
            .label("Eyes")
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s => s.copy(info = s.info.copy(eyes = data.value.toOption.fold("")(_.asInstanceOf[String]))))
            )
            .value(state.info.eyes),
          Form.Button
            .icon(true)(Icon.name(SemanticICONS.`shuffle`)).onClick(
              (
                _,
                _
              ) => $.modState(s => s.copy(info = s.info.copy(eyes = eyes)))
            )
        ),
        Form.Group(
          Form.Input
            .label("Height")
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info = s.info.copy(height = data.value.toOption.fold("")(_.asInstanceOf[String])))
                )
            )
            .value(state.info.height),
          Form.Button
            .icon(true)(Icon.name(SemanticICONS.`shuffle`)).onClick(
              (
                _,
                _
              ) => $.modState(s => s.copy(info = s.info.copy(height = height(s.info.size))))
            )
        ),
        Form.Group(
          Form.Input
            .label("Weight")
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info = s.info.copy(weight = data.value.toOption.fold("")(_.asInstanceOf[String])))
                )
            )
            .value(state.info.weight),
          Form.Button
            .icon(true)(Icon.name(SemanticICONS.`shuffle`)).onClick(
              (
                _,
                _
              ) => $.modState(s => s.copy(info = s.info.copy(weight = weight(s.info.size))))
            )
        ),
        Form.Group(
          Form.Input
            .label("Age")
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s => s.copy(info = s.info.copy(age = data.value.toOption.fold("")(_.asInstanceOf[String]))))
            )
            .value(state.info.age),
          Form.Button
            .icon(true)(Icon.name(SemanticICONS.`shuffle`)).onClick(
              (
                _,
                _
              ) => $.modState(s => s.copy(info = s.info.copy(age = age)))
            )
        ),
        Form.Group(
          Form.Input
            .label("Gender")
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info = s.info.copy(gender = data.value.toOption.fold("")(_.asInstanceOf[String])))
                )
            )
            .value(state.info.gender),
          Form.Button
            .icon(true)(Icon.name(SemanticICONS.`shuffle`)).onClick(
              (
                _,
                _
              ) => $.modState(s => s.copy(info = s.info.copy(gender = gender)))
            )
        )
      )

    private def Other(state: State): VdomNode =
      Form(
        Form.Group(
          Form.Button
            .primary(true)
            .onClick {
              (
                _,
                _
              ) =>
                $.modState(
                  s => s.copy(generating = true),
                  DND5eGraphQLRepository.live
                    .aiGenerateNPCDetails(
                      NonPlayerCharacter(
                        header = state.header,
                        jsonInfo = state.info.toJsonAST.toOption.get
                      )
                    ).map { npc =>
                      $.modState(s => s.copy(header = npc.header, info = npc.info, generating = false))
                    }.completeWith(_.get)
                )
            }("Generate Other Details (AI)").when(!state.generating),
          Loader
            .inline(true)
            .active(state.generating)
            .indeterminate(true)("Generating Details"),
          Button
            .secondary(true)
            .onClick(
              (
                _,
                _
              ) =>
                $.modState(s =>
                  s.copy(info =
                    s.info.copy(
                      faith = None,
                      traits = Traits(),
                      organizations = "",
                      allies = "",
                      enemies = "",
                      backstory = ""
                    )
                  )
                )
            )
            .disabled(state.generating)("Clear Generated Fields")
        ),
        Form.Group(
          Form.Input
            .label("Name")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(header = s.header.copy(name = data.value.toOption.map(_.asInstanceOf[String]).getOrElse("")))
                )
            )
            .value(state.header.name)
        ),
        Form.Group(
          Label("Relation To Players"),
          Form.Dropdown
            .placeholder("Choose Relation To Players")
            .clearable(true)
            .compact(true)
            .allowAdditions(false)
            .search(true)
            .options(
              RelationToPlayers.values
                .sortBy(_.toString)
                .map(relation =>
                  DropdownItemProps()
                    .setValue(relation.toString)
                    .setText(relation.toString)
                ).toJSArray
            )
            .onChange(
              (
                _,
                changedData
              ) =>
                $.modState { s =>
                  val relationToPlayers: RelationToPlayers = changedData.value match {
                    case str: String if str.isEmpty => s.info.relationToPlayers
                    case str: String =>
                      RelationToPlayers.values.find(_.toString == str).getOrElse(RelationToPlayers.unknown)
                    case _ => throw RuntimeException("Unexpected value")
                  }

                  s.copy(info = s.info.copy(relationToPlayers = relationToPlayers))
                }
            )
            .value(state.info.relationToPlayers.toString)
        ),
        Form.Group(
          Label("Lifestyle"),
          Form.Dropdown
            .placeholder("Choose Lifestyle")
            .clearable(true)
            .compact(true)
            .allowAdditions(false)
            .search(true)
            .options(
              Lifestyle.values
                .sortBy(_.toString)
                .map(relation =>
                  DropdownItemProps()
                    .setValue(relation.toString)
                    .setText(relation.toString)
                ).toJSArray
            )
            .onChange(
              (
                _,
                changedData
              ) =>
                $.modState { s =>
                  val lifestyle: Lifestyle = changedData.value match {
                    case str: String if str.isEmpty => s.info.lifestyle
                    case str: String =>
                      Lifestyle.values.find(_.toString == str).getOrElse(Lifestyle.unknown)
                    case _ => throw RuntimeException("Unexpected value")
                  }

                  s.copy(info = s.info.copy(lifestyle = lifestyle))
                }
            )
            .value(state.info.lifestyle.toString)
        ),
        Form.Group(
          Form.Input
            .label("Faith")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) => $.modState(s => s.copy(info = s.info.copy(faith = data.value.toOption.map(_.asInstanceOf[String]))))
            ).value(state.info.faith.getOrElse(""))
        ),
        Form.Group(
          Form.Input
            .label("Personality Traits")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info =
                    s.info
                      .copy(traits =
                        s.info.traits.copy(personalityTraits = data.value.toOption.map(_.asInstanceOf[String]))
                      )
                  )
                )
            ).value(state.info.traits.personalityTraits.getOrElse(""))
        ),
        Form.Group(
          Form.Input
            .label("Ideals")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info =
                    s.info
                      .copy(traits = s.info.traits.copy(ideals = data.value.toOption.map(_.asInstanceOf[String])))
                  )
                )
            ).value(state.info.traits.ideals.getOrElse(""))
        ),
        Form.Group(
          Form.Input
            .label("Bonds")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info =
                    s.info
                      .copy(traits = s.info.traits.copy(bonds = data.value.toOption.map(_.asInstanceOf[String])))
                  )
                )
            ).value(state.info.traits.bonds.getOrElse(""))
        ),
        Form.Group(
          Form.Input
            .label("Flaws")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info =
                    s.info
                      .copy(traits = s.info.traits.copy(flaws = data.value.toOption.map(_.asInstanceOf[String])))
                  )
                )
            ).value(state.info.traits.flaws.getOrElse(""))
        ),
        Form.Group(
          Form.Input
            .label("Organizations")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info = s.info.copy(organizations = data.value.toOption.fold("")(_.asInstanceOf[String])))
                )
            ).value(state.info.organizations)
        ),
        Form.Group(
          Form.Input
            .label("Allies")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info = s.info.copy(allies = data.value.toOption.fold("")(_.asInstanceOf[String])))
                )
            ).value(state.info.allies)
        ),
        Form.Group(
          Form.Input
            .label("Enemies")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info = s.info.copy(enemies = data.value.toOption.fold("")(_.asInstanceOf[String])))
                )
            ).value(state.info.enemies)
        ),
        Form.Group(
          Form.Input
            .label("Backstory")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info = s.info.copy(backstory = data.value.toOption.fold("")(_.asInstanceOf[String])))
                )
            ).value(state.info.backstory)
        ),
        Form.Group(
          Form.Input
            .label("Physical Description")
            .width(SemanticWIDTHS.fourteen)
            .onChange(
              (
                _,
                data
              ) =>
                $.modState(s =>
                  s.copy(info =
                    s.info
                      .copy(traits = s.info.traits.copy(appearance = data.value.toOption.map(_.asInstanceOf[String])))
                  )
                )
            ).value(state.info.traits.appearance.getOrElse(""))
        )
      )

    private def readyToSave(state: State): Boolean = {
      // Do any save validation here.
      true
    }

    private def ConfirmTab(
      state:         State,
      props:         Props,
      dmScreenState: DMScreenState
    ): VdomNode = {
      Form(
        Form.Button
          .primary(true)
          .disabled(!readyToSave(state))
          .onClick {
            (
              _,
              _
            ) =>
              val npc = NonPlayerCharacter(
                header = state.header,
                jsonInfo = state.info.toJsonAST.getOrElse(Json.Null)
              )

              val removedScenes = state.scenes.map(_.id).filterNot(state.selectedScenes.contains)

              (
                for {
                  newNpcId <- DND5eGraphQLRepository.live.upsert(npc.header, npc.jsonInfo)
                  _ <- AsyncCallback.traverse(removedScenes)(sceneId =>
                    DND5eGraphQLRepository.live.removeNpcFromScene(sceneId, newNpcId)
                  )
                  _ <- AsyncCallback.traverse(state.selectedScenes)(sceneId =>
                    DND5eGraphQLRepository.live.addNpcToScene(sceneId, newNpcId)
                  )
                } yield props.onSaved(npc.copy(header = npc.header.copy(id = newNpcId)))
              ).completeWith(_.get)
          }("Save and Close"),
        Form.Button
          .secondary(true).onClick(
            (
              _,
              _
            ) => Confirm.confirm("Are you sure you want to cancel and undo all that work?", onConfirm = props.onCancel)
          )("Cancel")
      )

    }

    def render(
      props: Props,
      state: State
    ): VdomElement = {

      DMScreenState.ctx.consume { dmScreenState =>
        def renderStep(step: WizardStepType): VdomNode = {
          step match {
            case WizardStepType.scenes                    => Scenes(state)
            case WizardStepType.race                      => RaceTab(state)
            case WizardStepType.npcClass                  => NPCClass(state)
            case WizardStepType.`personalCharacteristics` => PersonalCharacteristics(state)
            case WizardStepType.stats                     => Stats(state)
            case WizardStepType.other                     => Other(state)
            case WizardStepType.confirmWizard             => ConfirmTab(state, props, dmScreenState)
          }
        }
        dmScreenState.campaignState.fold {
          <.div("Campaign Loading")
        } { case campaignState: DND5eCampaignState =>

          <.div(
            //        ^.className := "ui container",
            ^.style := CSSProperties().set("backgroundColor", "#ffffff").set("border", "1px solid black"),
            Step.Group
              .size(mini)
              .ordered(true)(
                WizardStepType.values.map { step =>
                  Step
                    .withKey(step.name)
                    .active(state.currentStep == step)
                    .onClick(
                      (
                        _,
                        _
                      ) => $.modState(_.copy(currentStep = step))
                    )(
                      Step.Content(
                        Step.Title(step.name),
                        Step.Description(step.name)
                      )
                    )
                }*
              ),
            WizardStepType.values
              .find(_ == state.currentStep).fold(EmptyVdom)(s => <.div(^.marginLeft := 8.px, renderStep(s)))
          )
        }
      }

    }

  }

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("EncounterWizard")
    .initialStateFromProps { props =>
      State(
        header = NonPlayerCharacterHeader(id = NonPlayerCharacterId.empty, campaignId = props.campaign.id, name = ""),
        info = NonPlayerCharacterInfo(
          health = Health(deathSave = DeathSave.empty, currentHitPoints = 1, maxHitPoints = 1),
          armorClass = 10,
          classes = List.empty
        ),
        selectedScenes = props.selectedScenes
      )
    }
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.loadState())
    .shouldComponentUpdatePure($ => $.nextState.editingMode != EditingMode.edit) // Don't update while we have a dialog open
    .build

  def apply(
    campaign:       Campaign,
    selectedScenes: Set[SceneId] = Set.empty,
    onCancel:       Callback = Callback.empty,
    onSaved:        NonPlayerCharacter => Callback = _ => Callback.empty,
    npcId:          Option[NonPlayerCharacterId] = None
  ): Unmounted[Props, State, Backend] =
    component(
      Props(campaign = campaign, selectedScenes = selectedScenes, onCancel = onCancel, onSaved = onSaved, npcId = npcId)
    )

}
