package tron4s.models

import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.domain.Address
import tron4s.infrastructure.exporter.{Field, Record}

case class WithdrawBalanceModel(
  address: Address
) extends BaseContractModel {

  val contractType = ContractType.WithdrawBalanceContract.value

  def toRecord = Record(
    Field("address", address.address),
  )
}
