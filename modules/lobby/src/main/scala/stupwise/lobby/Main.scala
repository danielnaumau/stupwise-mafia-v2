package stupwise.lobby

import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.redis4cats.effect.Log.NoOp._
import stupwise.common.Codecs
import stupwise.common.GenUUIDInstances._
import stupwise.common.kafka.{Consumer, Fs2KafkaComponent, LogComponents, Producer}
import stupwise.common.models.KafkaMsg.{GameCommand, LobbyCommand, LobbyEvent}
import stupwise.common.models.State.RoomState
import stupwise.common.redis.RedisStateStore

object Main extends IOApp with Fs2KafkaComponent with Codecs with LogComponents with RedisStateStore {
  override def run(args: List[String]): IO[ExitCode] =
    stateStore[IO, RoomState].use { store =>
      val handler         = CommandHandler(store)
      val processCommands = for {
        consumer     <- Consumer.kafka[IO, LobbyCommand](kafkaConfig.settings, kafkaConfig.topics.lobbyCommands)
        producer     <- Producer.kafka[IO, LobbyEvent](kafkaConfig.settings, kafkaConfig.topics.lobbyEvents)
        gameProducer <- Producer.kafka[IO, GameCommand](kafkaConfig.settings, kafkaConfig.topics.gameCommands)
        res          <- consumer.receive().evalMap(handler.handle(_)(producer, gameProducer))
      } yield res
      processCommands.compile.drain
    }
      .as(ExitCode.Success)
}
