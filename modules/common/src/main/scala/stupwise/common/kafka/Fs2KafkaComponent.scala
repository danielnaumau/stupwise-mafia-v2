package stupwise.common.kafka

import stupwise.common.AppConfig
import stupwise.common.AppConfig.KafkaConfig

trait Fs2KafkaComponent {
  val kafkaConfig: KafkaConfig = AppConfig.load.kafka
}
