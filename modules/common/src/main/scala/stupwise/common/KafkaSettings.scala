package stupwise.common

import pureconfig.generic.semiauto.deriveReader

final case class KafkaSettings(
  bootstrapServers: String,
  retries: Int
)

object KafkaSettings {
  implicit def kafkaConsumerConfigReader = deriveReader[KafkaSettings]
}
