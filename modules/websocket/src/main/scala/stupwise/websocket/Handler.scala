package stupwise.websocket

import cats.effect.kernel.Sync
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.{Decoder, Encoder}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.typelevel.log4cats.Logger
import stupwise.common.GenUUID
import stupwise.common.models.{PlayerId, Reason}
import stupwise.websocket.Protocol.{InMessage, OutMessage}

trait Handler[F[_]] {
  def send: Stream[F, Text]
  def receive(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit]
}

object Handler {
  def make[F[_]: GenUUID: Sync: Logger, In <: InMessage: Decoder, Out <: OutMessage: Encoder](
    topic: Topic[F, Out],
    dispatcher: Dispatcher[F]
  ): F[Handler[F]] = GenUUID[F].generate.map { playerId =>
    new Handler[F] {
      def send: Stream[F, Text] =
        topic
          .subscribe(1000)
          .filter(_.playerId.value == playerId)
          .map(msg => Text(WSCodecs.encode(msg)))

      def process(frame: WebSocketFrame): F[Unit] = frame match {
        case Close(_)     => ().pure[F]
        case Text(msg, _) =>
          WSCodecs
            .decode(msg)
            .fold(
              e => topic.publish1(OutMessage.Error(PlayerId(playerId), Reason(e.getMessage)).asInstanceOf[Out]).void,
              msg => dispatcher.dispatch(playerId, msg)
            )
        case e            =>
          topic
            .publish1(OutMessage.Error(PlayerId(playerId), Reason(s"Unexpected WS message: $e")).asInstanceOf[Out])
            .void
      }

      def receive(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit] =
        wsfStream.evalMap(process)
    }
  }
}
