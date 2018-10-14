package tron4s.services

import com.google.protobuf.ByteString
import com.google.protobuf.any.Any
import javax.inject.Inject
import org.tron.api.api.EmptyMessage
import org.tron.api.api.WalletGrpc.Wallet
import org.tron.common.utils.{ByteArray, Sha256Hash}
import org.tron.protos.Contract.TransferContract
import org.tron.protos.Tron.Transaction
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.Implicits._
import tron4s.domain.{Address, PrivateKey}
import tron4s.models.BlockReference

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
  def buildTrxTransfer(from: Address, to: String, amount: Long): Transaction = {

    val transferContract = TransferContract(
      ownerAddress = from.address.toByteString,
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
    getBlockReference().map(_.setReference(transaction))
  }

  /**
    * Add block reference
    */
  def getBlockReference()(implicit executionContext: ExecutionContext): Future[BlockReference] = {
    for {
      latestBlock <- wallet.getNowBlock(EmptyMessage())
    } yield {
      BlockReference(
        blockHash = ByteString.copyFrom(ByteArray.subArray(latestBlock.rawHash.getBytes, 8, 16)),
        blockRef = ByteString.copyFrom(ByteArray.subArray(ByteArray.fromLong(latestBlock.getBlockHeader.getRawData.number), 6, 8)),
        expiration = latestBlock.getBlockHeader.getRawData.timestamp + (60 * 5 * 1000),
      )
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
