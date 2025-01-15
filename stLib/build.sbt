//////////////////////////////////////////////////////////////////////////////////////////////////
// Global stuff
lazy val SCALA = "3.6.2"

val scalajsReactVersion = "2.1.1"
val reactVersion = "^18.3.0"

version := "0.8.0-SNAPSHOT"

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
  "@types/react"                   -> reactVersion,
  "@types/react-dom"               -> reactVersion,
  "react"                          -> reactVersion,
  "react-dom"                      -> reactVersion,
  "@3d-dice/dice-box"              -> "^1.1.0",
  "@3d-dice/dice-parser-interface" -> "^0.2.0",
  "@3d-dice/dice-ui"               -> "^0.5.0",
  "@types/prop-types"              -> "^15.7.0",
  "apexcharts"                     -> "^3.51.0",
  "babylonjs-gltf2interface"       -> "^5.22.0",
  "csstype"                        -> "^3.1.0",
  "react-apexcharts"               -> "^1.4.0",
  "react-quill"                    -> "^2.0.0",
  "semantic-ui-react"              -> "^2.1.5",
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
