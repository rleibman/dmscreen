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
import caliban.client.scalajs.DND5eClient.{
  Alignment as CalibanAlignment,
  Biome as CalibanBiome,
  Campaign as CalibanCampaign,
  CampaignHeader as CalibanCampaignHeader,
  CampaignHeaderInput,
  CampaignStatus as CalibanCampaignStatus,
  CreatureSize as CalibanCreatureSize,
  EncounterHeaderInput,
  GameSystem as CalibanGameSystem,
  Monster as CalibanMonster,
  MonsterHeader as CalibanMonsterHeader,
  MonsterHeaderInput,
  MonsterType as CalibanMonsterType,
  Mutations,
  NonPlayerCharacterHeaderInput,
  PlayerCharacterHeaderInput,
  Queries,
  SceneHeaderInput
}
import caliban.client.scalajs.{DND5eClient, given}
import caliban.client.{ArgEncoder, SelectionBuilder}
import dmscreen.*
import japgolly.scalajs.react.callback.AsyncCallback
import zio.*
import zio.json.*
import zio.json.ast.Json

import java.util.ResourceBundle
import scala.reflect.ClassTag

object GraphQLRepository {

  val live: DND5eRepository[AsyncCallback] = new DND5eRepository[AsyncCallback] {

    override def campaigns: AsyncCallback[Seq[CampaignHeader]] = {
      val campaignSB = (
        CalibanCampaignHeader.id ~
          CalibanCampaignHeader.dmUserId ~
          CalibanCampaignHeader.name ~
          CalibanCampaignHeader.gameSystem ~
          CalibanCampaignHeader.campaignStatus
      ).map {
        case (
              id:             Long,
              dmUserId:       Long,
              name:           String,
              gameSystem:     CalibanGameSystem,
              campaignStatus: CalibanCampaignStatus
            ) =>
          CampaignHeader(
            id = CampaignId(id),
            dmUserId = UserId(dmUserId),
            name = name,
            gameSystem = GameSystem.valueOf(gameSystem.value),
            campaignStatus = CampaignStatus.valueOf(campaignStatus.value)
          )
      }

      asyncCalibanCall(Queries.campaigns(campaignSB)).map(_.toSeq.flatten)
    }

    override def campaign(campaignId: CampaignId): AsyncCallback[Option[Campaign]] = {
      val campaignSB: SelectionBuilder[CalibanCampaign, Campaign] = (CalibanCampaign.header(
        CalibanCampaignHeader.id ~ CalibanCampaignHeader.name ~ CalibanCampaignHeader.dmUserId ~ CalibanCampaignHeader.gameSystem ~ CalibanCampaignHeader.campaignStatus
      ) ~ CalibanCampaign.jsonInfo).map {
        (
          id:             Long,
          name:           String,
          dmUserId:       Long,
          system:         CalibanGameSystem,
          campaignStatus: CalibanCampaignStatus,
          info:           Json
        ) =>
          Campaign(
            CampaignHeader(
              CampaignId(id),
              UserId(dmUserId),
              name,
              GameSystem.valueOf(system.value),
              CampaignStatus.valueOf(campaignStatus.value)
            ),
            info
          )
      }

      asyncCalibanCall(Queries.campaign(campaignId.value)(campaignSB))
    }

    override def scene(sceneId: SceneId): AsyncCallback[Option[Scene]] = ???

    override def applyOperations[IDType](
      entityType: EntityType[IDType],
      id:         IDType,
      operations: DMScreenEvent*
    ): AsyncCallback[Unit] = ???

    override def deleteEntity[IDType](
      entityType: EntityType[IDType],
      id:         IDType,
      softDelete: Boolean
    ): AsyncCallback[Unit] = {

      val sb = Mutations.deleteEntity(entityType.name, id.asInstanceOf[Long], softDelete)
      asyncCalibanCall(sb).map(_.get)
    }

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
        gameSystem = CalibanGameSystem.values.find(_.value == campaignHeader.gameSystem.toString).get,
        campaignStatus = CalibanCampaignStatus.values.find(_.value == campaignHeader.campaignStatus.toString).get
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
        sourceId = monsterHeader.sourceId.value,
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

    override def monster(monsterId: MonsterId): AsyncCallback[Option[Monster]] = {
      val monsterSB: SelectionBuilder[CalibanMonster, Monster] = (CalibanMonster.header(
        CalibanMonsterHeader.id ~
          CalibanMonsterHeader.name ~
          CalibanMonsterHeader.sourceId ~
          CalibanMonsterHeader.monsterType ~
          CalibanMonsterHeader.biome ~
          CalibanMonsterHeader.alignment ~
          CalibanMonsterHeader.cr ~
          CalibanMonsterHeader.xp ~
          CalibanMonsterHeader.armorClass ~
          CalibanMonsterHeader.maximumHitPoints ~
          CalibanMonsterHeader.size ~
          CalibanMonsterHeader.initiativeBonus
      ) ~ CalibanMonster.jsonInfo).map {
        case (
              id:               Long,
              name:             String,
              sourceId:         String,
              monsterType:      CalibanMonsterType,
              biome:            Option[CalibanBiome],
              alignment:        Option[CalibanAlignment],
              cr:               Double,
              xp:               Long,
              armorClass:       Int,
              maximumHitPoints: Int,
              size:             CalibanCreatureSize,
              initiativeBonus:  Int,
              info:             Json
            ) =>
          Monster(
            MonsterHeader(
              id = MonsterId(id),
              name = name,
              sourceId = SourceId(sourceId),
              monsterType = MonsterType.valueOf(monsterType.value),
              biome = biome.map(a => Biome.valueOf(a.value)),
              alignment = alignment.map(a => Alignment.valueOf(a.value)),
              cr = ChallengeRating.fromDouble(cr).getOrElse(ChallengeRating.`0`),
              xp = xp,
              armorClass = armorClass,
              maximumHitPoints = maximumHitPoints,
              size = CreatureSize.valueOf(size.value),
              initiativeBonus = initiativeBonus
            ),
            info
          )
      }

      asyncCalibanCall(Queries.monster(monsterId.value)(monsterSB))
    }
  }

}
