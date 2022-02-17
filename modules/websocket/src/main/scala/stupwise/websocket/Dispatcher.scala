package stupwise.websocket

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import Protocol.OutMessage.TestResultMsg
import Protocol.{InMessage, OutMessage}

import java.util.UUID

object Dispatcher {
  def dispatch[F[_]: Applicative](playerId: UUID, msg: InMessage): F[List[OutMessage]] = {
    val mappedMsg = msg match {
      case InMessage.TestMsg(value) => TestResultMsg(playerId, msg = value)
    }
    List[OutMessage](mappedMsg).pure[F]
  }
}
