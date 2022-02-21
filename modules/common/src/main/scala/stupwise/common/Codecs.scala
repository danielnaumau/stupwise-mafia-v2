package stupwise.common

import cats.effect.Sync
import cats.implicits._
import fs2.kafka.{Deserializer, Serializer}
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredCodec, deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.syntax._
import stupwise.common.models.KafkaMsg.{Event, LobbyCommand}
import stupwise.common.models.State.RoomState
import stupwise.common.models.{KafkaMsg, Player, State}

import java.nio.charset.StandardCharsets

trait Codecs {
  implicit val withDiscriminatorConfig: Configuration = Configuration.default.withDiscriminator("type")

  implicit val commandCodec: Codec[LobbyCommand]  = deriveConfiguredCodec
  implicit val playerCodec: Codec[Player]         = deriveConfiguredCodec
  implicit val eventEncoder: Encoder[Event]       = deriveConfiguredEncoder
  implicit val eventDecoder: Decoder[Event]       = deriveConfiguredDecoder
  implicit val kafkaMsgCodec: Codec[KafkaMsg]     = deriveConfiguredCodec
  implicit val roomStateDecoder: Codec[RoomState] = deriveConfiguredCodec
  implicit val stateDecoder: Codec[State]         = deriveConfiguredCodec
}

object Codecs {

  private val printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit def circeSerializer[F[_]: Sync, A: Encoder] = Serializer.lift[F, A] { a =>
    printer.print(a.asJson).getBytes(StandardCharsets.UTF_8).pure[F]
  }

  implicit def circeDeserializer[F[_]: Sync, A: Decoder] = Deserializer.lift[F, A] { bytes =>
    parser.decode[A](new String(bytes, StandardCharsets.UTF_8)).liftTo[F]
  }

}
