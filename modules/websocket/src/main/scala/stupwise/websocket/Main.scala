package stupwise.websocket

import cats.effect.{ExitCode, IO, IOApp}
import fs2.concurrent.Topic
import org.http4s.server.websocket.WebSocketBuilder2
import stupwise.common.GenUUIDInstances._
import stupwise.common.kafka.{Consumer, Fs2KafkaComponent, LogComponents, Producer}
import stupwise.common.models.Alert
import stupwise.common.models.KafkaMsg.LobbyCommand
import stupwise.engine.Engine
import stupwise.websocket.Protocol.{InMessage, OutMessage}

object Main extends IOApp with Fs2KafkaComponent with WSCodecs with LogComponents {

  def run(args: List[String]): IO[ExitCode] =
    (for {
      topic         <- fs2.Stream.eval(Topic[IO, OutMessage])
      lobbyProducer <- Producer.kafka[IO, LobbyCommand](kafkaConfig.settings, kafkaConfig.topics.lobbyCommands)
      alertConsumer <- Consumer.kafka[IO, Alert](kafkaConfig.settings, kafkaConfig.topics.alerts)
      alertProcessor = new AlertProcessor.Live[IO]
      engine         = new Engine.Live[IO](lobbyProducer)
      dispatcher     = new Dispatcher.Live[IO](engine)
      wsOutPublisher =
        alertConsumer.receive().evalMap(alertProcessor.process).flatMap(fs2.Stream.emits).through(topic.publish)
      websocket      =
        HttpServer
          .makeWebsocket(
            (wb: WebSocketBuilder2[IO]) =>
              new WebsocketRoutes[IO, InMessage, OutMessage](
                topic,
                engine,
                dispatcher,
                wb
              ).routes,
            8080
          )
    } yield websocket concurrently wsOutPublisher)
      .parJoin(10)
      .compile
      .drain
      .as(ExitCode.Success)

}
