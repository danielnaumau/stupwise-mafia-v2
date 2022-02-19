import sbt._

object Dependencies {
  object V {
    val http4s     = "0.23.7"
    val circe      = "0.14.1"
    val redis4cats = "1.0.0"
    val cats       = "3.3.5"
    val log4j      = "2.2.0"
    val fs2        = "3.2.4"
    val fs2Kafka   = "3.0.0-M4"
    val pureConfig = "0.17.1"
    val logback    = "1.2.10"
  }

  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"   %% s"circe-$artifact"  % V.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% s"http4s-$artifact" % V.http4s

    val circeCore    = circe("core")
    val circeParser  = circe("parser")
    val circeGeneric = circe("generic")
    val circeExtras  = circe("generic-extras")

    val http4sDsl    = http4s("dsl")
    val http4sServer = http4s("blaze-server")

    val pureConfig = "com.github.pureconfig" %% "pureconfig" % V.pureConfig

    val redis4catsEffects = "dev.profunktor" %% "redis4cats-effects" % V.redis4cats

    val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % V.fs2Kafka
    val fs2Core  = "co.fs2"          %% "fs2-core"  % V.fs2

    val catsEffect = "org.typelevel" %% "cats-effect" % V.cats

    val slf4jCats = "org.typelevel" %% "log4cats-slf4j"  % V.log4j
    val logback   = "ch.qos.logback" % "logback-classic" % V.logback

  }
}
