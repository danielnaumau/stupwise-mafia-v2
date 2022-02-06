package stupwise.lobby

import cats.effect.Sync
import cats.implicits._
import fs2.kafka.{Deserializer, Serializer}
import io.circe.syntax._
import io.circe.{parser, Decoder, Encoder, Printer}

import java.nio.charset.StandardCharsets

trait Codecs {

  private val printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit def circeJsonSerializer[F[_]: Sync, A](implicit encoder: Encoder[A]) = Serializer.lift[F, A] { a =>
    printer.print(a.asJson).getBytes(StandardCharsets.UTF_8).pure[F]
  }

  implicit def circeJsonDeserializer[F[_]: Sync, A](implicit decoder: Decoder[A]) = Deserializer.lift[F, A] { a =>
    parser.parse(if (a == null) "" else new String(a, StandardCharsets.UTF_8)).flatMap(_.as[A]) match {
      case Left(value)  =>
        throw new IllegalArgumentException(s"Invalid JSON object: ${value.getMessage}")
      case Right(value) => value.pure[F]
    }
  }

}
