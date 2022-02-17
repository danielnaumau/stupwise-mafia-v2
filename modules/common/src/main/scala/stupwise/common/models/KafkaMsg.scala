package stupwise.common.models

import java.util.UUID

sealed trait KafkaMsg {
  def id: UUID
}

object KafkaMsg {
  sealed trait Command                                                extends KafkaMsg
  final case class InitRoom(id: UUID, player: Player)                 extends Command
  final case class JoinRoom(id: UUID, roomId: String, player: Player) extends Command

  sealed trait Event                                                             extends KafkaMsg
  final case class RoomCreated(id: UUID, roomId: String, player: Player)         extends Event
  final case class PlayerJoined(id: UUID, roomId: String, players: List[Player]) extends Event
  final case class CustomError(id: UUID, msg: String)                            extends Event
}
