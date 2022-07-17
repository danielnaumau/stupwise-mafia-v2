package stupwise.common.redis

import cats._
import cats.syntax.all._
import dev.profunktor.redis4cats.RedisCommands
import io.circe.parser.{decode => jsonDecode}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import stupwise.common.models.{Reason, State}

final class StateStore[F[_]: Monad, S <: State: Decoder: Encoder](redis: RedisCommands[F, String, String]) {
  def set(state: S): F[Boolean] =
    redis.setNx(state.key, state.asJson.noSpaces)

  def latest(keyPattern: String): F[Option[S]] =
    for {
      allKeys <- redis.keys(keyPattern)
      values  <- if (allKeys.isEmpty) List.empty.pure[F] else redis.mGet(allKeys.toSet).map(_.values)
      result   = values.flatMap(jsonDecode[S](_).toOption).toList.sortBy(_.version).lastOption
    } yield result

  // toDo: make it tailrec
  def updateState(keyPattern: String)(f: S => Either[Reason, S]): F[Either[Reason, S]] = {
    val saved = for {
      latestState <- latest(keyPattern)
      newState     = latestState.map(f).getOrElse(Left(Reason("Error")))
      res         <- newState.traverse(set)
    } yield (res, newState)

    saved.flatMap { case (res, newState) =>
      val state: F[Either[Reason, S]] = res match {
        case Left(value)           => Left(value).asInstanceOf[Either[Reason, S]].pure[F]
        case Right(value) if value => newState.pure[F]
        case _                     => updateState(keyPattern)(f)
      }
      state
    }
  }
}
