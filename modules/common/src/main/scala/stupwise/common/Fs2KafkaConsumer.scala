package stupwise.common

import cats.effect.{Async, Concurrent}
import fs2.kafka._
import io.circe.Decoder

object Fs2KafkaConsumer {

  def consume[F[_]: Concurrent: Async, V: Decoder](
    kafkaSettings: KafkaSettings,
    processRecord: ConsumerRecord[Unit, V] => F[Unit],
    topic: String
  ): fs2.Stream[F, Unit] = {
    val settings =
      ConsumerSettings[F, Unit, V](
        keyDeserializer = Deserializer.unit,
        valueDeserializer = Codecs.circeDeserializer[F, V]
      )
        .withBootstrapServers(kafkaSettings.bootstrapServers)
        .withAutoOffsetReset(AutoOffsetReset.Latest)
        .withGroupId("group")

    KafkaConsumer
      .stream(settings)
      .subscribeTo(topic)
      .partitionedRecords
      .map { partitionStream =>
        partitionStream.evalMap { committable =>
          processRecord(committable.record)
        }
      }
      .parJoinUnbounded
  }
}
