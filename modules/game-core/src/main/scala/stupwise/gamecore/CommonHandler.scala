package stupwise.gamecore

import cats._
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.models.KafkaMsg.GameEvent._
import stupwise.common.models.KafkaMsg.{GameCommand, GameEvent}
import stupwise.common.models.State.GameState
import stupwise.common.models.game.Game
import stupwise.common.models.{MsgId, Player, Reason}
import stupwise.common.redis.StateStore
import stupwise.common.{availableGames, GenUUID}

class CommonHandler[F[_]: Monad: GenUUID: Logger](stateStore: StateStore[F, GameState], game: Game) {
  def handle: PartialFunction[GameCommand, F[GameEvent]] = {
    case GameCommand.CreateGame(msgId, roomId, variant, players) =>
      for {
        _          <- debug"Start game $variant"
        gamePlayers = game.assignRoles(players).map { case (id, role) => Player(id, role) }
        state       = GameState.empty(gamePlayers.toList, roomId, game.settings.startPhase)
        res        <- stateStore.set(state)
        event       = if (res) GameEvent.GameCreated(msgId, roomId, state.players, variant)
                      else GameEvent.GameError(msgId, roomId, players, Reason("Cannot create game"))
      } yield event

    case GameCommand.LeaveGame(msgId, roomId, playerId) =>
      for {
        newState <- stateStore.updateState(s"state-game-$roomId-*")(_.removePlayer(playerId))
        event     =
          newState.fold(
            error => GameError(msgId, roomId, List(playerId), error),
            res => PlayerLeftGame(msgId, roomId, res.players.map(_.id))
          )
        _        <- debug"Player $playerId left the game"
      } yield event
  }
}
