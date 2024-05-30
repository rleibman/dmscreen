/*
 * Copyright (c) 2024 Roberto Leibman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dmscreen

import japgolly.scalajs.react.extra.router.StaticDsl.RouteB
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.*
import net.leibman.dmscreen.std.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsCollectionsMenuMenuItemMod.MenuItemProps
import dmscreen.dnd5e.*
import org.scalajs.dom.HTMLAnchorElement
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticWIDTHS

object AppRouter {

  private def layout(
    page:       RouterCtl[AppPage],
    resolution: Resolution[AppPage]
  ) = {
    DMScreenState.ctx.consume { dmscreenState =>

      def renderMenu = {
        Menu.Menu
          .fluid(true)
          .vertical(true)
          .tabular(true)
          .className("mainMenu")(
            Menu.Item
              .active(resolution.page == AppPage.home)
              .onClick {
                (
                  event: ReactMouseEventFrom[HTMLAnchorElement],
                  data:  MenuItemProps
                ) =>
                  page.setEH(AppPage.home)(event.asInstanceOf[ReactEvent])

              }("Home"),
            VdomArray(
              Menu.Item
                .active(resolution.page == AppPage.dashboard)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.dashboard)(event.asInstanceOf[ReactEvent])

                }("Dashboard"),
              Menu.Item
                .active(resolution.page == AppPage.player)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.player)(event.asInstanceOf[ReactEvent])

                }("PCs"),
              Menu.Item
                .active(resolution.page == AppPage.encounter)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.encounter)(event.asInstanceOf[ReactEvent])

                }("Encounters"),
              Menu.Item
                .active(resolution.page == AppPage.npc)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.npc)(event.asInstanceOf[ReactEvent])

                }("NPCs"),
              Menu.Item
                .active(resolution.page == AppPage.scene)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.scene)(event.asInstanceOf[ReactEvent])

                }("Scenes"),
              Menu.Item
                .active(resolution.page == AppPage.bestiary)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.bestiary)(event.asInstanceOf[ReactEvent])

                }("Bestiary")
            ).when(dmscreenState.campaignState.isDefined),
            Menu.Item
              .active(resolution.page == AppPage.about)
              .onClick {
                (
                  event: ReactMouseEventFrom[HTMLAnchorElement],
                  data:  MenuItemProps
                ) =>
                  page.setEH(AppPage.about)(event.asInstanceOf[ReactEvent])

              }("About")
          )

      }
      Grid
        .className("mainMenu")(
          Grid
            .Column()
            .className("mainMenu")
            .width(SemanticWIDTHS.`2`)(renderMenu),
          Grid
            .Column()
            .width(SemanticWIDTHS.`14`)(
              resolution.render()
            )
        )
    }
  }

  trait AppPage {}

  object AppPage {

    case object home extends AppPage
    case object dashboard extends AppPage
    case object player extends AppPage
    case object encounter extends AppPage
    case object npc extends AppPage
    case object scene extends AppPage
    case object bestiary extends AppPage
    case object about extends AppPage

  }

  private val config: RouterConfig[AppPage] = RouterConfigDsl[AppPage].buildConfig { dsl =>
    import dsl.*
    (
      trimSlashes
        | staticRoute("#home", AppPage.home) ~> renderR(_ => HomePage())
        | staticRoute(root, AppPage.dashboard) ~> renderR(_ => DashboardPage())
        | staticRoute("#player", AppPage.player) ~> renderR(_ => PlayerPage())
        | staticRoute("#encounter", AppPage.encounter) ~> renderR(_ => EncounterPage())
        | staticRoute("#npc", AppPage.npc) ~> renderR(_ => NPCPage())
        | staticRoute("#scene", AppPage.scene) ~> renderR(_ => ScenePage())
        | staticRoute("#bestiary", AppPage.bestiary) ~> renderR(_ => BestiaryPage())
        | staticRoute("#about", AppPage.about) ~> renderR(_ => AboutPage())
    )
      .notFound(redirectToPage(AppPage.dashboard)(SetRouteVia.HistoryReplace))
      .renderWith(layout)
  }

  private val baseUrl: BaseUrl = BaseUrl.fromWindowOrigin_/

  val router: Router[AppPage] = Router.apply(baseUrl, config)

}
