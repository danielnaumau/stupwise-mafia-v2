package stupwise.websocket

import io.circe.generic.extras.semiauto.{deriveConfiguredCodec, deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.syntax.EncoderOps
import io.circe.{jawn, Codec, Decoder, Encoder, Error}
import stupwise.common.Codecs
import stupwise.common.models.views.{GameInfoView, RoleSetupEntry}
import stupwise.websocket.Protocol._

trait WSCodecs extends Codecs {
  implicit val roleSetupCodec: Codec[RoleSetupEntry]      = deriveConfiguredCodec
  implicit val gameViewCodec: Codec[GameInfoView]         = deriveConfiguredCodec
  implicit val incomeMessageDecoder: Decoder[InMessage]   = deriveConfiguredDecoder
  implicit val outcomeMessageEncoder: Encoder[OutMessage] = deriveConfiguredEncoder
}

object WSCodecs {
  def decode(json: String)(implicit d: Decoder[InMessage]): Either[Error, InMessage] =
    jawn.decode[InMessage](json)

  def encode(outcomeMessage: OutMessage)(implicit e: Encoder[OutMessage]): String =
    outcomeMessage.asJson.noSpaces
}
