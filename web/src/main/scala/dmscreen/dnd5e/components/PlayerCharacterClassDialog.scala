package dmscreen.dnd5e.components

import dmscreen.dnd5e.components.PlayerCharacterComponent.{Backend, Props, State, component}
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

object PlayerCharacterClassDialog {

  case class State(
    classes: List[PlayerCharacterClass]
  )

  case class Props(
    classes:  List[PlayerCharacterClass],
    onSave:   List[PlayerCharacterClass] => Callback,
    onCancel: Callback
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
  given Reusability[Props] = Reusability.by((_: Props).classes)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("PlayerCharacterClassDialog")
    .initialStateFromProps(p => State(p.classes))
    .renderBackend[Backend]
    .componentDidMount($ => Callback.empty)
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    classes:  List[PlayerCharacterClass],
    onSave:   List[PlayerCharacterClass] => Callback = _ => Callback.empty,
    onCancel: Callback = Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(classes, onSave, onCancel))

}
