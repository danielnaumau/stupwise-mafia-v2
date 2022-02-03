import sbt._

object Dependencies {
  object V {
    val http4s = "0.23.7"
    val circe  = "0.14.1"
    val redis4cats = "1.0.0"

  }

  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"   %% s"circe-$artifact"  % V.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% s"http4s-$artifact" % V.http4s

    val circeCore    = circe("core")
    val circeParser  = circe("parser")
    val circeGeneric = circe("generic")
    val circeExtras  = circe("generic-extras")

    val http4sDsl     = http4s("dsl")
    val http4sServer  = http4s("blaze-server")

    val redis4catsEffects = "dev.profunktor" %% "redis4cats-effects" % V.redis4cats
  }
}
