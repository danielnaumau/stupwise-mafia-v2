package stupwise.engine

import cats.MonadThrow
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.kafka.Producer
import stupwise.common.models.Alert
import stupwise.common.models.KafkaMsg.LobbyEvent

trait LobbyEventProcessor[F[_]] {
  def process(event: LobbyEvent): F[Unit]
}

object LobbyEventProcessor {
  class Live[F[_]: MonadThrow: Logger](alertProducer: Producer[F, Alert]) extends LobbyEventProcessor[F] {
    override def process(event: LobbyEvent): F[Unit] = event match {
      case LobbyEvent.RoomCreated(_, roomId, player)   =>
        debug"process lobby room created" *> alertProducer
          .send(Alert.RoomCreated(roomId, player))
          .handleErrorWith(e => error"Alert publish failed: $e")
      case LobbyEvent.PlayerJoined(_, roomId, players) =>
        alertProducer.send(Alert.PlayerJoined(roomId, players)).handleErrorWith(e => error"Alert publish failed: $e")
      case LobbyEvent.PlayerLeft(_, roomId, players)   =>
        alertProducer.send(Alert.PlayerLeft(roomId, players)).handleErrorWith(e => error"Alert publish failed: $e")
      case LobbyEvent.ErrorHappened(_, roomId, reason) =>
        debug"process lobby error" *> alertProducer
          .send(Alert.Error(roomId, reason))
          .handleErrorWith(e => error"Alert publish failed: $e")
      case _                                           => ().pure[F]
    }
  }
}
