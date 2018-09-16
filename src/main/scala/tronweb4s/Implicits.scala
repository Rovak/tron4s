package tronweb4s

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import com.google.protobuf.ByteString
import org.tron.common.BlockId
import org.tron.common.utils.{Base58, ByteArray, Sha256Hash}
import org.tron.protos.Tron.{Account, Block, Transaction}
import tronweb4s.protocol.ProtocolUtils

object Implicits {

  implicit class ImplicitBlock(block: Block) {
    def hash: BlockHash = ByteArray.toHexString(hashBytes)
    def rawHash = Sha256Hash.of(block.getBlockHeader.getRawData.toByteArray)
    def hashBytes = BlockId(number, rawHash.getBytes).hash
    def number: Long = block.getBlockHeader.getRawData.number
    def parentHash = BlockId(number - 1, block.getBlockHeader.getRawData.parentHash.toByteArray).hash

    def witness = block.getBlockHeader.getRawData.witnessAddress.encode58
    def contracts = block.transactions.flatMap(_.getRawData.contract)
    def transactionContracts = block.transactions.flatMap(t => t.getRawData.contract.map(c => (t, c)))
  }

  implicit class ImplicitTransaction(trx: Transaction) {
    def hash: TxHash = Sha256Hash.of(trx.getRawData.toByteArray).toString
    def hashBytes = Sha256Hash.of(trx.getRawData.toByteArray).getBytes
  }

  implicit class ImplicitContract(contract: Transaction.Contract) {
    def addresses = ProtocolUtils.getAddresses(contract)
    def sender: String = ProtocolUtils.getOwnerAddress(contract)
    def receiver: Option[String] = ProtocolUtils.getReceiverAddress(contract)
  }

  implicit class ByteStringUtils(byteString: ByteString) {
    def encode58 = Base58.encode58Check(byteString.toByteArray)
    def decodeString = new String(byteString.toByteArray)
  }

  implicit class StringUtils(str: String) {
    def decode58 = ByteString.copyFrom(Base58.decode58Check(str))
    def toByteString = ByteString.copyFromUtf8(str)
    def toAccount = Account(address = str.decode58)
  }

  implicit class StreamUtils[A](streams: List[Flow[A, A, NotUsed]]) {
    def pipe = streams.foldLeft(Flow[A]) {
      case (current, res) =>
        current.via(res)
    }
  }

  implicit class SinkUtils[A](sink: Sink[A, _]) {
    def toFlow = Flow[A].alsoTo(sink)
  }
}
