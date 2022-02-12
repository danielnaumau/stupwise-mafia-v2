package stupwise.lobby

import stupwise.lobby.Models.{Command, Event, InitRoom, RoomState, RoomCreated}

object LobbyHandler {
  def handle: PartialFunction[(RoomState, Command), (RoomState, Event)] = {
    case (_, InitRoom(player)) =>
      (RoomState(List(player), 0, "testRoomId"), RoomCreated("testRoomId", player))
  }
}
