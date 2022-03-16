package stupwise.lobby

import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.redis4cats.effect.Log.NoOp._
import stupwise.common.GenUUIDInstances._
import fs2.kafka.ConsumerRecord
import stupwise.common.Codecs
import stupwise.common.kafka.{KafkaComponents, LogComponents}
import stupwise.common.models.KafkaMsg.{Event, LobbyCommand}
import stupwise.common.models.State.RoomState
import stupwise.common.redis.RedisStateStore

object Main extends IOApp with KafkaComponents with Codecs with LogComponents with RedisStateStore {
  override def run(args: List[String]): IO[ExitCode] =
    stateStore[IO, RoomState].use { store =>
      val handler     = LobbyHandler(store)
      val eventStream = subscribe(kafkaConfig.topics.commands, processRecord(handler))
      eventStream.map {
        case event: KafkaMsg.Event => publish(kafkaConfig.topics.gameEvents, fs2.Stream(event))
        case other                 => publish(kafkaConfiguration.topics.commands, fs2.Stream(other))
      }.compile.drain
    }
      .as(ExitCode.Success)

  def processRecord(handler: LobbyHandler[IO])(record: ConsumerRecord[Unit, LobbyCommand]): IO[KafkaMsg] =
    handler.handle(record.value)
}
