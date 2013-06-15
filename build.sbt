name := "activator-akka-spray"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-actor"       % "2.2.0-RC1",
  "com.typesafe.akka"  %% "akka-slf4j"       % "2.2.0-RC1",
  "ch.qos.logback"      % "logback-classic"  % "1.0.13",
  "io.spray"            % "spray-can"        % "1.2-M8",
  "io.spray"            % "spray-routing"    % "1.2-M8",
  "io.spray"           %% "spray-json"       % "1.2.3",
  "org.specs2"         %% "specs2"           % "1.14"      % "test",
  "io.spray"            % "spray-testkit"    % "1.2-M8"    % "test",
  "com.typesafe.akka"  %% "akka-testkit"     % "2.2.0-RC1" % "test",
  "com.novocode"        % "junit-interface"  % "0.7"       % "test->default"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

// Note: These settings are defaults for Activator but can be changed.
//Seq(
//  scalaSource in Compile <<= baseDirectory / "app",
//  javaSource in Compile <<= baseDirectory / "app",
//  sourceDirectory in Compile <<= baseDirectory / "app",
//  scalaSource in Test <<= baseDirectory / "test",
//  javaSource in Test <<= baseDirectory / "test",
//  sourceDirectory in Test <<= baseDirectory / "test",
//  resourceDirectory in Compile <<= baseDirectory / "conf"
//)
