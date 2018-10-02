package tron4s.services

import com.google.protobuf.ByteString
import com.google.protobuf.any.Any
import javax.inject.Inject
import org.joda.time.DateTime
import org.tron.api.api.EmptyMessage
import org.tron.api.api.WalletGrpc.Wallet
import org.tron.common.crypto.ECKey
import org.tron.common.utils.{ByteArray, Sha256Hash}
import org.tron.protos.Contract.TransferContract
import org.tron.protos.Tron.Transaction
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.Implicits._
import tron4s.domain.PrivateKey

import scala.concurrent.{ExecutionContext, Future}

/**
  * Transaction Builder Utils
  */
class TransactionBuilder @Inject() (wallet: Wallet) {

  /**
    * Build transaction which includes the given contract
    */
  def buildTransactionWithContract(contract: Transaction.Contract): Transaction = {
    Transaction(
      rawData = Some(Transaction.raw(
        contract = Seq(contract)
      ))
    )
  }

  /**
    * Build transfer contract
    */
  def buildTrxTransfer(from: Array[Byte], to: String, amount: Long): Transaction = {

    val transferContract = TransferContract(
      ownerAddress = ByteString.copyFrom(from),
      toAddress = to.decode58,
      amount = amount)

    val contract = Transaction.Contract(
      `type` = ContractType.TransferContract,
      parameter = Some(Any.pack(transferContract))
    )

    buildTransactionWithContract(contract)
  }

  /**
    * Add block reference
    */
  def setReference(transaction: Transaction)(implicit executionContext: ExecutionContext): Future[Transaction] = {
    for {
      latestBlock <- wallet.getNowBlock(EmptyMessage())
    } yield {
      val raw = transaction.rawData.get
        .withRefBlockHash(ByteString.copyFrom(ByteArray.subArray(latestBlock.rawHash.getBytes, 8, 16)))
        .withRefBlockBytes(ByteString.copyFrom(ByteArray.subArray(ByteArray.fromLong(latestBlock.getBlockHeader.getRawData.number), 6, 8)))
        .withExpiration(latestBlock.getBlockHeader.getRawData.timestamp + (60 * 5 * 1000))
        .withTimestamp(DateTime.now().getMillis)

      transaction
        .withRawData(raw)
    }
  }

  /**
    * Add signature to the transaction
    */
  def sign(transaction: Transaction, pk: PrivateKey): Transaction = {
    val signature = pk.key.sign(Sha256Hash.hash(transaction.getRawData.toByteArray))
    val sig = ByteString.copyFrom(signature.toByteArray)
    transaction.addSignature(sig)
  }
}
