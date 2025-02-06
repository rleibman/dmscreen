////////////////////////////////////////////////////////////////////////////////////
// Common Stuff

import com.typesafe.sbt.SbtGit.GitKeys.gitDescribedVersion
import org.apache.commons.io.FileUtils

import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

lazy val buildTime: SettingKey[String] = SettingKey[String]("buildTime", "time of build").withRank(KeyRanks.Invisible)

lazy val SCALA = "3.6.3"
Global / onChangedBuildSource := ReloadOnSourceChanges
scalaVersion                  := SCALA
Global / scalaVersion         := SCALA

import scala.concurrent.duration.*
Global / watchAntiEntropy := 1.second

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
  "-Yretain-trees" // Retain trees for debugging.,
)

enablePlugins(
  GitVersioning
)

val calibanVersion = "2.9.1"
val langchainVersion = "1.0.0-alpha1"
val quillVersion = "4.8.6"
val tapirVersion = "1.10.8"
val testContainerVersion = "0.41.8"
val zioConfigVersion = "4.0.3"
val zioHttpVersion = "3.0.1"
val zioJsonVersion = "0.7.14"
val zioVersion = "2.1.14"

lazy val commonSettings = Seq(
  organization     := "net.leibman",
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
    name             := "dmscreen-common",
    buildInfoPackage := "dmscreen",
    commonSettings
  )
  .jvmEnablePlugins(GitVersioning, BuildInfoPlugin)
  .jvmSettings(
    version := gitDescribedVersion.value.getOrElse("0.0.1-SNAPSHOT"),
    libraryDependencies ++= Seq(
      "dev.zio"     %% "zio"                   % zioVersion withSources (),
      "dev.zio"     %% "zio-nio"               % "2.0.2" withSources (),
      "dev.zio"     %% "zio-config"            % zioConfigVersion withSources (),
      "dev.zio"     %% "zio-config-derivation" % zioConfigVersion withSources (),
      "dev.zio"     %% "zio-config-magnolia"   % zioConfigVersion withSources (),
      "dev.zio"     %% "zio-config-typesafe"   % zioConfigVersion withSources (),
      "dev.zio"     %% "zio-json"              % zioJsonVersion withSources (),
      "dev.zio"     %% "zio-prelude"           % "1.0.0-RC37" withSources (),
      "io.getquill" %% "quill-jdbc-zio"        % quillVersion withSources (),
//      "org.gnieh"   %% "diffson-core"          % "4.6.0" withSources (),
//      "io.megl"     %% "zio-json-diffson"      % "0.6.2" withSources (),
//      "io.megl"     %% "zio-json-extra"        % "0.6.2" withSources (),
      "io.kevinlee" %% "just-semver-core"      % "1.1.0" withSources ()
    )
  )
  .jsEnablePlugins(GitVersioning, BuildInfoPlugin)
  .jsSettings(
    version := gitDescribedVersion.value.getOrElse("0.0.1-SNAPSHOT"),
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio"                                                 % zioVersion withSources (),
      "dev.zio" %%% "zio-json"                                            % zioJsonVersion withSources (),
      "dev.zio" %%% "zio-prelude"                                         % "1.0.0-RC37" withSources (),
//      "org.gnieh" %%% "diffson-core"                                      % "4.6.0" withSources (),
//      "io.megl" %%% "zio-json-extra"                                      % "0.6.2" withSources (),
//      "io.megl" %%% "zio-json-diffson"                                    % "0.6.2" withSources (),
      "io.kevinlee" %%% "just-semver-core"                                % "1.1.0" withSources (),
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"   % "2.33.1",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros" % "2.33.1"
    )
  )

lazy val db = project
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .dependsOn(commonJVM)
  .settings(
    name := "dmscreen-db",
    libraryDependencies ++= Seq(
      // DB
      "org.mariadb.jdbc" % "mariadb-java-client" % "3.5.1" withSources (),
      "io.getquill"     %% "quill-jdbc-zio"      % quillVersion withSources (),
      // Log
      "ch.qos.logback" % "logback-classic" % "1.5.16" withSources (),
      // ZIO
      "dev.zio"                %% "zio"                   % zioVersion withSources (),
      "dev.zio"                %% "zio-nio"               % "2.0.2" withSources (),
      "dev.zio"                %% "zio-cache"             % "0.2.3" withSources (),
      "dev.zio"                %% "zio-config"            % zioConfigVersion withSources (),
      "dev.zio"                %% "zio-config-derivation" % zioConfigVersion withSources (),
      "dev.zio"                %% "zio-config-magnolia"   % zioConfigVersion withSources (),
      "dev.zio"                %% "zio-config-typesafe"   % zioConfigVersion withSources (),
      "dev.zio"                %% "zio-logging-slf4j2"    % "2.4.0" withSources (),
      "dev.zio"                %% "izumi-reflect"         % "2.3.10" withSources (),
      "dev.zio"                %% "zio-json"              % zioJsonVersion withSources (),
      "org.scala-lang.modules" %% "scala-xml"             % "2.3.0" withSources (),
      // Other random utilities
      "com.github.pathikrit"  %% "better-files"                 % "3.9.2" withSources (),
      "com.github.daddykotex" %% "courier"                      % "3.2.0" withSources (),
      "commons-codec"          % "commons-codec"                % "1.18.0",
      "com.dimafeng"          %% "testcontainers-scala-mariadb" % testContainerVersion withSources (),
      // Testing
      "dev.zio" %% "zio-test"     % zioVersion % "test" withSources (),
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test" withSources ()
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
    SystemdPlugin,
    CalibanPlugin
  )
  .settings(debianSettings, commonSettings)
  .dependsOn(commonJVM, db, ai)
  .settings(
    name := "dmscreen-server",
    libraryDependencies ++= Seq(
      // DB
      "org.mariadb.jdbc" % "mariadb-java-client" % "3.5.1" withSources (),
      "io.getquill"     %% "quill-jdbc-zio"      % quillVersion withSources (),
      // Log
      "ch.qos.logback" % "logback-classic" % "1.5.16" withSources (),
      // ZIO
      "dev.zio"                %% "zio"                   % zioVersion withSources (),
      "dev.zio"                %% "zio-nio"               % "2.0.2" withSources (),
      "dev.zio"                %% "zio-cache"             % "0.2.3" withSources (),
      "dev.zio"                %% "zio-config"            % zioConfigVersion withSources (),
      "dev.zio"                %% "zio-config-derivation" % zioConfigVersion withSources (),
      "dev.zio"                %% "zio-config-magnolia"   % zioConfigVersion withSources (),
      "dev.zio"                %% "zio-config-typesafe"   % zioConfigVersion withSources (),
      "dev.zio"                %% "zio-logging-slf4j2"    % "2.4.0" withSources (),
      "dev.zio"                %% "izumi-reflect"         % "3.0.1" withSources (),
      "com.github.ghostdogpr"  %% "caliban"               % calibanVersion withSources (),
      "com.github.ghostdogpr"  %% "caliban-zio-http"      % calibanVersion withSources (),
      "com.github.ghostdogpr"  %% "caliban-quick"         % calibanVersion withSources (),
      "dev.zio"                %% "zio-http"              % zioHttpVersion withSources (),
      "com.github.jwt-scala"   %% "jwt-circe"             % "10.0.1" withSources (),
      "dev.zio"                %% "zio-json"              % zioJsonVersion withSources (),
      "org.scala-lang.modules" %% "scala-xml"             % "2.3.0" withSources (),
      // Other random utilities
      "com.github.pathikrit"  %% "better-files"                 % "3.9.2" withSources (),
      "com.github.daddykotex" %% "courier"                      % "3.2.0" withSources (),
      "commons-codec"          % "commons-codec"                % "1.18.0",
      "com.dimafeng"          %% "testcontainers-scala-mariadb" % testContainerVersion withSources (),
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
// AI
lazy val ai = project
  .enablePlugins(
    AutomateHeaderPlugin,
    GitVersioning
  )
  .settings(commonSettings)
  .dependsOn(commonJVM, db)
  .settings(
    name := "dmscreen-ai",
    libraryDependencies ++= Seq(
      // ZIO
      "dev.zio" %% "zio"     % zioVersion withSources (),
      "dev.zio" %% "zio-nio" % "2.0.2" withSources (),
      // AI stuff
      "com.dimafeng" %% "testcontainers-scala-core" % testContainerVersion withSources(),
      "org.testcontainers" % "qdrant" % "1.20.4" withSources(),
      "dev.langchain4j" % "langchain4j-core" % langchainVersion withSources(),
      "dev.langchain4j" % "langchain4j" % langchainVersion withSources(),
      "dev.langchain4j" % "langchain4j-ollama" % langchainVersion withSources(),
      "dev.langchain4j" % "langchain4j-easy-rag" % langchainVersion withSources(),
      "dev.langchain4j" % "langchain4j-qdrant" % langchainVersion withSources(),
      // Other random utilities
      "com.github.pathikrit"  %% "better-files"    % "3.9.2" withSources (),
      "com.github.daddykotex" %% "courier"         % "3.2.0" withSources (),
      "ch.qos.logback"         % "logback-classic" % "1.5.16" withSources (),
      // Testing
      "dev.zio" %% "zio-test"     % zioVersion % "test" withSources (),
      "dev.zio" %% "zio-test-sbt" % zioVersion % "test" withSources ()
    )
  )

////////////////////////////////////////////////////////////////////////////////////
// Web
val scalajsReactVersion = "2.1.2"

lazy val bundlerSettings: Project => Project =
  _.enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      webpack / version := "5.96.1",
      Compile / fastOptJS / artifactPath := ((Compile / fastOptJS / crossTarget).value /
        ((fastOptJS / moduleName).value + "-opt.js")),
      Compile / fullOptJS / artifactPath := ((Compile / fullOptJS / crossTarget).value /
        ((fullOptJS / moduleName).value + "-opt.js")),
      useYarn                                   := true,
      run / fork                                := true,
      Global / scalaJSStage                     := FastOptStage,
      Compile / scalaJSUseMainModuleInitializer := true,
      Test / scalaJSUseMainModuleInitializer    := false,
      webpackEmitSourceMaps                     := false,
      scalaJSLinkerConfig ~= { a =>
        a.withSourceMap(true) // .withRelativizeSourceMapBase(None)
      },
      Compile / npmDependencies ++= Seq(
        "@3d-dice/dice-ui"               -> "^0.5.0",
        "@3d-dice/dice-parser-interface" -> "^0.2.0",
        "@3d-dice/dice-box"              -> "^1.1.0",
        "@3d-dice/theme-rust"            -> "^0.2.0",
        "babylonjs-gltf2interface"       -> "^5.22.0"
      )
    )

lazy val withCssLoading: Project => Project =
  _.settings(
    /* custom webpack file to include css */
    webpackConfigFile := Some((ThisBuild / baseDirectory).value / "custom.webpack.config.js"),
    Compile / npmDevDependencies ++= Seq(
      "webpack-merge" -> "6.0.1",
      "css-loader"    -> "7.1.2",
      "style-loader"  -> "4.0.0",
      "file-loader"   -> "6.2.0",
      "url-loader"    -> "4.1.1"
    )
  )

lazy val commonWeb: Project => Project =
  _.settings(
    libraryDependencies ++= Seq(
      "net.leibman" %%% "dmscreen-stlib"              % "0.8.0-SNAPSHOT" withSources (),
      "com.github.ghostdogpr" %%% "caliban-client"    % calibanVersion withSources (),
      "dev.zio" %%% "zio"                             % zioVersion withSources (),
      "com.softwaremill.sttp.client3" %%% "core"      % "3.10.3" withSources (),
      "io.github.cquiroz" %%% "scala-java-time"       % "2.6.0" withSources (),
      "io.github.cquiroz" %%% "scala-java-time-tzdb"  % "2.6.0" withSources (),
      "org.scala-js" %%% "scalajs-dom"                % "2.8.0" withSources (),
      "com.olvind" %%% "scalablytyped-runtime"        % "2.4.2",
      "com.github.japgolly.scalajs-react" %%% "core"  % scalajsReactVersion withSources (),
      "com.github.japgolly.scalajs-react" %%% "extra" % scalajsReactVersion withSources (),
      "com.lihaoyi" %%% "scalatags"                   % "0.13.1" withSources (),
      "com.github.japgolly.scalacss" %%% "core"       % "1.0.0" withSources (),
      "com.github.japgolly.scalacss" %%% "ext-react"  % "1.0.0" withSources ()
      //      ("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0").cross(CrossVersion.for3Use2_13)
    ),
    organizationName := "Roberto Leibman",
    startYear        := Some(2024),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories    := Seq((Test / scalaSource).value)
    //    webpackDevServerPort                 := 8009
  )

lazy val web: Project = project
  .dependsOn(commonJS)
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
      "dev.zio" %%% "zio-json" % zioJsonVersion withSources ()
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

//        println(s"Trying to copy ${artifact.data.toPath} to ${target.toPath}")
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

//        println(s"Trying to copy ${artifact.data.toPath} to ${target.toPath}")
        Files.copy(artifact.data.toPath, target.toPath, REPLACE_EXISTING)
      }

      distFolder
    }
  )

//////////////////////////////////////////////////////////////////////////////////////////////////
// Root project
lazy val root = project
  .in(file("."))
  .aggregate(commonJVM, commonJS, server, web, ai)
  .settings(
    name           := "dmscreen",
    publish / skip := true,
    version        := "0.1.0",
    headerLicense  := None
  )
