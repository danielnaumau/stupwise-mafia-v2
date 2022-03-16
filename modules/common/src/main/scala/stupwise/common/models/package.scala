package stupwise.common

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
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

  case class RoleOrder(priorities: Map[GamePhase, List[Role]])
  case class RoleKnowledge(knowledge: Map[Role, List[Role]]) // mafia should see another mafia players etc.
}
