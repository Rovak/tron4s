package tron4s.infrastructure.network

import org.joda.time.DateTime
import org.tron.protos.Tron.Account
import tron4s.domain.network.Node.{Full, NodeType}
import tron4s.infrastructure.exporter.{Field, Record, RecordFormatter}
import tron4s.models.AccountModel

case class Node(ip: String, port: Int, nodeType: NodeType)
case class GRPCState(active: Boolean = false, responseTime: Long = -1L)
case class HttpState(active: Boolean = false, responseTime: Long = -1L, url: String = "")
case class PingState(active: Boolean = false, responseTime: Long = -1L)

object NetworkNode {

  implicit val recordFormatter = new RecordFormatter[NetworkNode] {
    override def format(record: NetworkNode): Record = {
      Record(
        Field("ip", record.ip),
        Field("port", record.port.toString),
        Field("nodeType", record.nodeType.toString),
        Field("hostname", record.hostname),
        Field("lastSeen", record.lastSeen.toString),
        Field("lastBlock", record.lastBlock.toString),
        Field("country", record.country),
        Field("city", record.city),
        Field("lat", record.lat.toString),
        Field("lng", record.lng.toString),
      )
    }
  }
}

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