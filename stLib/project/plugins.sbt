////////////////////////////////////////////////////////////////////////////////////
// Common stuff
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"                   % "1.0.2")
addSbtPlugin("de.heikoseeberger" % "sbt-header"                % "5.10.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"              % "2.5.4")
addSbtPlugin("com.github.cb372"  % "sbt-explicit-dependencies" % "0.3.1")

////////////////////////////////////////////////////////////////////////////////////
// Web client
addSbtPlugin("org.scala-js"                % "sbt-scalajs"              % "1.19.0")
addSbtPlugin("org.portable-scala"          % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter"            % "1.0.0-beta44")

libraryDependencies ++= Seq("org.eclipse.jgit" % "org.eclipse.jgit" % "7.2.0.202503040940-r")
