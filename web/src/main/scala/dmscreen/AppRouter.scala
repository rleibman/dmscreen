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

import dmscreen.components.DiceRoller
import dmscreen.dnd5e.*
import dmscreen.dnd5e.pages.{DashboardPage, EncounterPage, NPCPage, PCPage, ScenePage}
import dmscreen.pages.{AboutPage, HomePage}
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

  private enum CommonPages {

    case home extends CommonPages with AppPageType
    case about extends CommonPages with AppPageType

  }

  private val homePage = AppPage(CommonPages.home, "Home", _ => HomePage())
  private val aboutPage = AppPage(CommonPages.about, "About", _ => AboutPage())

  private def layout(
    gameUI: Option[GameUI]
  )(
    page:       RouterCtl[AppPageType],
    resolution: Resolution[AppPageType]
  ) = {
    val allPages: Seq[AppPage] =
      homePage +:
        gameUI.map(_.pages).toSeq.flatten :+
        aboutPage

    def renderMenu = {
      Menu.Menu
        .fluid(true)
        .vertical(true)
        .tabular(true)
        .withKey("mainMenu")
        .className("mainMenu")(
          allPages.map { pageInfo =>
            Menu.Item
              .withKey(pageInfo.title)
              .active(resolution.page == pageInfo.pageType)
              .onClick {
                (
                  event: ReactMouseEventFrom[HTMLAnchorElement],
                  data:  MenuItemProps
                ) =>
                  page.setEH(pageInfo.pageType)(event.asInstanceOf[ReactEvent])

              }(pageInfo.title): VdomElement
          }.toVdomArray,
          <.div(^.width := 100.pct, ^.textAlign := "center", ^.display := "inline-block", DiceRoller())
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

  private def config(gameUI: Option[GameUI]): RouterConfig[AppPageType] =
    RouterConfigDsl[AppPageType].buildConfig { dsl =>
      import dsl.*

      val campaignPageRules = gameUI
        .map(_.pages)
        .toSeq
        .flatten
        .map { page =>
          staticRoute(s"#${page.title}", page.pageType) ~> renderR(_ => page.createComponentFn(()))
        }
        .fold(trimSlashes)(_ | _)

      (trimSlashes
        | staticRoute(root, homePage.pageType) ~> renderR(_ => homePage.createComponentFn(()))
        | campaignPageRules
        | staticRoute(s"#${aboutPage.title}", aboutPage.pageType) ~> renderR(_ => aboutPage.createComponentFn(())))
        .notFound(redirectToPage(homePage.pageType)(SetRouteVia.HistoryReplace))
        .renderWith(layout(gameUI))
    }

  private val baseUrl: BaseUrl = BaseUrl.fromWindowOrigin_/

  def router(gameUI: Option[GameUI]): Router[AppPageType] = {
    val c: RouterConfig[AppPageType] = config(gameUI)
    Router.apply(baseUrl, c)
  }

}
