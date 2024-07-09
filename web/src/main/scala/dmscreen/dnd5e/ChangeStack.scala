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
  clear:      Boolean = true,
  campaign:   Boolean = false,
  pcs:        Set[PlayerCharacterId] = Set.empty,
  npcs:       Set[NonPlayerCharacterId] = Set.empty,
  scenes:     Set[SceneId] = Set.empty,
  encounters: Set[EncounterId] = Set.empty,
  monsters:   Set[MonsterId] = Set.empty
) {

  def logCampaignChange(): ChangeStack = {
    copy(campaign = true, clear = false)
  }
  def logPCChanges(playerCharacterIds: PlayerCharacterId*): ChangeStack = {
    copy(pcs = pcs ++ playerCharacterIds, clear = false)
  }
  def logNPCChanges(nonPlayerCharacterIds: NonPlayerCharacterId*): ChangeStack = {
    copy(npcs = npcs ++ nonPlayerCharacterIds, clear = false)
  }
  def logSceneChanges(sceneIds: SceneId*): ChangeStack = {
    copy(scenes = scenes ++ sceneIds, clear = false)
  }
  def logEncounterChanges(encounterIds: EncounterId*): ChangeStack = {
    copy(encounters = encounters ++ encounterIds, clear = false)
  }
  def logMonsterChanges(monsterIds: MonsterId*): ChangeStack = {
    copy(monsters = monsters ++ monsterIds, clear = false)
  }

  // ENHANCEMENT add to this a stream that will let us know when other uses have made changes

}
