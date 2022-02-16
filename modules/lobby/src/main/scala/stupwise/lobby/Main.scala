package stupwise.lobby

import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.NoOp.instance
import fs2.kafka.ConsumerRecord
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import stupwise.common.kafka.KafkaComponents
import stupwise.common.models.KafkaMsg.{Command, Event}
import stupwise.lobby.State.RoomState

object Main extends IOApp with KafkaComponents {

  override def run(args: List[String]): IO[ExitCode] = {
    Redis[IO]
      .utf8("redis://localhost:6379")
      .map(new StateStore[IO, RoomState](_))
      .use { stateStore =>
        val handler = LobbyHandler(stateStore)
        val eventStream = subscribe(kafkaConfiguration.topics.commands, processRecord(handler))
        publish("res-topic", eventStream)
      }.as(ExitCode.Success)
  }

  def processRecord(handler: LobbyHandler[IO])(record: ConsumerRecord[Unit, Command]): IO[Event] = {
    handler.handle(record.value)
  }


}
