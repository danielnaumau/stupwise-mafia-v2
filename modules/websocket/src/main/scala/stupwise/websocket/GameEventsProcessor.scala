package stupwise.websocket

import cats.Applicative
import cats.implicits._
import fs2.kafka.ConsumerRecord
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.models.KafkaMsg
import stupwise.common.models.KafkaMsg.Event
import stupwise.websocket.Protocol.OutMessage
import stupwise.websocket.Protocol.OutMessage._

trait GameEventsProcessor[F[_]] {
  def processRecord(record: ConsumerRecord[Unit, Event]): F[List[OutMessage]]
}

object GameEventsProcessor {
  class Live[F[_]: Applicative: Logger] extends GameEventsProcessor[F] {
    override def processRecord(record: ConsumerRecord[Unit, Event]): F[List[OutMessage]] = {
      val messages = record.value match {
        case KafkaMsg.RoomCreated(_, roomId, player)   => RoomCreated(roomId, player.id) :: Nil
        case KafkaMsg.PlayerJoined(_, roomId, players) =>
          players.map(player => PlayerJoined(roomId, player.id, players))
        case KafkaMsg.CustomError(id, msg)             => DecodingError(id, msg) :: Nil
      }

      debug"Receive event from kafka: ${record.value}" *> messages.pure[F].asInstanceOf[F[List[OutMessage]]]
    }
  }
}
