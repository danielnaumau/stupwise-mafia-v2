package stupwise.common.models

import stupwise.common.models.game.GameVariant

sealed trait Alert
object Alert {
  final case class RoomCreated(roomId: RoomId, player: LobbyPlayer)                         extends Alert
  final case class PlayerJoined(roomId: RoomId, players: List[LobbyPlayer])                 extends Alert
  final case class PlayerLeft(roomId: RoomId, players: List[LobbyPlayer])                   extends Alert
  final case class GameStarted(roomId: RoomId, players: List[Player], variant: GameVariant) extends Alert
  final case class Error(roomId: RoomId, reason: Reason)                                    extends Alert
}
