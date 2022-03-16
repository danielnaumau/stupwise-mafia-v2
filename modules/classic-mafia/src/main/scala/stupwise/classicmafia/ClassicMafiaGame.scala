package stupwise.classicmafia

import stupwise.common.models.game.Roles._
import stupwise.common.models
import stupwise.common.models.game.GamePhase.{Day, Night}
import stupwise.common.models.game._
import stupwise.common.models.{RoleKnowledge, RoleOrder}

case object ClassicMafiaGame extends Game {

  override def description: String = "Classic mafia game"

  override def settings: GameSettings = new GameSettings {

    override def minPlayers: Int = 4

    override def maxPlayers: Int = 12

    override def startPhase: GamePhase = Day

    override def phases: List[GamePhase] = List(Day, Night)

    override def roles: List[Role] = List(Civilian, Mafia, Detective, Doctor)

    override def roleOrder: models.RoleOrder = RoleOrder(
      Map[GamePhase, List[Role]](Night -> List(Roles.Detective, Roles.Mafia, Roles.Doctor))
    )

    override def roleKnowledge: models.RoleKnowledge = RoleKnowledge(Map(Roles.Mafia -> List(Roles.Mafia)))

    override def roleDistribution = {
      case 0                             => Map.empty
      case 1                             => Map(Roles.Civilian -> 1)
      case 2                             => Map(Roles.Mafia -> 1, Roles.Civilian -> 1)
      case 3                             => Map(Roles.Mafia -> 1, Roles.Detective -> 1, Roles.Civilian -> 1)
      case 4                             => Map(Roles.Mafia -> 1, Roles.Detective -> 1, Roles.Doctor -> 1, Roles.Civilian -> 1)
      case playersCount @ (5 | 6 | 7)    =>
        Map(Roles.Mafia -> 2, Roles.Detective -> 1, Roles.Doctor -> 1, Roles.Civilian -> (playersCount - 4))
      case playersCount @ (8 | 9)        =>
        Map(Roles.Mafia -> 3, Roles.Detective -> 1, Roles.Doctor -> 1, Roles.Civilian -> (playersCount - 5))
      case playersCount @ (10 | 11 | 12) =>
        Map(Roles.Mafia -> 4, Roles.Detective -> 1, Roles.Doctor -> 1, Roles.Civilian -> (playersCount - 6))
      case _                             => Map.empty
    }
  }
}
