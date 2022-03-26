package stupwise.lobby

import cats._
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.GenUUID
import stupwise.common.kafka.Producer
import stupwise.common.models.KafkaMsg.LobbyCommand._
import stupwise.common.models.KafkaMsg.LobbyEvent._
import stupwise.common.models.KafkaMsg.{GameCommand, LobbyCommand, LobbyEvent}
import stupwise.common.models.State.RoomState
import stupwise.common.models.{MsgId, Reason, RoomId, RoomStatus}
import stupwise.common.redis.StateStore

final case class CommandHandler[F[_]: Monad: GenUUID: Logger](stateStore: StateStore[F, RoomState]) {

  private val key = (roomId: RoomId) => s"state-lobby-$roomId-*"

  def handle(
    command: LobbyCommand
  )(eventProducer: Producer[F, LobbyEvent], gameCommandProducer: Producer[F, GameCommand]): F[Unit] = command match {
    case InitRoom(eventId, player) =>
      val state = RoomState.empty(List(player))
      for {
        res     <- stateStore.set(state)
        event    = if (res) RoomCreated(eventId, state.roomId, player)
                   else
                     LobbyError(
                       eventId,
                       state.roomId,
                       List(player.id),
                       Reason("Redis error while creating room")
                     )
        _       <- debug"Init room"
        _       <- eventProducer.send(event)
      } yield ()

    case JoinRoom(eventId, roomId, player) =>
      for {
        newState <- stateStore.updateState(key(roomId))(_.addPlayer(player))
        event     =
          newState.fold(
            error => LobbyError(eventId, roomId, List(player.id), error),
            res => PlayerJoined(eventId, roomId, res.players)
          )
        _        <- debug"Player ${player.userName} joined room"
        _        <- eventProducer.send(event)
      } yield ()

    case LeaveRoom(eventId, roomId, playerId) =>
      for {
        newState <- stateStore.updateState(key(roomId))(_.removePlayer(playerId))
        event     =
          newState.fold(
            error => LobbyError(eventId, roomId, List(playerId), error),
            res => PlayerLeft(eventId, roomId, res.players)
          )
        _        <- debug"Player $playerId left the room"
        _        <- eventProducer.send(event)
      } yield ()

    case InitGame(eventId, roomId, variant) =>
      for {
        newState   <-
          stateStore.updateState(s"state-lobby-$roomId-*")(_.changeStatus(RoomStatus.GameInProgress))
        event       =
          newState
            .fold(error => LobbyError(eventId, roomId, Nil, error), st => GameInitiated(eventId, roomId, st.status))
        gameCommand = newState.map(s => GameCommand.CreateGame(eventId, roomId, variant, s.players.map(_.id)))
        _          <- debug"Game in progress in room $roomId"
        _          <- eventProducer.send(event)
        _          <- gameCommand.map(gameCommandProducer.send(_)).getOrElse(().pure[F])
      } yield ()
  }
}
