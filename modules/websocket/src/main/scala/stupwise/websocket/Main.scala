package stupwise.websocket

import cats.effect.{ExitCode, IO, IOApp}
import fs2.concurrent.Topic
import org.http4s.server.websocket.WebSocketBuilder2
import stupwise.common.Engine
import stupwise.common.GenUUIDInstances._
import stupwise.common.kafka.{Consumer, Fs2KafkaComponent, LogComponents, Producer}
import stupwise.common.models.KafkaMsg.{GameCommand, GameEvent, LobbyCommand, LobbyEvent}
import stupwise.websocket.Protocol.{InMessage, OutMessage}
import stupwise.websocket.eventProcessors.{GameEventProcessor, LobbyEventProcessor}

object Main extends IOApp with Fs2KafkaComponent with WSCodecs with LogComponents {

  def run(args: List[String]): IO[ExitCode] =
    (for {
      topic              <- fs2.Stream.eval(Topic[IO, OutMessage])
      lobbyProducer      <- Producer.kafka[IO, LobbyCommand](kafkaConfig.settings, kafkaConfig.topics.lobbyCommands)
      gameProducer       <- Producer.kafka[IO, GameCommand](kafkaConfig.settings, kafkaConfig.topics.gameCommands)
      lobbyConsumer      <- Consumer.kafka[IO, LobbyEvent](kafkaConfig.settings, kafkaConfig.topics.lobbyEvents)
      gameConsumer       <- Consumer.kafka[IO, GameEvent](kafkaConfig.settings, kafkaConfig.topics.gameEvents)
      gameEventProcessor  = new GameEventProcessor.Live[IO]
      lobbyEventProcessor = new LobbyEventProcessor.Live[IO]
      engine              = new Engine.Live[IO](lobbyProducer, gameProducer)
      dispatcher          = new Dispatcher.Live[IO](engine)
      processGame         = gameConsumer.receive().evalMap(gameEventProcessor.process)
      processLobby        = lobbyConsumer.receive().evalMap(lobbyEventProcessor.process)
      publishLobbyWs      = processLobby.flatMap(fs2.Stream.emits).through(topic.publish)
      publishGameWs       = processGame.flatMap(fs2.Stream.emits).through(topic.publish)
      websocket           =
        HttpServer
          .makeWebsocket(
            (wb: WebSocketBuilder2[IO]) =>
              new WebsocketRoutes[IO, InMessage, OutMessage](
                topic,
                dispatcher,
                wb
              ).routes,
            8080
          )
    } yield websocket concurrently publishLobbyWs concurrently publishGameWs)
      .parJoin(10)
      .compile
      .drain
      .as(ExitCode.Success)

}
