package tron4s.services

import javax.inject.Inject
import org.tron.api.api.{EmptyMessage, NumberMessage}
import play.api.inject.ConfigurationProvider
import tron4s.client.grpc.WalletClient
import tron4s.domain.NodeState
import tron4s.importer.db.models.{AccountModelRepository, AddressBalanceModelRepository, BlockModelRepository}
import scala.concurrent.ExecutionContext.Implicits.global
import tron4s.Implicits._


class SynchronisationService @Inject() (
  walletClient: WalletClient,
  blockModelRepository: BlockModelRepository,
  accountModelRepository: AccountModelRepository,
  addressBalanceModelRepository: AddressBalanceModelRepository,
  configurationProvider: ConfigurationProvider) {

  val syncSolidity = configurationProvider.get.get[Boolean]("sync.solidity")

  /**
    * Reset all the blockchain data in the database
    */
  def resetDatabase() = {
    blockModelRepository.clearAll
  }

  /**
    * Checks if the chain is the same for the given block
    */
  def isSameChain(blockNumber: Long = 0) = {
    for {
      wallet <- walletClient.full
      dbBlock <- blockModelRepository.findByNumber(blockNumber)
      genesisBlock <- wallet.getBlockByNum(NumberMessage(blockNumber))
    } yield dbBlock.exists(_.hash == genesisBlock.hash)
  }

  /**
    * If the database has any blocks
    */
  def hasData = {
    blockModelRepository.findByNumber(0).map(_.isDefined)
  }

  /**
    * Last synchronized block in the database
    */
  def currentSynchronizedBlock = {
    blockModelRepository.findLatest
  }

  /**
    * Last confirmed block in the database
    */
  def currentConfirmedBlock = {
    blockModelRepository.findLatestUnconfirmed
  }

  def getFullNodeHashByNum(number: Long) = {
    for {
      wallet <- walletClient.full
      hash <- wallet.getBlockByNum(NumberMessage(number)).map(_.hash)
    } yield hash
  }

  def getSolidityHashByNum(number: Long) = {
    for {
      wallet <- walletClient.solidity
      hash <- wallet.getBlockByNum(NumberMessage(number)).map(_.hash)
    } yield hash
  }

  def getDBHashByNum(number: Long) = {
    blockModelRepository.findByNumber(number).map {
      case Some(block) =>
        block.hash
      case _ =>
        ""
    }
  }

  /**
    * Retrieves the import status for full and solidity nodes
    */
  def nodeState = {
    for {
      wallet <- walletClient.full
      walletSolidity <- walletClient.solidity

      lastFulNodeNumberF = wallet.getNowBlock(EmptyMessage())
      lastSolidityNumberF = walletSolidity.getNowBlock(EmptyMessage())
      lastDatabaseBlockF = blockModelRepository.findLatest
      lastUnconfirmedDatabaseBlockF = blockModelRepository.findLatestUnconfirmed

      lastFulNodeNumber <- lastFulNodeNumberF.map(_.getBlockHeader.getRawData.number).recover { case _ => -1L }
      lastSolidityNumber <- lastSolidityNumberF.map(_.getBlockHeader.getRawData.number).recover { case _ => -1L }
      lastDatabaseBlock <- lastDatabaseBlockF
      lastUnconfirmedDatabaseBlock <- lastUnconfirmedDatabaseBlockF

      lastFullNodeBlockHash <- lastFulNodeNumberF.map(_.hash).recover { case _ => "" }
      lastSolidityNodeBlockHash <- lastSolidityNumberF.map(_.hash).recover { case _ => "" }
      lastDbBlockHash <- lastDatabaseBlockF.map(_.get.hash).recover { case _ => "" }
    } yield NodeState(
      solidityEnabled = syncSolidity,
      fullNodeBlock = lastFulNodeNumber,
      solidityBlock = lastSolidityNumber,
      dbUnconfirmedBlock = lastUnconfirmedDatabaseBlock.map(_.number).getOrElse(-1),
      dbLatestBlock = lastDatabaseBlock.map(_.number).getOrElse(-1),
      fullNodeBlockHash = lastFullNodeBlockHash,
      solidityBlockHash = lastSolidityNodeBlockHash,
      dbBlockHash = lastDbBlockHash
    )
  }
}
