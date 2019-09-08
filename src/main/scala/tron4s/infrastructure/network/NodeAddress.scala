package tron4s.infrastructure.network

case class NodeAddress(ip: String, port: Int) {
  def info = s"$ip,$port"
}
