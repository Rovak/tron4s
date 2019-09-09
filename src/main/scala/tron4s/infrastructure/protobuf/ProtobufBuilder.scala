package tron4s.infrastructure.protobuf

import com.google.protobuf.any.Any
import org.tron.protos.Tron.Transaction
import scalapb.{GeneratedMessage, Message}
import tron4s.domain.blockchain.Contract

trait ProtobufBuilder[A, B <: GeneratedMessage with Message[B]] {
  def toProto(record: A): B
}

object TransactionFactory {
  def toTransaction[B <: GeneratedMessage with Message[B], A <: Contract with ProtoBuilder[B]](contract: A) = {
    Transaction.Contract(
      `type` = contract.contractType,
      parameter = Some(Any.pack(contract.buildProto)))
  }
}

trait ProtoBuilder[A <: GeneratedMessage with Message[A]] {

  def buildProto: A

}