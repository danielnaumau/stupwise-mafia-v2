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
import stupwise.common.models.KafkaMsg
import stupwise.common.models.KafkaMsg.CustomError
import stupwise.websocket.Protocol.{InMessage, OutMessage}

trait Handler[F[_]] {
  def send: Stream[F, Text]
  def receive(wsfStream: Stream[F, WebSocketFrame]): Stream[F, Unit]
}

object Handler {
  def make[F[_]: GenUUID: Sync: Logger](
    topic: Topic[F, OutMessage],
    publish: fs2.Stream[F, KafkaMsg] => F[Unit]
  )(implicit outEncoder: Encoder[OutMessage], inDecoder: Decoder[InMessage]): F[Handler[F]] = GenUUID[F].generate.map {
    playerId =>
      new Handler[F] {
        def send: Stream[F, Text] =
          topic
            .subscribe(1000)
            .filter(_.playerId == playerId)
            .map(msg => Text(WSCodecs.encode(msg)))

        def decode(frame: WebSocketFrame): F[List[KafkaMsg]] = frame match {
          case Close(_)     => List[KafkaMsg](CustomError(playerId, "socket closed")).pure[F]
          case Text(msg, _) =>
            WSCodecs
              .decode(msg)
              .fold(
                err => List[KafkaMsg](CustomError(playerId, err.getMessage)).pure[F],
                msg => Dispatcher.dispatch[F](playerId, msg)
              )
          case e            => List[KafkaMsg](CustomError(playerId, s" Unexpected WS message: $e")).pure[F]
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
