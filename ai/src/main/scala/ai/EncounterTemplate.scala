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

package ai

import dmscreen.Campaign
import dmscreen.dnd5e.*

object EncounterTemplate {

  def template(
    campaign:  Campaign,
    sceneOpt:  Option[Scene],
    encounter: Encounter
  ): String = {
    val sceneInfo = sceneOpt.map(_.info)
    val info = encounter.info

    s"""I am running a dungeons and dragons encounter.
       |The Encounter is part of the campaign called ${campaign.header.name}
       |${sceneOpt.fold("")(scene => s"It is part of the scene called ${scene.header.name}")}
       |${sceneInfo.fold("")(si => s"Notes about the scene: ${si.notes}")}
       |Encounter Information:
       |  ${info.timeOfDay match {
        case EncounterTimeOfDay.unimportant => ""
        case other                          => s"Encounter Time of Day: $other.toString"
      }}
       |  ${info.biome match {
        case Biome.Unimportant => ""
        case other             => s"Biome: $other.toString"
      }}
       | Encounter Difficulty: ${info.desiredDifficulty.toString}
       | ${info.locationNotes.trim.headOption.fold("")(_ => s"Location Notes: ${info.locationNotes}")}
       | ${info.initialDescription.trim.headOption.fold("")(_ => s"Initial Description: ${info.initialDescription}")}
       | ${info.notes.trim.headOption.fold("")(_ => s"Other Notes: ${info.notes}")}
       | 
       | Monsters:
       | ${info.monsters.map(monster => s"  ${monster.monsterHeader.name} ").mkString("\n")}
       |
       | NPCs:
       | ${info.npcs.map(npc => s"  ${npc.name} ").mkString("\n")}
       | 
       | Given all of the above, please generate a description of the encounter, no more than 3 paragraphs.
       | 
       |""".stripMargin
  }

  /*
    combatants:           List[EncounterCombatant] = List.empty,
    desiredDifficulty:    EncounterDifficulty = EncounterDifficulty.Medium,
    treasure:             Treasure = Treasure(),
    generatedDescription: String = ""
   */
}
