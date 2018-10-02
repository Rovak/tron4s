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
import tron4s.client.grpc.WalletClient
import tron4s.domain.PrivateKey
import tron4s.models
import tron4s.models.{TransactionException, TransactionResult}
import tron4s.services.TransactionBuilder

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class TransactionFacade @Inject() (
  transactionBuilder: TransactionBuilder,
  walletClient: WalletClient) {

  /**
    * Sends TRX
    */
  def sendTRX(privateKey: PrivateKey, to: String, amount: Long, data: Option[String] = None)(implicit executionContext: ExecutionContext): Future[Either[TransactionException, TransactionResult]] = async {

    val addressStr = privateKey.address

    val transferContract = TransferContract(
      ownerAddress = addressStr.toByteString,
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

    val wallet = await(walletClient.full)

    await(wallet.broadcastTransaction(transaction)) match {
      case result if result.result =>
        Right(TransactionResult(transaction, result.code, result.message.decodeString))
      case result if !result.result =>
        Left(TransactionException(transaction, result.code, result.message.decodeString))
    }
  }

  /**
    * Sends TRX
    */
  def sendToken(privateKey: PrivateKey, to: String, token: String, amount: Long, data: Option[String] = None)(implicit executionContext: ExecutionContext): Future[Either[TransactionException, TransactionResult]] = async {

    val addressStr = privateKey.address

//    println("creating contract")

    val transferContract = TransferAssetContract(
      ownerAddress = addressStr.toByteString,
      toAddress = to.decode58,
      assetName = token.toByteString,
      amount = amount
    )

//    println("packaging contract")

    val contract = Transaction.Contract(
      `type` = ContractType.TransferAssetContract,
      parameter = Some(Any.pack(transferContract.asInstanceOf[TransferAssetContract])))

//    println("bulding contract")

    var transaction = transactionBuilder.buildTransactionWithContract(contract)

//    println("setting reference")

    transaction = await(transactionBuilder.setReference(transaction))



    data.foreach { transactionData =>
//      println("setting data", transactionData)

      transaction = transaction.withRawData(transaction.getRawData.withData(transactionData.toByteString))
    }

//    println("signing")


    transaction = transactionBuilder.sign(transaction, privateKey)

//    println("getting client")


    val wallet = await(walletClient.full)

//    println("broadcasting")

    await(wallet.broadcastTransaction(transaction)) match {
      case result if result.result =>
        Right(TransactionResult(transaction, result.code, result.message.decodeString))
      case result if !result.result =>
        Left(TransactionException(transaction, result.code, result.message.decodeString))
    }
  }
}
