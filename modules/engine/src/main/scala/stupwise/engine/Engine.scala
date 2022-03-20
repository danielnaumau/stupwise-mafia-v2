package stupwise.engine

import cats.MonadThrow
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.GenUUID
import stupwise.common.kafka.Producer
import stupwise.common.models.KafkaMsg.LobbyCommand
import stupwise.common.models.game.GameVariant
import stupwise.common.models.{LobbyPlayer, MsgId, PlayerId, RoomId}

trait Engine[F[_]] {
  def initRoom(player: LobbyPlayer): F[Unit]
  def joinRoom(roomId: RoomId, player: LobbyPlayer): F[Unit]
  def leaveRoom(roomId: RoomId, playerId: PlayerId): F[Unit]

  def startGame(roomId: RoomId, variant: GameVariant): F[Unit]

  def handleError(msg: String): F[Unit]
}

object Engine {
  class Live[F[_]: MonadThrow: GenUUID: Logger](lobbyProducer: Producer[F, LobbyCommand]) extends Engine[F] {
    override def initRoom(player: LobbyPlayer): F[Unit] = {
      val command = for {
        msgId  <- GenUUID[F].generate.map(MsgId(_))
        command = LobbyCommand.InitRoom(msgId, player)
      } yield command

      val send = command.flatMap(lobbyProducer.send(_).handleErrorWith(e => error"Init room publish failed: $e"))

      debug"Send init room msg" *> send
    }

    override def joinRoom(roomId: RoomId, player: LobbyPlayer): F[Unit] = ().pure[F]

    override def leaveRoom(roomId: RoomId, playerId: PlayerId): F[Unit] = ().pure[F]

    override def startGame(roomId: RoomId, variant: GameVariant): F[Unit] = ().pure[F]

    override def handleError(msg: String): F[Unit] = ().pure[F]
  }
}
