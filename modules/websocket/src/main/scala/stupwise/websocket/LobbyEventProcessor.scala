package stupwise.websocket

import cats.Applicative
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.models.KafkaMsg.LobbyEvent
import stupwise.websocket.Protocol.OutMessage
import stupwise.websocket.Protocol.OutMessage._

trait LobbyEventProcessor[F[_]] {
  def process(event: LobbyEvent): F[List[OutMessage]]
}

object LobbyEventProcessor {
  class Live[F[_]: Applicative: Logger] extends LobbyEventProcessor[F] {
    override def process(event: LobbyEvent): F[List[OutMessage]] = {
      val messages: List[OutMessage] = event match {
        case LobbyEvent.RoomCreated(_, roomId, player)         => RoomCreated(roomId, player.id) :: Nil
        case LobbyEvent.PlayerJoined(_, roomId, players)       =>
          players.map(player => PlayerJoined(roomId, player.id, players))
        case LobbyEvent.PlayerLeft(_, roomId, players)         =>
          players.map(player => PlayerLeftLobby(roomId, player.id, players))
        case LobbyEvent.LobbyError(_, roomId, players, reason) => players.map(GameError(roomId, _, reason))
        case _                                                 => Nil
      }

      debug"Received event: $event" *> messages.pure[F]
    }
  }
}
