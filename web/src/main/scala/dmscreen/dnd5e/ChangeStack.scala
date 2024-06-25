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

import japgolly.scalajs.react.CallbackTo

case class ChangeStack(
  campaign:   Boolean = true,
  pcs:        Set[PlayerCharacterId] = Set.empty,
  npcs:       Set[NonPlayerCharacterId] = Set.empty,
  scenes:     Set[SceneId] = Set.empty,
  encounters: Set[EncounterId] = Set.empty,
  monsters:   Set[MonsterId] = Set.empty
) {

  def saveAll(DND5eCampaignState: DND5eCampaignState): CallbackTo[ChangeStack] = {
    CallbackTo(ChangeStack())
  }

  def logCampaignChange(): ChangeStack = {
    copy(campaign = true)
  }
  def logPCChange(playerCharacterId: PlayerCharacterId): ChangeStack = {
    copy(pcs = pcs + playerCharacterId)
  }
  def logNPCChange(nonPlayerCharacterId: NonPlayerCharacterId): ChangeStack = {
    copy(npcs = npcs + nonPlayerCharacterId)
  }
  def logSceneChange(sceneId: SceneId): ChangeStack = {
    copy(scenes = scenes + sceneId)
  }
  def logEncounterChange(encounterId: EncounterId): ChangeStack = {
    copy(encounters = encounters + encounterId)
  }
  def logMonsterChange(monsterId: MonsterId): ChangeStack = {
    copy(monsters = monsters + monsterId)
  }

  // TODO add to this a stream that will let us know when other uses have made changes

}
