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
