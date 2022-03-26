package stupwise.gamecore

import cats.effect.syntax.all._
import cats.effect.{Async, ExitCode}
import cats.implicits.toFunctorOps
import dev.profunktor.redis4cats.effect.Log
import stupwise.common.{Codecs, GenUUID}
import stupwise.common.kafka.{Consumer, Fs2KafkaComponent, LogComponents, Producer}
import stupwise.common.models.KafkaMsg.{GameCommand, GameEvent}
import stupwise.common.models.State.GameState
import stupwise.common.models.game.Game
import stupwise.common.redis.RedisStateStore

trait Runner extends Fs2KafkaComponent with Codecs with LogComponents with RedisStateStore {
  def run[F[_]: Async: Log: GenUUID](game: Game, gameHandler: GameHandler[F]): F[ExitCode] = {
    stateStore[F, GameState].use { store =>
      val commonHandler = new CommonHandler[F](store, game)
      val processCommands = for {
        consumer <- Consumer.kafka[F, GameCommand](kafkaConfig.settings, kafkaConfig.topics.gameCommands)
        producer <- Producer.kafka[F, GameEvent](kafkaConfig.settings, kafkaConfig.topics.gameEvents)
        receive  <- consumer.receive().evalMap(commonHandler.handle.orElse(gameHandler.handle)).evalMap(producer.send)
      } yield receive

      processCommands.compile.drain.as(ExitCode.Success)
    }
  }
}
