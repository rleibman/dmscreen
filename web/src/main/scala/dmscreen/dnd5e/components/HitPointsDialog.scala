package dmscreen.dnd5e.components

import dmscreen.dnd5e.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.VdomNode
import japgolly.scalajs.react.vdom.all.verticalAlign
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import org.scalajs.dom.html
import org.scalajs.dom.html.Span

object HitPointsDialog {

  case class State(
    hitPoints: HitPoints
  )

  case class Props(
    hitPoints: HitPoints,
    onSave:    HitPoints => Callback,
    onCancel:  Callback = Callback.empty
  )

  case class Backend($ : BackendScope[Props, State]) {

    def render(
      props: Props,
      state: State
    ): VdomNode = {
      <.div("Hello")
    }

  }

  import scala.language.unsafeNulls

  given Reusability[State] = Reusability.derive[State]
  given Reusability[Props] = Reusability.by((_: Props).hitPoints)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("HitPointsDialog")
    .initialStateFromProps(p => State(p.hitPoints))
    .renderBackend[Backend]
    .componentDidMount($ => Callback.empty)
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    hitPoints: HitPoints,
    onSave:    HitPoints => Callback = _ => Callback.empty,
    onCancel:  Callback = Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(hitPoints, onSave, onCancel))

}
