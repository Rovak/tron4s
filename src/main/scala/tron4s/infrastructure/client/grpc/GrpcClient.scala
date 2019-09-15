package tron4s.infrastructure.client.grpc

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Cancellable}
import io.grpc.ManagedChannelBuilder
import org.tron.api.api.{EmptyMessage, WalletGrpc}
import tron4s.Implicits._
import tron4s.domain.network.NodeAddress

import scala.concurrent.duration._

/**
  * Connects to GRPC and periodically pings the port to determine latency
  */
class GrpcClient(nodeAddress: NodeAddress) extends Actor {

  var pinger: Option[Cancellable] = None
  var latestBlocks = List[GrpcBlock]()
  var requestsHandled = 0
  var requestErrors = 0

  lazy val channel = ManagedChannelBuilder
    .forAddress(nodeAddress.ip, nodeAddress.port)
    .usePlaintext(true)
    .build

  lazy val walletStub = {
    WalletGrpc.stub(channel)
  }

  /**
    * Ping the node and gather stats, send it back to the balancer
    */
  def ping() = {
    import context.dispatcher

    val w = walletStub
    val startPing = System.currentTimeMillis()

    (for {
      block <- w.withDeadlineAfter(5, TimeUnit.SECONDS).getNowBlock(EmptyMessage())
      responseTime = System.currentTimeMillis() - startPing
    } yield {

      val currentBlock = GrpcBlock(block.getBlockHeader.getRawData.number, block.hash)

      // Keep a maximum of 5 recent blocks in the list
      latestBlocks = (if (latestBlocks.size > GrpcBalancerOptions.blockListSize) latestBlocks.drop(1) else latestBlocks) :+ currentBlock

      context.parent ! GrpcStats(
        ref = self,
        ip = nodeAddress.ip,
        responseTime = responseTime,
        blocks = latestBlocks,
        requestHandled = requestsHandled,
        requestErrors = requestErrors,
      )

      requestsHandled += 1
    }).recover {
      case _ =>
        context.parent ! GrpcStats(
          ref = self,
          ip = nodeAddress.ip,
          responseTime = 9999L,
          blocks = List.empty
        )
        requestErrors += 1
    }
  }

  def handleRequest(request: GrpcRequest, s: ActorRef) = {
    import context.dispatcher
    request.request(walletStub.withDeadlineAfter(5, TimeUnit.SECONDS)).map { x =>
      s ! GrpcResponse(x, nodeAddress)
      requestsHandled += 1
    }.recover {
      case _ =>
        requestErrors += 1
        context.parent.tell(GrpcRetry(request, s), s)
    }
  }

  override def preStart(): Unit = {
    import context.dispatcher
    pinger = Some(context.system.scheduler.schedule(0.second, GrpcBalancerOptions.pingInterval, self, "ping"))
  }

  override def postStop(): Unit = {
    pinger.foreach(_.cancel())
  }

  def receive = {
    case c: GrpcRequest =>
      handleRequest(c, sender())

    case "ping" =>
      ping()
  }
}
