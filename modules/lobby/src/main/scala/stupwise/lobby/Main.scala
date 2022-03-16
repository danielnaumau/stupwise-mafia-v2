package stupwise.lobby

import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.redis4cats.effect.Log.NoOp._
import fs2.kafka.ConsumerRecord
import stupwise.common.Codecs
import stupwise.common.GenUUIDInstances._
import stupwise.common.kafka.{KafkaComponents, LogComponents}
import stupwise.common.models.KafkaMsg
import stupwise.common.models.KafkaMsg.LobbyCommand
import stupwise.common.models.State.RoomState
import stupwise.common.redis.RedisStateStore

object Main extends IOApp with KafkaComponents with Codecs with LogComponents with RedisStateStore {
  override def run(args: List[String]): IO[ExitCode] =
    stateStore[IO, RoomState].use { store =>
      val handler     = LobbyHandler(store)
      val eventStream = subscribe(kafkaConfig.topics.commands, processRecord(handler))
      publish(kafkaConfig.topics.gameEvents, eventStream)
    }
      .as(ExitCode.Success)

  def processRecord(handler: LobbyHandler[IO])(record: ConsumerRecord[Unit, LobbyCommand]): IO[KafkaMsg] =
    handler.handle(record.value)
}
