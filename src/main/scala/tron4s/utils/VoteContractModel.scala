package tron4s.utils

import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.infrastructure.exporter.{Field, Record}
import tron4s.models.BaseContractModel

case class VoteContractModel(
   ownerAddress: String,
   votes: Map[String, Long] = Map.empty) extends BaseContractModel {

  val contractType = ContractType.VoteWitnessContract.value

  def toRecord = Record(
    Field("ownerAddress", ownerAddress),
    Field("votes", votes.map(x => x._1 + ":" + x._2).mkString(";"))
  )
}