package stupwise.lobby

import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.NoOp._
import stupwise.common.GenUUIDInstances._
import fs2.kafka.ConsumerRecord
import stupwise.common.Codecs
import stupwise.common.kafka.{KafkaComponents, LogComponents}
import stupwise.common.models.KafkaMsg.{Event, LobbyCommand}
import stupwise.common.models.State.RoomState
import stupwise.common.models.StateStore

object Main extends IOApp with KafkaComponents with Codecs with LogComponents {

  override def run(args: List[String]): IO[ExitCode] =
    Redis[IO]
      .utf8("redis://localhost:6379")
      .map(new StateStore[IO, RoomState](_))
      .use { stateStore =>
        val handler     = LobbyHandler(stateStore)
        val eventStream = subscribe(kafkaConfiguration.topics.commands, processRecord(handler))
        publish(kafkaConfiguration.topics.gameEvents, eventStream)
      }
      .as(ExitCode.Success)

  def processRecord(handler: LobbyHandler[IO])(record: ConsumerRecord[Unit, LobbyCommand]): IO[Event] =
    handler.handle(record.value)

}
