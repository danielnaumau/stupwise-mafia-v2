package stupwise.common.kafka

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import fs2.kafka.ConsumerRecord
import io.circe.{Decoder, Encoder}
import pureconfig.ConfigReader

trait KafkaComponents extends Fs2KafkaComponent[KafkaTopicSettings] {
  override implicit val kafkaTopicSettingsReader: ConfigReader[KafkaTopicSettings] =
    KafkaTopicSettings.kafkaTopicSettingsReader

  override def config: Config = ConfigFactory.load()

  def subscribe[V: Decoder, R](topic: String, processRecord: ConsumerRecord[Unit, V] => IO[R]) =
    Fs2KafkaConsumer
      .consume[IO, V, R](
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
}
