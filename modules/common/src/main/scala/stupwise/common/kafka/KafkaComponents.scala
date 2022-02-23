package stupwise.common.kafka

import cats.effect.IO
import fs2.Stream
import fs2.kafka.ConsumerRecord
import io.circe.{Decoder, Encoder}

trait KafkaComponents extends Fs2KafkaComponent {
  def subscribe[V: Decoder, R](topic: String, processRecord: ConsumerRecord[Unit, V] => IO[R]): Stream[IO, R] =
    Fs2KafkaConsumer
      .consume[IO, V, R](
        topic = topic,
        kafkaSettings = kafkaConfig.settings,
        processRecord = processRecord
      )

  def publish[V: Encoder](topic: String, eventStream: fs2.Stream[IO, V]): IO[Unit] = Fs2KafkaPublisher
    .publish(
      topic = topic,
      kafkaSettings = kafkaConfig.settings,
      eventStream = eventStream
    )
}
