package stupwise.websocket

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import stupwise.websocket.Protocol.OutcomeMessage.TestResultMsg
import stupwise.websocket.Protocol.{IncomeMessage, OutcomeMessage}

import java.util.UUID

object Dispatcher {
  def dispatch[F[_]: Applicative](playerId: UUID, msg: IncomeMessage): F[List[OutcomeMessage]] = {
    List[OutcomeMessage](TestResultMsg(playerId, "success")).pure[F]
  }
}
