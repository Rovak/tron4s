package tron4s.domain.blockchain

import com.google.protobuf.timestamp.Timestamp

case class Transaction[C <: Contract](
  contract: C,
  expires: Option[Timestamp] = None,
  timestamp: Option[Timestamp] = None,
) {

  def withContract[T <: Contract](contract: T) = {
    copy(contract = contract)
  }
}
