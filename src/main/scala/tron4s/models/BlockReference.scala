package tron4s.models

import com.google.protobuf.ByteString
import org.joda.time.DateTime
import org.tron.protos.Tron.Transaction

case class BlockReference(
  blockHash: ByteString,
  blockRef: ByteString,
  expiration: Long) {

  def setReference(transaction: Transaction) = {
    val raw = transaction.rawData.get
      .withRefBlockHash(blockHash)
      .withRefBlockBytes(blockRef)
      .withExpiration(expiration)
      .withTimestamp(DateTime.now().getMillis)

    transaction
      .withRawData(raw)
  }
}
