package stupwise.common.models.game

import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed abstract class Alignment(val value: String) extends EnumEntry

object Alignment extends Enum[Alignment] with CirceEnum[Alignment] {
  val values = findValues

  case object Town    extends Alignment("Town")
  case object Mafia   extends Alignment("Mafia")
  case object Unknown extends Alignment("Unknown")
}
