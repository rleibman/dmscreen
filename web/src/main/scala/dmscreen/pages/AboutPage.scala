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

package dmscreen.pages

import dmscreen.{BuildInfo, DMScreenPage}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^.{<, *}

object AboutPage extends DMScreenPage {

  case class State(
  )

  class Backend($ : BackendScope[Unit, State]) {

    def render(state: State) = {
      <.div(
        <.div(
          <.img(
            ^.src           := "/images/logo.png",
            ^.width         := 400.px,
            ^.paddingBottom := 40.px,
            ^.paddingTop    := 10.px
          )
        ),
        <.div(
          ^.width   := 800.px,
          ^.padding := 10.px,
          <.h1("Features"),
          <.p(
            "This project, dmscreen, is a tool designed to assist Dungeon Masters (DMs) in managing and running Dungeons & Dragons 5th Edition (D&D 5e) encounters. It provides a user-friendly interface for organizing and editing encounters, managing monsters, and tracking various aspects of gameplay."
          ),
          <.h1("About DMScreen™"),
          <.p(
            "DMScreen™ is a work in progress, it is a showcase of pretty cool technology. Send us an email (",
            <.a(^.href := "mailto:roberto@leibman.net", "roberto@leibman.net"),
            ") if you are interested in this project or have questions about any of the technologies used here."
          ),
          <.p(
            "Sourcecode available at:",
            <.a(
              ^.href := "https://github.com/rleibman/dmscreen",
              <.img(
                ^.src   := "https://github.githubassets.com/assets/GitHub-Mark-ea2971cee799.png",
                ^.width := 25.px
              )
            )
          ),
          <.h1("Powered by"),
          <.table(
            <.tbody(
              <.tr(
                <.td(
                  <.a(
                    ^.href := "http://www.scala-lang.org",
                    <.img(
                      ^.src   := "https://www.scala-lang.org/resources/img/scala-logo.png",
                      ^.width := 150.px
                    )
                  )
                ),
                <.td("Scala"),
                <.td("Functional and object oriented language")
              ),
              <.tr(
                <.td(
                  <.a(
                    ^.href := "https://zio.dev/",
                    <.img(
                      ^.src   := "https://zio.dev/img/navbar_brand2x.png",
                      ^.width := 150.px
                    )
                  )
                ),
                <.td("ZIO"),
                <.td("Type-safe, composable asynchronous and concurrent programming for Scala")
              ),
              <.tr(
                <.td(
                  <.a(
                    ^.href := "https://mariadb.org/         ",
                    <.img(
                      ^.src   := "https://mariadb.com/wp-content/uploads/2019/11/mariadb-logo-vertical_blue.svg",
                      ^.width := 150.px
                    )
                  )
                ),
                <.td("MariaDB Database"),
                <.td("MariaDB is an open-source relational database.")
              ),
              <.tr(
                <.td(
                  <.a(
                    ^.href := "http://www.scala-js.org",
                    <.img(
                      ^.src   := "https://www.scala-js.org/assets/img/scala-js-logo.svg",
                      ^.width := 150.px
                    )
                  )
                ),
                <.td("Scala.js"),
                <.td(
                  "Scala.js is a compiler that compiles Scala source code to equivalent Javascript code"
                )
              ),
              <.tr(
                <.td(
                  <.a(
                    ^.href := "https://reactjs.org",
                    <.img(
                      ^.src   := "https://upload.wikimedia.org/wikipedia/commons/a/a7/React-icon.svg",
                      ^.width := 150.px
                    )
                  )
                ),
                <.td("React.js"),
                <.td("React is a JavaScript library for building user interfaces.")
              ),
              <.tr(
                <.td(
                  <.a(
                    ^.href := "https://react.semantic-ui.com",
                    <.img(^.src := "https://semantic-ui.com/images/logo.png", ^.width := 150.px)
                  )
                ),
                <.td("Semantic-UI"),
                <.td(
                  "User interface is the language of the web. Good looking web component library."
                )
              ),
              <.tr(
                <.td(
                  <.a(
                    ^.href := "https://docs.langchain4j.dev/",
                    <.img(^.src := "https://docs.langchain4j.dev/img/logo.svg", ^.width := 150.px)
                  )
                ),
                <.td("Langchain4j"),
                <.td(
                  "Supercharge your JVM application with the power of LLMs"
                )
              ),
              <.tr(
                <.td(<.a(^.href := "https://bintray.com/oyvindberg", "ScalablyTyped")),
                <.td("ScalablyTyped"),
                <.td(
                  "Showcasing the most amazing ScalablyTyped project, with over 8000 Scala.Js wrappers of javascript projects"
                )
              ),
              <.tr(
                <.td(
                  ^.colSpan := 3,
                  """Aside from the libraries mentioned above, this project uses many other libraries, among them:
                    |Caliban (GraphQL Library), cats (Functional Programming), zio-config (Keeps Configuration), zio-json (For JSON conversion),
                    |quill (Great database/scala layer), zio-http (The HTTP Server), zio-cache (for caching various things),
                    |zio-test (the testing platform for zio), jsoniter (super fast json conversions), testcontainers (Great for testing), 
                    |qdrant (a database used for LLM embeddings), d3-dice (dice rollers), scalajs-react (allows us to use react components in scalajs).
                    |Please support them when you can!
                    |""".stripMargin
                )
              )
            )
          )
        ),
        <.div(
          <.h1(s"Legal Information"),
          <.h2("SDR license for the D&D 5E SRD"),
          <.div(
            """
The System Reference Document 5.2 (“SRD 5.2”) is provided to you free of charge by Wizards of the Coast LLC
(“Wizards”) under the terms of the Creative Commons Attribution 4.0 International License (“CC-BY-4.0”).
You are free to use the content in this document in any manner permitted under CC-BY-4.0, provided that you
include the following attribution statement in any of your work:
This work includes material from the System Reference Document 5.2 (“SRD 5.2”) by Wizards of the Coast
LLC, available at https://www.dndbeyond.com/srd. The SRD 5.2 is licensed under the Creative Commons
Attribution 4.0 International License, available at https://creativecommons.org/licenses/by/4.0/legalcode.
Please do not include any other attribution to Wizards or its parent or affiliates other than that provided
above. You may, however, include a statement on your work indicating that it is “compatible with fifth edition”
or “5E compatible.”
Section 5 of CC-BY-4.0 includes a Disclaimer of Warranties and Limitation of Liability that limits our liability
"""
          ),
          <.h2("MIT License"),
          <.h3("Copyright (c) 2024 Roberto Leibman"),
          <.div(
            """
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
"""
          ),
          <.h2(s"Version = ${BuildInfo.version}"),
          <.h2("Location of graphql schemas"),
          <.div(<.a(^.href := "/unauth/dmscreen/schema", "DMScreen Schema")),
          <.div(<.a(^.href := "/unauth/dnd5e/schema", "DND5E Schema")),
          <.div(<.a(^.href := "/unauth/sta/schema", "STA Schema"))
        )
      )
    }

  }
  private val component = ScalaComponent
    .builder[Unit]("AboutPage")
    .initialState {
      State()
    }
    .renderBackend[Backend]
    .build

  def apply(
  ): Unmounted[Unit, State, Backend] = component()

}
