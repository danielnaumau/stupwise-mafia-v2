package stupwise.lobby

import cats._
import cats.implicits._
import stupwise.common.GenUUID
import stupwise.common.models.KafkaMsg._
import stupwise.common.models.State.RoomState

final case class LobbyHandler[F[_]: FlatMap: GenUUID](stateStore: StateStore[F, RoomState]) {
  def handle(command: Command): F[Event] = command match {
    case InitRoom(_, player)         =>
      val state = RoomState.empty(List(player))
      for {
        res     <- stateStore.set(state)
        eventId <- GenUUID.generate[F]
        event    = if (res) RoomCreated(eventId, state.roomId, player) else CustomError(eventId, "Cannot create room")
      } yield event

    case JoinRoom(_, roomId, player) =>
      for {
        newState <- stateStore.updateState(s"state-lobby-$roomId-*")(_.addPlayer(player)) // toDo: Check unique userName and id
        eventId  <- GenUUID.generate[F]
        event     = newState
          .map(res => PlayerJoined(eventId, roomId, res.players))
          .getOrElse(CustomError(eventId, s"Player $player cannot join the room: $roomId"))
      } yield event
  }
}
