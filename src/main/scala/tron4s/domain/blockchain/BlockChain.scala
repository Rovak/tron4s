package tron4s.domain.blockchain

import scala.concurrent.{ExecutionContext, Future}

trait BlockChain {

  def genesisBlock(implicit executionContext: ExecutionContext): Future[org.tron.protos.Tron.Block]
  def headBlock(implicit executionContext: ExecutionContext): Future[org.tron.protos.Tron.Block]
  def getBlockByNum(number: Long)(implicit executionContext: ExecutionContext): Future[Option[org.tron.protos.Tron.Block]]

}
