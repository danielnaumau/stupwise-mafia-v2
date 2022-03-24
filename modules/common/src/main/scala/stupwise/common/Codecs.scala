package stupwise.common

import cats.effect.Sync
import cats.implicits._
import fs2.kafka.{Deserializer, Serializer}
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.syntax._
import stupwise.common.models.KafkaMsg._
import stupwise.common.models.State.{GameState, RoomState}
import stupwise.common.models._
import stupwise.common.models.game.Target.PlayerTarget
import stupwise.common.models.game.{Role, Target, Vote}

import java.nio.charset.StandardCharsets

trait Codecs {
  implicit val withDiscriminatorConfig: Configuration = Configuration.default.withDiscriminator("type")

  implicit val playerTargetCodec: Codec[PlayerTarget] = deriveConfiguredCodec
  implicit val targetCodec: Codec[Target]             = deriveConfiguredCodec
  implicit val voteCodec: Codec[Vote]                 = deriveConfiguredCodec
  implicit val roleCodec: Codec[Role]                 = deriveConfiguredCodec
  implicit val playerCodec: Codec[Player]             = deriveConfiguredCodec
  implicit val lobbyPlayerCodec: Codec[LobbyPlayer]   = deriveConfiguredCodec

  // state traits
  implicit val gameStateDecoder: Codec[GameState] = deriveConfiguredCodec
  implicit val roomStateDecoder: Codec[RoomState] = deriveConfiguredCodec
  implicit val stateDecoder: Codec[State]         = deriveConfiguredCodec

  // msg traits
  implicit val gameCommandCodec: Codec[GameCommand]   = deriveConfiguredCodec
  implicit val gameEventCodec: Codec[GameEvent]       = deriveConfiguredCodec
  implicit val lobbyCommandCodec: Codec[LobbyCommand] = deriveConfiguredCodec
  implicit val lobbyEventCodec: Codec[LobbyEvent]     = deriveConfiguredCodec
  implicit val kafkaMsgCodec: Codec[KafkaMsg]         = deriveConfiguredCodec
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
