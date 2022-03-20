package stupwise.common.kafka

import cats.effect.{Async, Concurrent}
import cats.implicits._
import fs2.kafka._
import io.circe.Decoder
import stupwise.common.AppConfig.KafkaConfig.KafkaSettings
import stupwise.common.Codecs

trait Consumer[F[_], R] {
  def receive(): fs2.Stream[F, R]
}

object Consumer {
  def kafka[F[_]: Concurrent: Async, R: Decoder](
    kafkaSettings: KafkaSettings,
    topic: String
  ): fs2.Stream[F, Consumer[F, R]] = {
    val settings =
      ConsumerSettings[F, Unit, R](
        keyDeserializer = Deserializer.unit,
        valueDeserializer = Codecs.circeDeserializer[F, R]
      )
        .withBootstrapServers(kafkaSettings.bootstrapServers)
        .withAutoOffsetReset(AutoOffsetReset.Latest)
        .withGroupId("group")

    KafkaConsumer
      .stream(settings)
      .subscribeTo(topic)
      .map(c => () => c.stream.evalMap(c => c.offset.commit.as(c.record.value)))
  }
}
