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

package dmscreen.sta

import zio.json.JsonCodec

given JsonCodec[CharacterId] = JsonCodec.long.transform(CharacterId.apply, _.value)
given JsonCodec[StarshipId] = JsonCodec.long.transform(StarshipId.apply, _.value)
given JsonCodec[EncounterId] = JsonCodec.long.transform(EncounterId.apply, _.value)
given JsonCodec[NonPlayerCharacterId] = JsonCodec.long.transform(NonPlayerCharacterId.apply, _.value)
given JsonCodec[SceneId] = JsonCodec.long.transform(SceneId.apply, _.value)

given JsonCodec[Era] = JsonCodec.string.transform(Era.valueOf, _.toString)
given JsonCodec[CharacterType] = JsonCodec.string.transform(CharacterType.valueOf, _.toString)
given JsonCodec[Rank] = JsonCodec.string.transform(Rank.valueOf, _.toString)
given JsonCodec[Role] = JsonCodec.string.transform(Role.valueOf, _.toString)
given JsonCodec[MissionPod] = JsonCodec.string.transform(MissionPod.valueOf, _.toString)
given JsonCodec[MissionProfile] = JsonCodec.string.transform(MissionProfile.valueOf, _.toString)
given JsonCodec[WeaponRange] = JsonCodec.string.transform(WeaponRange.valueOf, _.toString)
given JsonCodec[EnergyType] = JsonCodec.string.transform(EnergyType.valueOf, _.toString)
given JsonCodec[TorpedoLoadType] = JsonCodec.string.transform(TorpedoLoadType.valueOf, _.toString)
given JsonCodec[CaptureType] = JsonCodec.string.transform(CaptureType.valueOf, _.toString)
given JsonCodec[ShipClass] = JsonCodec.string.transform(ShipClass.valueOf, _.toString)

given JsonCodec[STACampaignInfo] = JsonCodec.derived[STACampaignInfo]
given JsonCodec[Attributes] = JsonCodec.derived[Attributes]
given JsonCodec[EthicalValue] = JsonCodec.derived[EthicalValue]
given JsonCodec[Focus] = JsonCodec.derived[Focus]
given JsonCodec[Skills] = JsonCodec.derived[Skills]
given JsonCodec[Lineage] = JsonCodec.derived[Lineage]
given JsonCodec[StarshipTrait] = JsonCodec.derived[StarshipTrait]
given JsonCodec[WeaponQualities] = JsonCodec.derived[WeaponQualities]
given JsonCodec[Weapon] = JsonCodec.derived[Weapon]
given JsonCodec[ShipStats] = JsonCodec.derived[ShipStats]
given JsonCodec[CharacterInfo] = JsonCodec.derived[CharacterInfo]
given JsonCodec[StarshipInfo] = JsonCodec.derived[StarshipInfo]
given JsonCodec[EncounterInfo] = JsonCodec.derived[EncounterInfo]
given JsonCodec[SceneInfo] = JsonCodec.derived[SceneInfo]
given JsonCodec[NonPlayerCharacterInfo] = JsonCodec.derived[NonPlayerCharacterInfo]
