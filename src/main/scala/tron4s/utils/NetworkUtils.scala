package tron4s.utils

import java.net.{InetSocketAddress, Socket}

import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object NetworkUtils {

  /**
    * Try to open a socket for the given ip and port
    */
  def ping(ip: String, port: Int, timeout: FiniteDuration = 5.seconds)(implicit executionContext: ExecutionContext) = {
    Future {
      AutoClose(new Socket()).map { socket =>
        socket.connect(new InetSocketAddress(ip, port), timeout.toMillis.toInt)
      }
      true
    }.recover {
      case _ =>
        false
    }
  }

  /**
    * Try to open a socket for the given ip and port
    */
  def pingHttp(url: String, timeout: FiniteDuration = 5.seconds)(implicit executionContext: ExecutionContext, wsClient: StandaloneWSClient) = {
    wsClient
      .url(url)
      .withRequestTimeout(timeout)
      .get()
      .map(x => (x.body[JsValue] \ "blockID").isDefined)
  }
}
