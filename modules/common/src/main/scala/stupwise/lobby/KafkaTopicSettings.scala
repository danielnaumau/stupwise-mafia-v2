package stupwise.lobby

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class KafkaTopicSettings(
  lobby: String
)

object KafkaTopicSettings {
  implicit val kafkaTopicSettingsReader: ConfigReader[KafkaTopicSettings] = deriveReader[KafkaTopicSettings]
}
