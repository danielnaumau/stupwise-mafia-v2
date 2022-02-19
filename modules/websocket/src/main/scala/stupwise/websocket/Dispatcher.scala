package stupwise.websocket

import cats.Applicative
import cats.effect.kernel.Sync
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.models.KafkaMsg.InitRoom
import stupwise.common.models.{KafkaMsg, Player}
import stupwise.websocket.Protocol.InMessage

import java.util.UUID

object Dispatcher {
  def dispatch[F[_]: Applicative: Sync: Logger](playerId: UUID, msg: InMessage): F[List[KafkaMsg]] = {
    val mappedMsg = msg match {
      case InMessage.InitRoom(userName) =>
        InitRoom(UUID.randomUUID(), Player(playerId, userName))
    }
    debug"Receive message from WS: $msg" *> List[KafkaMsg](mappedMsg).pure[F]
  }
}
