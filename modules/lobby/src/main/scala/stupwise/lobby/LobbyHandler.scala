package stupwise.lobby

import cats._
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.models.KafkaMsg._
import stupwise.common.models.State.RoomState
import stupwise.common.models.{KafkaMsg, MsgId, RoomStatus}
import stupwise.common.{GenUUID, StateStore}

final case class LobbyHandler[F[_]: Monad: GenUUID: Logger](stateStore: StateStore[F, RoomState]) {
  def handle(command: LobbyCommand): F[KafkaMsg] = command match {
    case InitRoom(_, player) =>
      val state = RoomState.empty(List(player))
      for {
        res     <- stateStore.set(state)
        eventId <- GenUUID.generate[F]
        event    = if (res) RoomCreated(MsgId(eventId), state.roomId, player)
                   else CustomError(MsgId(eventId), "Cannot create room")
      } yield event

    case JoinRoom(_, roomId, player) =>
      for {
        newState <- stateStore.updateState(s"state-lobby-$roomId-*")(_.addPlayer(player))
        _        <- newState.fold(warn"Player $player cannot join the room: $roomId")(_ => ().pure[F])
        msgId    <- GenUUID.generate[F].map(MsgId(_))
        event     = newState
                      .map(res => PlayerJoined(msgId, roomId, res.players))
                      .getOrElse(CustomError(msgId, s"Player $player cannot join the room: $roomId"))
      } yield event

    case InitGame(_, roomId, variant) =>
      for {
        newState <- stateStore.updateState(s"state-lobby-$roomId-*")(_.changeStatus(RoomStatus.GameInProgress))
        _        <- newState.fold(debug"Start $variant game in room $roomId")(_ => ().pure[F])
        msgId    <- GenUUID.generate[F].map(MsgId(_))
        message   = newState
                      .map(st => CreateGame(msgId, roomId, variant, st.players.map(_.id)))
                      .getOrElse(CustomError(msgId, s"Unexpected error in room $roomId"))
      } yield message
  }
}
