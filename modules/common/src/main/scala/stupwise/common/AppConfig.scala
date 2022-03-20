package stupwise.common

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import stupwise.common.AppConfig.KafkaConfig.{KafkaSettings, KafkaTopics}
import stupwise.common.AppConfig.{KafkaConfig, RedisConfig}

final case class AppConfig(kafka: KafkaConfig, redis: RedisConfig)

object AppConfig {
  final case class KafkaConfig(settings: KafkaSettings, topics: KafkaTopics)

  object KafkaConfig {
    final case class KafkaSettings(bootstrapServers: String, retries: Int)
    final case class KafkaTopics(
      gameEvents: String,
      gameCommands: String,
      lobbyEvents: String,
      lobbyCommands: String,
      alerts: String
    )
  }

  final case class RedisConfig(uri: String)

  def load: AppConfig = ConfigSource.default.loadOrThrow[AppConfig] // wrap it
}
