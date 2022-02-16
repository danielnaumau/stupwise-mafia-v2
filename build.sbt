import Dependencies.Libraries

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.8"

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:higherKinds",
  "-Ywarn-unused:imports,locals"
)

lazy val `kind-projector` = "org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full

val commonSettings = List(
  libraryDependencies ++= Seq(
    Libraries.circeCore,
    Libraries.circeParser,
    Libraries.circeExtras,
    Libraries.http4sDsl,
    Libraries.http4sServer,
    Libraries.pureConfig
  )
)

lazy val common = (project in file("modules/common"))
  .enablePlugins(DockerPlugin)
  .settings(commonSettings: _*)
  .settings(
    addCompilerPlugin(`kind-projector`),
    libraryDependencies ++= Seq(Libraries.fs2Kafka, Libraries.catsEffect)
  )

lazy val websocket = (project in file("modules/websocket"))
  .dependsOn(common)
  .enablePlugins(DockerPlugin)
