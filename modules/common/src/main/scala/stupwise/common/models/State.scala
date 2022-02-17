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
  }

  object RoomState {
    def empty(): RoomState =
      RoomState(List(), 0, Random.alphanumeric.take(7).mkString("").toUpperCase())
  }
}
