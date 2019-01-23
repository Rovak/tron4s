package tron4s.models

import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.domain.Address

case class TriggerContractModel(
  address: Address,
  username: String) extends BaseContractModel {

  val contractType = ContractType.AccountUpdateContract.value

  def toRecord = Record(
    Field("address", address.address),
    Field("username", username),
  )
}
