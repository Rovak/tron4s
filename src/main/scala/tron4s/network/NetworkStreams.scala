package tron4s.network

import java.net.InetAddress
import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.stream.scaladsl.Flow
import org.tron.api.api.EmptyMessage
import play.api.libs.concurrent.Futures
import play.api.libs.concurrent.Futures._
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import tron4s.Implicits._
import tron4s.domain.Node.Full
import tron4s.utils.NetworkUtils

object NetworkStreams {

  /**
    * Scans the given IP address
    */
  def networkScanner(nodeFromIp: NodeAddress => Future[NodeChannel], parallel: Int = 4)(implicit executionContext: ExecutionContext) = {
    Flow[NodeAddress]
      .mapAsyncUnordered(parallel) { ip =>

        (for {
          nc <- nodeFromIp(ip)
          nodeList <- nc.full.withDeadlineAfter(1, TimeUnit.SECONDS).listNodes(EmptyMessage())
        } yield {
          nodeList.nodes.map(_.address.get).map(a => NodeAddress(a.host.decodeString, a.port))
        }).recover {
          case _ =>
            List.empty
        }
      }
      .mapConcat(_.toList)
  }

  /**
    * Ping the given IPS and returns a node
    *
    * @param nodeFromIp factory which creates a node from a string
    * @param parallel parallel number of processes
    */
  def grpcPinger(nodeFromIp: NodeAddress => Future[NodeChannel], parallel: Int = 4)(implicit executionContext: ExecutionContext, futures: Futures): Flow[NodeAddress, NetworkNode, NotUsed] = {
    Flow[NodeAddress]
      .mapAsyncUnordered(parallel) { nodeAddress =>

        val ia = InetAddress.getByName(nodeAddress.ip)
        val startPing = System.currentTimeMillis()

        (for {
          n <- nodeFromIp(nodeAddress)
          r <- n.full.withDeadlineAfter(5, TimeUnit.SECONDS).getNowBlock(EmptyMessage())
          response = System.currentTimeMillis() - startPing
          hostname <- Future(ia.getCanonicalHostName).withTimeout(6.seconds).recover { case _ => nodeAddress.ip }
        } yield {
          NetworkNode(
            ip = nodeAddress.ip,
            port = nodeAddress.port,
            lastBlock = r.getBlockHeader.getRawData.number,
            hostname = hostname,
            grpc = GRPCState(active = true, response)
          )
        }).recover {
          case _ =>
            NetworkNode(
              ip = nodeAddress.ip,
              hostname = ia.getCanonicalHostName,
              port = nodeAddress.port,
              grpc = GRPCState(active = false, -1L)
            )
        }
      }
  }

  /**
    * Ping the given IPS and returns a node
    *
    * @param parallel parallel number of processes
    */
  def nodePinger(parallel: Int = 4)(implicit executionContext: ExecutionContext, futures: Futures): Flow[NetworkNode, NetworkNode, NotUsed] = {
    Flow[NetworkNode]
      .mapAsyncUnordered(parallel) { networkNode =>

        val ia = InetAddress.getByName(networkNode.ip)
        val startPing = System.currentTimeMillis()

        (for {
          online <- NetworkUtils.ping(networkNode.ip, networkNode.port)
          response = System.currentTimeMillis() - startPing
        } yield {
          if (online) {
            networkNode.copy(ping = PingState(online, response))
          } else {
            networkNode.copy(ping = PingState())
          }
        }).recover {
          case _ =>
            networkNode.copy(ping = PingState())
        }
      }
  }

  /**
    * Ping the given IPS and returns a node
    *
    * @param parallel parallel number of processes
    */
  def httpPinger(parallel: Int = 4)(implicit executionContext: ExecutionContext, wsClient: StandaloneWSClient): Flow[NetworkNode, NetworkNode, NotUsed] = {
    Flow[NetworkNode]
      .mapAsyncUnordered(parallel) { networkNode =>

        val ia = InetAddress.getByName(networkNode.ip)
        val startPing = System.currentTimeMillis()

        (for {
          online <- if (networkNode.nodeType == Full) {
            NetworkUtils.pingHttp(s"http://${networkNode.ip}:8090/wallet/getnowblock")
          } else {
            NetworkUtils.pingHttp(s"http://${networkNode.ip}:8091/walletsolidity/getnowblock")
          }
          response = System.currentTimeMillis() - startPing
        } yield {
          if (online) {
            val httpPort = if (networkNode.nodeType == Full) 8090 else 8091
            networkNode.copy(http = HttpState(online, response, s"http://${networkNode.ip}:$httpPort"))
          } else {
            networkNode.copy(http = HttpState())
          }
        }).recover {
          case _ =>
            networkNode.copy(http = HttpState())
        }
      }
  }
}
