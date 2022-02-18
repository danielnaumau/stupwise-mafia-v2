package stupwise.websocket

import java.util.UUID

object Protocol {
  sealed trait OutMessage {
    def playerId: UUID
  }

  object OutMessage {
    final case class SocketClosed(playerId: UUID)                    extends OutMessage
    final case class DecodingError(playerId: UUID, errorMsg: String) extends OutMessage
    final case class RoomCreated(roomId: String, playerId: UUID)     extends OutMessage
    final case class TestResultMsg(playerId: UUID, msg: String)      extends OutMessage
  }

  sealed trait InMessage
  object InMessage {
    final case class InitRoom(userName: String) extends InMessage
  }
}
