package stupwise.common.models

import stupwise.common.models.game.GameVariant

sealed trait KafkaMsg {
  def id: MsgId
}

object KafkaMsg {
  sealed trait LobbyCommand extends KafkaMsg

  object LobbyCommand {
    final case class InitRoom(id: MsgId, player: LobbyPlayer) extends LobbyCommand

    final case class JoinRoom(id: MsgId, roomId: RoomId, player: LobbyPlayer) extends LobbyCommand

    final case class InitGame(id: MsgId, roomId: RoomId) extends LobbyCommand
  }

  sealed trait LobbyEvent extends KafkaMsg
  object LobbyEvent {
    final case class RoomCreated(id: MsgId, roomId: RoomId, player: LobbyPlayer) extends LobbyEvent

    final case class PlayerJoined(id: MsgId, roomId: RoomId, players: List[LobbyPlayer]) extends LobbyEvent

    final case class PlayerLeft(id: MsgId, roomId: RoomId, players: List[LobbyPlayer]) extends LobbyEvent

    final case class GameInitiated(id: MsgId, roomId: RoomId, status: RoomStatus) extends LobbyEvent

    final case class ErrorHappened(id: MsgId, roomId: RoomId, reason: Reason) extends LobbyEvent
  }

  sealed trait GameCommand extends KafkaMsg
  object GameCommand {
    final case class CreateGame(id: MsgId, roomId: RoomId, variant: GameVariant, players: List[PlayerId])
        extends GameCommand
  }

  sealed trait GameEvent extends KafkaMsg
  object GameEvent {
    final case class GameCreated(id: MsgId, roomId: RoomId, players: List[Player], variant: GameVariant)
        extends GameEvent

    final case class Error(id: MsgId, roomId: RoomId, reason: Reason) extends GameEvent
  }
}
