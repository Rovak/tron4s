package tron4s.infrastructure.grpc

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import tron4s.infrastructure.network.NetworkNode

object GrpcPool {
  case class RequestChannel(ip: String, port: Int)
  case class RequestChannels(nodes: List[NetworkNode])
  case class Channel(channel: ManagedChannel)
  case class Channels(channel: List[ManagedChannel])
}

class GrpcPool extends Actor {

  import GrpcPool._

  var channels = Map[String, ManagedChannel]()

  def requestChannel(ip: String, port: Int) = {
    val childName = s"$ip:$port"
    channels.getOrElse(childName, {
      val channel = ManagedChannelBuilder
        .forAddress(ip, port)
        .usePlaintext(true)
        .build
      channels = channels + (childName -> channel)
      channel
    })
  }


  override def postStop(): Unit = {
    channels.values.foreach { channel =>
      channel.shutdown().awaitTermination(1, TimeUnit.SECONDS)
    }
  }

  def receive = {
    case RequestChannel(ip, port) =>
      sender() ! GrpcPool.Channel(requestChannel(ip, port))
    case RequestChannels(nodes) =>
      sender() ! Channels(nodes.map(x => requestChannel(x.ip, x.port)))
  }
}

