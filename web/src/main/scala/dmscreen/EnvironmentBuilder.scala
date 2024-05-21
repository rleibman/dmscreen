package dmscreen

import dmscreen.dnd5e.{DND5eGameService, GraphQLClientGameService}
import zio.ZLayer

type DMScreenClientEnvironment = DND5eGameService

object EnvironmentBuilder {

  def live =
    ZLayer
      .make[DND5eGameService](
        ClientConfiguration.live,
        GraphQLClientGameService.live
      )

}
