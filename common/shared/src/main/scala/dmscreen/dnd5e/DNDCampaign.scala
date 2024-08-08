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

import dmscreen.{CampaignId, *}
import just.semver.SemVer
import zio.json.ast.Json

object DND5eEntityType {

  val encounter: EntityType[EncounterId] = new DND5eEntityType[EncounterId](name = "encounter") {
    override def createId(id: Long): EncounterId = EncounterId(id)
  }
  val playerCharacter: EntityType[PlayerCharacterId] =
    new DND5eEntityType[PlayerCharacterId](name = "playerCharacter") {
      override def createId(id: Long): PlayerCharacterId = PlayerCharacterId(id)
    }
  val nonPlayerCharacter: EntityType[NonPlayerCharacterId] =
    new DND5eEntityType[NonPlayerCharacterId](name = "nonPlayerCharacter") {
      override def createId(id: Long): NonPlayerCharacterId = NonPlayerCharacterId(id)
    }
  val scene: EntityType[SceneId] = new DND5eEntityType[SceneId](name = "scene") {
    override def createId(id: Long): SceneId = SceneId(id.asInstanceOf[Long])
  }
  val monster: EntityType[MonsterId] = new DND5eEntityType[MonsterId](name = "monster") {
    override def createId(id: Long): MonsterId = MonsterId(id)
  }
  val spell: EntityType[SpellId] = new DND5eEntityType[SpellId](name = "spell") {
    override def createId(id: Long): SpellId = SpellId(id)
  }

  val values: Set[EntityType[?]] =
    Set(encounter, playerCharacter, nonPlayerCharacter, scene, monster, spell)

  def valueOf(value:       String): EntityType[?] = values.find(v => value.equalsIgnoreCase(v.name)).get
  def valueOfOption(value: String): Option[EntityType[?]] = values.find(v => value.equalsIgnoreCase(v.name))

}

sealed abstract class DND5eEntityType[EntityId](override val name: String) extends EntityType[EntityId]
