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

import dmscreen.{*, given}
import dmscreen.GameSystem.dnd5e
import zio.*
import zio.json.*
import zio.json.ast.*
import zio.test.*

object StorageSpec extends ZIOSpecDefault {

  private val testCampaignId = CampaignId(1)
  private val testUser = UserId(1)

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("Testing Storage")(
      test("Should be able to store and retrieve a campaign") {
        for {
          dnd5eRepo    <- ZIO.service[DND5eZIORepository]
          dmScreenRepo <- ZIO.service[DMScreenZIORepository]
          listAtStart  <- dmScreenRepo.campaigns
          startObject  <- dmScreenRepo.campaign(listAtStart.head.id)
          newId <- dmScreenRepo.upsert(
            CampaignHeader(
              CampaignId.empty,
              testUser,
              "Test Campaign 2",
              gameSystem = dnd5e,
              campaignStatus = CampaignStatus.active
            ),
            CampaignInfo(notes = "These are some notes").toJsonAST.getOrElse(Json.Null)
          )
          listAfterInsert <- dmScreenRepo.campaigns
          insertedObject  <- dmScreenRepo.campaign(newId)
          //          _ <- service.applyOperations(
          //            entityType = CampaignEntityType,
          //            id = newId,
          //            operations = Replace(JsonPath("$.notes"), Json.Str("These are some updated notes"))
          //          )
          listAfterUpdates <- dmScreenRepo.campaigns
          updatedCampaign  <- dmScreenRepo.campaign(newId)
          _                <- dmScreenRepo.deleteEntity(entityType = CampaignEntityType, id = newId)
          listAfterDelete  <- dmScreenRepo.campaigns
          deletedObject    <- dmScreenRepo.campaign(newId)
        } yield {
          assertTrue(
            listAtStart.nonEmpty,
            startObject.isDefined,
            //            info.notes.nonEmpty,
            newId.value > 0,
            listAfterInsert.length == listAtStart.length + 1,
            insertedObject.isDefined,
            listAfterUpdates.length == listAfterInsert.length,
            //            updatedInfo.notes.contains("These are some updated notes"),
            listAfterDelete.length == listAfterUpdates.length - 1,
            deletedObject.isEmpty
          )
        }
      },
      test("characters") {
        for {
          service         <- ZIO.service[DND5eZIORepository]
          startCharacters <- service.playerCharacters(testCampaignId)
          newId <- service.upsert(
            PlayerCharacterHeader(
              id = PlayerCharacterId.empty,
              campaignId = testCampaignId,
              name = "Test Character 2",
              source = DMScreenSource,
              playerName = Some("Test Player 2")
            ),
            PlayerCharacterInfo(
              health = Health(
                deathSave = DeathSave.empty,
                currentHitPoints = 30,
                maxHitPoints = 30
              ),
              armorClass = 16,
              classes = List(
                PlayerCharacterClass(
                  characterClass = CharacterClassId.paladin,
                  subclass = Option(SubClass("Oath of Vengance")),
                  level = 3
                )
              )
            ).toJsonAST.getOrElse(Json.Null)
          )
          listAfterInsert <- service.playerCharacters(testCampaignId)
//          _ <- service.applyOperations(
//            entityType = DND5eEntityType.playerCharacter,
//            id = newId,
//            Replace(JsonPath("$.notes"), Json.Str("These are some updated notes")),
//            Replace(JsonPath("$.armorClass"), Json.Num(17))
//          )
          _               <- service.deleteEntity(entityType = DND5eEntityType.playerCharacter, id = newId)
          listAfterDelete <- service.playerCharacters(testCampaignId)
        } yield assertTrue(
          startCharacters.nonEmpty,
          newId != PlayerCharacterId.empty,
          listAfterInsert.size == startCharacters.size + 1,
          listAfterDelete.size == startCharacters.size
        )
      },
      test("player character diff") {
        import diffson.*
        import diffson.jsonpatch.*
        import diffson.jsonpatch.lcsdiff.*
        import diffson.lcs.*
        import diffson.zjson.*
        import zio.json.*
        import zio.json.ast.Json

        val info1 = PlayerCharacterInfo(
          health = Health(
            deathSave = DeathSave.empty,
            currentHitPoints = 30,
            maxHitPoints = 30
          ),
          armorClass = 16,
          classes = List(
            PlayerCharacterClass(
              characterClass = CharacterClassId.paladin,
              subclass = Option(SubClass("Oath of Vengance")),
              level = 3
            )
          )
        )

        given Patience[Json] = new Patience[Json]

        val info2 = info1.copy(armorClass = 17)

        val res: Either[String, JsonPatch[Json]] = for {
          one <- info1.toJsonAST
          two <- info2.toJsonAST
        } yield diff(one, two)

        assertTrue(
          res.isRight,
          res.toOption.get.ops.size == 1,
          res.toOption.get.ops.head.toJson == """{"op":"replace","path":"/armorClass","value":17}"""
        )
      }
    ).provideLayerShared(EnvironmentBuilder.withContainer)

  }

}
