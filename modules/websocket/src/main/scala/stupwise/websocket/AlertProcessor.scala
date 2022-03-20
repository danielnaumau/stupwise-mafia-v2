package stupwise.websocket

import cats.Applicative
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.models.{Alert, PlayerId}
import stupwise.websocket.Protocol.OutMessage
import stupwise.websocket.Protocol.OutMessage._

import java.util.UUID

trait AlertProcessor[F[_]] {
  def process(alert: Alert): F[List[OutMessage]]
}

object AlertProcessor {
  class Live[F[_]: Applicative: Logger] extends AlertProcessor[F] {
    override def process(alert: Alert): F[List[OutMessage]] = {
      val messages: List[OutMessage] = alert match {
        case Alert.RoomCreated(roomId, player)   => RoomCreated(roomId, player.id) :: Nil
        case Alert.PlayerJoined(roomId, players) => players.map(player => PlayerJoined(roomId, player.id, players))
        case Alert.PlayerLeft(roomId, players)   => players.map(player => PlayerLeft(roomId, player.id, players))
        case Alert.GameStarted(roomId, _, _)     => RoomCreated(roomId, PlayerId(UUID.randomUUID())) :: Nil // TODO: update
        case Alert.Error(_, reason)              => Error(PlayerId(UUID.randomUUID()), reason) :: Nil       // TODO: update
      }
      /*case KafkaMsg.GameStarted(_, roomId, players, variant) =>
          games
            .get(variant)
            .map { game =>
              GameInfoView.create(game.settings, players)
            }
            .map(info => players.map(pl => GameStarted(roomId, pl.id, pl.role, info)))
            .getOrElse(Nil)*/

      debug"Received alert: $alert" *> messages.pure[F]
    }
  }
}
