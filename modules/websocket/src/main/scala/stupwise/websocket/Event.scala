package stupwise.websocket

import java.util.UUID

sealed trait Event {
  def id: UUID
}

object Event {
  case class TestEvent(id: UUID, msg: String) extends Event
}
