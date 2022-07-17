package stupwise.websocket.eventProcessors

import cats.Applicative
import org.typelevel.log4cats.Logger
import stupwise.common.availableGames
import stupwise.common.models.KafkaMsg.GameEvent
import org.typelevel.log4cats.syntax._
import cats.implicits._
import stupwise.common.models.views.GameInfoView
import stupwise.websocket.Protocol.OutMessage
import stupwise.websocket.Protocol.OutMessage.{GameError, GameStarted, PlayerLeftGame}

object GameEventProcessor {
  class Live[F[_]: Applicative: Logger] extends EventProcessor[F, GameEvent] {
    override def process(event: GameEvent): F[List[OutMessage]] = {
      val messages: List[OutMessage] = event match {
        case GameEvent.GameCreated(_, roomId, players, variant) =>
          availableGames
            .get(variant)
            .map(game => GameInfoView.create(game.settings, players))
            .map(info => players.map(pl => GameStarted(roomId, pl.id, pl.role, info)))
            .getOrElse(Nil)
        case GameEvent.PlayerLeftGame(_, roomId, players)       => players.map(PlayerLeftGame(roomId, _, players))
        case GameEvent.GameError(_, roomId, players, reason)    => players.map(GameError(roomId, _, reason))
      }

      debug"Received event: $event" *> messages.pure[F]
    }
  }
}
