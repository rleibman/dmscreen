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

import caliban.ScalaJSClientAdapter.asyncCalibanCall
import caliban.client.ArgEncoder
import caliban.client.scalajs.DND5eClient.{
  CampaignHeaderInput,
  EncounterHeaderInput,
  GameSystem as CalibanGameSystem,
  MonsterHeaderInput,
  Mutations,
  NonPlayerCharacterHeaderInput,
  PlayerCharacterHeaderInput,
  SceneHeaderInput
}
import caliban.client.scalajs.{DND5eClient, given}
import dmscreen.*
import japgolly.scalajs.react.callback.AsyncCallback
import zio.*
import zio.json.ast.Json

import java.util.ResourceBundle
import scala.reflect.ClassTag
import zio.json.*

object GraphQLRepository {

  val live: DND5eRepository[AsyncCallback] = new DND5eRepository[AsyncCallback] {

    override def campaigns: AsyncCallback[Seq[CampaignHeader]] = ???

    override def campaign(campaignId: CampaignId): AsyncCallback[Option[DND5eCampaign]] = ???

    override def scene(sceneId: SceneId): AsyncCallback[Option[Scene]] = ???

    override def applyOperations[IDType](
      entityType: EntityType,
      id:         IDType,
      operations: DMScreenEvent*
    ): AsyncCallback[Unit] = ???

    override def deleteEntity[IDType](
      entityType: EntityType,
      id:         IDType,
      softDelete: Boolean
    ): AsyncCallback[Unit] = ???

    override def playerCharacters(campaignId: CampaignId): AsyncCallback[Seq[PlayerCharacter]] = ???

    override def scenes(campaignId: CampaignId): AsyncCallback[Seq[Scene]] = ???

    override def playerCharacter(playerCharacterId: PlayerCharacterId): AsyncCallback[Option[PlayerCharacter]] = ???

    override def nonPlayerCharacters(campaignId: CampaignId): AsyncCallback[Seq[NonPlayerCharacter]] = ???

    override def encounters(campaignId: CampaignId): AsyncCallback[Seq[Encounter]] = ???

    override def bestiary(search: MonsterSearch): AsyncCallback[MonsterSearchResults] = ???

    override def sources: AsyncCallback[Seq[Source]] = ???

    override def classes: AsyncCallback[Seq[CharacterClass]] = ???

    override def races: AsyncCallback[Seq[Race]] = ???

    override def backgrounds: AsyncCallback[Seq[Background]] = ???

    override def subClasses(characterClass: CharacterClassId): AsyncCallback[Seq[SubClass]] = ???

    override def spells: AsyncCallback[Seq[Spell]] = ???

    override def upsert(
      campaignHeader: CampaignHeader,
      info:           Json
    ): AsyncCallback[CampaignId] = {
      val headerInput = CampaignHeaderInput(
        id = campaignHeader.id.value,
        dmUserId = campaignHeader.dmUserId.value,
        name = campaignHeader.name,
        gameSystem = CalibanGameSystem.values.find(_.value == campaignHeader.gameSystem.toString).get
      )

      val sb = Mutations.upsertCampaign(headerInput, info, dmscreen.BuildInfo.version)
      asyncCalibanCall(sb).map(_.fold(CampaignId.empty)(CampaignId.apply))
    }

    override def upsert(
      playerCharacterHeader: PlayerCharacterHeader,
      info:                  Json
    ): AsyncCallback[PlayerCharacterId] = {
      val headerInput = PlayerCharacterHeaderInput(
        id = playerCharacterHeader.id.value,
        campaignId = playerCharacterHeader.campaignId.value,
        name = playerCharacterHeader.name,
        playerName = playerCharacterHeader.playerName
      )

      val sb = Mutations.upsertPlayerCharacter(headerInput, info, dmscreen.BuildInfo.version)
      asyncCalibanCall(sb).map(_.fold(PlayerCharacterId.empty)(PlayerCharacterId.apply))
    }

    override def upsert(
      nonPlayerCharacterHeader: NonPlayerCharacterHeader,
      info:                     Json
    ): AsyncCallback[NonPlayerCharacterId] = {
      val headerInput = NonPlayerCharacterHeaderInput(
        id = nonPlayerCharacterHeader.id.value,
        campaignId = nonPlayerCharacterHeader.campaignId.value,
        name = nonPlayerCharacterHeader.name
      )

      val sb = Mutations.upsertNonPlayerCharacter(headerInput, info, dmscreen.BuildInfo.version)
      asyncCalibanCall(sb).map(_.fold(NonPlayerCharacterId.empty)(NonPlayerCharacterId.apply))

    }

    override def upsert(
      monsterHeader: MonsterHeader,
      info:          Json
    ): AsyncCallback[MonsterId] = {
      val headerInput = MonsterHeaderInput(
        id = monsterHeader.id.value,
        name = monsterHeader.name,
        monsterType = DND5eClient.MonsterType.values.find(_.value == monsterHeader.monsterType.toString).get,
        biome = monsterHeader.biome,
        alignment = monsterHeader.alignment,
        cr = monsterHeader.cr.value,
        xp = monsterHeader.xp,
        armorClass = monsterHeader.armorClass,
        maximumHitPoints = monsterHeader.maximumHitPoints,
        size = DND5eClient.CreatureSize.values.find(_.value == monsterHeader.size.toString).get,
        initiativeBonus = monsterHeader.initiativeBonus
      )

      val sb = Mutations.upsertMonster(headerInput, info, dmscreen.BuildInfo.version)
      asyncCalibanCall(sb).map(_.fold(MonsterId.empty)(MonsterId.apply))
    }

    override def upsert(
      spellHeader: SpellHeader,
      info:        Json
    ): AsyncCallback[SpellId] = ???

    override def upsert(
      encounterHeader: EncounterHeader,
      info:            Json
    ): AsyncCallback[EncounterId] = {
      val headerInput = EncounterHeaderInput(
        id = encounterHeader.id.value,
        campaignId = encounterHeader.campaignId.value,
        name = encounterHeader.name,
        status = encounterHeader.status.toString,
        sceneId = encounterHeader.sceneId.map(_.value),
        orderCol = encounterHeader.orderCol
      )

      val sb = Mutations.upsertEncounter(headerInput, info, dmscreen.BuildInfo.version)
      asyncCalibanCall(sb).map(_.fold(EncounterId.empty)(EncounterId.apply))

    }

    override def upsert(
      sceneHeader: SceneHeader,
      info:        Json
    ): AsyncCallback[SceneId] = {
      val headerInput = SceneHeaderInput(
        id = sceneHeader.id.value,
        campaignId = sceneHeader.campaignId.value,
        name = sceneHeader.name,
        orderCol = sceneHeader.orderCol,
        isActive = sceneHeader.isActive
      )

      val sb = Mutations.upsertScene(headerInput, info, dmscreen.BuildInfo.version)
      asyncCalibanCall(sb).map(_.fold(SceneId.empty)(SceneId.apply))

    }
  }

}
