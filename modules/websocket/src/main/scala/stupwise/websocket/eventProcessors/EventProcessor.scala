package stupwise.websocket.eventProcessors

import stupwise.websocket.Protocol.OutMessage

trait EventProcessor[F[_], E] {
  def process(event: E): F[List[OutMessage]]
}
