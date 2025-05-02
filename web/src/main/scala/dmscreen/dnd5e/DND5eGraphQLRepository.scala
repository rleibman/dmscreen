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
import caliban.client.Operations.{RootMutation, RootQuery}
import caliban.client.scalajs.DND5eClient.{
  Alignment as CalibanAlignment,
  Background as CalibanBackground,
  Biome as CalibanBiome,
  ChallengeRating as CalibanChallengeRating,
  CharacterClass as CalibanCharacterClass,
  CreatureSize as CalibanCreatureSize,
  DiceRoll as CalibanDiceRoll,
  Encounter as CalibanEncounter,
  EncounterHeader as CalibanEncounterHeader,
  EncounterHeaderInput,
  KVLongListOfLong,
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
  RandomTable as CalibanRandomTable,
  RandomTableEntry as CalibanRandomTableEntry,
  RandomTableType as CalibanRandomTableType,
  Scene as CalibanScene,
  SceneHeader as CalibanSceneHeader,
  SceneHeaderInput,
  SubClass as CalibanSubclass
}
import caliban.client.scalajs.{DND5eClient, given}
import caliban.client.{ArgEncoder, SelectionBuilder}
import dmscreen.*
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

}

object DND5eGraphQLRepository {

  trait ExtendedRepository extends DND5eRepository[AsyncCallback] {

    def importDndBeyondCharacter(
      campaignId:  CampaignId,
      dndBeyondId: DndBeyondId,
      fresh:       Boolean = false
    ): AsyncCallback[PlayerCharacter]

    def aiGenerateEncounterDescription(encounter: Encounter):          AsyncCallback[String]
    def aiGenerateNPCDetails(npc:                 NonPlayerCharacter): AsyncCallback[NonPlayerCharacter]

  }

  val live: ExtendedRepository = new ExtendedRepository {
    override def bestiaryCount(search: MonsterSearch): AsyncCallback[Long] = ???

    private val calibanClient: ScalaJSClientAdapter = caliban.ScalaJSClientAdapter("dnd5e")

    override def deleteEntity[IDType](
      entityType: EntityType[IDType],
      id:         IDType,
      softDelete: Boolean
    ): AsyncCallback[Unit] = {

      val sb = Mutations.deleteEntity(entityType.name, id.asInstanceOf[Long], softDelete)
      calibanClient.asyncCalibanCall(sb).map(_.get)
    }

    private val randomTableSB: SelectionBuilder[CalibanRandomTable, RandomTable] = (
      CalibanRandomTable.id ~
        CalibanRandomTable.name ~
        CalibanRandomTable.tableType ~
        CalibanRandomTable.subType ~
        CalibanRandomTable.diceRoll(CalibanDiceRoll.roll) ~
        CalibanRandomTable.entries(
          CalibanRandomTableEntry.randomTableId ~
            CalibanRandomTableEntry.rangeLow ~
            CalibanRandomTableEntry.rangeHigh ~
            CalibanRandomTableEntry.name ~
            CalibanRandomTableEntry.description
        )
    ).map {
      (
        id,
        name,
        tableType,
        subType,
        diceRoll,
        entries
      ) =>
        RandomTable(
          id = RandomTableId(id),
          name = name,
          tableType = RandomTableType.valueOf(tableType.value),
          subType = subType,
          entries = entries.map { case (itemId, rangeLow, rangeHigh, name, description) =>
            RandomTableEntry(
              RandomTableId(itemId),
              rangeLow,
              rangeHigh,
              name,
              description
            )
          },
          diceRoll = DiceRoll(diceRoll)
        )
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

    override def scene(sceneId: SceneId): AsyncCallback[Option[Scene]] = ???

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

    override def fullBestiary(monsterSearch: MonsterSearch): AsyncCallback[FullMonsterSearchResults] = {
      ???
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
            cr = ChallengeRating.valueOf(cr.value),
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
        challengeRating = monsterSearch.challengeRating,
        size = monsterSearch.size,
        alignment = alignmentConv(monsterSearch.alignment),
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
        cr = monsterHeader.cr,
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
              cr:               CalibanChallengeRating,
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
              cr = ChallengeRating.valueOf(cr.value),
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

    override def randomTables(tableTypeOpt: Option[RandomTableType]): AsyncCallback[Seq[RandomTable]] = {
      val par: Option[CalibanRandomTableType] =
        tableTypeOpt.flatMap(t => CalibanRandomTableType.values.find(_.value == t.toString))
      calibanClient
        .asyncCalibanCall(
          Queries.randomTables(par)(randomTableSB)
        ).map(
          _.toList.flatten
        )
    }

    override def randomTable(id: RandomTableId): AsyncCallback[Option[RandomTable]] =
      calibanClient.asyncCalibanCall(Queries.randomTable(id.value)(randomTableSB))

    private def htmlify(str: String) = {
      str
        .replaceAll("\n\n", "\n")
        .split("\n")
        .map(s => s"<p>$s</p>\n")
        .mkString
    }

    override def snapshot(
      oldId: CampaignId,
      newId: CampaignId
    ): AsyncCallback[Unit] = ??? // Don't implement, not necessary

    override def npcsForScene(campaignId: CampaignId): AsyncCallback[Map[SceneId, Seq[NonPlayerCharacterId]]] = {
      calibanClient.asyncCalibanCall(
        Queries
          .npcsForScene(campaignId.value)(KVLongListOfLong.view.map { a =>
            (SceneId(a._1), a._2.map(NonPlayerCharacterId.apply))
          })
          .map(a => a.toSeq.flatten.toMap)
      )
    }

    override def addNpcToScene(
      sceneId: SceneId,
      npcId:   NonPlayerCharacterId
    ): AsyncCallback[Unit] =
      calibanClient
        .asyncCalibanCall(
          Mutations.addNpcToScene(sceneId.value, npcId.value)
        ).map(_ => ())

    override def removeNpcFromScene(
      sceneId: SceneId,
      npcId:   NonPlayerCharacterId
    ): AsyncCallback[Unit] =
      calibanClient
        .asyncCalibanCall(
          Mutations.removeNpcFromScene(sceneId.value, npcId.value)
        ).map(_ => ())

    override def aiGenerateEncounterDescription(encounter: Encounter): AsyncCallback[String] = {
      val calibanEncounterHeader = EncounterHeaderInput(
        id = encounter.header.id.value,
        campaignId = encounter.header.campaignId.value,
        name = encounter.header.name,
        status = encounter.header.status.toString,
        sceneId = encounter.header.sceneId.map(_.value),
        orderCol = encounter.header.orderCol
      )

      calibanClient.asyncCalibanCall(
        Queries
          .aiGenerateEncounterDescription(calibanEncounterHeader, encounter.jsonInfo, dmscreen.BuildInfo.version).map(
            _.fold("")(htmlify)
          )
      )
    }

//    private val deathSaveSB: SelectionBuilder[CalibanDeathSave, DeathSave] = CalibanDeathSave.view.map(caliban =>
//      DeathSave(fails = caliban.fails, successes = caliban.successes, isStabilized = caliban.isStabilized)
//    )

//    private val healthSB: SelectionBuilder[CalibanHealth, Health] = CalibanHealth.view(deathSaveSB).map { caliban =>
//      Health(
//        deathSave = caliban.deathSave,
//        currentHitPoints = caliban.currentHitPoints,
//        maxHitPoints = caliban.maxHitPoints,
//        overrideMaxHitPoints = caliban.overrideMaxHitPoints,
//        temporaryHitPoints = caliban.temporaryHitPoints
//      )
//    }

//    private val subclassSB = DND5eClient.SubClass.view.map(caliban => SubClass(caliban.name))
//    private val classesSB: SelectionBuilder[DND5eClient.PlayerCharacterClass, PlayerCharacterClass] =
//      DND5eClient.PlayerCharacterClass.view(subclassSB).map { caliban =>
//        PlayerCharacterClass(
//          characterClass =
//            CharacterClassId.values.find(_.toString == caliban.characterClass).getOrElse(CharacterClassId.unknown),
//          level = caliban.level,
//          subclass = caliban.subclass
//        )
//      }

//    private val physicalCharacteristicsSB =
//      DND5eClient.PhysicalCharacteristics.view.map(caliban =>
//        PhysicalCharacteristics(
//          hair = caliban.hair,
//          skin = caliban.skin,
//          eyes = caliban.eyes,
//          height = caliban.height,
//          weight = caliban.weight,
//          age = caliban.age
//        )
//      )

//    private val abilitySB = DND5eClient.Ability.view.map(caliban =>
//      Ability(
//        abilityType = AbilityType.valueOf(caliban.abilityType.value),
//        value = caliban.value,
//        overrideValue = caliban.overrideValue,
//        isProficient = caliban.isProficient
//      )
//    )

//    private val abilitiesSB = DND5eClient.Abilities
//      .view(
//        abilitySB,
//        abilitySB,
//        abilitySB,
//        abilitySB,
//        abilitySB,
//        abilitySB
//      ).map(caliban =>
//        Abilities(
//          caliban.strength,
//          caliban.dexterity,
//          caliban.constitution,
//          caliban.intelligence,
//          caliban.wisdom,
//          caliban.charisma
//        )
//      )

//    private val skillSB = DND5eClient.Skill.view.map(caliban =>
//      Skill(
//        skillType = SkillType.valueOf(caliban.skillType.value),
//        proficiencyLevel = ProficiencyLevel.valueOf(caliban.proficiencyLevel.value),
//        advantage = AdvantageDisadvantage.valueOf(caliban.advantage.value)
//      )
//    )

//    private val skillsSB = DND5eClient.Skills
//      .view(
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB,
//        skillSB
//      ).map(caliban =>
//        Skills(
//          caliban.acrobatics,
//          caliban.animalHandling,
//          caliban.arcana,
//          caliban.athletics,
//          caliban.deception,
//          caliban.history,
//          caliban.insight,
//          caliban.intimidation,
//          caliban.investigation,
//          caliban.medicine,
//          caliban.nature,
//          caliban.perception,
//          caliban.performance,
//          caliban.persuasion,
//          caliban.religion,
//          caliban.sleightOfHand,
//          caliban.stealth,
//          caliban.survival
//        )
//      )

    private val backgroundSB =
      DND5eClient.Background.view.map(caliban => Background(caliban.name))

    private val raceSB = DND5eClient.Race.view.map(caliban => Race(caliban.name))

//    private val traitsSB = DND5eClient.Traits.view.map(caliban =>
//      Traits(
//        personalityTraits = caliban.personalityTraits,
//        ideals = caliban.ideals,
//        bonds = caliban.bonds,
//        flaws = caliban.flaws,
//        appearance = caliban.appearance
//      )
//    )

//    private val inventorySB = DND5eClient.InventoryItem.view.map(caliban =>
//      InventoryItem(
//        name = caliban.name,
//        quantity = caliban.quantity
//      )
//    )

//    private val walletSB = DND5eClient.Wallet.view.map(caliban =>
//      Wallet(
//        cp = caliban.cp,
//        sp = caliban.sp,
//        ep = caliban.ep,
//        gp = caliban.gp,
//        pp = caliban.pp
//      )
//    )

//    private val featSB = DND5eClient.Feat.view.map(caliban =>
//      Feat(
//        name = caliban.name
//      )
//    )

//    private val spellSlotsSB, pactMagicSB = DND5eClient.SpellSlots.view.map(caliban =>
//      SpellSlots(level = caliban.level, used = caliban.used, total = caliban.total)
//    )

//    private val languagesSB = DND5eClient.Language.view.map(caliban => Language(caliban.name))

//    private val damageTypeSB: SelectionBuilder[DND5eClient.DamageType, DamageType] =
//      DND5eClient.DamageType.view.map(caliban => DamageType.valueOf(caliban.description))

//    private val dcSB = DND5eClient.ActionDC.view.map(caliban =>
//      ActionDC(
//        dcType = AbilityType.valueOf(caliban.dcType.value),
//        dcValue = caliban.dcValue
//      )
//    )

    private val diceRollSB: SelectionBuilder[CalibanDiceRoll, DiceRoll] =
      DND5eClient.DiceRoll.view.map(caliban =>
        DiceRoll(
          caliban.roll
        )
      )

//    private val singleActionSB: SelectionBuilder[DND5eClient.SingleAction, SingleAction] =
//      DND5eClient.SingleAction
//        .view(diceRollSB, dcSB).map(caliban =>
//          SingleAction(
//            actionType = ActionType.valueOf(caliban.actionType.value),
//            name = caliban.name,
//            description = caliban.description,
//            attackBonus = caliban.attackBonus,
//            damage = caliban.damage,
//            dc = caliban.dc
//          )
//        )

//    private val multiActionSB: SelectionBuilder[DND5eClient.MultiAction, MultiAction] = DND5eClient.MultiAction
//      .view(singleActionSB).map(caliban =>
//        MultiAction(
//          name = caliban.name,
//          description = caliban.description,
//          actions = caliban.actions
//        )
//      )

//    private val spellHeaderSB =
//      DND5eClient.SpellHeader.view.map(caliban => SpellHeader(id = SpellId(caliban.id), name = caliban.name))

//    private val creaturesSB = DND5eClient.Creature.view.map(caliban =>
//      Creature(
//        name = caliban.name,
//        creatureType = MonsterId(caliban.creatureType)
//      )
//    )

//    private val speedsSB = DND5eClient.Speed.view.map(caliban =>
//      Speed(
//        speedType = SpeedType.valueOf(caliban.speedType.value),
//        value = caliban.value
//      )
//    )

//    private val sensesSB = DND5eClient.SenseRange.view.map(caliban =>
//      SenseRange(
//        sense = Sense.valueOf(caliban.sense.value),
//        range = caliban.range
//      )
//    )

//    private val damageVulnerabilitiesSB =
//      DND5eClient.DamageType.view.map(caliban => DamageType.valueOf(caliban.description))

//    private val rollplayInfoSB = DND5eClient.RollplayInfo.view.map(caliban =>
//      RollplayInfo(
//        occupation = caliban.occupation,
//        personality = caliban.personality,
//        ideal = caliban.ideal,
//        bond = caliban.bond,
//        flaw = caliban.flaw,
//        characteristic = caliban.characteristic,
//        speech = caliban.speech,
//        hobby = caliban.hobby,
//        fear = caliban.fear,
//        currently = caliban.currently,
//        nickname = caliban.nickname,
//        weapon = caliban.weapon,
//        rumor = caliban.rumor,
//        raisedBy = caliban.raisedBy,
//        parent1 = caliban.parent1,
//        parent2 = caliban.parent2,
//        siblingCount = caliban.siblingCount,
//        childhood = caliban.childhood,
//        children = caliban.children,
//        spouse = caliban.spouse
//      )
//    )

//    private val npcInfoSB = DND5eClient.NonPlayerCharacterInfo
//      .view(
//        NonPlayerCharacterInfoViewSelectionArgs(
//          healthSelection = healthSB,
//          classesSelection = classesSB,
//          physicalCharacteristicsSelection = physicalCharacteristicsSB,
//          abilitiesSelection = abilitiesSB,
//          skillsSelection = skillsSB,
//          backgroundSelection = backgroundSB,
//          raceSelection = raceSB,
//          traitsSelection = traitsSB,
//          inventorySelection = inventorySB,
//          walletSelection = walletSB,
//          featsSelection = featSB,
//          spellSlotsSelection = spellSlotsSB,
//          pactMagicSelection = pactMagicSB,
//          languagesSelection = languagesSB,
//          actionsSelectionOnMultiAction = multiActionSB,
//          actionsSelectionOnSingleAction = singleActionSB,
//          classSpellsSelection = spellHeaderSB,
//          creaturesSelection = creaturesSB,
//          speedsSelection = speedsSB,
//          sensesSelection = sensesSB,
//          damageVulnerabilitiesSelection = damageTypeSB,
//          damageResistancesSelection = damageTypeSB,
//          damageImmunitiesSelection = damageTypeSB,
//          rollplayInfoSelection = rollplayInfoSB
//        )
//      ).map(caliban =>
//        NonPlayerCharacterInfo(
//          health = caliban.health,
//          armorClass = caliban.armorClass,
//          classes = caliban.classes,
//          physicalCharacteristics = caliban.physicalCharacteristics,
//          faith = caliban.faith,
//          overrideInitiative = caliban.overrideInitiative,
//          currentXp = caliban.currentXp,
//          alignment = Alignment.valueOf(caliban.alignment.value),
//          lifestyle = Lifestyle.valueOf(caliban.lifestyle.value),
//          abilities = caliban.abilities,
//          skills = caliban.skills,
//          background = caliban.background,
//          race = caliban.race,
//          size = CreatureSize.valueOf(caliban.size.value),
//          traits = caliban.traits,
//          inventory = caliban.inventory,
//          wallet = caliban.wallet,
//          feats = caliban.feats,
//          conditions = caliban.conditions.map(c => Condition.valueOf(c.value)).toSet,
//          spellSlots = caliban.spellSlots,
//          pactMagic = caliban.pactMagic,
//          languages = caliban.languages.toSet,
//          actions = caliban.actions,
//          classSpells = caliban.classSpells,
//          creatures = caliban.creatures,
//          speeds = caliban.speeds,
//          senses = caliban.senses,
//          hair = caliban.hair,
//          skin = caliban.skin,
//          eyes = caliban.eyes,
//          height = caliban.height,
//          weight = caliban.weight,
//          age = caliban.age,
//          gender = caliban.gender,
//          conditionImmunities = caliban.conditionImmunities.map(c => Condition.valueOf(c.value)),
//          damageVulnerabilities = caliban.damageVulnerabilities,
//          damageResistances = caliban.damageResistances,
//          damageImmunities = caliban.damageImmunities,
//          notes = caliban.notes,
//          rollplayInfo = caliban.rollplayInfo,
//          monster = caliban.monster.map(MonsterId(_)),
//          challengeRating = caliban.challengeRating.map(cr => ChallengeRating.valueOf(cr.value)),
//          relationToPlayers = RelationToPlayers.valueOf(caliban.relationToPlayers.value),
//          organizations = caliban.organizations,
//          allies = caliban.allies,
//          enemies = caliban.enemies,
//          backstory = caliban.backstory
//        )
//      )

    override def aiGenerateNPCDetails(npc: NonPlayerCharacter): AsyncCallback[NonPlayerCharacter] = {
      val calibanNPCHeader = NonPlayerCharacterHeaderInput(
        id = npc.header.id.value,
        campaignId = npc.header.campaignId.value,
        name = npc.header.name
      )

      calibanClient
        .asyncCalibanCall(
          Queries
            .aiGenerateNPCDetails(calibanNPCHeader, npc.jsonInfo, dmscreen.BuildInfo.version)(npcSB)
        ).map(_.getOrElse(npc))
    }

  }

}
