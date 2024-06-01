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

object SkillsDialog {

  case class State(
    skills: Skills
  )

  case class Props(
    skills:   Skills,
    onSave:   Skills => Callback,
    onCancel: Callback = Callback.empty
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
  given Reusability[Props] = Reusability.by((_: Props).skills)

  private val component: Component[Props, State, Backend, CtorType.Props] = ScalaComponent
    .builder[Props]("SkillsDialog")
    .initialStateFromProps(p => State(p.skills))
    .renderBackend[Backend]
    .componentDidMount($ => Callback.empty)
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
    skills:   Skills,
    onSave:   Skills => Callback = _ => Callback.empty,
    onCancel: Callback = Callback.empty
  ): Unmounted[Props, State, Backend] = component(Props(skills, onSave, onCancel))

}
