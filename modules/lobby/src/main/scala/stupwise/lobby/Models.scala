package stupwise.lobby

import java.util.UUID

object Models {
  sealed trait State {
    def key: String
    def keyPattern: String
    def version: Int
  }
  final case class Player(id: UUID, userName: String)

  final case class RoomState(players: List[Player], version: Int, roomId: String) extends State {
    override def key: String = s"state-lobby-$roomId-$version"
    override def keyPattern: String = s"state-lobby-$roomId-*"
  }


  sealed trait Command
  final case class InitRoom(player: Player) extends Command
  final case class JoinRoom(roomId: String, player: Player) extends Command

  sealed trait Event
  final case class UserJoined()                                extends Event
  final case class RoomCreated(roomId: String, player: Player) extends Event
}
