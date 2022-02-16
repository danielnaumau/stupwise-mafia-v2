package stupwise.lobby

import stupwise.lobby.Models.Command

final class Processor[F[_]](store: StateStore[F]) {

  def process(command: Command) = {
    for {
      roomState
    }
  }

}
