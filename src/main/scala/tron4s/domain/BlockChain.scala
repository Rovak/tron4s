package tron4s.domain

import org.tron.protos.Tron.Block

import scala.concurrent.{ExecutionContext, Future}

trait BlockChain {

  def genesisBlock(implicit executionContext: ExecutionContext): Future[Block]
  def headBlock(implicit executionContext: ExecutionContext): Future[Block]
  def getBlockByNum(number: Long)(implicit executionContext: ExecutionContext): Future[Option[Block]]

}
