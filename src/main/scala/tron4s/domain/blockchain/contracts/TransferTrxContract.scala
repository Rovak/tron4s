package tron4s.domain.blockchain.contracts

import org.tron.protos.Contract.TransferContract
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.domain.Address
import tron4s.domain.blockchain.Contract
import tron4s.infrastructure.protobuf.ProtoBuilder

case class TransferTrxContract(
  from: Address,
  to: Address,
  amount: Long,
) extends Contract with ProtoBuilder[TransferContract] {

  val contractType = ContractType.TransferContract

  override def buildProto = {
    TransferContract(
      ownerAddress = from.toByteString,
      toAddress = to.toByteString,
      amount = amount
    )
  }
}
