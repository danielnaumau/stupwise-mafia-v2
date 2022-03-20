import Dependencies.Libraries

ThisBuild / version := git.gitHeadCommit.value.getOrElse("0.1").take(7)
ThisBuild / scalaVersion := "2.13.8"

ThisBuild / organization := "stupwise"
ThisBuild / organizationName := "stupwise"

ThisBuild / publishTo := Some(Resolver.file("file", new File("/tmp/my/artifactory")))

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ywarn-unused:imports,locals"
)

def dockerSettings(name: String) = List(
  Docker / packageName := s"mafia-$name",
  Docker / dockerRepository := Some("registry.digitalocean.com/stupwise")
)

lazy val `kind-projector` = "org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full

val commonSettings = List(
  libraryDependencies ++= Seq(
    Libraries.circeCore,
    Libraries.circeParser,
    Libraries.circeExtras,
    Libraries.http4sDsl,
    Libraries.http4sServer,
    Libraries.pureConfig,
    Libraries.logback,
    Libraries.slf4jCats,
    Libraries.enumeratumCirce,
    Libraries.enumeratum
  )
)

lazy val common = (project in file("modules/common"))
  .settings(commonSettings: _*)
  .settings(
    addCompilerPlugin(`kind-projector`),
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      Libraries.fs2Kafka,
      Libraries.catsEffect,
      Libraries.newType,
      Libraries.redis4catsEffects
    )
  )

lazy val websocket = (project in file("modules/websocket"))
  .dependsOn(engine)
  .enablePlugins(JavaAppPackaging)
  .settings(dockerSettings("websocket"))

lazy val lobby = (project in file("modules/lobby"))
  .dependsOn(common)
  .enablePlugins(JavaAppPackaging)
  .settings(dockerSettings("lobby"))

lazy val gameCore = (project in file("modules/game-core"))
  .dependsOn(common, classicMafia)
  .enablePlugins(JavaAppPackaging)
  .settings(dockerSettings("gameCore"))

lazy val classicMafia = (project in file("modules/classic-mafia"))
  .dependsOn(common)
  .enablePlugins(JavaAppPackaging)
  .settings(dockerSettings("classicMafia"))

lazy val engine = (project in file("modules/engine"))
  .dependsOn(common)
  .enablePlugins(JavaAppPackaging)
  .settings(dockerSettings("engine"))

lazy val root = (project in file("."))
  .settings(
    name := "stupwise-mafia-app"
  )
  .aggregate(websocket, lobby, common, gameCore, classicMafia, engine)
