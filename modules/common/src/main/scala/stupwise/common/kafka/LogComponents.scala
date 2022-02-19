package stupwise.common.kafka

import cats.effect.Sync
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait LogComponents {
  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]
}
