package stupwise.lobby

import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp}
import fs2.kafka.{ConsumerRecord, ProducerSettings}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}

object ProducerExample extends IOApp {

  case class Hey(field: String)

  object Hey {
    implicit val withDiscriminatorConfig: Configuration = Configuration.default

    implicit val encoder = deriveConfiguredEncoder[Hey]
    implicit val decoder = deriveConfiguredDecoder[Hey]
  }

  def run(args: List[String]): IO[ExitCode] = {

    val runtime = cats.effect.unsafe.IORuntime.global

    val settings = ProducerSettings[IO, Unit, String]
      .withBootstrapServers("localhost:9092")

    def processRecord(record: ConsumerRecord[Unit, String]): IO[Unit] =
      IO(println(s"Processing record: $record"))

    lazy val testEventsQueue = Queue.unbounded[IO, Hey].unsafeRunSync()(runtime)
    //val publishPipe = produce[F, Unit, V, Unit](settings)
    //val stream      = eventStream.through(oneToProducerRecordsPipe[F, Unit, V](topic)).through(publishPipe)
    /*val stream               =
      Fs2KafkaPublisher.publish(
        testEventsQueue,
        "test"
      )*/
    /*.subscribeTo("test")
      .partitionedRecords
      .map { partitionStream =>
        partitionStream.evalMap { committable =>
          processRecord(committable.record)
        }
      }
      .parJoinUnbounded*/

    //stream.compile.drain.as(ExitCode.Success)
    IO.unit.as(ExitCode.Success)
  }
}
