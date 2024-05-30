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

import dmscreen.dnd5e.*
import dmscreen.dnd5e.dndbeyond.DNDBeyondImporter
import dmscreen.dnd5e.given
import dmscreen.*
import zio.*
import zio.json.*
import zio.prelude.NonEmptyList

object TestCreator extends ZIOApp {

  override type Environment = DNDBeyondImporter
  override def environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]
  override def bootstrap: ZLayer[ZIOAppArgs, Any, DNDBeyondImporter] = {
    ZLayer.make[Environment](DNDBeyondImporter.live)
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
      classes = NonEmptyList(
        PlayerCharacterClass(
          characterClass = CharacterClassId.ranger,
          subclass = Some(Subclass("Swarmkeeper")),
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
      hitPoints = HitPoints(currentHitPoints = Right(30), 30),
      armorClass = 16
    ).toJsonAST.toOption.get
  )

  val createCharacters: ZIO[DNDBeyondImporter, DMScreenError, List[PlayerCharacter]] =
    for {
      dndBeyondImporter <- ZIO.service[DNDBeyondImporter]
      wryax <- dndBeyondImporter.importPlayerCharacter(getClass.getResource("/wryrax-dndbeyond.json").nn.toURI.nn)
      nut   <- dndBeyondImporter.importPlayerCharacter(getClass.getResource("/nut-dndbeyond.json").nn.toURI.nn)
    } yield List(`Chr'zyzx`, wryax, nut)

  override def run: ZIO[DNDBeyondImporter & ZIOAppArgs & Scope, DMScreenError, Unit] = {
    for {
      all <- createCharacters
      _ <- ZIO.foreach(all) { c =>
        ZIO.logInfo(c.header.toString + ": " + c.jsonInfo.toJsonPretty)
      }
    } yield ()
  }

}
