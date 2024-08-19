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

import caliban.ScalaJSClientAdapter
import caliban.client.Operations.{RootMutation, RootQuery, RootSubscription}
import caliban.client.scalajs.DND5eClient.{
  Alignment as CalibanAlignment,
  Background as CalibanBackground,
  Biome as CalibanBiome,
  CharacterClass as CalibanCharacterClass,
  CreatureSize as CalibanCreatureSize,
  DiceRoll as CalibanDiceRoll,
  Encounter as CalibanEncounter,
  EncounterHeader as CalibanEncounterHeader,
  EncounterHeaderInput,
  Monster as CalibanMonster,
  MonsterHeader as CalibanMonsterHeader,
  MonsterHeaderInput,
  MonsterSearchOrder as CalibanMonsterSearchOrder,
  MonsterSearchResults as CalibanMonsterSearchResults,
  MonsterType as CalibanMonsterType,
  Mutations,
  NonPlayerCharacter as CalibanNonPlayerCharacter,
  NonPlayerCharacterHeader as CalibanNonPlayerCharacterHeader,
  NonPlayerCharacterHeaderInput,
  OrderDirection as CalibanOrderDirection,
  PlayerCharacter as CalibanPlayerCharacter,
  PlayerCharacterHeader as CalibanPlayerCharacterHeader,
  PlayerCharacterHeaderInput,
  PlayerCharacterSearchInput,
  Queries,
  Race as CalibanRace,
  Scene as CalibanScene,
  SceneHeader as CalibanSceneHeader,
  SceneHeaderInput,
  SubClass as CalibanSubclass
}
import caliban.client.scalajs.{DND5eClient, given}
import caliban.client.{ArgEncoder, SelectionBuilder}
import dmscreen.*
import dmscreen.dnd5e.ImportSource
import japgolly.scalajs.react.callback.AsyncCallback
import zio.*
import zio.json.*
import zio.json.ast.Json

type QueryOrMutationSelectionBuilder[A] = SelectionBuilder[RootQuery & RootMutation, A]

object SelectionBuilderRepository {

  private val calibanClient: ScalaJSClientAdapter = caliban.ScalaJSClientAdapter("dnd5e")

  def query[A](sb: SelectionBuilder[RootQuery, A]): AsyncCallback[A] = {
    calibanClient.asyncCalibanCall(sb)
  }

  def mutation[A](sb: SelectionBuilder[RootMutation, A]): AsyncCallback[A] = {
    calibanClient.asyncCalibanCall(sb)
  }

  val live = new DND5eRepository[QueryOrMutationSelectionBuilder] {
    override def monster(monsterId: MonsterId): SelectionBuilder[RootQuery, Option[Monster]] = ???

    override def deleteEntity[IDType](
      entityType: EntityType[IDType],
      id:         IDType,
      softDelete: Boolean
    ): SelectionBuilder[RootMutation, Unit] = {
      Mutations.deleteEntity(entityType.name, id.asInstanceOf[Long], softDelete).map(_.get)
    }

    override def playerCharacters(
      campaignId: CampaignId,
      search:     PlayerCharacterSearch
    ): SelectionBuilder[RootQuery, Seq[PlayerCharacter]] = ???

    override def playerCharacter(playerCharacterId: PlayerCharacterId)
      : SelectionBuilder[RootQuery, Option[PlayerCharacter]] = ???

    override def nonPlayerCharacter(nonPlayerCharacterId: NonPlayerCharacterId)
      : SelectionBuilder[RootQuery, Option[NonPlayerCharacter]] = ???

    override def scenes(campaignId: CampaignId): SelectionBuilder[RootQuery, Seq[Scene]] = ???

    override def nonPlayerCharacters(campaignId: CampaignId): SelectionBuilder[RootQuery, Seq[NonPlayerCharacter]] = ???

    override def encounters(campaignId: CampaignId): SelectionBuilder[RootQuery, Seq[Encounter]] = ???

    override def encounter(
      campaignId:  CampaignId,
      encounterId: EncounterId
    ): SelectionBuilder[RootQuery, Option[Encounter]] = ???

    override def bestiary(search: MonsterSearch): SelectionBuilder[RootQuery, MonsterSearchResults] = ???

    override def sources: SelectionBuilder[RootQuery, Seq[Source]] = ???

    override def classes: SelectionBuilder[RootQuery, Seq[CharacterClass]] = ???

    override def races: SelectionBuilder[RootQuery, Seq[Race]] = ???

    override def backgrounds: SelectionBuilder[RootQuery, Seq[Background]] = ???

    override def subClasses(characterClass: CharacterClassId): SelectionBuilder[RootQuery, Seq[SubClass]] = ???

    override def spells: SelectionBuilder[RootQuery, Seq[Spell]] = ???

    override def upsert(
      header: PlayerCharacterHeader,
      info:   Json
    ): SelectionBuilder[RootMutation, PlayerCharacterId] = ???

    override def upsert(
      header: NonPlayerCharacterHeader,
      info:   Json
    ): SelectionBuilder[RootMutation, NonPlayerCharacterId] = ???

    override def upsert(
      header: MonsterHeader,
      info:   Json
    ): SelectionBuilder[RootMutation, MonsterId] = ???

    override def upsert(
      header: SpellHeader,
      info:   Json
    ): SelectionBuilder[RootMutation, SpellId] = ???

    override def upsert(
      header: EncounterHeader,
      info:   Json
    ): SelectionBuilder[RootMutation, EncounterId] = ???

    override def upsert(
      header: SceneHeader,
      info:   Json
    ): SelectionBuilder[RootMutation, SceneId] = ???

  }

}

object DND5eGraphQLRepository {

  trait ExtendedRepository extends DND5eRepository[AsyncCallback] {

    def importDndBeyondCharacter(
      campaignId:  CampaignId,
      dndBeyondId: DndBeyondId,
      fresh:       Boolean = false
    ): AsyncCallback[PlayerCharacter]

  }

  val live: ExtendedRepository = new ExtendedRepository {

    private val calibanClient: ScalaJSClientAdapter = caliban.ScalaJSClientAdapter("dnd5e")

    override def deleteEntity[IDType](
      entityType: EntityType[IDType],
      id:         IDType,
      softDelete: Boolean
    ): AsyncCallback[Unit] = {

      val sb = Mutations.deleteEntity(entityType.name, id.asInstanceOf[Long], softDelete)
      calibanClient.asyncCalibanCall(sb).map(_.get)
    }

    private val pcSB: SelectionBuilder[CalibanPlayerCharacter, PlayerCharacter] =
      (CalibanPlayerCharacter.header(
        CalibanPlayerCharacterHeader.campaignId ~
          CalibanPlayerCharacterHeader.id ~
          CalibanPlayerCharacterHeader.name ~
          CalibanPlayerCharacterHeader.source ~
          CalibanPlayerCharacterHeader.playerName
      ) ~ CalibanPlayerCharacter.jsonInfo).map {
        (
          campaignId: Long,
          pcId:       Long,
          name:       String,
          source:     String,
          playerName: Option[String],
          info:       Json
        ) =>
          PlayerCharacter(
            PlayerCharacterHeader(
              id = PlayerCharacterId(pcId),
              campaignId = CampaignId(campaignId),
              name = name,
              source = source.fromJson[ImportSource].toOption.getOrElse(DMScreenSource),
              playerName = playerName
            ),
            info
          )
      }

    private val npcSB: SelectionBuilder[CalibanNonPlayerCharacter, NonPlayerCharacter] =
      (CalibanNonPlayerCharacter.header(
        CalibanNonPlayerCharacterHeader.campaignId ~
          CalibanNonPlayerCharacterHeader.id ~
          CalibanNonPlayerCharacterHeader.name
      ) ~ CalibanNonPlayerCharacter.jsonInfo).map {
        (
          campaignId: Long,
          pcId:       Long,
          name:       String,
          info:       Json
        ) =>
          NonPlayerCharacter(
            NonPlayerCharacterHeader(
              id = NonPlayerCharacterId(pcId),
              campaignId = CampaignId(campaignId),
              name = name
            ),
            info
          )
      }

    override def playerCharacters(
      campaignId: CampaignId,
      search:     PlayerCharacterSearch = PlayerCharacterSearch()
    ): AsyncCallback[Seq[PlayerCharacter]] = {

      val calibanSearch = PlayerCharacterSearchInput(dndBeyondId = search.dndBeyondId.map(_.value))
      calibanClient
        .asyncCalibanCall(Queries.playerCharacters(campaignId.value, calibanSearch)(pcSB))
        .map(_.toSeq.flatten)

    }

    override def scenes(campaignId: CampaignId): AsyncCallback[Seq[Scene]] = {
      val sb: SelectionBuilder[CalibanScene, Scene] = (CalibanScene.header(
        CalibanSceneHeader.id ~ CalibanSceneHeader.campaignId ~ CalibanSceneHeader.name ~ CalibanSceneHeader.orderCol ~ CalibanSceneHeader.isActive
      ) ~ CalibanScene.jsonInfo).map {
        (
          id:         Long,
          campaignId: Long,
          name:       String,
          orderCol:   Int,
          isActive:   Boolean,
          info:       Json
        ) =>
          Scene(
            SceneHeader(
              id = SceneId(id),
              campaignId = CampaignId(campaignId),
              name = name,
              orderCol = orderCol,
              isActive = isActive
            ),
            info
          )
      }
      calibanClient.asyncCalibanCall(Queries.scenes(campaignId.value)(sb)).map(_.toSeq.flatten)

    }

    override def nonPlayerCharacters(campaignId: CampaignId): AsyncCallback[Seq[NonPlayerCharacter]] = {
      val query: SelectionBuilder[RootQuery, Option[List[NonPlayerCharacter]]] =
        Queries.nonPlayerCharacters(campaignId.value)(npcSB)
      calibanClient
        .asyncCalibanCall(query)
        .map(_.toSeq.flatten)
    }

    private val encounterSB: SelectionBuilder[CalibanEncounter, Encounter] = (CalibanEncounter.header(
      CalibanEncounterHeader.id ~
        CalibanEncounterHeader.campaignId ~
        CalibanEncounterHeader.name ~
        CalibanEncounterHeader.status ~
        CalibanEncounterHeader.sceneId ~
        CalibanEncounterHeader.orderCol
    ) ~ CalibanEncounter.jsonInfo).map {
      (
        id:         Long,
        campaignId: Long,
        name:       String,
        status:     String,
        sceneId:    Option[Long],
        orderCol:   Int,
        info:       Json
      ) =>
        Encounter(
          EncounterHeader(
            EncounterId(id),
            CampaignId(campaignId),
            name,
            EncounterStatus.valueOf(status),
            sceneId.map(SceneId.apply),
            orderCol
          ),
          info
        )
    }

    override def encounters(campaignId: CampaignId): AsyncCallback[Seq[Encounter]] = {
      calibanClient.asyncCalibanCall(Queries.encounters(campaignId.value)(encounterSB)).map(_.toSeq.flatten)
    }

    override def bestiary(monsterSearch: MonsterSearch): AsyncCallback[MonsterSearchResults] = {
      val monsterSB: SelectionBuilder[CalibanMonsterHeader, MonsterHeader] = (
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
      ).map {
        (
          id,
          name,
          sourceId,
          monsterType,
          biome,
          alignment,
          cr,
          xp,
          armorClass,
          maximumHitPoints,
          size,
          initiativeBonus
        ) =>
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
          )
      }

      val resultsSB: SelectionBuilder[CalibanMonsterSearchResults, (List[MonsterHeader], Long)] =
        CalibanMonsterSearchResults.results(monsterSB) ~ CalibanMonsterSearchResults.total

      val sb = Queries.bestiary(
        name = monsterSearch.name,
        challengeRating = monsterSearch.challengeRating.map(_.value),
        size = monsterSearch.size,
        alignment = monsterSearch.alignment,
        biome = monsterSearch.biome,
        monsterType = monsterSearch.monsterType,
        orderCol = monsterSearch.orderCol,
        orderDir = monsterSearch.orderDir,
        page = monsterSearch.page,
        pageSize = monsterSearch.pageSize
      )(resultsSB)

      calibanClient
        .asyncCalibanCall(sb).map(result => result.fold(MonsterSearchResults())(r => MonsterSearchResults(r._1, r._2)))

    }

    override def sources: AsyncCallback[Seq[Source]] = ???

    override def classes: AsyncCallback[Seq[CharacterClass]] = {
      val sb = Queries.classes(
        (CalibanCharacterClass.id ~ CalibanCharacterClass.hitDice(CalibanDiceRoll.roll)).map(
          (
            id,
            hd
          ) =>
            CharacterClass(
              CharacterClassId.values.find(_.name.equalsIgnoreCase(id)).getOrElse(CharacterClassId.unknown),
              DiceRoll(hd)
            )
        )
      )
      calibanClient.asyncCalibanCall(sb).map(_.toList.flatten)
    }

    override def races: AsyncCallback[Seq[Race]] = {
      val sb = Queries.races(CalibanRace.name.map(Race.apply))
      calibanClient.asyncCalibanCall(sb).map(_.toList.flatten)

    }

    override def backgrounds: AsyncCallback[Seq[Background]] = {
      val sb = Queries.backgrounds(CalibanBackground.name.map(Background.apply))
      calibanClient.asyncCalibanCall(sb).map(_.toList.flatten)
    }

    override def subClasses(characterClass: CharacterClassId): AsyncCallback[Seq[SubClass]] = {
      val sb = Queries.subclasses(characterClass.name)(CalibanSubclass.name.map(SubClass.apply))
      calibanClient.asyncCalibanCall(sb).map(_.toSeq.flatten)

    }

    override def spells: AsyncCallback[Seq[Spell]] = ???

    override def upsert(
      playerCharacterHeader: PlayerCharacterHeader,
      info:                  Json
    ): AsyncCallback[PlayerCharacterId] = {
      val headerInput = PlayerCharacterHeaderInput(
        id = playerCharacterHeader.id.value,
        campaignId = playerCharacterHeader.campaignId.value,
        name = playerCharacterHeader.name,
        source = playerCharacterHeader.source.toJson,
        playerName = playerCharacterHeader.playerName
      )

      val sb = Mutations.upsertPlayerCharacter(headerInput, info, dmscreen.BuildInfo.version)
      calibanClient.asyncCalibanCall(sb).map(_.fold(PlayerCharacterId.empty)(PlayerCharacterId.apply))
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
      calibanClient.asyncCalibanCall(sb).map(_.fold(NonPlayerCharacterId.empty)(NonPlayerCharacterId.apply))

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
      calibanClient.asyncCalibanCall(sb).map(_.fold(MonsterId.empty)(MonsterId.apply))
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
      calibanClient.asyncCalibanCall(sb).map(_.fold(EncounterId.empty)(EncounterId.apply))

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
      calibanClient.asyncCalibanCall(sb).map(_.fold(SceneId.empty)(SceneId.apply))

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

      calibanClient.asyncCalibanCall(Queries.monster(monsterId.value)(monsterSB))
    }

    def importDndBeyondCharacter(
      campaignId:  CampaignId,
      dndBeyondId: DndBeyondId,
      fresh:       Boolean = false
    ): AsyncCallback[PlayerCharacter] = {
      calibanClient.asyncCalibanCall(
        Mutations.importCharacterDNDBeyond(campaignId.value, dndBeyondId.value, fresh)(pcSB).map(_.get)
      )
    }

    override def encounter(
      campaignId:  CampaignId,
      encounterId: EncounterId
    ): AsyncCallback[Option[Encounter]] =
      calibanClient.asyncCalibanCall(Queries.encounter(campaignId.value, encounterId.value)(encounterSB))

    override def playerCharacter(id: PlayerCharacterId): AsyncCallback[Option[PlayerCharacter]] = {
      calibanClient.asyncCalibanCall(Queries.playerCharacter(id.value)(pcSB))
    }

    override def nonPlayerCharacter(id: NonPlayerCharacterId): AsyncCallback[Option[NonPlayerCharacter]] = {
      calibanClient.asyncCalibanCall(Queries.nonPlayerCharacter(id.value)(npcSB))
    }

  }

}
