////////////////////////////////////////////////////////////////////////////////////
// Common stuff
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"                   % "1.0.2")
addSbtPlugin("de.heikoseeberger" % "sbt-header"                % "5.10.0")
addSbtPlugin("com.github.sbt"    % "sbt-native-packager"       % "1.10.0")
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"             % "0.12.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"              % "2.5.2")
addSbtPlugin("com.github.cb372"  % "sbt-explicit-dependencies" % "0.3.1")
addSbtPlugin("com.typesafe"      % "sbt-mima-plugin"           % "1.1.3")

////////////////////////////////////////////////////////////////////////////////////
// Server
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

////////////////////////////////////////////////////////////////////////////////////
// Web client
addSbtPlugin("org.scala-js"                % "sbt-scalajs"              % "1.16.0")
addSbtPlugin("ch.epfl.scala"               % "sbt-scalafix"             % "0.12.1")
addSbtPlugin("com.github.ghostdogpr"       % "caliban-codegen-sbt"      % "2.6.0")
addSbtPlugin("org.portable-scala"          % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter"            % "1.0.0-beta44")

////////////////////////////////////////////////////////////////////////////////////
// Testing
addSbtPlugin("io.stryker-mutator" % "sbt-stryker4s" % "0.16.1")
addSbtPlugin("org.scoverage"      % "sbt-scoverage" % "2.0.12")
