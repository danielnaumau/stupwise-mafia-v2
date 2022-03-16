package stupwise.common.models.game

import stupwise.common.models.PlayerId
import stupwise.common.models.game.Target.PlayerTarget

sealed trait Vote {
  def source: PlayerId
  def target: Target
}

object Vote {
  case class PlayerVote(source: PlayerId, target: PlayerTarget) extends Vote
}

sealed trait Target

object Target {
  case class PlayerTarget(target: PlayerId) extends Target
}
