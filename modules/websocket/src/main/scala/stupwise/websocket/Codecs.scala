package stupwise.websocket

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Error, jawn}
import stupwise.websocket.Protocol._

object Codecs {
  implicit val withDiscriminatorConfig: Configuration = Configuration.default.withDiscriminator("msgType")

  implicit val incomeMessageDecoder: Decoder[IncomeMessage] = deriveConfiguredDecoder
  implicit val outcomeMessageEncoder: Encoder[OutcomeMessage] = deriveConfiguredEncoder

  def decode(json: String): Either[Error, IncomeMessage] =
    jawn.decode[IncomeMessage](json)

  def encode(outcomeMessage: OutcomeMessage): String =
    outcomeMessage.asJson.noSpaces
}
