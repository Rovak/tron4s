package tron4s.domain

object Node {
  sealed trait NodeType
  case object Full extends NodeType
  case object Solidity extends NodeType
}
