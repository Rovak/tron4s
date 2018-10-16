package tron4s.utils

import cats.kernel.instances
import cats.kernel.instances.hash
import org.joda.time.DateTime
import org.tron.protos.Contract._
import org.tron.protos.Tron.{Block, Transaction}
import tron4s.importer.db.models
import tron4s.Implicits._
import tron4s.domain.Address
import tron4s.importer.db.models._
import tron4s.models.{BaseContractModel, TokenModel}

object ModelUtils {

  /**
    * Converts transaction to database model
    * @return
    */
  def transactionToModel(trx: Transaction, block: Block) = {
    val transactionHash = trx.hash
    val header = block.getBlockHeader.getRawData
    val transactionTime = new DateTime(header.timestamp)

    models.TransactionModel(
      hash = transactionHash,
      block = header.number,
      timestamp = transactionTime,
      ownerAddress = ContractUtils.getOwner(trx.getRawData.contract.head),
      contractData = TransactionSerializer.serializeContract(trx.getRawData.contract.head),
      contractType = trx.getRawData.contract.head.`type`.value,
    )
  }

  def fromProto(assetIssueContract: AssetIssueContract) = {
      TokenModel(
        ownerAddress = Address(assetIssueContract.ownerAddress.encode58),
        name = assetIssueContract.name.decodeString.trim,
        abbreviation = assetIssueContract.abbr.decodeString.trim,
        totalSupply = assetIssueContract.totalSupply,
        startTime = new DateTime(assetIssueContract.startTime),
        endTime = new DateTime(assetIssueContract.endTime),
        description = assetIssueContract.description.decodeString,
        url = assetIssueContract.url.decodeString,
      )
  }

  def contractModelFromProto(assetIssueContract: Transaction): Option[BaseContractModel] = {

    ProtoUtils.fromContract(assetIssueContract.getRawData.contract.head).flatMap {
      case c: TransferContract =>
        Some(tron4s.models.TransferContractModel(
          ownerAddress = c.ownerAddress.encode58,
          toAddress = c.toAddress.encode58,
          amount = c.amount,
          token = "TRX"))

      case c: TransferAssetContract =>
        Some(tron4s.models.TransferContractModel(
          ownerAddress = c.ownerAddress.encode58,
          toAddress = c.toAddress.encode58,
          amount = c.amount,
          token = c.assetName.decodeString))

      case c: VoteWitnessContract =>
        val votes = c.votes.map(x => (x.voteAddress.encode58, x.voteCount)).toMap
        Some(VoteContractModel(c.ownerAddress.encode58, votes))

      case c: WitnessCreateContract =>
        Some(tron4s.models.WitnessModel(
          address = Address(c.ownerAddress.encode58),
          url = c.url.decodeString))

      case c: WitnessUpdateContract =>
        Some(tron4s.models.WitnessModel(
          address = Address(c.ownerAddress.encode58),
          url = c.updateUrl.decodeString))

      case c: WithdrawBalanceContract =>
        Some(tron4s.models.WithdrawBalanceModel(
          address = Address(c.ownerAddress.encode58)))

      case c: AccountUpdateContract =>
        Some(tron4s.models.AccountUpdateModel(
          address = Address(c.ownerAddress.encode58),
          username = c.accountName.decodeString
        ))

      case x =>
        None
    }
  }

  /**
    * Converts a contract to a database model
    */
  def contractToModel(contract: Transaction.Contract, trx: Transaction, block: Block): Option[Any] = {

    val header = block.getBlockHeader.getRawData
    val transactionHash = trx.hash
    val transactionTime = new DateTime(header.timestamp)

    ProtoUtils.fromContract(contract).flatMap {
      case c: TransferContract =>
        Some(TransferModel(
          transactionHash = transactionHash,
          block = header.number,
          timestamp = transactionTime,
          transferFromAddress = c.ownerAddress.encode58,
          transferToAddress = c.toAddress.encode58,
          amount = c.amount,
          confirmed = header.number == 0))

      case c: TransferAssetContract =>
        Some(TransferModel(
          transactionHash = transactionHash,
          block = header.number,
          timestamp = transactionTime,
          transferFromAddress = c.ownerAddress.encode58,
          transferToAddress = c.toAddress.encode58,
          amount = c.amount,
          tokenName = new String(c.assetName.toByteArray),
          confirmed = header.number == 0))

      case c: VoteWitnessContract =>
        val inserts = for (vote <- c.votes) yield {
          VoteWitnessContractModel(
            id = transactionHash,
            transaction = transactionHash,
            block = header.number,
            timestamp = transactionTime,
            voterAddress = c.ownerAddress.encode58,
            candidateAddress = vote.voteAddress.encode58,
            votes = vote.voteCount,
          )
        }

        Some(VoteWitnessList(c.ownerAddress.encode58, inserts.toList))

      case c: AssetIssueContract =>
        Some(AssetIssueContractModel(
          block = header.number,
          transaction = transactionHash,
          ownerAddress = c.ownerAddress.encode58,
          name = c.name.decodeString.trim,
          abbr = c.abbr.decodeString.trim,
          totalSupply = c.totalSupply,
          trxNum = c.trxNum,
          num = c.num,
          startTime = new DateTime(c.startTime),
          endTime = new DateTime(c.endTime),
          voteScore = c.voteScore,
          description = c.description.decodeString,
          url = c.url.decodeString,
          dateCreated = transactionTime,
        ).withFrozen(c.frozenSupply))

      case c: ParticipateAssetIssueContract =>
        Some(ParticipateAssetIssueModel(
          transaction_hash = transactionHash,
          ownerAddress = c.ownerAddress.encode58,
          toAddress = c.toAddress.encode58,
          amount = c.amount,
          block = header.number,
          token = c.assetName.decodeString,
          dateCreated = transactionTime))

      case c: WitnessCreateContract =>
        Some(WitnessModel(
          address = c.ownerAddress.encode58,
          url = c.url.decodeString))

      case c: WitnessUpdateContract =>
        Some(WitnessModel(
          address = c.ownerAddress.encode58,
          url = c.updateUrl.decodeString))

      case c: UpdateAssetContract =>
        Some(UpdateAssetModel(
          ownerAddress = c.ownerAddress.encode58,
          description = c.description.decodeString,
          url = c.url.decodeString))

//      case c: AccountCreateContract =>
//        c.ownerAddress.encode58
//      case c: DeployContract =>
//        c.ownerAddress.encode58
//      case c: VoteAssetContract =>
//        c.ownerAddress.encode58
//      case c: FreezeBalanceContract =>
//        c.ownerAddress.encode58
//      case c: UnfreezeBalanceContract =>
//        c.ownerAddress.encode58
//      case c: AccountUpdateContract =>
//        c.ownerAddress.encode58
//      case c: WithdrawBalanceContract =>
//        c.ownerAddress.encode58
//      case c: UnfreezeAssetContract =>
//        c.ownerAddress.encode58
//      case c: UpdateAssetContract =>
//        c.ownerAddress.encode58
      case _ =>
        None
    }
  }
}
