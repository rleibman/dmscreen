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

package dmscreen.sta

import dmscreen.{EntityType, *}
import just.semver.SemVer
import zio.json.ast.Json

case class STACampaignInfo(
  era:   Era,
  notes: String
)

object STAEntityType {

  val character: EntityType[CharacterId] = new STAEntityType[CharacterId](name = "character") {
    override def createId(id: Long): CharacterId = CharacterId(id)
  }
  val starship: EntityType[StarshipId] = new STAEntityType[StarshipId](name = "starship") {
    override def createId(id: Long): StarshipId = StarshipId(id)
  }
  val nonPlayerCharacter: EntityType[NonPlayerCharacterId] =
    new STAEntityType[NonPlayerCharacterId](name = "nonPlayerCharacter") {
      override def createId(id: Long): NonPlayerCharacterId = NonPlayerCharacterId(id)
    }
  val scene: EntityType[SceneId] = new STAEntityType[SceneId](name = "scene") {
    override def createId(id: Long): SceneId = SceneId(id)
  }

  val values: Set[EntityType[?]] =
    Set(CampaignEntityType, character, starship, nonPlayerCharacter, scene)

  def valueOf(value:       String): EntityType[?] = values.find(v => value.equalsIgnoreCase(v.name)).get
  def valueOfOption(value: String): Option[EntityType[?]] = values.find(v => value.equalsIgnoreCase(v.name))

}

sealed abstract class STAEntityType[EntityId](override val name: String) extends EntityType[EntityId]
