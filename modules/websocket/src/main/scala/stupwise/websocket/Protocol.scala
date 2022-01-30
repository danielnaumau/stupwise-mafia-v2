package stupwise.websocket

import java.util.UUID

object Protocol {
  sealed trait OutcomeMessage {
    def playerId: UUID
  }

  object OutcomeMessage {
    final case class SocketClosed(playerId: UUID) extends OutcomeMessage
    final case class DecodingError(playerId: UUID, errorMsg: String) extends OutcomeMessage
    final case class TestResultMsg(playerId: UUID, errorMsg: String) extends OutcomeMessage
  }

  sealed trait IncomeMessage
  object IncomeMessage {
    final case class TestMsg(value: String) extends IncomeMessage
  }
}
