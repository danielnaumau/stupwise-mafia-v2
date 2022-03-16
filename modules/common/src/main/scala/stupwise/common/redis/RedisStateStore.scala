package stupwise.common.redis

import cats.effect.{Async, Resource}
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log
import io.circe.{Decoder, Encoder}
import stupwise.common.AppConfig.RedisConfig
import stupwise.common.{AppConfig, Codecs}
import stupwise.common.models.State

trait RedisStateStore extends Codecs {
  def redisConfig: RedisConfig = AppConfig.load.redis

  def stateStore[F[_]: Async: Log, S <: State: Decoder: Encoder]: Resource[F, StateStore[F, S]] =
    Redis[F]
      .utf8(redisConfig.uri)
      .map(new StateStore[F, S](_))
}
