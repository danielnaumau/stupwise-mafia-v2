package stupwise.lobby

import cats.Applicative
import cats.implicits._
import stupwise.common.models.KafkaMsg._
import stupwise.lobby.State.RoomState

final case class LobbyHandler[F[_]: Applicative](stateStore: StateStore[F, RoomState]) {
  def handle(command: Command): F[Event] = command match {
    case InitRoom(id, player)         =>
      val state = RoomState.empty().copy(players = List(player))
      stateStore
        .set(state)
        .map(res => if (res) RoomCreated(id, state.roomId, player) else CustomError(id, "Cannot create room"))
    case JoinRoom(id, roomId, player) =>
      stateStore
        .updateState(s"state-lobby-$roomId-*")(state =>
          state.copy(players = state.players :+ player, version = state.version + 1)
        )
        .map(_.map(resState => PlayerJoined(id, roomId, resState.players)).getOrElse(CustomError(id, "Cannot join room")))
  }
}
