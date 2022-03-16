package stupwise.gamecore

import cats._
import cats.implicits._
import org.typelevel.log4cats.Logger
import stupwise.common.models.KafkaMsg._
import stupwise.common.models.State.GameState
import stupwise.common.models.{KafkaMsg, MsgId, Player}
import stupwise.common.{GenUUID, StateStore}

class GameHandler[F[_]: Monad: GenUUID: Logger](stateStore: StateStore[F, GameState]) {
  def handle(command: GameCommand): F[KafkaMsg] = command match {
    case CreateGame(id, roomId, variant, players) =>
      val state = games.get(variant).map { game =>
        val gamePlayers = game.assignRoles(players).map { case (id, role) =>
          Player(id, role)
        }
        GameState.empty(gamePlayers.toList, roomId, game.settings.startPhase)
      }

      state
        .traverse(st =>
          for {
            res     <- stateStore.set(st)
            eventId <- GenUUID.generate[F]
            event    = if (res) GameCreated(MsgId(eventId), roomId, st.players, variant)
                       else CustomError(MsgId(eventId), "Cannot create game")
          } yield event
        )
        .map(_.getOrElse(CustomError(id, s"No such game: $variant")))
  }
}
