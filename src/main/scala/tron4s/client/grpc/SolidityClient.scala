package tron4s.client.grpc

import org.tron.api.api.WalletSolidityGrpc.WalletSolidityStub
import org.tron.api.api.{EmptyMessage, NumberMessage}
import org.tron.protos.Tron.Block
import tron4s.domain.BlockChain

import scala.concurrent.{ExecutionContext, Future}

trait SolidityClient {
  def client: WalletSolidityStub
}

class SolidityBlockChain(val client: WalletSolidityStub) extends BlockChain with SolidityClient {

  def genesisBlock(implicit executionContext: ExecutionContext): Future[Block] = {
    client.getBlockByNum(NumberMessage(0))
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
