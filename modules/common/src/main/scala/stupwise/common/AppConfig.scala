package stupwise.common

import pureconfig.ConfigSource
import stupwise.common.AppConfig.{KafkaConfig, RedisConfig}
import stupwise.common.AppConfig.KafkaConfig.{KafkaSettings, KafkaTopics}
import pureconfig.generic.auto._

final case class AppConfig(kafka: KafkaConfig, redis: RedisConfig)

object AppConfig {
  final case class KafkaConfig(settings: KafkaSettings, topics: KafkaTopics)

  object KafkaConfig {
    final case class KafkaSettings(bootstrapServers: String, retries: Int)
    final case class KafkaTopics(gameEvents: String, commands: String)
  }

  final case class RedisConfig(uri: String)

  def load: AppConfig = ConfigSource.default.loadOrThrow[AppConfig] // wrap it
}