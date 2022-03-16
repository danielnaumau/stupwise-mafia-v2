package stupwise.common.models.game

trait Game {
  def description: String
  def settings: GameSettings
}
