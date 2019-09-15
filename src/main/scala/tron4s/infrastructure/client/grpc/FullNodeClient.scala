package tron4s.infrastructure.client.grpc

import org.tron.api.api.WalletGrpc.WalletStub
import org.tron.api.api.{EmptyMessage, NumberMessage}
import org.tron.protos.Tron.Block
import tron4s.domain.blockchain.BlockChain

import scala.concurrent.{ExecutionContext, Future}

trait FullNodeClient {
  def client: WalletStub
}

/**
  * Full Node BlockChain
  */
class FullNodeBlockChain(val client: WalletStub) extends BlockChain with FullNodeClient {

  def genesisBlock(implicit executionContext: ExecutionContext): Future[Block] = {
    client.getBlockByNum(NumberMessage(num = 0))
  }

  def headBlock(implicit executionContext: ExecutionContext): Future[Block] = {
    client.getNowBlock(EmptyMessage())
  }

  def getBlockByNum(number: Long)(implicit executionContext: ExecutionContext) = {
    client.getBlockByNum(NumberMessage(number)).map {
      case block if block.blockHeader.isDefined =>
        Some(block)
      case _ =>
        None
    }
  }
}
