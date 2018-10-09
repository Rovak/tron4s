package tron4s.models

import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.domain.Address

case class WitnessModel(
  address: Address,
  url: String) extends BaseContractModel {

  val contractType = ContractType.WitnessCreateContract.value

  def toRecord = Record(
    Field("address", address.address),
    Field("url", url)
  )
}
