package stupwise.websocket

import cats.Applicative
import cats.implicits._
import fs2.kafka.ConsumerRecord
import stupwise.common.models.KafkaMsg
import stupwise.websocket.Protocol.OutMessage
import stupwise.websocket.Protocol.OutMessage.TestResultMsg

trait GameEventsProcessor[F[_]] {
  def processRecord(record: ConsumerRecord[Unit, KafkaMsg]): F[List[OutMessage]]
}

object GameEventsProcessor {
  class Live[F[_]: Applicative] extends GameEventsProcessor[F] {
    override def processRecord(record: ConsumerRecord[Unit, KafkaMsg]): F[List[OutMessage]] =
      ((record.value match {
        case command: KafkaMsg.Command => TestResultMsg(command.id, "no reaction")
        case event: KafkaMsg.Event     =>
          event match {
            case KafkaMsg.RoomCreated(_, roomId, player) => OutMessage.RoomCreated(roomId, player.id)
            case KafkaMsg.PlayerJoined(id, _, _)         => OutMessage.TestResultMsg(id, "player joined")
            case KafkaMsg.CustomError(id, msg)           => OutMessage.DecodingError(id, msg)
          }
      }) :: Nil).pure[F].asInstanceOf[F[List[OutMessage]]]
  }
}
