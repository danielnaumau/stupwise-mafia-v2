package stupwise.websocket.script

import com.github.andyglow.websocket.WebsocketClient
import io.circe.generic.extras.Configuration
import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, parser}
import stupwise.common.models.LobbyPlayer
import stupwise.websocket.Protocol.OutMessage.PlayerJoined

import java.util.concurrent.Semaphore

object GameStartScript extends App {

  implicit val withDiscriminatorConfig: Configuration = Configuration.default.withDiscriminator("type")

  implicit val playerCodec: Codec[LobbyPlayer] = deriveCodec
  implicit val outCodec: Codec[PlayerJoined] = deriveCodec

  var roomCode = "ABCDEF"
  val semaphore = new Semaphore(0)

  val roomCreatedPattern = "\\{\"roomId\":\"([A-Z\\d]+)\".+(\"RoomCreated\")}".r
  val playerJoinedPattern = "\\{\"roomId\":\"([A-Z\\d]+)\".+(\"PlayerJoined\")}".r
  val gameCreatedPattern = "\\{\"roomId\":\"([A-Z\\d]+)\".+(\"GameCreated\")}".r

  val playersNum = args.headOption.flatMap(_.toIntOption).getOrElse(5)

  val clients = List
    .fill(playersNum)(WebsocketClient[String]("ws://localhost:8080/v1/ws") {
      case str if roomCreatedPattern.matches(str) =>
        println(str)
        val roomCreatedPattern(code, _) = str
        roomCode = code
        semaphore.release()
      case str if playerJoinedPattern.matches(str) =>
        println(str)
        val msg = parser.decode[PlayerJoined](str)
        val playersJoined = msg.map(_.players.size).getOrElse(0)
        if (playersJoined == playersNum) semaphore.release()
      case str if gameCreatedPattern.matches(str) =>
        println(str)
        semaphore.release()
      case str => println(str)
    })

  val ws = clients.map(_.open())

  // 1. Init room
  ws.headOption.foreach(_ ! s"{\n\"userName\": \"player$playersNum\",\n\"type\": \"InitRoom\"\n}")

  // 2. Join room
  semaphore.acquire(1)
  ws.tail.zipWithIndex.foreach {
    case (el, index) => el ! s"{\n\"userName\": \"player$index\",\n\"roomId\":\"$roomCode\",\n\"type\": \"JoinRoom\"\n}"
  }

  // Start game
  semaphore.acquire()
  ws.headOption.foreach(_ ! s"{\n\"playerId\": \"ac2e559e-6086-4dc2-bb6c-20cfa9bf1eb3\",\n\"roomId\":\"$roomCode\",\n\"variant\": \"ClassicMafia\",\n\"type\": \"StartGame\"\n}")

  // Stop ws
  semaphore.acquire()
  clients.foreach(_.shutdownSync())
}
