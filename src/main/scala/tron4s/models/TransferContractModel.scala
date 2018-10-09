package tron4s.models

import org.tron.protos.Tron.Transaction.Contract.ContractType

case class TransferContractModel(
  ownerAddress: String = "",
  toAddress: String = "",
  amount: Long,
  token: String) extends BaseContractModel {

  val contractType = if (token == "TRX") {
    ContractType.TransferContract.value
  } else {
    ContractType.TransferAssetContract.value
  }

  def toRecord = Record(
    Field("ownerAddress", ownerAddress),
    Field("toAddress", toAddress),
    Field("amount", amount.toString),
    Field("token ", token),
  )
}
