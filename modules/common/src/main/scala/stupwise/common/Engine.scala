package stupwise.common

import cats.MonadThrow
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.kafka.Producer
import stupwise.common.models.KafkaMsg.{GameCommand, LobbyCommand}
import stupwise.common.models._
import stupwise.common.models.game.GameVariant

trait Engine[F[_]] {
  def initRoom(player: LobbyPlayer): F[Unit]
  def joinRoom(roomId: RoomId, player: LobbyPlayer): F[Unit]
  def leaveRoom(roomId: RoomId, playerId: PlayerId): F[Unit]

  def startGame(roomId: RoomId, variant: GameVariant): F[Unit]
  def leaveGame(roomId: RoomId, playerId: PlayerId): F[Unit]
}

object Engine {
  class Live[F[_]: MonadThrow: GenUUID: Logger](
    lobbyProducer: Producer[F, LobbyCommand],
    gameProducer: Producer[F, GameCommand]
  ) extends Engine[F] {
    override def initRoom(player: LobbyPlayer): F[Unit] = {
      val command = for {
        msgId  <- GenUUID[F].generate.map(MsgId(_))
        command = LobbyCommand.InitRoom(msgId, player)
      } yield command

      val send = command.flatMap(lobbyProducer.send(_).handleErrorWith(e => error"Init room publish failed: $e"))

      debug"Send init room msg" *> send
    }

    override def joinRoom(roomId: RoomId, player: LobbyPlayer): F[Unit] = {
      val command = for {
        msgId  <- GenUUID[F].generate.map(MsgId(_))
        command = LobbyCommand.JoinRoom(msgId, roomId, player)
      } yield command

      val send = command.flatMap(lobbyProducer.send(_).handleErrorWith(e => error"Join room publish failed: $e"))

      debug"Send join room $roomId msg" *> send
    }

    override def leaveRoom(roomId: RoomId, playerId: PlayerId): F[Unit] = {
      val command = for {
        msgId  <- GenUUID[F].generate.map(MsgId(_))
        command = LobbyCommand.LeaveRoom(msgId, roomId, playerId)
      } yield command

      val send = command.flatMap(lobbyProducer.send(_).handleErrorWith(e => error"Leave room publish failed: $e"))

      debug"Send leave room $roomId msg" *> send
    }

    override def startGame(roomId: RoomId, variant: GameVariant): F[Unit] = {
      val command = for {
        msgId  <- GenUUID[F].generate.map(MsgId(_))
        command = LobbyCommand.InitGame(msgId, roomId, variant)
      } yield command

      val send = command.flatMap(lobbyProducer.send(_).handleErrorWith(e => error"Start game publish failed: $e"))

      debug"Send start game msg" *> send
    }

    override def leaveGame(roomId: RoomId, playerId: PlayerId): F[Unit] = {
      val command = for {
        msgId  <- GenUUID[F].generate.map(MsgId(_))
        command = GameCommand.LeaveGame(msgId, roomId, playerId)
      } yield command

      val send = command.flatMap(gameProducer.send(_).handleErrorWith(e => error"Leave game publish failed: $e"))

      debug"Leave game msg" *> send
    }
  }
}
