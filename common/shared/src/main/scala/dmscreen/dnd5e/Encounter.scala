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

sealed abstract class EncounterEntity(
) {

  def notes: String

  def concentration: Boolean

  def hide: Boolean

  def hp: Int

  def ac: Int

  def initiative: Int

  def conditions: List[Condition]

  def name: String

}

case class MonsterEncounterEntity(
  monster:                    Monster,
  override val notes:         String,
  override val concentration: Boolean,
  override val hide:          Boolean,
  override val hp:            Int,
  override val ac:            Int,
  override val initiative:    Int,
  override val conditions:    List[Condition]
) extends EncounterEntity() {

  override val name: String = ???

}

case class PlayerCharacterEncounterEntity(
  playerCharacter:            PlayerCharacter,
  override val notes:         String,
  override val concentration: Boolean,
  override val hide:          Boolean,
  override val hp:            Int,
  override val ac:            Int,
  override val initiative:    Int,
  override val conditions:    List[Condition]
) extends EncounterEntity() {

  override val name: String = playerCharacter.info.name

}

opaque type EncounterId = Long

enum EncounterDifficulty {

  case Easy
  case Medium
  case Hard
  case Deadly

}

case class Encounter(
  id:         EncounterId,
  name:       String,
  entities:   List[EncounterEntity],
  difficulty: EncounterDifficulty,
  xp:         Int
)
