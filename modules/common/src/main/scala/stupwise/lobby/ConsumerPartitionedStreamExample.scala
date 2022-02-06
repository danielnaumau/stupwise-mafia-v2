package stupwise.lobby

import cats.effect.{ExitCode, IO, IOApp}
import fs2.kafka.{AutoOffsetReset, ConsumerRecord, ConsumerSettings, KafkaConsumer}

object ConsumerPartitionedStreamExample extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val consumerSettings =
      ConsumerSettings[IO, Unit, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    def processRecord(record: ConsumerRecord[Unit, String]): IO[Unit] =
      IO(println(s"Processing record: $record"))

    val stream =
      KafkaConsumer
        .stream(consumerSettings)
        .subscribeTo("test")
        .partitionedRecords
        .map { partitionStream =>
          partitionStream.evalMap { committable =>
            processRecord(committable.record)
          }
        }
        .parJoinUnbounded

    stream.compile.drain.as(ExitCode.Success)
  }
}
