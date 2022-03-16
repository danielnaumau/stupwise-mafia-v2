package stupwise.common.models.game

import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed abstract class RoleId(val value: String) extends EnumEntry

object RoleId extends Enum[RoleId] with CirceEnum[RoleId] {
  val values = findValues

  case object Detective extends RoleId("Detective")
  case object Doctor    extends RoleId("Doctor")
  case object Civilian  extends RoleId("Civilian")
  case object Mafia     extends RoleId("Mafia")
  case object Unknown   extends RoleId("Unknown")
}
