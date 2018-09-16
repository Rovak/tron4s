package tronweb4s.protocol

import org.tron.protos.Contract._
import org.tron.protos.Tron.Transaction
import tronweb4s.Base58Address
import tronweb4s.Implicits._

object ProtocolUtils {

  /**
    * Convert proto any contract to contract protobuf
    */
  def toContractModel(contract: Transaction.Contract): Any = {

    import org.tron.protos.Tron.Transaction.Contract.ContractType._

    val any = contract.getParameter
    contract.`type` match {
      case TransferContract =>
        org.tron.protos.Contract.TransferContract.parseFrom(any.value.toByteArray)

      case TransferAssetContract =>
        org.tron.protos.Contract.TransferAssetContract.parseFrom(any.value.toByteArray)

      case VoteWitnessContract =>
        org.tron.protos.Contract.VoteWitnessContract.parseFrom(any.value.toByteArray)

      case AssetIssueContract =>
        org.tron.protos.Contract.AssetIssueContract.parseFrom(any.value.toByteArray)

      case UpdateAssetContract =>
        org.tron.protos.Contract.UpdateAssetContract.parseFrom(any.value.toByteArray)

      case ParticipateAssetIssueContract =>
        org.tron.protos.Contract.ParticipateAssetIssueContract.parseFrom(any.value.toByteArray)

      case WitnessCreateContract =>
        org.tron.protos.Contract.WitnessCreateContract.parseFrom(any.value.toByteArray)

      case WitnessUpdateContract =>
        org.tron.protos.Contract.WitnessUpdateContract.parseFrom(any.value.toByteArray)

      case UnfreezeBalanceContract =>
        org.tron.protos.Contract.UnfreezeBalanceContract.parseFrom(any.value.toByteArray)

      case FreezeBalanceContract =>
        org.tron.protos.Contract.FreezeBalanceContract.parseFrom(any.value.toByteArray)

      case WithdrawBalanceContract =>
        org.tron.protos.Contract.WithdrawBalanceContract.parseFrom(any.value.toByteArray)

      case AccountUpdateContract =>
        org.tron.protos.Contract.AccountUpdateContract.parseFrom(any.value.toByteArray)

      case UnfreezeAssetContract =>
        org.tron.protos.Contract.UnfreezeAssetContract.parseFrom(any.value.toByteArray)

      case AccountCreateContract =>
        org.tron.protos.Contract.AccountCreateContract.parseFrom(any.value.toByteArray)

      case ProposalCreateContract =>
        org.tron.protos.Contract.ProposalCreateContract.parseFrom(any.value.toByteArray)

      case ProposalApproveContract =>
        org.tron.protos.Contract.ProposalApproveContract.parseFrom(any.value.toByteArray)

      case ProposalDeleteContract =>
        org.tron.protos.Contract.ProposalDeleteContract.parseFrom(any.value.toByteArray)

      case CreateSmartContract =>
        org.tron.protos.Contract.CreateSmartContract.parseFrom(any.value.toByteArray)

      case TriggerSmartContract =>
        org.tron.protos.Contract.TriggerSmartContract.parseFrom(any.value.toByteArray)

      case ExchangeCreateContract =>
        org.tron.protos.Contract.ExchangeCreateContract.parseFrom(any.value.toByteArray)

      case ExchangeInjectContract =>
        org.tron.protos.Contract.ExchangeInjectContract.parseFrom(any.value.toByteArray)

      case ExchangeWithdrawContract =>
        org.tron.protos.Contract.ExchangeWithdrawContract.parseFrom(any.value.toByteArray)

      case ExchangeTransactionContract =>
        org.tron.protos.Contract.ExchangeTransactionContract.parseFrom(any.value.toByteArray)

      case _ =>
        throw new Exception("Unknown Contract")
    }
  }


  def getOwnerAddress(contract: Transaction.Contract): Base58Address = {

    toContractModel(contract) match {
      case c: AccountCreateContract =>
        c.ownerAddress.encode58

      case c: TransferContract =>
        c.ownerAddress.encode58

      case c: TransferAssetContract =>
        c.ownerAddress.encode58

      case c: VoteAssetContract =>
        c.ownerAddress.encode58

      case c: VoteWitnessContract =>
        c.ownerAddress.encode58

      case c: AssetIssueContract =>
        c.ownerAddress.encode58

      case c: ParticipateAssetIssueContract =>
        c.ownerAddress.encode58

      case c: WitnessCreateContract =>
        c.ownerAddress.encode58

      case c: WitnessUpdateContract =>
        c.ownerAddress.encode58

      case c: FreezeBalanceContract =>
        c.ownerAddress.encode58

      case c: UnfreezeBalanceContract =>
        c.ownerAddress.encode58

      case c: AccountUpdateContract =>
        c.ownerAddress.encode58

      case c: WithdrawBalanceContract =>
        c.ownerAddress.encode58

      case c: UnfreezeAssetContract =>
        c.ownerAddress.encode58

      case c: UpdateAssetContract =>
        c.ownerAddress.encode58

      case c: ProposalCreateContract =>
        c.ownerAddress.encode58

      case c: ProposalApproveContract =>
        c.ownerAddress.encode58

      case c: ProposalDeleteContract =>
        c.ownerAddress.encode58

      case c: CreateSmartContract =>
        c.ownerAddress.encode58

      case c: TriggerSmartContract =>
        c.ownerAddress.encode58

      case c: BuyStorageBytesContract =>
        c.ownerAddress.encode58

      case c: BuyStorageContract =>
        c.ownerAddress.encode58

      case c: SellStorageContract =>
        c.ownerAddress.encode58

      case c: ExchangeCreateContract =>
        c.ownerAddress.encode58

      case c: ExchangeInjectContract =>
        c.ownerAddress.encode58

      case c: ExchangeWithdrawContract =>
        c.ownerAddress.encode58

      case c: ExchangeTransactionContract =>
        c.ownerAddress.encode58

      case _ =>
        ""
    }
  }

  def getReceiverAddress(contract: Transaction.Contract): Option[Base58Address] = {

    toContractModel(contract) match {
      case c: AccountCreateContract =>
        Some(c.accountAddress.encode58)

      case c: TransferContract =>
        Some(c.toAddress.encode58)

      case c: TransferAssetContract =>
        Some(c.toAddress.encode58)

      case c: ParticipateAssetIssueContract =>
        Some(c.toAddress.encode58)

      case _ =>
        None
    }
  }

  def getAddresses(contract: Transaction.Contract): List[Base58Address] = {
    List(getOwnerAddress(contract)) ++ getReceiverAddress(contract).map(List(_)).getOrElse(List.empty)
  }
}
