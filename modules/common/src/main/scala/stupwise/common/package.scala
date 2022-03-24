package stupwise

import stupwise.common.games.ClassicMafiaGame
import stupwise.common.models.game.{Game, GameVariant}

package object common {
  val availableGames: Map[GameVariant, Game] = Map(
    GameVariant.ClassicMafia -> ClassicMafiaGame
  )
}
