//////////////////////////////////////////////////////////////////////////////////////////////////
// Global stuff
lazy val SCALA = "3.5.0-RC2"

val scalajsReactVersion = "2.1.1"
val reactVersion = "^18.3.1"

version := "0.4.0-SNAPSHOT"

enablePlugins(ScalablyTypedConverterGenSourcePlugin)

Global / onChangedBuildSource := ReloadOnSourceChanges
scalaVersion                  := SCALA
Global / scalaVersion         := SCALA

organization     := "net.leibman"
startYear        := Some(2024)
organizationName := "Roberto Leibman"
headerLicense    := Some(HeaderLicense.MIT("2024", "Roberto Leibman", HeaderLicenseStyle.Detailed))
name             := "dmscreen-stlib"
useYarn          := true
stOutputPackage  := "net.leibman.dmscreen"
stFlavour        := Flavour.ScalajsReact

/* javascript / typescript deps */
Compile / npmDependencies ++= Seq(
  "react-dom"                      -> reactVersion,
  "@types/react-dom"               -> reactVersion,
  "react"                          -> reactVersion,
  "@types/react"                   -> reactVersion,
  "csstype"                        -> "^3.0.0",
  "@types/prop-types"              -> "^15.0.0",
  "semantic-ui-react"              -> "^2.1.5",
  "react-apexcharts"               -> "^1.0.0",
  "apexcharts"                     -> "^3.0.0",
  "react-quill"                    -> "^2.0.0",
  "@3d-dice/dice-ui"               -> "^0.4.0",
  "@3d-dice/dice-parser-interface" -> "^0.2.0",
  "@3d-dice/dice-box"              -> "^1.0.0"
)

Test / npmDependencies ++= Seq(
  "react"     -> reactVersion,
  "react-dom" -> reactVersion
)

/* disabled because it somehow triggers many warnings */
scalaJSLinkerConfig ~= (_.withSourceMap(false))

// focus only on these libraries
stMinimize := Selection.AllExcept("semantic-ui-react", "react-apexcharts", "react-quill")

stIgnore ++= List(
  "@3d-dice/dice-ui",
  "@3d-dice/dice-parser-interface",
  "@3d-dice/dice-box"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

doc / sources := Nil
