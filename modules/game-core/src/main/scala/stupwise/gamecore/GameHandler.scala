package stupwise.gamecore

import cats._
import cats.implicits._
import org.typelevel.log4cats.Logger
import stupwise.common.GenUUID
import stupwise.common.models.KafkaMsg._
import stupwise.common.models.State.GameState
import stupwise.common.models.{MsgId, Player, Reason}
import stupwise.common.redis.StateStore

class GameHandler[F[_]: Monad: GenUUID: Logger](stateStore: StateStore[F, GameState]) {
  def handle(command: GameCommand): F[GameEvent] = command match {
    case GameCommand.CreateGame(id, roomId, variant, players) =>
      val state = games.get(variant).map { game =>
        val gamePlayers = game.assignRoles(players).map { case (id, role) =>
          Player(id, role)
        }
        GameState.empty(gamePlayers.toList, roomId, game.settings.startPhase)
      }

      state
        .traverse(st =>
          for {
            res   <- stateStore.set(st)
            msgId <- GenUUID.generate[F].map(MsgId(_))
            event  = if (res) GameEvent.GameCreated(msgId, roomId, st.players, variant)
                     else GameEvent.Error(msgId, roomId, Reason("Cannot create game"))
          } yield event
        )
        .map(_.getOrElse(GameEvent.Error(id, roomId, Reason(s"No such game: $variant"))))
  }
}
