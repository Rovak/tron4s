package tron4s.importer

import akka.NotUsed
import akka.event.EventStream
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.tron.api.api.WalletGrpc.WalletStub
import org.tron.api.api.WalletSolidityGrpc.WalletSolidityStub
import org.tron.api.api.{BlockLimit, EmptyMessage, NumberMessage}
import org.tron.protos.Tron.Transaction.Contract.ContractType.{AssetIssueContract, ParticipateAssetIssueContract, TransferAssetContract, TransferContract, VoteWitnessContract, WitnessCreateContract}
import org.tron.protos.Tron.{Block, Transaction}
import play.api.Logger
import tron4s.infrastructure.client.grpc.GrpcWalletClient
import Events._
import tron4s.importer.StreamTypes.ContractFlow
import tron4s.importer.db.models._
import tron4s.utils.ModelUtils

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class BlockChainStreamBuilder {

  /**
    * Reads the blocks with getBlockByNum
    */
  def readSolidityBlocks(from: Long, to: Long, parallel: Int = 36)(client: WalletSolidityStub): Source[Block, NotUsed] = {
    Source(from to to)
      .mapAsync(parallel) { i => client.getBlockByNum(NumberMessage(i)) }
      .filter(_.blockHeader.isDefined)
  }

  /**
    * Reads the blocks with getBlockByNum
    */
  def readFullNodeBlocks(from: Long, to: Long, parallel: Int = 36)(client: WalletStub): Source[Block, NotUsed] = {
    Source(from to to)
      .mapAsync(parallel) { i => client.getBlockByNum(NumberMessage(i)) }
      .filter(_.blockHeader.isDefined)
  }

  /**
    * Reads the blocks with getBlockByNum
    */
  def readFullNodeBlocksContinously(client: WalletStub) = {
    Source.tick(0.seconds, 2.5.seconds, "")
      .mapAsync(1) { _ => client.getNowBlock(EmptyMessage()) }
      .via(ImportStreamFactory.buildBlockSequenceChecker)
  }

  /**
    * Reads all the blocks using batch calls
    */
  def readFullNodeBlocksBatched(from: Long, to: Long, batchSize: Int = 50)(client: GrpcWalletClient)(implicit executionContext: ExecutionContext): Source[Block, NotUsed] = {
    Source.unfold(from) { fromBlock =>
      if (fromBlock < to) {

        val nextBlock = fromBlock + batchSize
        val toBlock = if (nextBlock <= to) nextBlock else to
        Some((toBlock + 1, (fromBlock, toBlock)))

      } else {
        None
      }
    }
    .mapAsync(12) { case (fromBlock, toBlock) =>
      val range = BlockLimit(fromBlock, toBlock + 1)
      client.fullRequest(_.getBlockByLimitNext(range)).map { blocks =>
        blocks.block.sortBy(_.getBlockHeader.getRawData.number)
      }
    }
    .mapConcat(x => x.toList)
    .buffer(10000, OverflowStrategy.backpressure)
  }

  def filterContracts(contractTypes: List[Transaction.Contract.ContractType]) = {
    Flow[Transaction.Contract]
      .filter(contract  => contractTypes.contains(contract.`type`))
  }

  /**
    * Publishes contracts to the given eventstream
    */
  def publishContractEvents(eventStream: EventStream, contractTypes: List[Transaction.Contract.ContractType]) = {
    Flow[ContractFlow]
      .filter(contract  => contractTypes.contains(contract._3.`type`))
      .toMat(Sink.foreach { contractBlock =>

        val (block, transaction, contract) = contractBlock

        (contract.`type`, ModelUtils.contractToModel(contract, transaction, block)) match {
          case (TransferContract, Some(transfer: TransferModel)) =>
            eventStream.publish(TransferCreated(transfer))

          case (TransferAssetContract, Some(transfer: TransferModel)) =>
            eventStream.publish(AssetTransferCreated(transfer))

          case (WitnessCreateContract, Some(witness: WitnessModel)) =>
            eventStream.publish(WitnessCreated(witness))

          case (VoteWitnessContract, Some(votes: VoteWitnessList)) =>
            votes.votes.foreach { vote =>
              eventStream.publish(VoteCreated(vote))
            }

          case (AssetIssueContract, Some(assetIssue: AssetIssueContractModel)) =>
            eventStream.publish(AssetIssueCreated(assetIssue))

          case (ParticipateAssetIssueContract, Some(participate: ParticipateAssetIssueModel)) =>
            eventStream.publish(ParticipateAssetIssueModelCreated(participate))

          case _ =>
            // ignore
        }
      })(Keep.right)
  }
}
