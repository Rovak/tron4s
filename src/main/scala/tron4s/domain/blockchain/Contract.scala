package tron4s.domain.blockchain

import tron4s.TokenId
import tron4s.domain.Address

sealed trait Contract

case class TransferTrxContract(
  from: Address,
  to: Address,
  amount: Long,
) extends Contract

case class TransferTokenContract(
  from: Address,
  to: Address,
  tokenId: TokenId,
  amount: Long,
) extends Contract

case class Vote(
  address: Address,
  votes: Long,
)

case class VoteWitnessContract(
  from: Address,
  votes: List[Vote] = List.empty
) extends Contract