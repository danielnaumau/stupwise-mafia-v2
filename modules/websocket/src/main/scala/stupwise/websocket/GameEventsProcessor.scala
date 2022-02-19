package stupwise.websocket

import cats.Applicative
import cats.implicits._
import fs2.kafka.ConsumerRecord
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.models.KafkaMsg
import stupwise.websocket.Protocol.OutMessage
import stupwise.websocket.Protocol.OutMessage.TestResultMsg

trait GameEventsProcessor[F[_]] {
  def processRecord(record: ConsumerRecord[Unit, KafkaMsg]): F[List[OutMessage]]
}

object GameEventsProcessor {
  class Live[F[_]: Applicative: Logger] extends GameEventsProcessor[F] {
    override def processRecord(record: ConsumerRecord[Unit, KafkaMsg]): F[List[OutMessage]] = {
      val messages = record.value match {
        case command: KafkaMsg.Command => TestResultMsg(command.id, "no reaction") :: Nil
        case event: KafkaMsg.Event     =>
          event match {
            case KafkaMsg.RoomCreated(_, roomId, player) => OutMessage.RoomCreated(roomId, player.id) :: Nil
            case KafkaMsg.PlayerJoined(id, _, _)         =>
              OutMessage.TestResultMsg(id, "player joined") :: Nil
            case KafkaMsg.CustomError(id, msg)           => OutMessage.DecodingError(id, msg) :: Nil
          }
      }

      debug"Receive event from kafka: ${record.value}" *> messages.pure[F].asInstanceOf[F[List[OutMessage]]]
    }
  }
}
