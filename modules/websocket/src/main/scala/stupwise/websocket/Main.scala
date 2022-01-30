package stupwise.websocket

import cats.effect.{ExitCode, IO, IOApp}
import fs2.concurrent.Topic
import stupwise.websocket.Protocol.OutcomeMessage
import stupwise.websocket.GenUUIDInstances._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      topic    <- Topic[IO, OutcomeMessage]
      exitCode <- HttpServer.makeWebsocket(new WebsocketRoutes[IO](topic, _).routes, 8080)
    } yield exitCode

}
