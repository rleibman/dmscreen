package dmscreen

trait CampaignState

case class DMScreenState(
  user:            Option[User] = None,
  campaignState:   Option[CampaignState] = None,
  operationStream: Option[WebSocketHandler] = None
)

object DMScreenState {

  val ctx: Context[DMScreenState] = React.createContext(DMScreenState())

}
