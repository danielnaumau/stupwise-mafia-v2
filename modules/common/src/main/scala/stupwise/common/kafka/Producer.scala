package stupwise.common.kafka

import cats.effect.{Async, Concurrent}
import cats.syntax.all._
import fs2.Pipe
import fs2.kafka._
import io.circe.Encoder
import stupwise.common.AppConfig.KafkaConfig.KafkaSettings
import stupwise.common.Codecs

trait Producer[F[_], V] {
  def send(v: V): F[Unit]
}

object Producer {
  def kafka[F[_]: Concurrent: Async, V: Encoder](
    kafkaSettings: KafkaSettings,
    topic: String
  ): fs2.Stream[F, Producer[F, V]] = {
    val settings = ProducerSettings[F, Unit, V](
      keySerializer = Serializer.unit,
      valueSerializer = Codecs.circeSerializer[F, V]
    )
      .withBootstrapServers(kafkaSettings.bootstrapServers)
      .withRetries(kafkaSettings.retries)

    KafkaProducer.stream(settings).map(s => (v: V) => s.produceOne_(topic, (), v).flatten.void)
  }

  def kafkaPipe[F[_]: Concurrent: Async, V: Encoder](
    kafkaSettings: KafkaSettings,
    topic: String
  ): Pipe[F, V, ProducerResult[Unit, Unit, V]] = { stream =>
    val settings = ProducerSettings[F, Unit, V](
      keySerializer = Serializer.unit,
      valueSerializer = Codecs.circeSerializer[F, V]
    )
      .withBootstrapServers(kafkaSettings.bootstrapServers)
      .withRetries(kafkaSettings.retries)

    def oneToProducerRecordsPipe =
      (stream: fs2.Stream[F, V]) => stream.chunks.map(chunk => ProducerRecords(chunk.map(ProducerRecord(topic, (), _))))

    stream.through(oneToProducerRecordsPipe).through(KafkaProducer.pipe(settings))

  }
  /*def kafka[F[_]: Concurrent: Async, V: Encoder](kafkaSettings: KafkaSettings, topic: String) =
    new Producer[F, V] {
      val settings = ProducerSettings[F, Unit, V](
        keySerializer = Serializer.unit,
        valueSerializer = Codecs.circeSerializer[F, V]
      )
        .withBootstrapServers(kafkaSettings.bootstrapServers)
        .withRetries(kafkaSettings.retries)

      val streamProducer = KafkaProducer.stream(settings)

      override def send(v: V): Stream[F, F[Unit]] = streamProducer.map(_.produceOne_(topic, (), v).flatten.void)


      override def through: Pipe[F, V, ProducerResult[Unit, Unit, V]] = { eventStream =>
        eventStream.through(oneToProducerRecordsPipe).through(KafkaProducer.pipe(settings))
      }

      private def oneToProducerRecordsPipe =
        (stream: fs2.Stream[F, V]) =>
          stream.chunks.map(chunk => ProducerRecords(chunk.map(ProducerRecord(topic, (), _))))
    }*/
}
