package stupwise.common.models.game

sealed trait Role {
  def id: RoleId
  def alignment: Alignment
}

object Roles {

  case object Mafia extends Role {
    override def id: RoleId = RoleId.Mafia

    override def alignment: Alignment = Alignment.Mafia
    //override def actions = Map(GamePhase.Day -> List(EliminatePlayerAction), GamePhase.Night -> List(KillAction))
  }

  case object Doctor extends Role {
    override def id: RoleId = RoleId.Doctor

    override def alignment: Alignment = Alignment.Town
  }

  case object Unknown extends Role {
    override def id: RoleId = RoleId.Unknown

    override def alignment: Alignment = Alignment.Unknown
  }

  case object Detective extends Role {
    override def id: RoleId = RoleId.Detective

    override def alignment: Alignment = Alignment.Town
  }

  case object Civilian extends Role {
    override def id: RoleId = RoleId.Civilian

    override def alignment: Alignment = Alignment.Town
  }
}
