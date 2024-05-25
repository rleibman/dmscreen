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
import zio.json.ast.Json

case class Scene(
  name:       String,
  isActive:   Boolean,
  notes:      String,
  npcs:       List[NonPlayerCharacterId] = List.empty,
  encounters: List[EncounterId] = List.empty
)

case class DND5eCampaignInfo(
  notes:  String,
  scenes: List[Scene] = List.empty
)

case class DND5eCampaign(
  override val header:   CampaignHeader,
  override val jsonInfo: Json
) extends Campaign[DND5eCampaignInfo] {

  override val entityType: EntityType = DND5eEntityType.campaign

}

enum DND5eEntityType(val name: String) {

  case campaign extends DND5eEntityType("campaign") with EntityType
  case encounter extends DND5eEntityType("encounter") with EntityType
  case playerCharacter extends DND5eEntityType("playerCharacter") with EntityType
  case nonPlayerCharacter extends DND5eEntityType("nonPlayerCharacter") with EntityType
  case scene extends DND5eEntityType("scene") with EntityType
  case monster extends DND5eEntityType("monster") with EntityType
  case spell extends DND5eEntityType("spell") with EntityType

}
