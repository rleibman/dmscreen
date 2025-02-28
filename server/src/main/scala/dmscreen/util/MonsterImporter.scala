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

package dmscreen.util

import dmscreen.db.dnd5e
import dmscreen.db.dnd5e.DND5eZIORepository
import dmscreen.db.sta.STAZIORepository
import dmscreen.dnd5e.MonsterSearch
import dmscreen.dnd5e.dndbeyond.DNDBeyondImporter
import dmscreen.dnd5e.fifthEditionCharacterSheet.FifthEditionCharacterSheetImporter
import dmscreen.dnd5e.otherImporters.XLSImporter
import dmscreen.dnd5e.srd.SRDImporter
import dmscreen.{ConfigurationService, DMScreenError, DMScreenSession, EnvironmentBuilder}
import zio.{EnvironmentTag, ULayer, ZIO, ZIOApp, ZIOAppArgs, ZLayer}

import java.io.File

object MonsterImporter extends ZIOApp {

  override type Environment = STAZIORepository & DND5eZIORepository & ConfigurationService & DNDBeyondImporter &
    SRDImporter & XLSImporter & DMScreenSession
  override def environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]
  override def bootstrap: ULayer[
    STAZIORepository & DND5eZIORepository & ConfigurationService & DNDBeyondImporter & SRDImporter & XLSImporter &
      DMScreenSession
  ] =
    ZLayer.make[Environment](
      EnvironmentBuilder.live,
      SRDImporter.live,
      XLSImporter.live,
      DMScreenSession.adminSession.toLayer
    )

  override def run: ZIO[Environment, DMScreenError, Unit] = {
    for {
      repo <- ZIO.service[DND5eZIORepository]

      srdImporter <- ZIO.service[SRDImporter]
      srdFile = File("/home/rleibman/projects/dmscreen/common/shared/src/main/resources/5e-SRD-Monsters.json")
      _ <- ZIO.fail(DMScreenError(s"File ${srdFile.getAbsolutePath} does not exist")).when(!srdFile.exists())
      _ <- srdImporter
        .importMonsters(srdFile).foreach(monster =>
          for {
            found    <- repo.bestiary(MonsterSearch(name = Some(monster.header.name)))
            upserted <- repo.upsert(monster.header, monster.jsonInfo).when(found.total == 0)
          } yield found
        )

      xlsImporter <- ZIO.service[XLSImporter]
      xlsFile = File("/home/rleibman/projects/dmscreen/common/shared/src/main/resources/monstersFromXLS.json")
      _ <- ZIO.fail(DMScreenError(s"File ${xlsFile.getAbsolutePath} does not exist")).when(!xlsFile.exists())
      _ <- xlsImporter
        .importMonsters(xlsFile).drop(200).foreach(monster =>
          for {
            found    <- repo.bestiary(MonsterSearch(name = Some(monster.header.name)))
            upserted <- repo.upsert(monster.header, monster.jsonInfo).when(found.total == 0)
          } yield found
        )
    } yield ()
  }

}
