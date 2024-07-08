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

case class DND5eCampaignInfo(
  notes: String
)

case class DND5eCampaign(
  override val header:   CampaignHeader,
  override val jsonInfo: Json,
  override val version:  SemVer = SemVer.parse(dmscreen.BuildInfo.version).getOrElse(SemVer.unsafeParse("0.0.0"))
) extends Campaign[DND5eCampaignInfo] {

  override val entityType: EntityType[CampaignId] = DND5eEntityType.campaign

}

object DND5eEntityType {

  val campaign: DND5eEntityType[CampaignId] = new DND5eEntityType[CampaignId](name = "campaign") {
    override def createId(id: Long): CampaignId = CampaignId(id)
  }
  val encounter: DND5eEntityType[EncounterId] = new DND5eEntityType[EncounterId](name = "encounter") {
    override def createId(id: Long): EncounterId = EncounterId(id)
  }
  val playerCharacter: DND5eEntityType[PlayerCharacterId] =
    new DND5eEntityType[PlayerCharacterId](name = "playerCharacter") {
      override def createId(id: Long): PlayerCharacterId = PlayerCharacterId(id)
    }
  val nonPlayerCharacter: DND5eEntityType[NonPlayerCharacterId] =
    new DND5eEntityType[NonPlayerCharacterId](name = "nonPlayerCharacter") {
      override def createId(id: Long): NonPlayerCharacterId = NonPlayerCharacterId(id)
    }
  val scene: DND5eEntityType[SceneId] = new DND5eEntityType[SceneId](name = "scene") {
    override def createId(id: Long): SceneId = SceneId(id.asInstanceOf[Long])
  }
  val monster: DND5eEntityType[MonsterId] = new DND5eEntityType[MonsterId](name = "monster") {
    override def createId(id: Long): MonsterId = MonsterId(id)
  }
  val spell: DND5eEntityType[SpellId] = new DND5eEntityType[SpellId](name = "spell") {
    override def createId(id: Long): SpellId = SpellId(id)
  }

  val values: Set[DND5eEntityType[?]] =
    Set(campaign, encounter, playerCharacter, nonPlayerCharacter, scene, monster, spell)

  def valueOf(value:       String): DND5eEntityType[?] = values.find(v => value.equalsIgnoreCase(v.name)).get
  def valueOfOption(value: String): Option[DND5eEntityType[?]] = values.find(v => value.equalsIgnoreCase(v.name))

}

sealed abstract class DND5eEntityType[EntityId](override val name: String) extends EntityType[EntityId]
