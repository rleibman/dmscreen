package dmscreen

import dmscreen.dnd5e.{DND5eGameService, GraphQLClientGameService}
import zio.ZLayer

type DMScreenClientEnvironment = ClientConfiguration

object EnvironmentBuilder {

  def live =
    ZLayer
      .make[DMScreenServerEnvironment & DND5eGameService](
        ConfigurationService.live,
        GraphQLClientGameService.live
      ).orDie

}
