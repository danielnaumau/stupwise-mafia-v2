package stupwise.gamecore

import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.redis4cats.effect.Log.NoOp._
import stupwise.common.Codecs
import stupwise.common.GenUUIDInstances._
import stupwise.common.kafka.{Consumer, Fs2KafkaComponent, LogComponents, Producer}
import stupwise.common.models.KafkaMsg.{GameCommand, GameEvent}
import stupwise.common.models.State.GameState
import stupwise.common.redis.RedisStateStore

object Main extends IOApp with Fs2KafkaComponent with Codecs with LogComponents with RedisStateStore {
  override def run(args: List[String]): IO[ExitCode] =
    stateStore[IO, GameState].use { store =>
      val handler         = new GameHandler(store)
      val processCommands = for {
        consumer <- Consumer.kafka[IO, GameCommand](kafkaConfig.settings, kafkaConfig.topics.gameCommands)
        producer <- Producer.kafka[IO, GameEvent](kafkaConfig.settings, kafkaConfig.topics.gameEvents)
        receive  <- consumer.receive().evalMap(handler.handle).evalMap(producer.send)
      } yield receive
      processCommands.compile.drain
    }
      .as(ExitCode.Success)
}
