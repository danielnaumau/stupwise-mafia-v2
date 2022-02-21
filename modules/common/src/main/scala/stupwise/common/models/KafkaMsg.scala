package stupwise.common.models

import java.util.UUID

sealed trait KafkaMsg {
  def id: UUID
}

object KafkaMsg {
  sealed trait LobbyCommand                                           extends KafkaMsg
  final case class InitRoom(id: UUID, player: Player)                 extends LobbyCommand
  final case class JoinRoom(id: UUID, roomId: String, player: Player) extends LobbyCommand
  //final case class StartGame(id: UUID, roomId: String)                extends LobbyCommand

  sealed trait MafiaEvent                                   extends KafkaMsg
  final case class StartMafiaGame(id: UUID, roomId: String) extends MafiaEvent

  sealed trait Event                                                             extends KafkaMsg
  final case class RoomCreated(id: UUID, roomId: String, player: Player)         extends Event
  final case class PlayerJoined(id: UUID, roomId: String, players: List[Player]) extends Event
  final case class CustomError(id: UUID, msg: String)                            extends Event
}
