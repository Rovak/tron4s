package tron4s.domain.blockchain

import com.google.protobuf.timestamp.Timestamp

case class Transaction(
  contracts: List[Contract] = List.empty,
  expires: Option[Timestamp] = None,
  timestamp: Option[Timestamp] = None,
)
