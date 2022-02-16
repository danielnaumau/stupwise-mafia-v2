package stupwise.common

import cats.effect.{Async, Concurrent, ExitCode}
import cats.implicits._
import fs2.kafka._
import io.circe.Encoder

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

    stream.compile.drain.as(ExitCode.Success)
  }

  private def oneToProducerRecordsPipe[F[_], K, V](topic: String) =
    (stream: fs2.Stream[F, V]) => stream.chunks.map(chunk => ProducerRecords(chunk.map(ProducerRecord(topic, (), _))))
}
