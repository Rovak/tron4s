package tron4s.network

import akka.{Done, NotUsed}
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.Timeout
import com.typesafe.config.Config
import javax.inject.{Inject, Named}
import org.apache.commons.lang3.exception.ExceptionUtils
import org.joda.time.DateTime
import org.tron.api.api.{Node => _}
import play.api.Logger
import play.api.inject.ConfigurationProvider
import play.api.libs.concurrent.Futures
import play.api.libs.ws.StandaloneWSClient
import tron4s.domain.Node.Solidity
import tron4s.grpc.GrpcPool.{Channel, RequestChannel}
import tron4s.utils.StreamUtils

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Scans the network for nodes
  */
class NetworkScanner @Inject()(
  @Named("grpc-pool") actorRef: ActorRef,
  config: Config,
  actorSystem: ActorSystem,
  implicit val futures: Futures,
  implicit val WSClient: StandaloneWSClient) {

  import scala.concurrent.ExecutionContext.Implicits.global

  val debugEnabled = true
  var networkNodes = Map[String, NetworkNode]()

  val fullNodeList = List("54.236.37.243:50051")
  val solidityNodeList = List("39.105.66.80:50051")

  val decider: Supervision.Decider = { exc =>
    println("WATCHDOG ERROR", exc, ExceptionUtils.getStackTrace(exc))
    Supervision.Resume
  }

  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(actorSystem)
      .withSupervisionStrategy(decider))(actorSystem)

  def channelFromIp(ip: String, port: Int = 50051) = {
    implicit val timeout = Timeout(5.seconds)
    (actorRef ? RequestChannel(ip, port)).mapTo[Channel].map(_.channel)
  }

  def channelFromNode(node: NetworkNode) = {
    implicit val timeout = Timeout(5.seconds)
    (actorRef ? RequestChannel(node.ip, node.port)).mapTo[Channel].map(_.channel)
  }

  def nodeFromIp(nodeAddress: NodeAddress) = {

    for {
      channel <- channelFromIp(nodeAddress.ip, nodeAddress.port)
    } yield NodeChannel(nodeAddress.ip, nodeAddress.port, channel)
  }

  def buildReadStream: Flow[NodeAddress, NodeAddress, NotUsed] = {
    Flow[NodeAddress]
      .via(NetworkStreams.networkScanner(nodeFromIp))
      .via(StreamUtils.distinct)
      .map { node =>
        if (debugEnabled) {
          Logger.debug("Node: " + node)
        }
        node
      }
  }

  def readNodeChannels(ips: List[NodeAddress]) = {
    Source(ips)
      .via(buildReadStream.async)
      .via(buildReadStream.async)
  }

  def readNodeHealth: Flow[NodeAddress, NetworkNode, NotUsed] = {
    Flow[NodeAddress]
      .via(NetworkStreams.grpcPinger(nodeFromIp, 12).async)
      .map { node =>
        if (debugEnabled) {
          if (node.grpc.active) {
            Logger.debug("GRPC Online: " + node.hostname + ":" + node.ip)
          } else {
            Logger.debug("GRPC Offline: " + node.hostname + ":" + node.ip)
          }
        }
        node
      }
      .via(NetworkStreams.nodePinger(12).async)
      .map { node =>
        if (debugEnabled) {
          if (node.ping.active) {
            Logger.debug("Online: " + node.hostname + ":" + node.ip)
          } else {
            Logger.debug("Offline: " + node.hostname + ":" + node.ip)
          }
        }
        node
      }
      .via(NetworkStreams.httpPinger(12).async)
      .map { node =>
        if (debugEnabled) {
          if (node.http.active) {
            Logger.debug("HTTP Online: " + node.ip)
          } else {
            Logger.debug("HTTP Offline: " + node.ip)
          }
        }
        node
      }
  }

  def seedNodes = {
    fullNodeList.map { uri =>
      val Array(ip, port) = uri.split(":")
      NodeAddress(ip, port.toInt)
    }
  }

  def soliditySeedNodes = {
    solidityNodeList.map { uri =>
      val Array(ip, port) = uri.split(":")
      NodeAddress(ip, port.toInt)
    }
  }

  //  def includeGeo(node: NetworkNode) = {
  //    implicit val executionContext = workContext
  //
  //    geoIPService.findForIp(node.ip).map { geo =>
  //      node.copy(
  //        country = geo.country,
  //        city = geo.city,
  //        lat = geo.lat,
  //        lng = geo.lng,
  //      )
  //    }
  //  }

  def startReader() = {

    Source
      .single(seedNodes)
      .flatMapConcat(readNodeChannels)
      .via(readNodeHealth)
      //      .mapAsync(4)(includeGeo)
      .map(n => {
        updateNode(n)
        n
      })
      .runWith(Sink.ignore)
  }

  def getBestNodes(count: Int, filter: NetworkNode => Boolean = n => true) = {
    networkNodes.values
      .filter(filter)
      // Only take nodes which have their GRPC ports open
      .filter(_.grpc.active).toList
      // Sort by the best response time first
      .sortBy(_.grpc.responseTime).take(count)
  }

  def cleanup() = {
    val cleanupAfter = DateTime.now.minusMinutes(30)
    networkNodes = networkNodes
      .filter {
        case (_, node) if node.lastSeen.isAfter(cleanupAfter) =>
          true
        case _ =>
          false
      }
  }

  def start(): Future[Done] = {

    seedNodes.map { seedNodeAddress =>
      nodeFromIp(seedNodeAddress).map { _ =>
        updateNode(NetworkNode(
          ip = seedNodeAddress.ip,
          port = seedNodeAddress.port,
          grpc = GRPCState(true, 1)
        ))
      }
    }

    soliditySeedNodes.map { seedNodeAddress =>
      nodeFromIp(seedNodeAddress).map { _ =>
        updateNode(NetworkNode(
          ip = seedNodeAddress.ip,
          port = seedNodeAddress.port,
          nodeType = Solidity,
          grpc = GRPCState(true, 1)
        ))
      }
    }

    startReader()
  }

  def updateNode(node: NetworkNode): Unit = {
    networkNodes.get(node.ip) match {
      case None =>
        networkNodes = networkNodes + (node.ip -> node)
      case x =>
    }
  }
}
