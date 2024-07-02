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

import dmscreen.CampaignId
import zio.json.*
import zio.json.ast.Json

import java.net.URI

//Field encoders
given JsonFieldEncoder[CharacterClassId] = JsonFieldEncoder[String].contramap(_.toString)
given JsonFieldDecoder[CharacterClassId] =
  JsonFieldDecoder[String].mapOrFail(s =>
    CharacterClassId.values.find(a => s.equalsIgnoreCase(a.toString)).toRight(s"$s is not a valid CharacterClassId")
  )

//Other
given JsonCodec[URI] = JsonCodec.string.transform(new URI(_), _.toString)

//Ids
given JsonCodec[CharacterClassId] =
  JsonCodec.string.transformOrFail(
    s =>
      CharacterClassId.values.find(a => s.equalsIgnoreCase(a.toString)).toRight(s"$s is not a valid CharacterClassId"),
    _.toString
  )
given JsonCodec[SourceId] = JsonCodec.string.transform(SourceId.apply, _.value)
given JsonCodec[CombatantId] = JsonCodec.long.transform(CombatantId.apply, _.value)
given JsonCodec[CampaignId] = JsonCodec.long.transform(CampaignId.apply, _.value)
given JsonCodec[EncounterId] = JsonCodec.long.transform(EncounterId.apply, _.value)
given JsonCodec[NonPlayerCharacterId] = JsonCodec.long.transform(NonPlayerCharacterId.apply, _.value)
given JsonCodec[PlayerCharacterId] = JsonCodec.long.transform(PlayerCharacterId.apply, _.value)
given JsonCodec[MonsterId] = JsonCodec.long.transform(MonsterId.apply, _.value)
given JsonCodec[SpellId] = JsonCodec.long.transform(SpellId.apply, _.value)
given JsonCodec[SceneId] = JsonCodec.long.transform(SceneId.apply, _.value)

//Enums
given JsonCodec[ChallengeRating] = JsonCodec.string.transform(ChallengeRating.valueOf, _.toString)
given JsonCodec[ProficiencyLevel] = JsonCodec.string.transform(ProficiencyLevel.valueOf, _.toString)
given JsonCodec[CreatureSize] = JsonCodec.string.transform(CreatureSize.valueOf, _.toString)
given JsonCodec[SkillType] = JsonCodec.string.transform(SkillType.valueOf, _.toString)
given JsonCodec[AdvantageDisadvantage] = JsonCodec.string.transform(AdvantageDisadvantage.valueOf, _.toString)
given JsonCodec[MonsterType] = JsonCodec.string.transform(MonsterType.valueOf, _.toString)
given JsonCodec[Biome] = JsonCodec.string.transform(Biome.valueOf, _.toString)
given JsonCodec[Alignment] = JsonCodec.string.transform(Alignment.valueOf, _.toString)
given JsonCodec[Lifestyle] = JsonCodec.string.transform(Lifestyle.valueOf, _.toString)
given JsonCodec[AbilityType] = JsonCodec.string.transform(AbilityType.valueOf, _.toString)
given JsonCodec[Condition] = JsonCodec.string.transform(Condition.valueOf, _.toString)
given JsonCodec[Sense] = JsonCodec.string.transform(Sense.valueOf, _.toString)
given JsonCodec[SpeedType] = JsonCodec.string.transform(SpeedType.valueOf, _.toString)

given JsonCodec[Source] = JsonCodec.derived[Source]
given JsonCodec[SubClass] = JsonCodec.derived[SubClass]
given JsonCodec[CharacterClass] = JsonCodec.derived[CharacterClass]
given JsonCodec[Race] = JsonCodec.derived[Race]
given JsonCodec[Background] = JsonCodec.derived[Background]
given JsonCodec[DND5eCampaignInfo] = JsonCodec.derived[DND5eCampaignInfo]
given JsonCodec[Map[CharacterClassId, SubClass]] = JsonCodec.map[CharacterClassId, SubClass]
given JsonCodec[ImportSource] = JsonCodec.derived[ImportSource]
given JsonCodec[PhysicalCharacteristics] = JsonCodec.derived[PhysicalCharacteristics]
given JsonCodec[Ability] = JsonCodec.derived[Ability]
given JsonCodec[Traits] = JsonCodec.derived[Traits]
given JsonCodec[InventoryItem] = JsonCodec.derived[InventoryItem]
given JsonCodec[Wallet] = JsonCodec.derived[Wallet]
given JsonCodec[PlayerCharacterClass] = JsonCodec.derived[PlayerCharacterClass]
given JsonCodec[Feat] = JsonCodec.derived[Feat]
given JsonCodec[DeathSave] = JsonCodec.derived[DeathSave]
given JsonCodec[SpellSlots] = JsonCodec.derived[SpellSlots]
given JsonCodec[Creature] = JsonCodec.derived[Creature]
given JsonCodec[SenseRange] = JsonCodec.derived[SenseRange]
given JsonCodec[HitPoints] = JsonCodec.derived[HitPoints]
given JsonCodec[Abilities] = JsonCodec.derived[Abilities]
given JsonCodec[Skill] = JsonCodec.derived[Skill]
given JsonCodec[Skills] = JsonCodec.derived[Skills]
given JsonCodec[Marker] = JsonCodec.derived[Marker]
given JsonCodec[Language] = JsonCodec.string.transform(Language.fromName, _.name)
given JsonCodec[Action] = JsonCodec.derived[Action]
given JsonCodec[SpellHeader] = JsonCodec.derived[SpellHeader]
given JsonCodec[Speed] = JsonCodec.derived[Speed]
given JsonCodec[PlayerCharacterInfo] = JsonCodec.derived[PlayerCharacterInfo]

given JsonCodec[NonPlayerCharacterInfo] = JsonCodec.derived[NonPlayerCharacterInfo]

given JsonCodec[MonsterInfo] = JsonCodec.derived[MonsterInfo]

given JsonCodec[SpellInfo] = JsonCodec.derived[SpellInfo]

given JsonCodec[PlayerCharacterHeader] = JsonCodec.derived[PlayerCharacterHeader]
given JsonCodec[MonsterHeader] = JsonCodec.derived[MonsterHeader]
given JsonCodec[SceneHeader] = JsonCodec.derived[SceneHeader]
given JsonCodec[SceneInfo] = JsonCodec.derived[SceneInfo]
given JsonCodec[EncounterCombatant] = JsonCodec.derived[EncounterCombatant]
given JsonCodec[MonsterCombatant] = JsonCodec.derived[MonsterCombatant]
given JsonCodec[PlayerCharacterCombatant] = JsonCodec.derived[PlayerCharacterCombatant]
given JsonCodec[EncounterDifficulty] = JsonCodec.derived[EncounterDifficulty]
given JsonCodec[EncounterInfo] = JsonCodec.derived[EncounterInfo]

given JsonDecoder[DeathSave | Int] = JsonDecoder[Json].mapOrFail(json => json.as[Int].orElse(json.as[DeathSave]))

given JsonEncoder[DeathSave | Int] =
  JsonEncoder[Json].contramap {
    case i:  Int       => i.toJsonAST.toOption.get
    case ds: DeathSave => ds.toJsonAST.toOption.get
  }
