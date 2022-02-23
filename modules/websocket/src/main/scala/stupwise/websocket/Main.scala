package stupwise.websocket

import cats.effect.{ExitCode, IO, IOApp}
import fs2.concurrent.Topic
import org.http4s.server.websocket.WebSocketBuilder2
import stupwise.common.kafka.{KafkaComponents, LogComponents}
import stupwise.common.models.KafkaMsg
import stupwise.common.GenUUIDInstances._
import stupwise.websocket.Protocol.OutMessage

object Main extends IOApp with KafkaComponents with WSCodecs with LogComponents {

  def run(args: List[String]): IO[ExitCode] =
    (for {
      topic              <- fs2.Stream.eval(Topic[IO, OutMessage])
      gameEventsProcessor = new GameEventsProcessor.Live[IO]
      eventConsumer       = subscribe(kafkaConfig.topics.gameEvents, gameEventsProcessor.processRecord)
      outPublisher        = eventConsumer.flatMap(fs2.Stream.emits).through(topic.publish)
      kafkaPublish        = (stream: fs2.Stream[IO, KafkaMsg]) => publish(kafkaConfig.topics.commands, stream)
      websocket           = HttpServer
                              .makeWebsocket(
                                (wb: WebSocketBuilder2[IO]) => new WebsocketRoutes(topic, kafkaPublish, wb).routes,
                                8080
                              )
    } yield eventConsumer concurrently websocket concurrently outPublisher)
      .parJoin(10)
      .compile
      .drain
      .as(ExitCode.Success)

}
