package stupwise.websocket

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import stupwise.common.models.KafkaMsg.InitRoom
import stupwise.common.models.{KafkaMsg, Player}
import stupwise.websocket.Protocol.InMessage

import java.util.UUID

object Dispatcher {
  def dispatch[F[_]: Applicative](playerId: UUID, msg: InMessage): F[List[KafkaMsg]] = {
    val mappedMsg = msg match {
      case InMessage.InitRoom(userName) => InitRoom(UUID.randomUUID(), Player(playerId, userName))
    }
    List[KafkaMsg](mappedMsg).pure[F]
  }
}
