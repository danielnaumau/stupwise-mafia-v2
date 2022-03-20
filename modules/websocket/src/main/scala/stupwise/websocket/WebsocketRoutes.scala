package stupwise.websocket

import cats.effect.kernel.Sync
import cats.implicits._
import fs2.concurrent.Topic
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.Logger
import stupwise.common.GenUUID
import stupwise.engine.Engine
import stupwise.websocket.Protocol.{InMessage, OutMessage}

final class WebsocketRoutes[F[_]: Sync: GenUUID: Logger, In <: InMessage: Decoder, Out <: OutMessage: Encoder](
  topic: Topic[F, Out],
  engine: Engine[F],
  dispatcher: Dispatcher[F],
  wsb: WebSocketBuilder2[F]
) extends Http4sDsl[F] {
  val routes: HttpRoutes[F] = HttpRoutes.of { case GET -> Root / "v1" / "ws" =>
    Handler.make[F, In, Out](topic, engine, dispatcher).flatMap { h =>
      wsb.build(h.send, h.receive)
    }
  }
}
