package stupwise.lobby

import cats._
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.GenUUID
import stupwise.common.models.KafkaMsg.LobbyCommand._
import stupwise.common.models.KafkaMsg.LobbyEvent._
import stupwise.common.models.KafkaMsg.{LobbyCommand, LobbyEvent}
import stupwise.common.models.State.RoomState
import stupwise.common.models.{MsgId, Reason, RoomId, RoomStatus}
import stupwise.common.redis.StateStore

final case class LobbyHandler[F[_]: Monad: GenUUID: Logger](stateStore: StateStore[F, RoomState]) {
  private val key                                  = (roomId: RoomId) => s"state-lobby-$roomId-*"
  def handle(command: LobbyCommand): F[LobbyEvent] = command match {
    case InitRoom(_, player) =>
      val state = RoomState.empty(List(player))
      for {
        res     <- stateStore.set(state)
        eventId <- GenUUID.generate[F]
        event    = if (res) RoomCreated(MsgId(eventId), state.roomId, player)
                   else ErrorHappened(MsgId(eventId), state.roomId, Reason("Redis error while creating room"))
        _       <- debug"Init room"
      } yield event

    case JoinRoom(_, roomId, player) =>
      for {
        newState <- stateStore.updateState(key(roomId))(_.addPlayer(player))
        msgId    <- GenUUID.generate[F].map(MsgId(_))
        event     =
          newState.fold(error => ErrorHappened(msgId, roomId, error), res => PlayerJoined(msgId, roomId, res.players))
        _        <- debug"Player ${player.userName} joined room"
      } yield event

    case InitGame(_, roomId) =>
      for {
        newState <- stateStore.updateState(s"state-lobby-$roomId-*")(_.changeStatus(RoomStatus.GameInProgress))
        msgId    <- GenUUID.generate[F].map(MsgId(_))
        message   =
          newState
            .fold(error => ErrorHappened(msgId, roomId, error), st => GameInitiated(msgId, roomId, st.status))
        _        <- debug"Game in progress in room $roomId"
      } yield message
  }
}
