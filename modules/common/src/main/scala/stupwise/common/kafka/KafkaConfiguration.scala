package stupwise.common.kafka

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class KafkaConfiguration[T](
  settings: KafkaSettings,
  topics: T
)

object KafkaConfiguration {
  implicit def kafkaConfigurationReader[T](implicit cr: ConfigReader[T]) = deriveReader[KafkaConfiguration[T]]
}
