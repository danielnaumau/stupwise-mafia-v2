package stupwise.engine

import cats.MonadThrow
import cats.implicits._
import org.typelevel.log4cats.Logger
import stupwise.common.kafka.Producer
import stupwise.common.models.Alert
import stupwise.common.models.KafkaMsg.GameEvent._
import stupwise.common.models.KafkaMsg.{GameEvent, LobbyCommand}

trait GameEventProcessor[F[_]] {
  def process(event: GameEvent): F[Unit]
}

object GameEventProcessor {
  class Live[F[_]: MonadThrow: Logger](lobbyProducer: Producer[F, LobbyCommand], alertProducer: Producer[F, Alert])
      extends GameEventProcessor[F] {
    override def process(event: GameEvent): F[Unit] = event match {
      case GameCreated(id, roomId, players, variant) =>
        lobbyProducer.send(LobbyCommand.InitGame(id, roomId)) *> alertProducer.send(
          Alert.GameStarted(roomId, players, variant)
        )
      case Error(_, roomId, reason)                  => alertProducer.send(Alert.Error(roomId, reason))
    }
  }
}
