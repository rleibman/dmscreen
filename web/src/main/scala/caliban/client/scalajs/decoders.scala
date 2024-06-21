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

package caliban.client.scalajs

import caliban.client.scalajs.DND5eClient.{
  Alignment as CalibanAlignment,
  Biome as CalibanBiome,
  CreatureSize as CalibanCreatureSize,
  Encounter as CalibanEncounter,
  EncounterHeader as CalibanEncounterHeader,
  Monster as CalibanMonster,
  MonsterHeader as CalibanMonsterHeader,
  MonsterSearchOrder as CalibanMonsterSearchOrder,
  MonsterSearchResults as CalibanMonsterSearchResults,
  MonsterType as CalibanMonsterType,
  OrderDirection as CalibanOrderDirection,
  Queries
}
import caliban.client.CalibanClientError.DecodingError
import caliban.client.__Value.__ObjectValue
import caliban.client.{ScalarDecoder, __Value}
import com.github.plokhotnyuk.jsoniter_scala.core.JsonWriter
import zio.json.ast.Json
import zio.json.*
import com.github.plokhotnyuk.jsoniter_scala
import com.github.plokhotnyuk.jsoniter_scala.core.*
import dmscreen.dnd5e.*

given ScalarDecoder[Json] = {
  case input: __ObjectValue =>
    writeToString(input).fromJson[Json].left.map(DecodingError(_))
  case _ => Left(DecodingError("Expected an object"))
}

given monsterTypeConv: Conversion[Option[MonsterType], Option[CalibanMonsterType]] =
  monsterTypeOpt =>
    monsterTypeOpt.flatMap(monsterType => CalibanMonsterType.values.find(_.toString == monsterType.toString))

given biomeConv: Conversion[Option[Biome], Option[CalibanBiome]] =
  biomeOpt => biomeOpt.flatMap(biome => CalibanBiome.values.find(_.toString == biome.toString))

given alignmentConv: Conversion[Option[Alignment], Option[CalibanAlignment]] =
  alignmentOpt => alignmentOpt.flatMap(alignment => CalibanAlignment.values.find(_.toString == alignment.toString))

given creatureSizeConv: Conversion[Option[CreatureSize], Option[CalibanCreatureSize]] =
  creatureSizeOpt =>
    creatureSizeOpt.flatMap(creatureSize => CalibanCreatureSize.values.find(_.toString == creatureSize.toString))

given Conversion[MonsterSearchOrder, CalibanMonsterSearchOrder] =
  order =>
    CalibanMonsterSearchOrder.values
      .find(_.toString == order.toString).getOrElse(CalibanMonsterSearchOrder.name)

given Conversion[OrderDirection, CalibanOrderDirection] =
  dir =>
    CalibanOrderDirection.values
      .find(_.toString == dir.toString).getOrElse(CalibanOrderDirection.asc)
