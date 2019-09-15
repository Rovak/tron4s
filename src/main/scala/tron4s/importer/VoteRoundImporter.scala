package tron4s.importer

import akka.stream.scaladsl.{Keep, Sink, Source}
import io.circe.Json
import javax.inject.Inject
import org.tron.protos.Tron.Transaction.Contract.ContractType
import play.api.Logger
import tron4s.importer.db.models.{MaintenanceRoundModelRepository, RoundVoteModelRepository}
import tron4s.Implicits._
import scala.concurrent.ExecutionContext
import tron4s.domain.transaction.TransactionSerializer._

class VoteRoundImporter @Inject() (
  maintenanceRepository: MaintenanceRoundModelRepository,
  roundVoteModelRepository: RoundVoteModelRepository) {

  /**
    * Import all vote rounds
    */
  def importRounds()(implicit executionContext: ExecutionContext) = {

    Source
      .single(0)
      // Load rounds
      .mapAsync(1)(_ => maintenanceRepository.findAllRounds)
      // Combine rounds
      .mapConcat(_.sliding(2).toList)
      // Iterate all votes
      .foldAsync(Map[String, Map[String, Long]]()) { case (previousVotes, Seq(currentRound, nextRound)) =>
        Logger.info(s"Importing round ${currentRound.number}")
        for {
          votes <- maintenanceRepository.getVotesBetweenBlocks(currentRound.block, nextRound.block)
          newMap = buildVotes(previousVotes, votes)
          _ <- roundVoteModelRepository.insertVoteRounds(newMap, currentRound.number)
        } yield newMap
      }
      .toMat(Sink.ignore)(Keep.right)
  }

  /**
    * Build the vote map based on the round votes
    */
  def buildVotes(voteMap: Map[String, Map[String, Long]] = Map.empty, roundVotes: Vector[(String, Int, Json)]) = {

    roundVotes.foldLeft(voteMap) {
      case (votes, (address, contractType, contractData)) =>
        if (contractType == ContractType.UnfreezeBalanceContract.value) {
          // Reset votes when unfreezing balance
          votes - address
        } else if (contractType == ContractType.VoteWitnessContract.value) {
          // Read votes from the contract data and add them to the total votes
          val contractVotes = contractData.as[org.tron.protos.Contract.VoteWitnessContract].toOption.get
          votes ++ Map(address -> contractVotes.votes.map(x => (x.voteAddress.encode58, x.voteCount)).toMap)
        } else {
          // Do nothing
          votes
        }
    }
  }

}
