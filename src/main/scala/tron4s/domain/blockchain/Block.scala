package tron4s.domain.blockchain

import cats.Monoid
import tron4s._

object Block {

  implicit val monoid: Monoid[Block] = new Monoid[Block] {
    def empty = Block(0)
    def combine(x: Block, y: Block) = {
      x.copy(
        transactions = x.transactions ++ y.transactions
      )
    }
  }
}

case class Block(
  number: BlockNumber,
  timestamp: Option[Timestamp] = None,
  transactions: List[Transaction] = List.empty,
)
