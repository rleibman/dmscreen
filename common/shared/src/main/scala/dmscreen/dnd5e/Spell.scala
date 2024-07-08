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
import zio.json.ast.Json

opaque type SpellId = Long

object SpellId {

  def apply(spellId: Long): SpellId = spellId

  extension (spellId: SpellId) {

    def value: Long = spellId

  }

}

case class SpellHeader(
  id:   SpellId,
  name: String
) extends HasId[SpellId]

case class SpellInfo(
  level:          Int,
  school:         String,
  castingTime:    String,
  range:          String,
  components:     String,
  duration:       String,
  description:    String,
  atHigherLevels: Option[String],
  classes:        List[CharacterClassId],
  source:         Source
)

case class Spell(
  header:               SpellHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[SpellId, SpellHeader, SpellInfo] {

  override def entityType: EntityType[SpellId] = DND5eEntityType.spell

}
