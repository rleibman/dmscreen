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

import dmscreen.*
import zio.*
import zio.json.ast.Json

import scala.reflect.ClassTag

trait STARepository[F[_]] extends GameRepository {

  def scene(sceneId: SceneId): F[Option[Scene]]

  def applyOperations[IDType](
    entityType: EntityType[IDType],
    id:         IDType,
    operations: DMScreenEvent*
  ): F[Unit]

  def deleteEntity[IDType](
    entityType: EntityType[IDType],
    id:         IDType,
    softDelete: Boolean = true
  ): F[Unit]

  def characters(campaignId: CampaignId): F[Seq[Character]]

  def starships(campaignId: CampaignId): F[Seq[Starship]]

  def scenes(campaignId: CampaignId): F[Seq[Scene]]

  def character(characterId: CharacterId): F[Option[Character]]

  def nonPlayerCharacters(campaignId: CampaignId): F[Seq[NonPlayerCharacter]]

  def encounters(campaignId: CampaignId): F[Seq[Encounter]]
  def upsert(
    header: CharacterHeader,
    info:   Json
  ): F[CharacterId]
  def upsert(
    header: StarshipHeader,
    info:   Json
  ): F[StarshipId]
  def upsert(
    header: NonPlayerCharacterHeader,
    info:   Json
  ): F[NonPlayerCharacterId]
  def upsert(
    header: SceneHeader,
    info:   Json
  ): F[SceneId]
  def upsert(
    header: EncounterHeader,
    info:   Json
  ): F[EncounterId]

}
