package stupwise.common.models

import scala.util.Random

sealed trait State {
  def key: String
  def keyPattern: String
  def version: Int
}

object State {
  final case class RoomState(players: List[Player], version: Int, roomId: String) extends State {
    override def key: String        = s"state-lobby-$roomId-$version"
    override def keyPattern: String = s"state-lobby-$roomId-*"

    def addPlayer(newPlayer: Player): Option[RoomState] =
      Option.when(players.exists(player => newPlayer.userName == player.userName || newPlayer.id == player.id))(
        this.copy(players = players :+ newPlayer, version = version + 1)
      )
  }

  object RoomState {
    def empty(players: List[Player]): RoomState =
      RoomState(players, 0, Random.alphanumeric.take(7).mkString("").toUpperCase())
  }
}
