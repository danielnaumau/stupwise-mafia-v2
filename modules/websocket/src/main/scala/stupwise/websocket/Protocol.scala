package stupwise.websocket

import stupwise.common.models.game.Role
import stupwise.common.models.views.GameInfoView
import stupwise.common.models.{LobbyPlayer, PlayerId, RoomId}

object Protocol {
  sealed trait OutMessage {
    def playerId: PlayerId
  }

  object OutMessage {
    final case class SocketClosed(playerId: PlayerId)                                                extends OutMessage
    //final case class DecodingError(playerId: PlayerId, errorMsg: String)                             extends OutMessage
    final case class RoomCreated(roomId: RoomId, playerId: PlayerId)                                 extends OutMessage
    final case class PlayerJoined(roomId: RoomId, playerId: PlayerId, players: List[LobbyPlayer])    extends OutMessage
    final case class GameStarted(roomId: RoomId, playerId: PlayerId, role: Role, info: GameInfoView) extends OutMessage
  }

  sealed trait InMessage
  object InMessage {
    final case class InitRoom(userName: String)                 extends InMessage
    final case class JoinRoom(roomId: RoomId, userName: String) extends InMessage
  }
}
