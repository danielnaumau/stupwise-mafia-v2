package stupwise.engine

import cats.effect.{ExitCode, IO, IOApp}
import stupwise.common.Codecs
import stupwise.common.kafka.{Consumer, Fs2KafkaComponent, LogComponents, Producer}
import stupwise.common.models.Alert
import stupwise.common.models.KafkaMsg.{GameEvent, LobbyCommand, LobbyEvent}

object Main extends IOApp with Fs2KafkaComponent with LogComponents with Codecs {

  def run(args: List[String]): IO[ExitCode] =
    (for {
      lobbyProducer      <- Producer.kafka[IO, LobbyCommand](kafkaConfig.settings, kafkaConfig.topics.lobbyCommands)
      alertProducer      <- Producer.kafka[IO, Alert](kafkaConfig.settings, kafkaConfig.topics.alerts)
      gameEventProcessor  = new GameEventProcessor.Live[IO](lobbyProducer, alertProducer)
      lobbyEventProcessor = new LobbyEventProcessor.Live[IO](alertProducer)
      lobbyConsumer      <- Consumer.kafka[IO, LobbyEvent](kafkaConfig.settings, kafkaConfig.topics.lobbyEvents)
      gameConsumer       <- Consumer.kafka[IO, GameEvent](kafkaConfig.settings, kafkaConfig.topics.gameEvents)
      processGame         = gameConsumer.receive().evalMap(gameEventProcessor.process)
      processLobby        = lobbyConsumer.receive().evalMap(lobbyEventProcessor.process)
    } yield processGame concurrently processLobby)
      .parJoin(10)
      .compile
      .drain
      .as(ExitCode.Success)

}
