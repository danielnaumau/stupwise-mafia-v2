package stupwise.websocket

import cats.effect.{ExitCode, IO, IOApp}
import fs2.concurrent.Topic
import org.http4s.server.websocket.WebSocketBuilder2
import stupwise.common.kafka.KafkaComponents
import stupwise.websocket.GenUUIDInstances._
import stupwise.websocket.Protocol.OutMessage

object Main extends IOApp with KafkaComponents with Codecs {
  val gameEventsProcessor = new GameEventsProcessor.Live[IO]

  def run(args: List[String]): IO[ExitCode] =
    (for {
      topic        <- fs2.Stream.eval(Topic[IO, OutMessage])
      eventConsumer = subscribe(kafkaConfiguration.topics.gameEvents, gameEventsProcessor.processRecord)
      kafkaPublish  = (stream: fs2.Stream[IO, OutMessage]) => publish(kafkaConfiguration.topics.commands, stream)
      websocket     = HttpServer
                        .makeWebsocket(
                          (wb: WebSocketBuilder2[IO]) => new WebsocketRoutes(topic, kafkaPublish, wb).routes,
                          8080
                        )
    } yield eventConsumer concurrently websocket).compile.drain.as(ExitCode.Success)

}
