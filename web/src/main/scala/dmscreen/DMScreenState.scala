package dmscreen

import caliban.client.scalajs.WebSocketHandler
import japgolly.scalajs.react.React.Context
import japgolly.scalajs.react.{Callback, React}
import org.scalajs.dom.window

trait CampaignState

case class DMScreenState(
  user:            Option[User] = None,
  campaignState:   Option[CampaignState] = None,
  operationStream: Option[WebSocketHandler] = None
)

object DMScreenState {

  val ctx: Context[DMScreenState] = React.createContext(DMScreenState())

}
