package tron4s.models

import com.google.protobuf.ByteString
import org.joda.time.DateTime
import org.tron.common.utils.ByteArray
import org.tron.protos.Tron.{Block, Transaction}
import tron4s.Implicits._

object BlockReference {
  def fromBlock(block: Block) = {
    BlockReference(
      blockHash = ByteString.copyFrom(ByteArray.subArray(block.rawHash.getBytes, 8, 16)),
      blockRef = ByteString.copyFrom(ByteArray.subArray(ByteArray.fromLong(block.getBlockHeader.getRawData.number), 6, 8)),
      expiration = block.getBlockHeader.getRawData.timestamp + (60 * 5 * 1000),
    )
  }
}

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
