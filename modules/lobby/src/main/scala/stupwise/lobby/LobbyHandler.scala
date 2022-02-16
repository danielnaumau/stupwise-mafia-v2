package stupwise.lobby

import stupwise.lobby.Models.{Command, Event, InitRoom, RoomCreated}
import stupwise.lobby.State.RoomState

final case class LobbyHandler[F[_]](stateStore: StateStore[F]) {
  def handle: PartialFunction[(RoomState, Command), (RoomState, Event)] = {
    case (_, InitRoom(player)) =>
      (RoomState(List(player), 0, "testRoomId"), RoomCreated("testRoomId", player))
  }
}
