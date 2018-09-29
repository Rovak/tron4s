package tron4s.importer

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink}
import akka.{Done, NotUsed}
import javax.inject.Inject
import org.tron.protos.Tron.{Block, Transaction}
import org.tron.protos.Tron.Transaction.Contract.ContractType.{AssetIssueContract, ParticipateAssetIssueContract, TransferAssetContract, TransferContract, VoteWitnessContract, WitnessCreateContract}
import tron4s.client.grpc.WalletClient
import tron4s.domain.Address
import tron4s.importer.StreamTypes.ContractFlow
import tron4s.importer.db.models.MaintenanceRoundModelRepository
import tron4s.Implicits._
import tron4s.services.SynchronisationService

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class ImportersFactory @Inject() (
  syncService: SynchronisationService,
  blockChainBuilder: BlockChainStreamBuilder,
  accountImporter: AccountImporter,
  walletClient: WalletClient,
  maintenanceRoundModelRepository: MaintenanceRoundModelRepository,
  blockImporter: BlockImporter) {

  /**
    * Build importers for Full Node
    * @return
    */
  def buildFullNodeImporters(importAction: ImportAction)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext) = async {

//    val redisCleaner = if (importAction.cleanRedisCache) Flow[Address].alsoTo(redisCacheCleaner) else Flow[Address]

    val maintenanceRound = blockImporter.buildVotingRoundImporter(await(maintenanceRoundModelRepository.findLatest)).toFlow

    val accountUpdaterFlow: Flow[Address, Address, NotUsed] = {
      if (importAction.updateAccounts) {
        if (importAction.asyncAddressImport) {
          accountImporter.buildAddressMarkDirtyFlow.toFlow
        } else {
          accountImporter.buildAddressSynchronizerFlow(walletClient)(actorSystem.scheduler, executionContext).toFlow
        }
      } else {
        Flow[Address]
      }
    }

    val eventsPublisher = if (importAction.publishEvents) {
      blockChainBuilder.publishContractEvents(
        actorSystem.eventStream,
        List(
          TransferContract,
          TransferAssetContract,
          WitnessCreateContract
        )
      ).toFlow
    } else {
      Flow[ContractFlow]
    }

    val blockFlow = blockImporter.fullNodeBlockImporter(importAction.confirmBlocks).toFlow

    BlockchainImporters()
      .addAddress(accountUpdaterFlow)
//      .addAddress(redisCleaner)
      .addContract(eventsPublisher)
      .addBlock(blockFlow)
      .addBlock(maintenanceRound)
  }

  /**
    * Build Solidity Blockchain importers
    */
  def buildSolidityImporters(importAction: ImportAction)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): BlockchainImporters = {

    val eventsPublisher: Flow[(Block, Transaction, Transaction.Contract), (Block, Transaction, Transaction.Contract), NotUsed] = if (importAction.publishEvents) {
      blockChainBuilder.publishContractEvents(
        actorSystem.eventStream,
        List(
          VoteWitnessContract,
          AssetIssueContract,
          ParticipateAssetIssueContract
        )).toFlow
    } else {
      Flow[ContractFlow]
    }

    val blockFlow = Flow[Block].alsoTo(blockImporter.buildSolidityBlockQueryImporter)

    BlockchainImporters()
        .addContract(eventsPublisher)
        .addBlock(blockFlow)
  }
}
