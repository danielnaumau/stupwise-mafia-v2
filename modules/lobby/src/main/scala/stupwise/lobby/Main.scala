package stupwise.lobby

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._
import stupwise.lobby.Models.{RoomState, Player}

import java.util.UUID

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val redis = Redis[IO].utf8("redis://localhost:6379")


    val r = redis
      .map(new StateStore[IO](_))
      .use { store =>
        for {
          test <- store.set(RoomState(List(Player(UUID.randomUUID(), "test")), 8, "rew242s"))
          _    <- store.set(RoomState(List(Player(UUID.randomUUID(), "test")), 2, "rew242s"))
          res <- store.latest("state-lobby-rew242s-*")
          _  <- println(test).pure[IO]
          _  <- println(res).pure[IO]
        } yield ()
      }
    r.as(ExitCode.Success)
  }

}
