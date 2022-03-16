package stupwise.websocket

import cats.Applicative
import cats.effect.kernel.Sync
import cats.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._
import stupwise.common.GenUUID
import stupwise.common.models.KafkaMsg.{InitRoom, JoinRoom}
import stupwise.common.models._
import stupwise.websocket.Protocol.InMessage

import java.util.UUID

object Dispatcher {
  def dispatch[F[_]: Applicative: Sync: Logger: GenUUID](playerId: UUID, msg: InMessage): F[List[KafkaMsg]] = {
    val mappedMsg: F[List[KafkaMsg]] = msg match {
      case InMessage.InitRoom(userName)         =>
        GenUUID.generate.map(uuid => InitRoom(MsgId(uuid), LobbyPlayer(PlayerId(playerId), userName)) :: Nil)
      case InMessage.JoinRoom(roomId, userName) =>
        GenUUID.generate
          .map(uuid => JoinRoom(MsgId(uuid), roomId, LobbyPlayer(PlayerId(playerId), userName)))
          .map(_ :: Nil)
    }

    debug"Receive message from WS: $msg, playerId: $playerId" *> mappedMsg
  }
}
