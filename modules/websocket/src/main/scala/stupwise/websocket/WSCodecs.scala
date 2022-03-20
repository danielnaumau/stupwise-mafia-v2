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
  def decode[In](json: String)(implicit d: Decoder[In]): Either[Error, In] =
    jawn.decode[In](json)

  def encode[Out](outcomeMessage: Out)(implicit e: Encoder[Out]): String =
    outcomeMessage.asJson.noSpaces
}
