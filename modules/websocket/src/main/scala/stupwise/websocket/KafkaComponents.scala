package stupwise.websocket

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import fs2.kafka.ConsumerRecord
import io.circe.{Decoder, Encoder}
import pureconfig.ConfigReader
import stupwise.common.{Fs2KafkaComponent, Fs2KafkaConsumer, Fs2KafkaPublisher}

trait KafkaComponents extends Fs2KafkaComponent[KafkaTopicSettings] {
  override implicit val kafkaTopicSettingsReader: ConfigReader[KafkaTopicSettings] =
    KafkaTopicSettings.kafkaTopicSettingsReader

  override def config: Config = ConfigFactory.load()

  def subscribe[V: Decoder](topic: String, processRecord: ConsumerRecord[Unit, V] => IO[Unit]) =
    Fs2KafkaConsumer
      .consume[IO, V](
        topic = topic,
        kafkaSettings = kafkaConfiguration.settings,
        processRecord = processRecord
      )

  def publish[V: Encoder](topic: String, eventStream: fs2.Stream[IO, V]) = Fs2KafkaPublisher
    .publish(
      topic = topic,
      kafkaSettings = kafkaConfiguration.settings,
      eventStream = eventStream
    )

  val gameEventsProcessor = new GameEventsProcessor.Live[IO]
}
