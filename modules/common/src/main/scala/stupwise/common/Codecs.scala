package stupwise.common

import cats.effect.Sync
import cats.implicits._
import fs2.kafka.{Deserializer, Serializer}
import io.circe.syntax._
import io.circe.{parser, Decoder, Encoder, Printer}

import java.nio.charset.StandardCharsets

object Codecs {

  private val printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit def circeSerializer[F[_]: Sync, A: Encoder] = Serializer.lift[F, A] { a =>
    printer.print(a.asJson).getBytes(StandardCharsets.UTF_8).pure[F]
  }

  implicit def circeDeserializer[F[_]: Sync, A: Decoder] = Deserializer.lift { bytes =>
    parser.decode[A](new String(bytes)).liftTo[F]
  }

}
