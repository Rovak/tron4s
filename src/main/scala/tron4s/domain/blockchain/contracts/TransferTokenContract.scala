package tron4s.domain.blockchain.contracts

import org.tron.protos.Contract.{TransferAssetContract, TransferContract}
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.TokenId
import tron4s.domain.Address
import tron4s.domain.blockchain.Contract
import tron4s.infrastructure.protobuf.ProtoBuilder
import tron4s.Implicits._

case class TransferTokenContract(
  from: Address,
  to: Address,
  tokenId: TokenId,
  amount: Long,
) extends Contract with ProtoBuilder[TransferAssetContract] {

  val contractType = ContractType.TransferAssetContract

  override def buildProto = {
    TransferAssetContract(
      ownerAddress = from.toByteString,
      toAddress = to.toByteString,
      amount = amount,
      assetName = tokenId.toString.toByteString
    )
  }
}
