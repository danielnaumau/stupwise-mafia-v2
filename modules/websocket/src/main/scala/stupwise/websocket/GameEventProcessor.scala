package stupwise.websocket

import cats.Applicative
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.availableGames
import stupwise.common.models.KafkaMsg.GameEvent
import stupwise.common.models.views.GameInfoView
import stupwise.websocket.Protocol.OutMessage
import stupwise.websocket.Protocol.OutMessage._

trait GameEventProcessor[F[_]] {
  def process(event: GameEvent): F[List[OutMessage]]
}

object GameEventProcessor {
  class Live[F[_]: Applicative: Logger] extends GameEventProcessor[F] {
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
