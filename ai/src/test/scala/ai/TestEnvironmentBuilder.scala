package ai

import dmscreen.{ConfigurationServiceImpl, *}
import dmscreen.db.DMScreenZIORepository
import dmscreen.db.dnd5e.DND5eZIORepository
import dmscreen.db.sta.STAZIORepository
import zio.{ULayer, ZLayer}

type TestDMScreenServerEnvironment = DMScreenZIORepository & STAZIORepository & DND5eZIORepository &
  ConfigurationService

object TestEnvironmentBuilder {

  def live: ULayer[TestDMScreenServerEnvironment] =
    ZLayer
      .make[TestDMScreenServerEnvironment](
        ConfigurationServiceImpl.live,
        dmscreen.db.QuillRepository.db,
        dmscreen.db.dnd5e.QuillRepository.db,
        dmscreen.db.sta.QuillRepository.db
      ).orDie

}
