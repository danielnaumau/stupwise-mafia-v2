package stupwise.common.models.views

import stupwise.common.models.Player
import stupwise.common.models.game.{GamePhase, GameSettings, Role}

case class GameInfoView(
  phase: GamePhase,
  phases: List[GamePhase],
  roleSetup: List[RoleSetupEntry],
  players: List[Player]
)

object GameInfoView {
  def create(settings: GameSettings, players: List[Player]) =
    GameInfoView(
      settings.startPhase,
      settings.phases,
      players.map(_.role).groupBy(identity).map(r => RoleSetupEntry(r._1, r._2.length)).toList,
      players
    )
}

case class RoleSetupEntry(role: Role, count: Int)
