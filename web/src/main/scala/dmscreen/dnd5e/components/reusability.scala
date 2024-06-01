package dmscreen.dnd5e.components

import dmscreen.dnd5e.*
import japgolly.scalajs.react.Reusability

import scala.language.unsafeNulls

given Reusability[DeathSave] = Reusability.derive[DeathSave]
given Reusability[DeathSave | Int] =
  Reusability.by {
    case d: DeathSave => d.toString
    case i: Int       => i.toString
  }

given Reusability[Skill] = Reusability.derive[Skill]
given Reusability[Skills] = Reusability.derive[Skills]
given Reusability[HitPoints] = Reusability.derive[HitPoints]
given Reusability[Feat] = Reusability.derive[Feat]
given Reusability[Condition] = Reusability.derive[Condition]
given Reusability[PlayerCharacter] = Reusability.by(_.toString)
given Reusability[Subclass] = Reusability.string.contramap(_.name)
given Reusability[CharacterClassId] = Reusability.string.contramap(_.name)
given Reusability[SkillType] = Reusability.by(_.toString)
given Reusability[AdvantageDisadvantage] = Reusability.by(_.toString)
given Reusability[PlayerCharacterClass] = Reusability.derive[PlayerCharacterClass]
