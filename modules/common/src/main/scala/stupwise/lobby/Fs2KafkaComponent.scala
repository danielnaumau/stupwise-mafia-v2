package stupwise.lobby

trait Fs2KafkaComponent[KafkaTopicSettings] extends Codecs {

  //implicit val kafkaTopicSettingsReader: ConfigReader[KafkaTopicSettings] = KafkaTopicSettings.kafkaTopicSettingsReader
}
