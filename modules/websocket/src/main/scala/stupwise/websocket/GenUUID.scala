package stupwise.websocket

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId

import java.util.UUID

trait GenUUID[F[_]] {
  def generate: F[UUID]
}

object GenUUIDInstances {
  implicit val ioInstance = new GenUUID[IO] {
    override def generate: IO[UUID] =
      UUID.randomUUID().pure[IO]
  }
}

object GenUUID {
  def apply[F[_]: GenUUID]: GenUUID[F] = implicitly[GenUUID[F]]
  def generate[F[_]: GenUUID]: F[UUID] = GenUUID[F].generate
}
