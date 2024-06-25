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
import zio.*
import zio.test.*
import zio.json.*
import zio.json.ast.*

object StorageSpec extends ZIOSpecDefault {

  private val testCampaignId = CampaignId(1)
  private val testUser = UserId(1)

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("Testing Storage")(
      test("Should be able to store and retrieve a campaign") {
        for {
          service     <- ZIO.service[DND5eRepository]
          listAtStart <- service.campaigns
          startObject <- service.campaign(listAtStart.head.id)
          newId <- service.upsert(
            CampaignHeader(CampaignId.empty, testUser, "Test Campaign 2"),
            DND5eCampaignInfo(notes = "These are some notes").toJsonAST.getOrElse(Json.Null)
          )
          listAfterInsert <- service.campaigns
          insertedObject  <- service.campaign(newId)
          _ <- service.applyOperations(
            entityType = DND5eEntityType.campaign,
            id = newId,
            operations = Replace(JsonPath("$.notes"), Json.Str("These are some updated notes"))
          )
          listAfterUpdates <- service.campaigns
          updatedCampaign  <- service.campaign(newId)
          _                <- service.deleteEntity(entityType = DND5eEntityType.campaign, id = newId)
          listAfterDelete  <- service.campaigns
          deletedObject    <- service.campaign(newId)
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
          service         <- ZIO.service[DND5eRepository]
          startCharacters <- service.playerCharacters(testCampaignId)
          newId <- service.upsert(
            PlayerCharacterHeader(PlayerCharacterId.empty, testCampaignId, "Test Character 2", Some("Test Player 2")),
            PlayerCharacterInfo(
              hitPoints = HitPoints(
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
          objectAfterInsert <- service.playerCharacter(newId)
          listAfterInsert   <- service.playerCharacters(testCampaignId)
          _ <- service.applyOperations(
            entityType = DND5eEntityType.playerCharacter,
            id = newId,
            Replace(JsonPath("$.notes"), Json.Str("These are some updated notes")),
            Replace(JsonPath("$.armorClass"), Json.Num(17))
          )
          objectAfterUpdate <- service.playerCharacter(newId)
          _                 <- service.deleteEntity(entityType = DND5eEntityType.playerCharacter, id = newId)
          listAfterDelete   <- service.playerCharacters(testCampaignId)
          objectAfterDelete <- service.playerCharacter(newId)
        } yield assertTrue(
          startCharacters.nonEmpty,
          newId != PlayerCharacterId.empty,
          objectAfterInsert.isDefined,
          listAfterInsert.size == startCharacters.size + 1,
          objectAfterUpdate.isDefined,
          listAfterDelete.size == startCharacters.size,
          objectAfterDelete.isEmpty
        )
      },
      test("player character diff") {
        import diffson.*
        import diffson.lcs.*
        import diffson.jsonpatch.*
        import diffson.jsonpatch.lcsdiff.*
        import zio.json.*
        import zio.json.ast.Json
        import diffson.zjson.*

        val info1 = PlayerCharacterInfo(
          hitPoints = HitPoints(
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
