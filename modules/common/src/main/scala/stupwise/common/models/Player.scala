package stupwise.common.models

import stupwise.common.models.game.{Role, Roles}

final case class LobbyPlayer(id: PlayerId, userName: String)

final case class Player(id: PlayerId, role: Role = Roles.Unknown)
