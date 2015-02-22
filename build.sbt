import sbt._
import Keys._
import spray.revolver.RevolverPlugin.Revolver

name := "activator-akka-spray"

version := "1.0"

scalaVersion := "2.11.5"

//resolvers += "spray repo" at "http://repo.spray.io"
//resolvers += "spray nightlies" at "http://nightlies.spray.io"
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= {
  val akkaVersion  = "2.3.9"
  val sprayVersion = "1.3.2"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion exclude ("org.scala-lang" , "scala-library"),
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
      exclude ("org.slf4j", "slf4j-api")
      exclude ("org.scala-lang" , "scala-library"),
    "ch.qos.logback"    % "logback-classic" % "1.1.2",
    "io.spray"          %% "spray-can" % sprayVersion,
    "io.spray"          %% "spray-routing" % sprayVersion,
    "io.spray"          %% "spray-json" % "1.3.1" exclude ("org.scala-lang" , "scala-library"),
    "org.specs2"        %% "specs2" % "2.4.16" % "test",
    "io.spray"          %% "spray-testkit" % sprayVersion % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.novocode"      %  "junit-interface" % "0.11" % "test->default" exclude("org.hamcrest", "hamcrest-core"),
    "org.scalatest"     %% "scalatest" % "2.2.4" % "test",
    "com.paulhammant"   %  "ngwebdriver" % "0.9.2" % "test" ,
    "com.codahale.metrics"           % "metrics-core" % "3.0.2" % "test",
    "org.hamcrest"                   % "hamcrest-all" % "1.3" % "test",
    "org.seleniumhq.selenium.fluent" % "fluent-selenium" % "1.14.5" % "test",
    "com.github.detro.ghostdriver"   % "phantomjsdriver" % "1.1.0" % "test"
  )
}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

unmanagedResourceDirectories in Compile <++= baseDirectory {
  base => Seq(base / "src/main/angular")
}

Revolver.settings : Seq[sbt.Def.Setting[_]]

crossPaths := false

//conflictManager := ConflictManager.loose

net.virtualvoid.sbt.graph.Plugin.graphSettings
