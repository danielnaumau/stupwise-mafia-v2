package stupwise.websocket

import cats.effect.{Async, ExitCode}
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder2

object HttpServer {
  def makeWebsocket[F[_]: Async](
    routes: WebSocketBuilder2[F] => HttpRoutes[F],
    port: Int
  ): fs2.Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(port, "0.0.0.0")
      .withHttpWebSocketApp(routes(_).orNotFound)
      .serve
}
