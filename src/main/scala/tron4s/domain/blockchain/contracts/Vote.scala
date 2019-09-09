package tron4s.domain.blockchain.contracts

import tron4s.domain.Address

case class Vote(
  address: Address,
  votes: Long,
)
