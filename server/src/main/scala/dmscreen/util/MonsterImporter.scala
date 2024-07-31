package dmscreen.util

import dmscreen.{ConfigurationService, DMScreenError, EnvironmentBuilder}
import dmscreen.dnd5e.{DND5eZIORepository, MonsterSearch}
import dmscreen.dnd5e.dndbeyond.DNDBeyondImporter
import dmscreen.dnd5e.fifthEditionCharacterSheet.FifthEditionCharacterSheetImporter
import dmscreen.dnd5e.otherImporters.XLSImporter
import dmscreen.dnd5e.srd.SRDImporter
import dmscreen.sta.STAZIORepository
import zio.{EnvironmentTag, ULayer, ZIO, ZIOApp, ZIOAppArgs, ZLayer}

import java.io.File

object MonsterImporter extends ZIOApp {

  override type Environment = STAZIORepository & DND5eZIORepository & ConfigurationService & DNDBeyondImporter &
    SRDImporter & XLSImporter
  override def environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]
  override def bootstrap: ULayer[
    STAZIORepository & DND5eZIORepository & ConfigurationService & DNDBeyondImporter & SRDImporter & XLSImporter
  ] = {
    ZLayer.make[Environment](EnvironmentBuilder.live, SRDImporter.live, XLSImporter.live)
  }

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
