package tron4s.importer

import javax.inject.Inject
import org.tron.protos.Tron.Transaction
import org.tron.protos.Tron.Transaction.Contract.ContractType.{AssetIssueContract, ParticipateAssetIssueContract, TransferAssetContract, TransferContract, UnfreezeBalanceContract, UpdateAssetContract, VoteWitnessContract, WitnessCreateContract, WitnessUpdateContract}
import slick.dbio.{Effect, NoStream}
import slick.sql.FixedSqlAction
import tron4s.importer.db.models._
import tron4s.Implicits._


/**
  * Builds queries from transaction contracts
  */
class ContractImporter @Inject()(
  blockModelRepository: BlockModelRepository,
  transactionModelRepository: TransactionModelRepository,
  transferRepository: TransferModelRepository,
  voteWitnessContractModelRepository: VoteWitnessContractModelRepository,
  witnessModelRepository: WitnessModelRepository,
  assetIssueContractModelRepository: AssetIssueContractModelRepository,
  participateAssetIssueRepository: ParticipateAssetIssueModelRepository,
) {

  type ContractQueryBuilder = PartialFunction[(Transaction.Contract.ContractType, Transaction.Contract, Any), Seq[FixedSqlAction[Int, NoStream, Effect.Write]]]

  def importWitnessCreate: ContractQueryBuilder = {
    case (WitnessCreateContract, _, witness: WitnessModel) =>
      Seq(witnessModelRepository.buildInsertOrUpdate(witness))
  }

  def importWitnessUpdate: ContractQueryBuilder = {
    case (WitnessUpdateContract, _, witness: WitnessModel) =>
      Seq(witnessModelRepository.buildInsertOrUpdate(witness))
  }

  def importWitnessVote: ContractQueryBuilder = {
    case (VoteWitnessContract, _, votes: VoteWitnessList) =>
      voteWitnessContractModelRepository.buildInsertVotes(votes.votes)
  }

  def importAssetIssue: ContractQueryBuilder = {
    case (AssetIssueContract, _, assetIssue: AssetIssueContractModel) =>
      Seq(assetIssueContractModelRepository.buildInsert(assetIssue))
  }

  def importAssetUpdateIssue: ContractQueryBuilder = {
    case (UpdateAssetContract, _, updateAssetModel: UpdateAssetModel) =>
      Seq(assetIssueContractModelRepository.buildUpdateAsset(updateAssetModel))
  }

  def importParticipateAssetIssue: ContractQueryBuilder = {
    case (ParticipateAssetIssueContract, _, participate: ParticipateAssetIssueModel) =>
      Seq(participateAssetIssueRepository.buildInsert(participate))
  }

  def importUnfreezeBalance: ContractQueryBuilder = {
    case (UnfreezeBalanceContract, contract, _) =>
      val unfreezeBalanceContract = org.tron.protos.Contract.UnfreezeBalanceContract.parseFrom(contract.getParameter.value.toByteArray)
      Seq(voteWitnessContractModelRepository.buildDeleteVotesForAddress(unfreezeBalanceContract.ownerAddress.encode58))
  }

  def importTransfers: ContractQueryBuilder = {
    case (TransferContract, _, transferModel: TransferModel) =>
      Seq(transferRepository.buildInsert(transferModel))

    case (TransferAssetContract, _, transferModel: TransferModel) =>
      Seq(transferRepository.buildInsert(transferModel))
  }

  def elseEmpty: ContractQueryBuilder = {
    case _ =>
      Seq.empty
  }

  def buildConfirmedEvents = {
    importWitnessCreate orElse
    importWitnessVote orElse
    importWitnessUpdate orElse
    importTransfers orElse
    importAssetIssue orElse
    importParticipateAssetIssue orElse
    importUnfreezeBalance orElse
    importAssetUpdateIssue
  }
}
