package stupwise.gamecore

import cats._
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.models.KafkaMsg.GameEvent._
import stupwise.common.models.KafkaMsg.{GameCommand, GameEvent}
import stupwise.common.models.State.GameState
import stupwise.common.models.{MsgId, Player, Reason}
import stupwise.common.redis.StateStore
import stupwise.common.{availableGames, GenUUID}

class GameHandler[F[_]: Monad: GenUUID: Logger](stateStore: StateStore[F, GameState]) {
  def handle(command: GameCommand): F[GameEvent] = command match {
    case GameCommand.CreateGame(id, roomId, variant, players) =>
      val state = availableGames.get(variant).map { game =>
        val gamePlayers = game.assignRoles(players).map { case (id, role) =>
          Player(id, role)
        }
        GameState.empty(gamePlayers.toList, roomId, game.settings.startPhase)
      }

      debug"Start game $variant" *> state
        .traverse(st =>
          for {
            res   <- stateStore.set(st)
            msgId <- GenUUID.generate[F].map(MsgId(_))
            event  = if (res) GameEvent.GameCreated(msgId, roomId, st.players, variant)
                     else GameEvent.GameError(msgId, roomId, players, Reason("Cannot create game")) // use admin only
          } yield event
        )
        .map(_.getOrElse(GameEvent.GameError(id, roomId, players, Reason(s"No such game: $variant"))))

    case GameCommand.LeaveGame(_, roomId, playerId) =>
      for {
        newState <- stateStore.updateState(s"state-game-$roomId-*")(_.removePlayer(playerId))
        msgId    <- GenUUID.generate[F].map(MsgId(_))
        event     =
          newState.fold(
            error => GameError(msgId, roomId, List(playerId), error),
            res => PlayerLeftGame(msgId, roomId, res.players.map(_.id))
          )
        _        <- debug"Player $playerId left the game"
      } yield event
  }
}
