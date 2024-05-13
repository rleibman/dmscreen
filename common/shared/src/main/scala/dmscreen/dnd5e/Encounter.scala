/*
 * Copyright 2020 Roberto Leibman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
