package stupwise.common.models.game

sealed abstract class Role(val id: RoleId, val alignment: Alignment)

object Roles {

  case object Mafia extends Role(RoleId.Mafia, Alignment.Mafia)

  case object Doctor extends Role(RoleId.Doctor, Alignment.Town)

  case object Unknown extends Role(RoleId.Unknown, Alignment.Town)

  case object Detective extends Role(RoleId.Detective, Alignment.Town)

  case object Civilian extends Role(RoleId.Civilian, Alignment.Town)
}
