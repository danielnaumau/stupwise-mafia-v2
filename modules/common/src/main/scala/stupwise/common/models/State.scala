package stupwise.common.models

import cats.implicits._
import stupwise.common.models.game.{GamePhase, Vote}

import scala.util.Random

sealed trait State {
  def key: String
  def keyPattern: String
  def version: Int
}

object State {
  final case class RoomState(players: List[LobbyPlayer], status: RoomStatus, version: Int, roomId: RoomId)
      extends State {
    override def key: String        = s"state-lobby-${roomId.value}-$version"
    override def keyPattern: String = s"state-lobby-${roomId.value}-*"

    def addPlayer(newPlayer: LobbyPlayer): Either[Reason, RoomState] = {
      val playerExists = players.exists(player => newPlayer.userName == player.userName || newPlayer.id === player.id)

      val existsError     =
        if (playerExists) s"Player ${newPlayer.userName} is already in the room $roomId" :: Nil else Nil
      val inProgressError = if (status == RoomStatus.GameInProgress) s"Game already in progress" :: Nil else Nil
      val errors          = existsError ++ inProgressError

      Either.cond(
        errors.isEmpty,
        this.copy(players = players :+ newPlayer, version = version + 1),
        Reason.apply(errors)
      )
    }

    def changeStatus(newStatus: RoomStatus): Either[Reason, RoomState] = {
      val allowed = newStatus match {
        case RoomStatus.Init           => status == RoomStatus.GameInProgress
        case RoomStatus.GameInProgress => status == RoomStatus.Init
        case RoomStatus.Unknown        => false
      }
      Either.cond(
        allowed,
        this.copy(status = newStatus),
        Reason(s"Room status cannot be changed from $status to $newStatus")
      )
    }
  }

  object RoomState {
    def empty(players: List[LobbyPlayer]): RoomState =
      RoomState(players, RoomStatus.Init, 0, RoomId(Random.alphanumeric.take(7).mkString("").toUpperCase()))
  }

  final case class GameState(
    players: List[Player],
    votes: List[Vote],
    phase: GamePhase,
    version: Int,
    roomId: RoomId
  ) extends State {
    override def key: String        = s"state-game-${roomId.value}-$version"
    override def keyPattern: String = s"state-game-${roomId.value}-*"
  }

  object GameState {
    def empty(players: List[Player], roomId: RoomId, phase: GamePhase): GameState =
      GameState(players, List.empty, phase, 0, roomId)
  }
}
