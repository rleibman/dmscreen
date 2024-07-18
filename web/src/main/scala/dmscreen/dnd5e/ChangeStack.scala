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
  pcs:        Map[PlayerCharacterId, PlayerCharacter] = Map.empty,
  npcs:       Map[NonPlayerCharacterId, NonPlayerCharacter] = Map.empty,
  scenes:     Map[SceneId, Scene] = Map.empty,
  encounters: Map[EncounterId, Encounter] = Map.empty,
  monsters:   Map[MonsterId, Monster] = Map.empty
) {

  def logCampaignChange(): ChangeStack = {
    copy(campaign = true, clear = false)
  }
  def logPCChanges(changed: PlayerCharacter*): ChangeStack = {
    copy(pcs = pcs ++ changed.map(item => item.id -> item), clear = false)
  }
  def logNPCChanges(changed: NonPlayerCharacter*): ChangeStack = {
    copy(npcs = npcs ++ changed.map(item => item.id -> item), clear = false)
  }
  def logSceneChanges(changed: Scene*): ChangeStack = {
    copy(scenes = scenes ++ changed.map(item => item.id -> item), clear = false)
  }
  def logEncounterChanges(changed: Encounter*): ChangeStack = {
    copy(encounters = encounters ++ changed.map(item => item.id -> item), clear = false)
  }
  def logMonsterChanges(changed: Monster*): ChangeStack = {
    copy(monsters = monsters ++ changed.map(item => item.id -> item), clear = false)
  }

  // ENHANCEMENT add to this a stream that will let us know when other uses have made changes

}
