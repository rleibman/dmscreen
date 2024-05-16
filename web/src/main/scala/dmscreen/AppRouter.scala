package dmscreen

import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.<

object AppRouter {
  case class State()

  class Backend($: BackendScope[Unit, State]) {
    def render(s: State) = {
      <.div(
        <.div(^.className := "menu",
          "Menu goes here"
        ),
        <.div(^.className := "pageContainer",
          "Pages go here")
      )
    }
  }


  private val component = ScalaComponent
    .builder[Unit]("router")
    .initialState {
      State()
    }
    .renderBackend[Backend]
    .componentDidMount(
      //_.backend.refresh(initial = true)()
      $ =>
        Callback.empty
    )
    .componentWillUnmount($ =>
      //TODO close down streams here
      Callback.empty
    )
    .build

  def apply(): Unmounted[Unit, State, Backend] = component()

}
