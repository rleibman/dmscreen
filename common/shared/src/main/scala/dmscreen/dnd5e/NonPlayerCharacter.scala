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

import dmscreen.*
import just.semver.SemVer
import zio.json.*
import zio.json.ast.Json

case class StatBlock()

opaque type NonPlayerCharacterId = Long

object NonPlayerCharacterId {

  def apply(value: Long): NonPlayerCharacterId = value

  extension (npcId: NonPlayerCharacterId) {

    def value: Long = npcId

  }

}

case class NonPlayerCharacterHeader(
  id:         NonPlayerCharacterId,
  campaignId: CampaignId,
  name:       String
) extends HasId[NonPlayerCharacterId]

case class NonPlayerCharacterInfo(
  gender:         String,
  race:           Race,
  characterClass: CharacterClass,
  level:          Int,
  age:            Int,
  background:     Background,
  occupation:     String,
  personality:    String,
  ideal:          String,
  bond:           String,
  flaw:           String,
  characteristic: String,
  speech:         String,
  hobby:          String,
  fear:           String,
  currently:      String,
  nickname:       String,
  weapon:         String,
  rumor:          String,
  raisedBy:       String,
  parent1:        String,
  parent2:        String,
  siblingCount:   Int,
  childhood:      String,
  children:       String,
  spouse:         String,
  monster:        MonsterId
)

case class NonPlayerCharacter(
  header:               NonPlayerCharacterHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[NonPlayerCharacterId, NonPlayerCharacterHeader, NonPlayerCharacterInfo] {

  override val entityType: EntityType = DND5eEntityType.nonPlayerCharacter

}
