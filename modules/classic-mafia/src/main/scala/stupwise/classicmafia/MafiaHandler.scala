package stupwise.classicmafia

import cats.Monad
import org.typelevel.log4cats.Logger
import stupwise.common.GenUUID
import stupwise.common.models.KafkaMsg
import stupwise.common.models.State.RoomState
import stupwise.common.redis.StateStore
import stupwise.gamecore.GameHandler

final case class MafiaHandler[F[_]: Monad: GenUUID: Logger](stateStore: StateStore[F, RoomState]) extends GameHandler[F] {
  override def handle: PartialFunction[KafkaMsg.GameCommand, F[KafkaMsg.GameEvent]] = ???
}
