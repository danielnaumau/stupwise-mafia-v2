package stupwise.websocket

import cats.effect.{ExitCode, IO, IOApp}
import fs2.concurrent.Topic
import org.http4s.server.websocket.WebSocketBuilder2
import stupwise.websocket.GenUUIDInstances._
import stupwise.websocket.Protocol.OutcomeMessage

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      topic    <- Topic[IO, OutcomeMessage]
      exitCode <- HttpServer.makeWebsocket((wb: WebSocketBuilder2[IO]) => new WebsocketRoutes(topic, wb).routes, 8080)
    } yield exitCode

}
