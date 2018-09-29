package tron4s.network

case class NodeAddress(ip: String, port: Int) {
  def info = s"$ip,$port"
}
