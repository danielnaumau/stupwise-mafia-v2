package stupwise.common.kafka

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class KafkaTopicSettings(
  gameEvents: String, // events from processors to websocket
  commands: String    // commands from websocket to game processor
)

object KafkaTopicSettings {
  implicit val kafkaTopicSettingsReader: ConfigReader[KafkaTopicSettings] = deriveReader[KafkaTopicSettings]
}
