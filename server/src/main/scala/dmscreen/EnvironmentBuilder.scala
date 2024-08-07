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

import dmscreen.dnd5e.DND5eZIORepository
import dmscreen.dnd5e.dndbeyond.DNDBeyondImporter
import dmscreen.dnd5e.fifthEditionCharacterSheet.FifthEditionCharacterSheetImporter
import dmscreen.dnd5e.srd.SRDImporter
import dmscreen.sta.STAZIORepository
import dmscreen.util.TestCreator
import zio.*

type DMScreenTask[+A] = ZIO[Any, DMScreenError, A] // Succeed with an `A`, may fail with `Throwable`, no requirements.

type DMScreenServerEnvironment = DMScreenZIORepository & STAZIORepository & DND5eZIORepository & DNDBeyondImporter &
  ConfigurationService

object EnvironmentBuilder {

  def live =
    ZLayer
      .make[DMScreenZIORepository & STAZIORepository & DND5eZIORepository & ConfigurationService & DNDBeyondImporter](
        ConfigurationService.live,
        dmscreen.QuillRepository.db,
        dmscreen.dnd5e.QuillRepository.db,
        dmscreen.sta.QuillRepository.db,
        DNDBeyondImporter.live
      ).orDie

  case class InitializingLayer()

  private val containerInitializingLayer: ZLayer[
    DMScreenZIORepository & STAZIORepository & DND5eZIORepository & SRDImporter & DNDBeyondImporter &
      FifthEditionCharacterSheetImporter,
    DMScreenError,
    InitializingLayer
  ] =
    ZLayer.fromZIO {
      for {
        pcs          <- TestCreator.createPcs
        dmscreenRepo <- ZIO.service[DMScreenZIORepository]
        dnd5eRepo    <- ZIO.service[DND5eZIORepository]
        pcsWithIds <- ZIO.foreach(pcs)(pc =>
          dnd5eRepo.upsert(pc.header, pc.jsonInfo).map(id => pc.copy(pc.header.copy(id = id)))
        )
        monsters <- TestCreator.createMonsters
        monstersWithIds <- ZIO.foreach(monsters)(monster =>
          dnd5eRepo.upsert(monster.header, monster.jsonInfo).map(id => monster.copy(monster.header.copy(id = id)))
        )
        encounters <- TestCreator.createEncounters(monstersWithIds)
        encountersWithIds <- ZIO.foreach(encounters)(encounter =>
          dnd5eRepo
            .upsert(encounter.header, encounter.jsonInfo).map(id => encounter.copy(encounter.header.copy(id = id)))
        )
        scenes <- TestCreator.createScenes
        scenesWithIds <- ZIO.foreach(scenes)(scene =>
          dnd5eRepo.upsert(scene.header, scene.jsonInfo).map(id => scene.copy(scene.header.copy(id = id)))
        )
      } yield InitializingLayer()

    }

  def withContainer: ULayer[
    DMScreenZIORepository & STAZIORepository & DND5eZIORepository & ConfigurationService & DNDBeyondImporter &
      InitializingLayer
  ] = {
    ZLayer
      .make[
        DMScreenZIORepository & STAZIORepository & DND5eZIORepository & ConfigurationService & DNDBeyondImporter &
          InitializingLayer
      ](
        DMScreenContainer.containerLayer,
        ConfigurationService.live >>> DMScreenContainer.configLayer,
        dmscreen.QuillRepository.db,
        dmscreen.dnd5e.QuillRepository.db,
        dmscreen.sta.QuillRepository.db,
        DNDBeyondImporter.live,
        FifthEditionCharacterSheetImporter.live,
        SRDImporter.live,
        containerInitializingLayer
      ).orDie
  }

}
