package stupwise.websocket

import cats.effect.Concurrent
import cats.implicits._
import fs2.concurrent.Topic
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import Protocol.{InMessage, OutMessage}

final class WebsocketRoutes[F[_]: Concurrent: GenUUID](
  topic: Topic[F, OutMessage],
  publish: fs2.Stream[F, OutMessage] => F[Unit],
  wsb: WebSocketBuilder2[F]
)(implicit outEncoder: Encoder[OutMessage], inDecoder: Decoder[InMessage])
    extends Http4sDsl[F] {
  val routes: HttpRoutes[F] = HttpRoutes.of { case GET -> Root / "v1" / "ws" =>
    Handler.make[F](topic, publish).flatMap { h =>
      wsb.build(h.send, h.receive)
    }
  }
}
