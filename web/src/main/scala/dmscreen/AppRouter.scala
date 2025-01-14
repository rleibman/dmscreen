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
import dmscreen.dnd5e.pages.*
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
import org.scalajs.dom.{HTMLAnchorElement, window}
import org.scalajs.dom.html.Paragraph

object AppRouter {

  private enum CommonPages {

    case home extends CommonPages with AppPageType
    case about extends CommonPages with AppPageType

  }

  private val homePage = PageAppMenuItem(CommonPages.home, "Home", _ => HomePage())
  private val aboutPage = PageAppMenuItem(CommonPages.about, "About", _ => AboutPage())

  private def layout(
    campaignId: Option[CampaignId],
    gameUI:     Option[GameUI]
  )(
    page:       RouterCtl[AppPageType],
    resolution: Resolution[AppPageType]
  ) = {
    val allPageMenuItems: Seq[AppMenuItem] =
      homePage +:
        gameUI.map(_.menuItems).toSeq.flatten :+
        aboutPage

    def renderMenu = {
      Menu.Menu
        .fluid(true)
        .vertical(true)
        .tabular(true)
        .withKey("mainMenu")
        .className("mainMenu")(
          allPageMenuItems.map {
            case menuItemInfo: PageAppMenuItem =>
              Menu.Item
                .withKey(menuItemInfo.pageType.toString)
                .active(resolution.page == menuItemInfo.pageType)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    _
                  ) =>
                    page.setEH(menuItemInfo.pageType)(event.asInstanceOf[ReactEvent])

                }(menuItemInfo.title): VdomNode
            case menuItemInfo: ButtonAppMenuItem =>
              campaignId.fold(EmptyVdom) { id =>
                Menu.Item
                  .withKey(menuItemInfo.pageType.toString)
                  .active(resolution.page == menuItemInfo.pageType)
                  .onClick {
                    (
                      _,
                      _
                    ) =>
                      menuItemInfo.onClick(id)

                  }(menuItemInfo.title)
              }: VdomNode
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

  private def config(
    campaignId: Option[CampaignId],
    gameUI:     Option[GameUI]
  ): RouterConfig[AppPageType] =
    RouterConfigDsl[AppPageType].buildConfig { dsl =>
      import dsl.*

      val campaignPageRules = {
        campaignId.fold(trimSlashes)(id =>
          gameUI
            .map(_.menuItems.collect { case p: PageAppMenuItem => p })
            .toSeq
            .flatten
            .map { page =>
              staticRoute(s"#${page.pageType.toString}", page.pageType) ~> renderR(_ => page.createComponentFn(id))
            }
            .fold(trimSlashes)(_ | _)
        )
      }

      (trimSlashes
        | staticRoute(root, homePage.pageType) ~> renderR(_ =>
          homePage.createComponentFn(campaignId.getOrElse(CampaignId.empty))
        )
        | campaignPageRules
        | staticRoute(s"#${aboutPage.pageType.toString}", aboutPage.pageType) ~> renderR(_ =>
          aboutPage.createComponentFn(campaignId.getOrElse(CampaignId.empty))
        ))
        .notFound { path =>
          println(s"${path.value}")
          // We're possibly getting here because we haven't loaded the campaign yet, so we need to redirect to the home page
          if (gameUI.isEmpty) {
            println("the gameUI is empty, redirecting to home, but saving the path")
            // Let's save the current path so we can redirect to it after the campaign is loaded
            window.sessionStorage.setItem("redirectPath", path.value)
            redirectToPage(homePage.pageType)(SetRouteVia.HistoryReplace)
          } else {
            val savedPath: String = window.sessionStorage.getItem("redirectPath")
            println("the gameUI is not empty, get the path from session storage is $savedPath")
            // remove the item from storage to make sure we clear it so we don't end up in an infinite loop
            window.sessionStorage.removeItem("redirectPath")
            if (savedPath.trim.nonEmpty) {
              redirectToPath(savedPath)(SetRouteVia.HistoryReplace)
            } else {
              redirectToPage(homePage.pageType)(SetRouteVia.HistoryReplace)
            }
          }
        }
        .renderWith(layout(campaignId, gameUI))
    }

  private val baseUrl: BaseUrl = BaseUrl.fromWindowOrigin_/
  println(s"baseUrl: $baseUrl")

  def router(
    campaignId: Option[CampaignId],
    gameUI:     Option[GameUI]
  ): Router[AppPageType] = {
    val c: RouterConfig[AppPageType] = config(campaignId, gameUI)
    Router.apply(baseUrl, c)
  }

}
