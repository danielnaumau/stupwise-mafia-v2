package stupwise.common.models

import stupwise.common.models.game.GameVariant

sealed trait KafkaMsg {
  def id: MsgId
}

object KafkaMsg {
  sealed trait LobbyCommand                                                  extends KafkaMsg
  final case class InitRoom(id: MsgId, player: LobbyPlayer)                  extends LobbyCommand
  final case class JoinRoom(id: MsgId, roomId: RoomId, player: LobbyPlayer)  extends LobbyCommand
  final case class InitGame(id: MsgId, roomId: RoomId, variant: GameVariant) extends LobbyCommand

  sealed trait GameCommand extends KafkaMsg
  final case class CreateGame(id: MsgId, roomId: RoomId, variant: GameVariant, players: List[PlayerId])
      extends GameCommand

  sealed trait Event                                                                                   extends KafkaMsg
  final case class RoomCreated(id: MsgId, roomId: RoomId, player: LobbyPlayer)                         extends Event
  final case class PlayerJoined(id: MsgId, roomId: RoomId, players: List[LobbyPlayer])                 extends Event
  final case class GameCreated(id: MsgId, roomId: RoomId, players: List[Player], variant: GameVariant) extends Event
  final case class CustomError(id: MsgId, msg: String)                                                 extends Event
}
