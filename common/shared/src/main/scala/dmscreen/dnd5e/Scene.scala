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
import zio.json.ast.*

opaque type SceneId = Long

object SceneId {

  def empty: SceneId = SceneId(0)

  def apply(SceneId: Long): SceneId = SceneId

  extension (sceneId: SceneId) {

    def value: Long = sceneId

  }

}

case class SceneHeader(
  id:         SceneId,
  campaignId: CampaignId,
  name:       String,
  order:      Int
) extends HasId[SceneId]

case class SceneInfo(
  isActive: Boolean = false,
  notes:    String = "",
  npcs:     List[NonPlayerCharacterId] = List.empty,
  treasure: List[String] = List.empty
)

case class Scene(
  header:               SceneHeader,
  jsonInfo:             Json,
  override val version: SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends DMScreenEntity[SceneId, SceneHeader, SceneInfo] {

  override val entityType: EntityType = DND5eEntityType.scene

}
