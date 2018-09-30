package tron4s.dsl

import slick.collection.heterogeneous.Zero.*
import tron4s.domain.Address

object Query {


  def votes = Votes()

  case class Votes(address: Option[Address] = None, rounds: List[Long] = List.empty) {
    def forAddress(_address: Address) = {
      copy(address = Some(_address))
    }

    def rounds(rounds: Long*) = {
      copy(rounds = rounds.toList)
    }

    def round(round: Long) = {
      copy(rounds = List(round))
    }


    def execute(implicit blockChainStorage: BlockChainStorage) = {

    }
  }

}
