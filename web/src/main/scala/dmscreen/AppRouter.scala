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

import dmscreen.dnd5e.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.router.*
import japgolly.scalajs.react.extra.router.StaticDsl.RouteB
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsCollectionsMenuMenuItemMod.MenuItemProps
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.SemanticWIDTHS
import net.leibman.dmscreen.std.*
import org.scalajs.dom.HTMLAnchorElement

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
          .withKey("mainMenu")
          .className("mainMenu")(
            Menu.Item
              .withKey("home")
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
                .withKey("dashboard")
                .active(resolution.page == AppPage.dashboard)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.dashboard)(event.asInstanceOf[ReactEvent])

                }("Dashboard"),
              Menu.Item
                .withKey("pcs")
                .active(resolution.page == AppPage.player)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.player)(event.asInstanceOf[ReactEvent])

                }("PCs"),
              Menu.Item
                .withKey("encounters")
                .active(resolution.page == AppPage.encounter)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.encounter)(event.asInstanceOf[ReactEvent])

                }("Encounters"),
              Menu.Item
                .withKey("npcs")
                .active(resolution.page == AppPage.npc)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.npc)(event.asInstanceOf[ReactEvent])
                }("NPCs"),
              Menu.Item
                .withKey("scenes")
                .active(resolution.page == AppPage.scene)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    data:  MenuItemProps
                  ) =>
                    page.setEH(AppPage.scene)(event.asInstanceOf[ReactEvent])

                }("Scenes")
            ).when(dmscreenState.campaignState.isDefined),
            Menu.Item
              .withKey("about")
              .active(resolution.page == AppPage.about)
              .onClick {
                (
                  event: ReactMouseEventFrom[HTMLAnchorElement],
                  data:  MenuItemProps
                ) =>
                  page.setEH(AppPage.about)(event.asInstanceOf[ReactEvent])

              }("About"),
            <.div(^.width := 100.pct, ^.textAlign := "center", ^.display := "inline-block", DiceRoller())
//            Button("Roll").onClick(
//              (
//                _,
//                _
//              ) =>
//                DiceRoller
//                  .roll("10d20").map(r => Callback.alert(scala.scalajs.js.JSON.stringify(r))).completeWith(_.get)
//            )
          )

      }
      Grid
        .className("mainMenu")(
          Grid
            .Column()
            .withKey("mainMenu")
            .className("mainMenu")
            .width(SemanticWIDTHS.`2`)(renderMenu),
          Grid
            .Column()
            .withKey("mainContent")
            .width(SemanticWIDTHS.`14`)(
              resolution.render()
            )
        )
    }
  }

  sealed trait AppPage

  object AppPage {

    case object home extends AppPage
    case object dashboard extends AppPage
    case object player extends AppPage
    case object encounter extends AppPage
    case object npc extends AppPage
    case object scene extends AppPage
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
        | staticRoute("#about", AppPage.about) ~> renderR(_ => AboutPage())
    )
      .notFound(redirectToPage(AppPage.dashboard)(SetRouteVia.HistoryReplace))
      .renderWith(layout)
  }

  private val baseUrl: BaseUrl = BaseUrl.fromWindowOrigin_/

  val router: Router[AppPage] = Router.apply(baseUrl, config)

}
