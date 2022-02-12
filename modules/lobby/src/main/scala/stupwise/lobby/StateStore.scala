package stupwise.lobby

import cats._
import cats.syntax.all._
import dev.profunktor.redis4cats.RedisCommands
import stupwise.lobby.Models.State
import io.circe.parser.{decode => jsonDecode}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps

final class StateStore[F[_]: FlatMap](redis: RedisCommands[F, String, String]) {
  def set(state: State): F[Boolean] = {
    redis.setNx(state.key, state.asJson.noSpaces)
  }

  def latest(keyPattern: String): F[Option[State]] = {
    for {
      allKeys <- redis.keys(keyPattern)
      values  <- redis.mGet(allKeys.toSet).map(_.values)
      result   = values.flatMap(jsonDecode[State](_).toOption).toList.sortBy(_.version).lastOption
    } yield result
  }



}
