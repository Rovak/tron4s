package tron4s.models

import ch.qos.logback.core.encoder.ByteArrayUtil
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.domain.Address

case class SmartContractTriggerModel(
  ownerAddress: Address,
  contractAddress: Address,
  data: Array[Byte] = Array.empty) extends BaseContractModel {

  val contractType = ContractType.TriggerSmartContract.value

  def toRecord = Record(
    Field("ownerAddress", ownerAddress.address),
    Field("contractAddress", contractAddress.address),
    Field("data", ByteArrayUtil.toHexString(data)),
  )
}
