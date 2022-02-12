package stupwise.common

import com.typesafe.config.Config
import pureconfig.{ConfigReader, ConfigSource}

trait Fs2KafkaComponent[KafkaTopicSettings] {

  implicit val kafkaTopicSettingsReader: ConfigReader[KafkaTopicSettings]

  def config: Config

  lazy val kafkaConfiguration =
    ConfigSource.fromConfig(config).at("kafka").loadOrThrow[KafkaConfiguration[KafkaTopicSettings]]

}
