package stupwise.websocket

import cats.Applicative
import cats.effect.kernel.Sync
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.Engine
import stupwise.common.models._
import stupwise.websocket.Protocol.InMessage

import java.util.UUID

trait Dispatcher[F[_]] {
  def dispatch(playerId: UUID, msg: InMessage): F[Unit]
}

object Dispatcher {
  class Live[F[_]: Applicative: Sync: Logger](engine: Engine[F]) extends Dispatcher[F] {
    override def dispatch(playerId: UUID, msg: InMessage): F[Unit] = {
      val process: F[Unit] = msg match {
        case InMessage.InitRoom(userName)            => engine.initRoom(LobbyPlayer(PlayerId(playerId), userName))
        case InMessage.JoinRoom(roomId, userName)    => engine.joinRoom(roomId, LobbyPlayer(PlayerId(playerId), userName))
        case InMessage.LeaveRoom(roomId, playerId)   => engine.leaveRoom(roomId, playerId)
        case InMessage.LeaveGame(roomId, playerId)   => engine.leaveGame(roomId, playerId)
        case InMessage.StartGame(roomId, _, variant) => engine.startGame(roomId, variant)
      }

      debug"Receive message from WS: $msg, playerId: $playerId" *> process
    }
  }
}
