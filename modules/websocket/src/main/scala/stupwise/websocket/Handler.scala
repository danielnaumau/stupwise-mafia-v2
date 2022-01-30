package stupwise.websocket

import cats.effect.Concurrent
import cats.implicits._
import fs2._
import fs2.concurrent.Topic
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import stupwise.websocket.Protocol.OutcomeMessage
import stupwise.websocket.Protocol.OutcomeMessage.{DecodingError, SocketClosed}

trait Handler[F[_]] {
  def send: Stream[F, Text]
  def receive(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit]
}

object Handler {
  def make[F[_]: Concurrent: GenUUID](
    topic: Topic[F, OutcomeMessage]
  ): F[Handler[F]] = GenUUID[F].generate.map { playerId =>
    new Handler[F] {
      def send: Stream[F, Text] =
        topic
          .subscribe(1000)
          .filter(_.playerId == playerId)
          .map(msg => Text(Codecs.encode(msg)))

      def decode(frame: WebSocketFrame): F[List[OutcomeMessage]] = frame match {
        case Close(_)     => List[OutcomeMessage](SocketClosed(playerId)).pure[F]
        case Text(msg, _) =>
          Codecs
            .decode(msg)
            .fold(
              err => List[OutcomeMessage](DecodingError(playerId, err.getMessage)).pure[F],
              msg => Dispatcher.dispatch[F](playerId, msg)
            )
        case e            => List[OutcomeMessage](DecodingError(playerId, s" Unexpected WS message: $e")).pure[F]
      }

      def receive(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit] =
        wsfStream
          .evalMap(decode)
          .flatMap(Stream.emits)
          .through(topic.publish)
    }
  }
}
