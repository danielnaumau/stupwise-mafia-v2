package stupwise.lobby

import cats._
import cats.syntax.all._
import dev.profunktor.redis4cats.RedisCommands
import io.circe.{Decoder, Encoder}
import io.circe.parser.{decode => jsonDecode}
import stupwise.common.models.State
import io.circe.syntax.EncoderOps

final class StateStore[F[_]: Monad, S <: State: Decoder: Encoder](redis: RedisCommands[F, String, String]) {
  def set(state: S): F[Boolean] =
    redis.setNx(state.key, state.asJson.noSpaces)

  def latest(keyPattern: String): F[Option[S]] =
    for {
      allKeys <- redis.keys(keyPattern)
      values  <- redis.mGet(allKeys.toSet).map(_.values)
      result   = values.flatMap(jsonDecode[S](_).toOption).toList.sortBy(_.version).lastOption
    } yield result

  // toDo: make it tailrec
  def updateState(keyPattern: String)(f: S => S): F[Option[S]] = {
    val saved = for {
      latestState <- latest(keyPattern)
      newState     = latestState.map(f)
      res         <- newState.traverse(set).map(_.getOrElse(false))
    } yield (res, newState)

    saved.flatMap { case (res, newState) =>
      if (res) {
        newState.pure[F]
      } else {
        updateState(keyPattern)(f)
      }
    }
  }
}
