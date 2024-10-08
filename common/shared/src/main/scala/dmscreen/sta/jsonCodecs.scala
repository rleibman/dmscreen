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
given JsonCodec[NonPlayerCharacterId] = JsonCodec.long.transform(NonPlayerCharacterId.apply, _.value)
given JsonCodec[SceneId] = JsonCodec.long.transform(SceneId.apply, _.value)

given JsonCodec[Era] = JsonCodec.string.transform(Era.valueOf, _.toString)
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

given JsonCodec[AttributeRating] =
  JsonCodec.int.transformOrFail(
    {
      case v: Int if v >= 7 && v <= 12 => Right(v.asInstanceOf[AttributeRating]) // Ugly cast
      case v: Int                      => Left("AttributeRating must be between 7 and 12, not $v")
    },
    a => a
  )

given JsonCodec[DepartmentRating] =
  JsonCodec.int.transformOrFail(
    {
      case v: Int if v >= 0 && v <= 5 => Right(v.asInstanceOf[DepartmentRating]) // Ugly cast
      case v: Int                     => Left("DepartmentRating must be between 0 and 5, not $v")
    },
    a => a
  )

given JsonCodec[Attributes] = JsonCodec.derived[Attributes]
given JsonCodec[EthicalValue] = JsonCodec.derived[EthicalValue]
given JsonCodec[Focus] = JsonCodec.derived[Focus]
given JsonCodec[Departments] = JsonCodec.derived[Departments]
given JsonCodec[LineageType] =
  JsonCodec.string
    .transform(s => LineageType.values.find(_.name.equalsIgnoreCase(s)).getOrElse(LineageType.other(s)), _.toString)
given JsonCodec[Organization] = JsonCodec.derived[Organization]
given JsonCodec[CareerEvent] = JsonCodec.derived[CareerEvent]
given JsonCodec[Lineage] = JsonCodec.derived[Lineage]
given JsonCodec[StarshipTrait] = JsonCodec.derived[StarshipTrait]
given JsonCodec[WeaponQualities] = JsonCodec.derived[WeaponQualities]
given JsonCodec[Weapon] = JsonCodec.derived[Weapon]
given JsonCodec[ShipStats] = JsonCodec.derived[ShipStats]
given JsonCodec[Determination] = JsonCodec.derived[Determination]
given JsonCodec[Stress] = JsonCodec.derived[Stress]
given JsonCodec[Talent] = JsonCodec.derived[Talent]
given JsonCodec[Trait] = JsonCodec.derived[Trait]
given JsonCodec[InventoryItem] = JsonCodec.derived[InventoryItem]
given JsonCodec[CharacterInfo] = JsonCodec.derived[CharacterInfo]
given JsonCodec[StarshipInfo] = JsonCodec.derived[StarshipInfo]
given JsonCodec[SceneInfo] = JsonCodec.derived[SceneInfo]
given JsonCodec[NonPlayerCharacterInfo] = JsonCodec.derived[NonPlayerCharacterInfo]
