package stupwise

import stupwise.common.models.PlayerId
import stupwise.common.models.game.Game

import scala.util.Random

package object gamecore {

  implicit class GameOps(game: Game) {
    def assignRoles(players: List[PlayerId]) = {
      val roles      = game.settings
        .roleDistribution(players.length)
        .flatMap { case (id, count) =>
          List.fill(count)(id)
        }
        .toList
      val randomized = Random.shuffle(roles)
      (for ((p, r) <- players zip randomized) yield (p, r)).toMap
    }
  }
}
