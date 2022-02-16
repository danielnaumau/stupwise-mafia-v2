package stupwise.websocket

import cats.Applicative
import cats.implicits._
import fs2.kafka.ConsumerRecord
import stupwise.common.models.KafkaMsg

trait GameEventsProcessor[F[_]] {
  def processRecord(record: ConsumerRecord[Unit, KafkaMsg]): F[Unit]
}

object GameEventsProcessor {
  class Live[F[_]: Applicative] extends GameEventsProcessor[F] {
    override def processRecord(record: ConsumerRecord[Unit, KafkaMsg]): F[Unit] = println(record.value).pure[F]
  }
}
