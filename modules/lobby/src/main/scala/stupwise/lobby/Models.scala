package stupwise.lobby

import java.util.UUID

object Models {
  final case class Player(id: UUID, userName: String)


  sealed trait Command
  final case class InitRoom(player: Player) extends Command
  final case class JoinRoom(roomId: String, player: Player) extends Command

  sealed trait Event
  final case class UserJoined()                                extends Event
  final case class RoomCreated(roomId: String, player: Player) extends Event
}
