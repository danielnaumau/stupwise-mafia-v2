package stupwise.websocket

import cats.effect.Concurrent
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.{Decoder, Encoder}
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import stupwise.websocket.Protocol.OutMessage.{DecodingError, SocketClosed}
import stupwise.websocket.Protocol.{InMessage, OutMessage}

trait Handler[F[_]] {
  def send: Stream[F, Text]
  def receive(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit]
}

object Handler {
  def make[F[_]: Concurrent: GenUUID](
    topic: Topic[F, OutMessage],
    publish: fs2.Stream[F, OutMessage] => F[Unit]
  )(implicit outEncoder: Encoder[OutMessage], inDecoder: Decoder[InMessage]): F[Handler[F]] = GenUUID[F].generate.map {
    playerId =>
      new Handler[F] {
        def send: Stream[F, Text] =
          topic
            .subscribe(1000)
            .filter(_.playerId == playerId)
            .map(msg => Text(Codecs.encode(msg)))

        def decode(frame: WebSocketFrame): F[List[OutMessage]] = frame match {
          case Close(_)     => List[OutMessage](SocketClosed(playerId)).pure[F]
          case Text(msg, _) =>
            Codecs
              .decode(msg)
              .fold(
                err => List[OutMessage](DecodingError(playerId, err.getMessage)).pure[F],
                msg => Dispatcher.dispatch[F](playerId, msg)
              )
          case e            => List[OutMessage](DecodingError(playerId, s" Unexpected WS message: $e")).pure[F]
        }

        def receive(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit] =
          Stream.eval(
            publish(
              wsfStream
                .evalMap(decode)
                .flatMap(Stream.emits)
            )
          )
      }
  }
}
