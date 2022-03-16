package stupwise.common.models.game

import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed abstract class GamePhase(val name: String) extends EnumEntry

object GamePhase extends Enum[GamePhase] with CirceEnum[GamePhase] {
  val values = findValues

  case object Day   extends GamePhase("day")
  case object Night extends GamePhase("night")
}
