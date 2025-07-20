import com.scalapenos.sbt.prompt.SbtPrompt.autoImport.*
import com.scalapenos.sbt.prompt.*
import com.typesafe.sbt.packager.docker.*

import Dependencies.*

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion   := "3.7.1"
ThisBuild / version        := "0.1.0-SNAPSHOT"
ThisBuild / organization   := "com.gvolpe"
ThisBuild / scalacOptions ++= Seq("-new-syntax", "-rewrite")

// Compile / run / fork := true

promptTheme := PromptTheme(
  List(
    text("[sbt] ", fg(105)),
    text(_ => "fsm-streams", fg(15)).padRight(" λ ")
  )
)

val nixDockerSettings = List(
  name           := "sbt-nix-fsm-streams",
  dockerCommands := Seq(
    Cmd("FROM", "base-jre:latest"),
    Cmd("COPY", "1/opt/docker/lib/*.jar", "/lib/"),
    Cmd("COPY", "2/opt/docker/lib/*.jar", "/app.jar"),
    ExecCmd("ENTRYPOINT", "java", "-cp", "/app.jar:/lib/*", "com.gvolpe.fsm-streams.Hello")
  )
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    name := "fsm-streams",
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    // scalacOptions            += "-Ymacro-annotations",
    testFrameworks += new TestFramework("munit.Framework"),
    Test / parallelExecution := false,
    Compile / run / fork := true,
    libraryDependencies ++= Seq(
      fs2Core,
      monocleCore,
      monocleMacro,
      kittens,
      scalacheckEffect,
      catsLaws % Test,
      disciplineMunit % Test,
      munitCore % Test,
      munitScalaCheck % Test,
      munitCatsEffect % Test,
      scalaCheck % Test,
      scalacheckEffectMunit % Test,
      catsEffecTestkit % Test
    )
  )
  .settings(nixDockerSettings *)
