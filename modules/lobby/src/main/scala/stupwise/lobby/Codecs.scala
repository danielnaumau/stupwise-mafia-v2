package stupwise.lobby

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import stupwise.common.models.KafkaMsg.Player

object Codecs {
  implicit val defaultConfig: Configuration  = Configuration.default
  implicit val playerDecoder: Codec[Player] = deriveConfiguredCodec
  implicit val stateDecoder: Codec[State]    = deriveConfiguredCodec
}
