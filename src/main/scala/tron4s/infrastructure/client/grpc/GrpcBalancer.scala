package tron4s.infrastructure.client.grpc

import akka.actor.{Actor, ActorRef, Cancellable, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.Config
import javax.inject.Inject
import org.tron.api.api.WalletGrpc.WalletStub
import tron4s.domain.network.NodeAddress

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

case class GrpcRequest(request: WalletStub => Future[Any])
case class GrpcRetry(request: GrpcRequest, sender: ActorRef)
case class GrpcResponse(response: Any, ip: NodeAddress)
case class GrpcBlock(num: Long, hash: String)
case class OptimizeNodes()
case class GrpcBalancerStats(
  activeNodes: List[GrpcStats] = List.empty,
  nodes: List[GrpcStats] = List.empty)
case class GrpcBalancerRequest()

case class GrpcStats(
  ref: ActorRef,
  ip: String,
  responseTime: Long,
  blocks: List[GrpcBlock],
  requestHandled: Int = 0,
  requestErrors: Int = 0
)

object GrpcBalancerOptions {
  val pingInterval = 9.seconds
  val blockListSize = 12
}

/**
  * Manages the seedNodes and periodically checks which nodes are the fastest
  * The fastest nodes will be used to handle GRPC calls
  */
class GrpcBalancer @Inject() (config: Config) extends Actor {

  val seedNodes = config.getStringList("fullnode.list").asScala.map { uri =>
    val Array(ip, port) = uri.split(":")
    NodeAddress(ip, port.toInt)
  }.toList

  val maxClients = config.getInt("grpc.balancer.maxClients")

  // Contains the stats of all the seednodes
  var nodeStatuses = Map[String, GrpcStats]()
  var router: Router = Router(RoundRobinRoutingLogic(), Vector.empty)
  var totalStats = GrpcBalancerStats()

  def buildRouter(nodeIps: List[NodeAddress]) = {
    val routeRefs = nodeIps.map { ip =>
      context.actorOf(Props(classOf[GrpcClient], ip))
    }

    buildRouterWithRefs(routeRefs)
  }

  def buildRouterWithRefs(nodeIps: List[ActorRef]) = {
    val routees = nodeIps.map { ref =>
      context watch ref
      ActorRefRoutee(ref)
    }

    Router(RoundRobinRoutingLogic(), routees.toVector)
  }

  /**
    * Reconfigure the router to only take the fastest nodes
    */
  def optimizeNodes() = {

    // Cleanup nodes which don't share the common chain
    val chainCounts = nodeStatuses.values
      .flatMap(_.blocks.map(_.hash))
      .groupBy(x => x)
      .map(x => (x._1, x._2.size))

    if (chainCounts.nonEmpty) {

      // Find the most common block hash
      val mostCommonBlockHash = chainCounts.toList.maxBy(x => x._2)._1

      // Find all the nodes which have the common hash in their recent blocks
      val validChainNodes = for {
        (_, stats) <- nodeStatuses
        if stats.blocks.exists(_.hash == mostCommonBlockHash)
      } yield stats

      // Take the fastest nodes
      val sortedNodes = validChainNodes.toList.sortBy(_.responseTime)
      val fastestNodes = sortedNodes.take(maxClients)

      totalStats = GrpcBalancerStats(
        activeNodes = fastestNodes,
        nodes = sortedNodes.drop(maxClients)
      )

      router = buildRouterWithRefs(fastestNodes.map(_.ref))
    }
  }

  var pinger: Option[Cancellable] = None

  override def preStart(): Unit = {
    import context.dispatcher
    pinger = Some(context.system.scheduler.schedule(4.second, 6.seconds, self, OptimizeNodes()))
    router = buildRouter(seedNodes)
  }

  override def postStop(): Unit = {
    pinger.foreach(_.cancel())
  }

  def receive = {
    case w: GrpcRequest ⇒
      router.route(w, sender())
    case GrpcRetry(request, s) =>
      router.route(request, s)
    case stats: GrpcStats =>
      nodeStatuses = nodeStatuses ++ Map(stats.ip -> stats)
    case Terminated(a) ⇒
      router = router.removeRoutee(a)
    case OptimizeNodes() =>
      optimizeNodes()
    case GrpcBalancerRequest() =>
      sender() ! totalStats
  }
}


