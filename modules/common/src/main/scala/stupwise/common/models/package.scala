package stupwise.common

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import stupwise.common.models.RoomId
import stupwise.common.models.game.{GamePhase, Role}

import java.util.UUID

package object models {
  @newtype case class PlayerId(value: UUID)

  object PlayerId {
    implicit val decoder: Decoder[PlayerId] = deriving
    implicit val encoder: Encoder[PlayerId] = deriving
    implicit val playerIdEq: Eq[PlayerId]   = Eq.fromUniversalEquals
  }

  @newtype case class MsgId(value: UUID)

  object MsgId {
    implicit val decoder: Decoder[MsgId] = deriving
    implicit val encoder: Encoder[MsgId] = deriving
    implicit val msgIdEq: Eq[MsgId]      = Eq.fromUniversalEquals
  }

  @newtype case class RoomId(value: String)

  object RoomId {
    implicit val decoder: Decoder[RoomId] = deriving
    implicit val encoder: Encoder[RoomId] = deriving
    implicit val roomIdEq: Eq[RoomId]     = Eq.fromUniversalEquals
  }

  @newtype case class Reason(value: String)

  object Reason {
    def apply(errors: List[String]): Reason = Reason(errors.mkString(";"))

    implicit val decoder: Decoder[Reason] = deriving
    implicit val encoder: Encoder[Reason] = deriving
  }

  case class RoleOrder(priorities: Map[GamePhase, List[Role]])
  case class RoleKnowledge(knowledge: Map[Role, List[Role]]) // mafia should see another mafia players etc.
}
