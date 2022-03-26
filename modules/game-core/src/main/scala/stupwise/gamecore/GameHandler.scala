package stupwise.gamecore

import stupwise.common.models.KafkaMsg.{GameCommand, GameEvent}

trait GameHandler[F[_]] {
  def handle: PartialFunction[GameCommand, F[GameEvent]]
}
