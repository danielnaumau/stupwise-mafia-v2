package stupwise.common.models.game

import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed abstract class GameVariant(val value: String) extends EnumEntry

object GameVariant extends Enum[GameVariant] with CirceEnum[GameVariant] {
  val values = findValues

  case object ClassicMafia extends GameVariant("Classic mafia")
}
