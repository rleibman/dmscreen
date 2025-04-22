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

import auth.AuthClient
import dmscreen.components.DiceRoller
import dmscreen.dnd5e.*
import dmscreen.dnd5e.pages.*
import dmscreen.pages.{AboutPage, HomePage}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.router.*
import japgolly.scalajs.react.extra.router.StaticDsl.RouteB
import japgolly.scalajs.react.util.DefaultEffects.Sync
import japgolly.scalajs.react.vdom.html_<^.*
import net.leibman.dmscreen.semanticUiReact.*
import net.leibman.dmscreen.semanticUiReact.components.*
import net.leibman.dmscreen.semanticUiReact.distCommonjsCollectionsMenuMenuItemMod.MenuItemProps
import net.leibman.dmscreen.semanticUiReact.distCommonjsGenericMod.{SemanticCOLORS, SemanticWIDTHS}
import net.leibman.dmscreen.std.*
import org.scalajs.dom.html.Paragraph
import org.scalajs.dom.{HTMLAnchorElement, window}

object AppRouter {

  private object CommonPages {

    sealed trait CommonPages(override val name: String) extends AppPageType

    case object home extends CommonPages("home")
    case object about extends CommonPages("about")
    case object logout extends CommonPages("logout")

  }

  private val homePage = PageAppMenuItem(CommonPages.home, "Home", _ => HomePage())
  private val aboutPage = PageAppMenuItem(CommonPages.about, "About", _ => AboutPage())
  private val logoutPage = ButtonAppMenuItem(
    CommonPages.logout,
    title = "Logout",
    onClick = _ => AuthClient.logout().completeWith(_ => Callback.empty)
  )

  private def layout(
    campaignId: Option[CampaignId],
    gameUI:     Option[GameUI]
  )(
    page:       RouterCtl[AppPageType],
    resolution: Resolution[AppPageType]
  ) = {
    val allPageMenuItems: Seq[AppMenuItem] =
      homePage +:
        (gameUI.map(_.menuItems).toSeq.flatten ++
          Seq(aboutPage, logoutPage))

    def renderMenu = {
      Menu.Menu
        .fluid(true)
        .vertical(true)
        .tabular(true)
        .inverted(DND5eUI.menuInverted)
        .color(DND5eUI.menuColor)
        .withKey("mainMenu")
        .className("mainMenu")(
          allPageMenuItems.map {
            case menuItemInfo: PageAppMenuItem =>
              Menu.Item
                .withKey(menuItemInfo.pageType.name)
                .active(resolution.page == menuItemInfo.pageType)
                .onClick {
                  (
                    event: ReactMouseEventFrom[HTMLAnchorElement],
                    _
                  ) =>
                    page.setEH(menuItemInfo.pageType)(event.asInstanceOf[ReactEvent])

                }(menuItemInfo.title): VdomNode
            case menuItemInfo: ButtonAppMenuItem =>
              {
                Menu.Item
                  .withKey(menuItemInfo.pageType.name)
                  .active(resolution.page == menuItemInfo.pageType)
                  .onClick {
                    (
                      _,
                      _
                    ) =>
                      menuItemInfo.onClick(campaignId.getOrElse(CampaignId.empty))

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
    gameUIOpt:  Option[GameUI]
  ): RouterConfig[AppPageType] =
    RouterConfigDsl[AppPageType].buildConfig { dsl =>
      import dsl.*

      val campaignPageRules = {
        campaignId.fold(trimSlashes)(id =>
          gameUIOpt
            .map(_.menuItems.collect { case p: PageAppMenuItem => p })
            .toSeq
            .flatten
            .map { page =>
              staticRoute(s"#${page.pageType.name}", page.pageType) ~> renderR { ctl =>
                page.createComponentFn(id)
              }
            }
            .fold(trimSlashes)(_ | _)
        )
      }

      (trimSlashes
        | staticRoute(root, homePage.pageType) ~> renderR(_ =>
          homePage.createComponentFn(campaignId.getOrElse(CampaignId.empty))
        )
        | campaignPageRules
        | staticRoute(s"#${aboutPage.pageType.name}", aboutPage.pageType) ~> renderR(_ =>
          aboutPage.createComponentFn(campaignId.getOrElse(CampaignId.empty))
        ))
        .notFound { path =>
          println(s"${path.value}")
          // We're possibly getting here because we haven't loaded the campaign yet, so we need to redirect to the home page
          gameUIOpt.fold(
            // the gameUI is empty, redirecting to home
            redirectToPage(homePage.pageType)(SetRouteVia.HistoryReplace)
          ) { gameUI =>
            val savedPath = Option(window.localStorage.getItem("currentPage"))
            println("the gameUI is not empty, get the path from session storage is $savedPath")
            // remove the item from storage to make sure we clear it so we don't end up in an infinite loop
            // window.localStorage.removeItem("redirectPath")
            savedPath.fold(redirectToPage(homePage.pageType)(SetRouteVia.HistoryReplace)) { savedPath =>
              println("Redirecting to path $savedPath for game $gameUI")
              redirectToPath(savedPath)(SetRouteVia.HistoryReplace)
            }
          }
        }
        .renderWith(layout(campaignId, gameUIOpt))
        .onPostRender(
          (
            prev,
            cur
          ) =>
            Callback.log(s"setting current page to ${cur.name}") >> Callback(
              window.localStorage.setItem("currentPage", cur.name)
            )
        )
    }

  private val baseUrl: BaseUrl = BaseUrl.fromWindowOrigin_/
  println(s"baseUrl: $baseUrl")

  def router(
    campaignId: Option[CampaignId],
    gameUI:     Option[GameUI]
  ): Router[AppPageType] = {
    Router(baseUrl, config(campaignId, gameUI))
  }

}
