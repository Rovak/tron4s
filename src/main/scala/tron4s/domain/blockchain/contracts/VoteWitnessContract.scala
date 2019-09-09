package tron4s.domain.blockchain.contracts

import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.domain.Address
import tron4s.domain.blockchain.Contract

case class VoteWitnessContract(
  from: Address,
  votes: List[Vote] = List.empty
) extends Contract {
  val contractType = ContractType.VoteWitnessContract
}
