package tron4s.utils

import org.tron.protos.Contract._
import org.tron.protos.Tron.Transaction
import tron4s.Implicits._

object ContractUtils {

  def getOwner(contract: Transaction.Contract) = {

    ProtoUtils.fromContract(contract) match {
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

  def getTo(contract: Transaction.Contract): Option[String] = {

    ProtoUtils.fromContract(contract) match {
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

  def getAddresses(contract: Transaction.Contract): List[String] = {
    List(getOwner(contract)) ++ getTo(contract).map(List(_)).getOrElse(List.empty)
  }

}
