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

package dmscreen

import dmscreen.dnd5e.dndbeyond.DNDBeyondImporter
import dmscreen.dnd5e.fifthEditionCharacterSheet.FifthEditionCharacterSheetImporter
import dmscreen.dnd5e.{DND5eRepository, QuillDND5eRepository, RepositoryError}
import dmscreen.util.TestCreator
import zio.*

type DMScreenServerEnvironment = DND5eRepository & DNDBeyondImporter

object EnvironmentBuilder {

  def live =
    ZLayer
      .make[DND5eRepository & ConfigurationService & DNDBeyondImporter](
        ConfigurationService.live,
        QuillDND5eRepository.db,
        DNDBeyondImporter.live
      ).orDie

  case class InitializingLayer()

  private val initializingLayer: ZLayer[
    DND5eRepository & DNDBeyondImporter & FifthEditionCharacterSheetImporter,
    DMScreenError,
    InitializingLayer
  ] =
    ZLayer.fromZIO {
      for {
        playerCharacters <- TestCreator.createCharacters
        repo             <- ZIO.service[DND5eRepository]
        _                <- ZIO.foreachDiscard(playerCharacters)(pc => repo.insert(pc.header, pc.jsonInfo))
      } yield InitializingLayer()

    }

  def withContainer = {
    ZLayer
      .make[DND5eRepository & ConfigurationService & DNDBeyondImporter & InitializingLayer](
        DMScreenContainer.containerLayer,
        ConfigurationService.live >>> DMScreenContainer.configLayer,
        QuillDND5eRepository.db,
        DNDBeyondImporter.live,
        FifthEditionCharacterSheetImporter.live,
        initializingLayer
      ).orDie
  }

}
