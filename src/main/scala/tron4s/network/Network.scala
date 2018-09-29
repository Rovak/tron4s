package tron4s.network

import org.joda.time.DateTime
import tron4s.domain.Node.{Full, NodeType}

case class Node(ip: String, port: Int, nodeType: NodeType)
case class GRPCState(active: Boolean = false, responseTime: Long = -1L)
case class HttpState(active: Boolean = false, responseTime: Long = -1L, url: String = "")
case class PingState(active: Boolean = false, responseTime: Long = -1L)

case class NetworkNode(
  ip: String,
  port: Int,
  nodeType: NodeType = Full,
  hostname: String = "",
  lastSeen: DateTime = DateTime.now,
  lastBlock: Long = 0L,
  grpc: GRPCState = GRPCState(),
  http: HttpState = HttpState(),
  ping: PingState = PingState(),
  country: String = "",
  city: String = "",
  lat: Double = 0,
  lng: Double = 0)
