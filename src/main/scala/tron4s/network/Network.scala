package tron4s.network

import org.joda.time.DateTime
import tron4s.domain.Node.NodeType

case class Node(ip: String, port: Int, nodeType: NodeType)
case class GRPCState(active: Boolean, responseTime: Long)
case class HttpState(active: Boolean, responseTime: Long, url: String)
case class PingState(active: Boolean, responseTime: Long)

case class NetworkNode(
  ip: String,
  port: Int,
  nodeType: Int,
  hostname: String = "",
  lastSeen: DateTime = DateTime.now,
  permanent: Boolean = false,
  lastBlock: Long = 0L,
  grpcEnabled: Boolean = false,
  grpcResponseTime: Long = 0,
  pingOnline: Boolean = false,
  pingResponseTime: Long = 0,
  httpEnabled: Boolean = false,
  httpResponseTime: Long = 0,
  httpUrl: String = "",
  country: String = "",
  city: String = "",
  lat: Double = 0,
  lng: Double = 0)
