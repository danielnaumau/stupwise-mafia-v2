package stupwise.common.models.game

import stupwise.common.models.{RoleKnowledge, RoleOrder}

trait GameSettings {
  def minPlayers: Int
  def maxPlayers: Int // can be Option[Int]
  def startPhase: GamePhase
  def phases: List[GamePhase]
  def roles: List[Role]
  def roleOrder: RoleOrder
  def roleKnowledge: RoleKnowledge
  def roleDistribution: Int => Map[Role, Int]
}
