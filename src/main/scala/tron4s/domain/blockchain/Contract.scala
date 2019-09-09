package tron4s.domain.blockchain

import org.tron.protos.Tron.Transaction.Contract.ContractType

trait Contract {
  def contractType: ContractType
}








