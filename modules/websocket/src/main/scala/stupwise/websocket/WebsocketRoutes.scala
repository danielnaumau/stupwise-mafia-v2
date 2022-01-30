package stupwise.websocket

import cats.effect.Concurrent
import cats.implicits._
import fs2.concurrent.Topic
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import stupwise.websocket.Protocol.OutcomeMessage


final class WebsocketRoutes[F[_]: Concurrent: GenUUID](topic: Topic[F, OutcomeMessage], wsb: WebSocketBuilder2[F]) extends Http4sDsl[F] {
  val routes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "v1" / "ws" =>
      Handler.make[F](topic).flatMap { h =>
        wsb.build(h.send, h.receive)
      }
  }
}
