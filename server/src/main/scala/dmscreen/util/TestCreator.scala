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

package dmscreen.util

import dmscreen.*
import dmscreen.dnd5e.dndbeyond.DNDBeyondImporter
import dmscreen.dnd5e.fifthEditionCharacterSheet.FifthEditionCharacterSheetImporter
import dmscreen.dnd5e.srd.SRDImporter
import dmscreen.dnd5e.{*, given}
import zio.*
import zio.json.*

import java.io.File

object TestCreator extends ZIOApp {

  override type Environment = SRDImporter & DNDBeyondImporter & FifthEditionCharacterSheetImporter
  override def environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]
  override def bootstrap
    : ZLayer[ZIOAppArgs, Any, SRDImporter & DNDBeyondImporter & FifthEditionCharacterSheetImporter] = {
    ZLayer.make[Environment](DNDBeyondImporter.live, FifthEditionCharacterSheetImporter.live, SRDImporter.live)
  }

  private val `Chr'zyzx` = PlayerCharacter(
    PlayerCharacterHeader(
      id = PlayerCharacterId.empty,
      campaignId = CampaignId(1),
      name = "Chr'zyzx",
      playerName = Some("Kyle")
    ),
    PlayerCharacterInfo(
      source = DMScreenSource,
      race = Race("Thri-Kreen"),
      background = Some(Background("Volstrucker")),
      classes = List(
        PlayerCharacterClass(
          characterClass = CharacterClassId.ranger,
          subclass = Some(SubClass("Swarmkeeper")),
          level = 3
        )
      ),
      abilities = Abilities(
        strength = Ability(AbilityType.strength, 10, None, true),
        dexterity = Ability(AbilityType.dexterity, 16, None, true),
        constitution = Ability(AbilityType.constitution, 13, None, false),
        intelligence = Ability(AbilityType.intelligence, 10, None, false),
        wisdom = Ability(AbilityType.wisdom, 16, None, false),
        charisma = Ability(AbilityType.charisma, 10, None, false)
      ),
      hitPoints = HitPoints(currentHitPoints = DeathSave(fails = 2, successes = 2), 30),
      armorClass = 16
    ).toJsonAST.toOption.get
  )

  val createPcs: ZIO[DNDBeyondImporter & FifthEditionCharacterSheetImporter, DMScreenError, List[PlayerCharacter]] =
    for {
      dndBeyondImporter <- ZIO.service[DNDBeyondImporter]
      wryax <- dndBeyondImporter.importPlayerCharacter(getClass.getResource("/wryrax-dndbeyond.json").nn.toURI.nn)
      nut   <- dndBeyondImporter.importPlayerCharacter(getClass.getResource("/nut-dndbeyond.json").nn.toURI.nn)
      fifthEditionCharacterSheetImporter <- ZIO.service[FifthEditionCharacterSheetImporter]
      wryax2 <- fifthEditionCharacterSheetImporter.importPlayerCharacter(
        getClass.getResource("/Wryax-FifthEditionCharacterSheet.xml").nn.toURI.nn
      )
    } yield List(`Chr'zyzx`, wryax, nut, wryax2)

  val createMonsters
    : ZIO[SRDImporter & DNDBeyondImporter & FifthEditionCharacterSheetImporter, DMScreenError, List[Monster]] = {
    val file = File("/home/rleibman/projects/dmscreen/common/shared/src/main/resources/5e-SRD-Monsters.json")
    for {
      _        <- ZIO.fail(DMScreenError(s"File ${file.getAbsolutePath} does not exist")).when(!file.exists())
      importer <- ZIO.service[SRDImporter]
      monsters <- importer.importMonsters(file).take(30).runCollect
    } yield monsters.toList
  }

  def createEncounter(
    monsters: List[Monster]
  ): UIO[Encounter] = {
    for {
      howManyDifferentMonsters <- ZIO.random.flatMap(_.nextIntBetween(1, 4))
      differentMonsterIndexes <- ZIO.foreach(1 to howManyDifferentMonsters) { _ =>
        ZIO.random.flatMap(r => r.nextIntBetween(0, monsters.size))
      }
      differentMonsters = differentMonsterIndexes.map(monsters)
      monstersWithCounts <- ZIO.foreach(differentMonsters) { monster =>
        ZIO.random.flatMap(r => r.nextIntBetween(1, 4)).map(count => (monster, count))
      }
      totalEntities = monstersWithCounts.map(_._2).sum
      initiatives <- ZIO.random.flatMap(r => ZIO.foreach(1 to (totalEntities + 1))(_ => r.nextIntBetween(1, 21)))
    } yield {
      def encounterInfo: EncounterInfo = {
        val entities: Seq[MonsterCombatant] = monstersWithCounts.flatMap { case (monster, count) =>
          (1 to count).map { i =>
            MonsterCombatant(
              id = CombatantId.empty,
              monsterHeader = monster.header,
              notes = "These are some notes",
              hitPoints = HitPoints(
                currentHitPoints = monster.header.maximumHitPoints,
                maxHitPoints = monster.header.maximumHitPoints
              ),
              armorClass = monster.header.armorClass,
              initiative = 1,
              conditions = Set.empty,
              otherMarkers = List.empty,
              name = s"${monster.header.name} #$i",
              initiativeBonus = monster.info.initiativeBonus
            )
          }
        }

        // Reset initiatives based on random values, set the id of the index
        val modified = entities
          .zip(initiatives.toList).zipWithIndex.map { case ((entity, initiative), index) =>
            entity match {
              case monster: MonsterCombatant =>
                monster.copy(id = CombatantId(index), initiative = initiative + monster.initiativeBonus)
            }
          }.toList
        EncounterInfo(combatants = modified)
      }

      Encounter(
        header = EncounterHeader(
          id = EncounterId.empty,
          campaignId = CampaignId(1),
          sceneId = None,
          name = s"Encounter",
          status = EncounterStatus.planned,
          orderCol = 0
        ),
        jsonInfo = encounterInfo.toJsonAST.toOption.get
      )
    }

  }

  def createEncounters(
    monsters: List[Monster]
  ): UIO[List[Encounter]] = {
    ZIO
      .foreach(1 to 10) { index =>
        createEncounter(monsters).map(encounter =>
          encounter.copy(
            header = encounter.header.copy(
              status =
                if (index == 1) EncounterStatus.active
                else if (index >= 4) EncounterStatus.archived
                else EncounterStatus.planned,
              name = s"Encounter $index",
              orderCol = index - 1
            )
          )
        )
      }.map(_.toList)
  }

  def createScenes: UIO[IndexedSeq[Scene]] = {
    ZIO.succeed(
      (1 to 5).map { index =>
        Scene(
          header =
            SceneHeader(id = SceneId.empty, campaignId = CampaignId(1), name = s"Scene #$index", orderCol = index - 1),
          jsonInfo = SceneInfo().toJsonAST.toOption.get
        )
      }
    )
  }

  override def run: ZIO[
    SRDImporter & DNDBeyondImporter & FifthEditionCharacterSheetImporter & ZIOAppArgs & Scope,
    DMScreenError,
    Unit
  ] = {
    (for {
      monsters <- createMonsters
      _ <- ZIO.foreachDiscard(monsters) { m =>
        ZIO.logInfo(m.header.toString + ": " + m.jsonInfo.toJsonPretty)
      }
      pcs <- createPcs
      _ <- ZIO.foreachDiscard(pcs) { c =>
        ZIO.logInfo(c.header.toString + ": " + c.jsonInfo.toJsonPretty)
      }
    } yield ())
      .tapErrorCause(e => ZIO.logError(e.prettyPrint))
  }

}
