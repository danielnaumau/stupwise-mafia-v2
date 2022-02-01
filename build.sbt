import Dependencies.Libraries

ThisBuild / version := "0.1"
ThisBuild / scalaVersion     := "2.13.8"

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:higherKinds",
  "-Ywarn-unused:imports,locals"
)

val commonSettings = List(
  libraryDependencies ++= Seq(
    Libraries.circeCore,
    Libraries.circeParser,
    Libraries.circeExtras,
    Libraries.http4sDsl,
    Libraries.http4sServer
  )
)

lazy val websocket = (project in file("modules/websocket"))
  .enablePlugins(DockerPlugin)
  .settings(commonSettings: _*)
