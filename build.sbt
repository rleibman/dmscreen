////////////////////////////////////////////////////////////////////////////////////
// Common Stuff

import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import org.apache.commons.io.FileUtils

lazy val buildTime: SettingKey[String] = SettingKey[String]("buildTime", "time of build").withRank(KeyRanks.Invisible)

//////////////////////////////////////////////////////////////////////////////////////////////////
// Global stuff
Global / onChangedBuildSource := ReloadOnSourceChanges
scalaVersion                  := "3.4.1"

//////////////////////////////////////////////////////////////////////////////////////////////////
// Shared settings

lazy val start = TaskKey[Unit]("start")
lazy val dist = TaskKey[File]("dist")
lazy val debugDist = TaskKey[File]("debugDist")

lazy val scala3Opts = Seq(
  "-no-indent", // scala3
  "-old-syntax", // scala3
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:implicitConversions",
  "-language:higherKinds", // Allow higher-kinded types
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  //  "-explain-types", // Explain type errors in more detail.
  //  "-explain",
  "-Yexplicit-nulls", // Make reference types non-nullable. Nullable types can be expressed with unions: e.g. String|Null.
  "-Xmax-inlines",
  "128",
  "-Yretain-trees" // Retain trees for debugging.
)

enablePlugins(
  GitVersioning
)

val calibanVersion = "2.6.0"
val zioVersion = "2.1.1"
val quillVersion = "4.8.4"
val zioHttpVersion = "3.0.0-RC6"
val zioConfigVersion = "4.0.2"
val zioJsonVersion = "0.6.2"
val testContainerVersion = "0.41.3"
val tapirVersion = "1.10.6"

lazy val commonSettings = Seq(
  organization     := "net.leibman",
  scalaVersion     := "3.4.1",
  startYear        := Some(2024),
  organizationName := "Roberto Leibman",
  headerLicense    := Some(HeaderLicense.MIT("2024", "Roberto Leibman", HeaderLicenseStyle.Detailed)),
  resolvers += Resolver.mavenLocal,
  scalacOptions ++= scala3Opts
)

////////////////////////////////////////////////////////////////////////////////////
// common (i.e. model)
lazy val commonJVM = common.jvm
lazy val commonJS = common.js

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .enablePlugins(
    AutomateHeaderPlugin,
    GitVersioning,
    BuildInfoPlugin
  )
  .settings(
    scalaVersion     := "3.4.1",
    name             := "dmscreen-common",
    buildInfoPackage := "dmscreen"
  )
  .settings(
    commonSettings
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "dev.zio"   %% "zio"            % zioVersion withSources (),
      "dev.zio"   %% "zio-json"       % "0.6.2" withSources (),
      "io.megl"   %% "zio-json-extra" % "0.6.2" withSources (),
      "org.gnieh" %% "diffson-core"   % "4.6.0" withSources ()
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio"            % zioVersion withSources (),
      "dev.zio" %%% "zio-json"       % "0.6.2" withSources (),
      "io.megl" %%% "zio-json-extra" % "0.6.2" withSources (),
      "org.gnieh" %%% "diffson-core" % "4.6.0" withSources ()
    )
  )

lazy val server = project
  .enablePlugins(
    AutomateHeaderPlugin,
    GitVersioning,
    LinuxPlugin,
    JDebPackaging,
    DebianPlugin,
    DebianDeployPlugin,
    JavaServerAppPackaging,
    SystemloaderPlugin,
    SystemdPlugin
  )
  .settings(debianSettings, commonSettings)
  .dependsOn(commonJVM)
  .settings(
    name := "dmscreen-server",
//    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2",
    libraryDependencies ++= Seq(
      // DB
      "mysql"        % "mysql-connector-java" % "8.0.33" withSources (),
      "io.getquill" %% "quill-jdbc-zio"       % quillVersion withSources (),
      // ZIO
      "dev.zio"                     %% "zio"                   % zioVersion withSources (),
      "dev.zio"                     %% "zio-cache"             % "0.2.3" withSources (),
      "dev.zio"                     %% "zio-config"            % zioConfigVersion withSources (),
      "dev.zio"                     %% "zio-config-derivation" % zioConfigVersion withSources (),
      "dev.zio"                     %% "zio-config-magnolia"   % zioConfigVersion withSources (),
      "dev.zio"                     %% "zio-config-typesafe"   % zioConfigVersion withSources (),
      "dev.zio"                     %% "zio-logging-slf4j"     % "2.2.3" withSources (),
      "dev.zio"                     %% "izumi-reflect"         % "2.3.9" withSources (),
      "com.github.ghostdogpr"       %% "caliban"               % calibanVersion withSources (),
      "com.github.ghostdogpr"       %% "caliban-tapir"         % calibanVersion withSources (),
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion withSources (),
      "com.github.ghostdogpr"       %% "caliban-zio-http"      % calibanVersion withSources (),
      "com.github.ghostdogpr"       %% "caliban-quick"         % calibanVersion withSources (),
      "dev.zio"                     %% "zio-http"              % zioHttpVersion withSources (),
      "com.github.jwt-scala"        %% "jwt-circe"             % "10.0.1" withSources (),
      "dev.zio"                     %% "zio-json"              % zioJsonVersion withSources (),
      // Other random utilities
      ("com.github.pathikrit" %% "better-files"               % "3.9.2" withSources ()).cross(CrossVersion.for3Use2_13),
      "com.github.daddykotex" %% "courier"                    % "3.2.0" withSources (),
      "ch.qos.logback"         % "logback-classic"            % "1.5.6" withSources (),
      "commons-codec"          % "commons-codec"              % "1.17.0",
      "com.dimafeng"          %% "testcontainers-scala-mysql" % testContainerVersion withSources (),
      // Testing
      "dev.zio" %% "zio-test"     % zioVersion % "test" withSources (),
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test" withSources ()
    )
  )

lazy val debianSettings =
  Seq(
    Debian / name               := "dmscreen",
    Debian / normalizedName     := "dmscreen",
    Debian / packageDescription := "The game of dmscreen",
    Debian / packageSummary     := "The game of dmscreen",
    Linux / maintainer          := "Roberto Leibman <roberto@leibman.net>",
    Linux / daemonUser          := "dmscreen",
    Linux / daemonGroup         := "dmscreen",
    Debian / serverLoading      := Some(ServerLoader.Systemd),
    Universal / mappings += {
      val src = sourceDirectory.value
      val conf = src / "templates" / "application.conf"
      conf -> "conf/application.conf"
    }
  )

////////////////////////////////////////////////////////////////////////////////////
// Web
val scalajsReactVersion = "2.1.1"
val reactVersion = "18.3.0"

lazy val reactNpmDeps: Project => Project =
  _.settings(
    Compile / npmDependencies ++= Seq(
      "react-dom"         -> reactVersion,
      "@types/react-dom"  -> reactVersion,
      "react"             -> reactVersion,
      "@types/react"      -> reactVersion,
      "csstype"           -> "3.1.3",
      "@types/prop-types" -> "15.7.12",
      "semantic-ui-react" -> "2.1.5",
      "react-chartjs-2"   -> "5.2.0"
    )
  )

lazy val bundlerSettings: Project => Project =
  _.enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      startWebpackDevServer / version := "5.0.4",
      webpack / version               := "5.91.0",
//      Compile / fastOptJS / webpackExtraArgs += "--mode=development",
      Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
      Compile / fastOptJS / artifactPath := ((Compile / fastOptJS / crossTarget).value /
        ((fastOptJS / moduleName).value + "-opt.js")),
      //      Compile / fullOptJS / webpackExtraArgs += "--mode=production",
      Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production",
      Compile / fullOptJS / artifactPath := ((Compile / fullOptJS / crossTarget).value /
        ((fullOptJS / moduleName).value + "-opt.js")),
      useYarn                                   := false,
      run / fork                                := true,
      Global / scalaJSStage                     := FastOptStage,
      Compile / scalaJSUseMainModuleInitializer := true,
      Test / scalaJSUseMainModuleInitializer    := false,
      webpackEmitSourceMaps                     := false,
      scalaJSLinkerConfig ~= (_.withSourceMap(false))
    )

lazy val stLib = project
  .in(file("dmscreen-stLib"))
  .enablePlugins(ScalablyTypedConverterGenSourcePlugin)
  .configure(reactNpmDeps)
  .settings(
    name                            := "dmscreen-stLib",
    scalaVersion                    := "3.4.1",
    useYarn                         := false,
    stOutputPackage                 := "net.leibman.dmscreen",
    stFlavour                       := Flavour.ScalajsReact,
    stReactEnableTreeShaking        := Selection.All,
    scalaJSUseMainModuleInitializer := true,
    /* disabled because it somehow triggers many warnings */
    scalaJSLinkerConfig ~= (_.withSourceMap(false)),
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core"  % scalajsReactVersion withSources (),
      "com.github.japgolly.scalajs-react" %%% "extra" % scalajsReactVersion withSources ()
    )
  )

lazy val web: Project = project
  .dependsOn(commonJS, stLib)
  .settings(commonSettings)
  .configure(bundlerSettings)
  .configure(withCssLoading)
  .configure(commonWeb)
  .enablePlugins(
    AutomateHeaderPlugin,
    GitVersioning,
    ScalaJSPlugin
  )
  .settings(
    name := "dmscreen-web",
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio"      % zioVersion withSources (),
      "dev.zio" %%% "zio-json" % "0.6.2" withSources ()
    ),
    debugDist := {

      val assets = (ThisBuild / baseDirectory).value / "web" / "src" / "main" / "web"

      val artifacts = (Compile / fastOptJS / webpack).value
      val artifactFolder = (Compile / fastOptJS / crossTarget).value
      val debugFolder = (ThisBuild / baseDirectory).value / "debugDist"

      debugFolder.mkdirs()
      FileUtils.copyDirectory(assets, debugFolder, true)
      artifacts.foreach { artifact =>
        val target = artifact.data.relativeTo(artifactFolder) match {
          case None          => debugFolder / artifact.data.name
          case Some(relFile) => debugFolder / relFile.toString
        }

        println(s"Trying to copy ${artifact.data.toPath} to ${target.toPath}")
        Files.copy(artifact.data.toPath, target.toPath, REPLACE_EXISTING)
      }

      debugFolder
    },
    dist := {
      val assets = (ThisBuild / baseDirectory).value / "web" / "src" / "main" / "web"

      val artifacts = (Compile / fullOptJS / webpack).value
      val artifactFolder = (Compile / fullOptJS / crossTarget).value
      val distFolder = (ThisBuild / baseDirectory).value / "dist"

      distFolder.mkdirs()
      FileUtils.copyDirectory(assets, distFolder, true)
      artifacts.foreach { artifact =>
        val target = artifact.data.relativeTo(artifactFolder) match {
          case None          => distFolder / artifact.data.name
          case Some(relFile) => distFolder / relFile.toString
        }

        println(s"Trying to copy ${artifact.data.toPath} to ${target.toPath}")
        Files.copy(artifact.data.toPath, target.toPath, REPLACE_EXISTING)
      }

      distFolder
    }
  )

lazy val commonWeb: Project => Project =
  _.settings(
    libraryDependencies ++= Seq(
//      "commons-io" % "commons-io" % "2.15.1" withSources(),
      "com.github.ghostdogpr" %%% "caliban-client"    % calibanVersion withSources (),
      "dev.zio" %%% "zio"                             % zioVersion withSources (),
      "com.softwaremill.sttp.client3" %%% "core"      % "3.9.6" withSources (),
      "io.github.cquiroz" %%% "scala-java-time"       % "2.5.0" withSources (),
      "io.github.cquiroz" %%% "scala-java-time-tzdb"  % "2.5.0" withSources (),
      "org.scala-js" %%% "scalajs-dom"                % "2.8.0" withSources (),
      "com.olvind" %%% "scalablytyped-runtime"        % "2.4.2",
      "com.github.japgolly.scalajs-react" %%% "core"  % "2.1.1" withSources (),
      "com.github.japgolly.scalajs-react" %%% "extra" % "2.1.1" withSources (),
      "com.lihaoyi" %%% "scalatags"                   % "0.13.1" withSources (),
      "com.github.japgolly.scalacss" %%% "core"       % "1.0.0" withSources (),
      "com.github.japgolly.scalacss" %%% "ext-react"  % "1.0.0" withSources ()
      //      ("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0").cross(CrossVersion.for3Use2_13)
    ),
    organizationName := "Roberto Leibman",
    startYear        := Some(2024),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories    := Seq((Test / scalaSource).value),
    webpackDevServerPort                 := 8009
  )

lazy val withCssLoading: Project => Project =
  _.settings(
    /* custom webpack file to include css */
    webpackConfigFile := Some((ThisBuild / baseDirectory).value / "custom.webpack.config.js"),
    Compile / npmDevDependencies ++= Seq(
      "webpack-merge" -> "4.2.2",
      "css-loader"    -> "3.4.2",
      "style-loader"  -> "1.1.3",
      "file-loader"   -> "5.1.0",
      "url-loader"    -> "4.1.0"
    )
  )

//////////////////////////////////////////////////////////////////////////////////////////////////
// Root project
lazy val root = project
  .in(file("."))
  .aggregate(commonJVM, commonJS, server, web, stLib)
  .settings(
    name           := "dmscreen",
    publish / skip := true,
    version        := "0.1.0",
    headerLicense  := None
  )
