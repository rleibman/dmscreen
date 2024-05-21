package dmscreen

import org.scalajs.dom.window
import zio.*

case class ClientConfiguration() {

  val host =
    s"${window.location.hostname}${
        if (window.location.port != "" && window.location.port != "80")
          s":${window.location.port}"
        else ""
      }"

}

object ClientConfiguration {

  val config = ClientConfiguration()

  val live: ULayer[ClientConfiguration] = ZLayer.succeed(config)

}
