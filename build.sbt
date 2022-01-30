import Dependencies.Libraries

name := "stupwise-mafia-v2"

version := "0.1"

scalaVersion := "2.13.8"

val commonSettings = List(
  libraryDependencies ++= Seq(
    Libraries.circeCore,
    Libraries.circeParser,
    Libraries.circeExtras,
    Libraries.http4sDsl,
    Libraries.http4sServer,
  )
)

lazy val websocket = (project in file("modules/websocket"))
  .enablePlugins(DockerPlugin)
  .settings(commonSettings: _*)