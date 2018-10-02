package tron4s.facades

import com.google.protobuf.ByteString
import com.google.protobuf.any.Any
import javax.inject.Inject
import org.tron.api.api.WalletGrpc
import org.tron.common.crypto.ECKey
import org.tron.common.utils.ByteArray
import org.tron.protos.Contract.TransferContract
import org.tron.protos.Contract.TransferAssetContract
import org.tron.protos.Tron.Transaction
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.Implicits._
import tron4s.domain.PrivateKey
import tron4s.services.TransactionBuilder

import scala.async.Async._
import scala.concurrent.ExecutionContext

class TransactionFacade @Inject() (
  transactionBuilder: TransactionBuilder,
  walletClient: WalletGrpc.Wallet) {

  /**
    * Sends TRX
    */
  def sendTRX(privateKey: PrivateKey, to: String, amount: Long, data: Option[String] = None)(implicit executionContext: ExecutionContext) = async {

    val addressStr = privateKey.address

    val transferContract = TransferContract(
      ownerAddress = addressStr.decode58,
      toAddress = to.decode58,
      amount = amount
    )
    val contract = Transaction.Contract(
      `type` = ContractType.TransferContract,
      parameter = Some(Any.pack(transferContract.asInstanceOf[TransferContract])))

    var transaction = transactionBuilder.buildTransactionWithContract(contract)
    transaction = await(transactionBuilder.setReference(transaction))

    data.foreach { transactionData =>
      transaction = transaction.withRawData(transaction.getRawData.withData(transactionData.toByteString))
    }

    transaction = transactionBuilder.sign(transaction, privateKey)

    await(walletClient.broadcastTransaction(transaction))

    transaction
  }

  /**
    * Sends TRX
    */
  def sendToken(privateKey: PrivateKey, to: String, token: String, amount: Long, data: Option[String] = None)(implicit executionContext: ExecutionContext) = async {

    val addressStr = privateKey.address

    val transferContract = TransferAssetContract(
      ownerAddress = addressStr.decode58,
      toAddress = to.decode58,
      assetName = token.toByteString,
      amount = amount
    )

    val contract = Transaction.Contract(
      `type` = ContractType.TransferAssetContract,
      parameter = Some(Any.pack(transferContract.asInstanceOf[TransferAssetContract])))

    var transaction = transactionBuilder.buildTransactionWithContract(contract)
    transaction = await(transactionBuilder.setReference(transaction))

    data.foreach { transactionData =>
      transaction = transaction.withRawData(transaction.getRawData.withData(transactionData.toByteString))
    }

    transaction = transactionBuilder.sign(transaction, privateKey)

    await(walletClient.broadcastTransaction(transaction))

    transaction
  }

}
