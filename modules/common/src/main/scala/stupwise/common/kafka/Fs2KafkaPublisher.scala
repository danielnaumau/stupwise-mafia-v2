package stupwise.common.kafka

import cats.effect.{Async, Concurrent}
import fs2.kafka._
import io.circe.Encoder
import stupwise.common.AppConfig.KafkaConfig.KafkaSettings
import stupwise.common.Codecs

object Fs2KafkaPublisher {

  def publish[F[_]: Concurrent: Async, V: Encoder](
    kafkaSettings: KafkaSettings,
    eventStream: fs2.Stream[F, V],
    topic: String
  ): F[Unit] = {
    val settings = ProducerSettings[F, Unit, V](
      keySerializer = Serializer.unit,
      valueSerializer = Codecs.circeSerializer[F, V]
    )
      .withBootstrapServers(kafkaSettings.bootstrapServers)
      .withRetries(kafkaSettings.retries)

    val stream = eventStream.through(oneToProducerRecordsPipe(topic)).through(KafkaProducer.pipe(settings))

    stream.compile.drain
  }

  private def oneToProducerRecordsPipe[F[_], K, V](topic: String) =
    (stream: fs2.Stream[F, V]) => stream.chunks.map(chunk => ProducerRecords(chunk.map(ProducerRecord(topic, (), _))))
}
