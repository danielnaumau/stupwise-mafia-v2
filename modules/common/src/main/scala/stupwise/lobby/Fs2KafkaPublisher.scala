package stupwise.lobby

import cats.effect.{Async, Concurrent, ExitCode}
import cats.implicits._
import fs2.kafka._

object Fs2KafkaPublisher {

  def publish[F[_]: Concurrent: Async, V: Serializer[F, *]](
    eventStream: fs2.Stream[F, V],
    topic: String
  ): F[Unit] = {
    val settings = ProducerSettings[F, Unit, V]
      .withBootstrapServers("localhost:9092")
    val stream   = eventStream.through(oneToProducerRecordsPipe(topic)).through(KafkaProducer.pipe(settings))
    stream.compile.drain.as(ExitCode.Success)
  }

  private def oneToProducerRecordsPipe[F[_], K, V](topic: String) =
    (stream: fs2.Stream[F, V]) => stream.chunks.map(chunk => ProducerRecords(chunk.map(ProducerRecord(topic, (), _))))

  /*private def vectorToProducerRecordsPipe[F[_], A](topic: String) =
    (stream: fs2.Stream[F, Vector[A]]) => stream.map(v => ProducerRecords(v.map(ProducerRecord(topic, (), _))))*/
}
