package tron4s.domain.transaction

import com.google.protobuf.ByteString
import com.google.protobuf.any.Any
import javax.inject.Inject
import org.joda.time.DateTime
import org.tron.api.api.EmptyMessage
import org.tron.api.api.WalletGrpc.Wallet
import org.tron.common.utils.Sha256Hash
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
        contract = Seq(contract),
        timestamp = DateTime.now().getMillis,
      )),
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
    wallet
      .getNowBlock(EmptyMessage())
      .map(BlockReference.fromBlock)
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
