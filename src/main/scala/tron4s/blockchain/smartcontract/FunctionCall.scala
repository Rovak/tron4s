package tron4s.blockchain.smartcontract

case class FunctionCall(name: String, params: Map[String, String] = Map.empty)
