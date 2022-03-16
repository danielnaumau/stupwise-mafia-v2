package stupwise.common.models

import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed abstract class RoomStatus(val value: String) extends EnumEntry

object RoomStatus extends Enum[RoomStatus] with CirceEnum[RoomStatus] {
  val values = findValues

  case object Init           extends RoomStatus("Init")
  case object GameInProgress extends RoomStatus("In progress")
  case object Unknown        extends RoomStatus("Unknown")
}
