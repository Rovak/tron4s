package tron4s.domain.transaction

import java.math.BigInteger
import java.util

import com.google.protobuf.ByteString
import com.google.protobuf.any.Any
import javax.inject.Inject
import monix.execution.Scheduler
import org.tron.api.api.{BytesMessage, WalletGrpc}
import org.tron.common.utils.ByteArray
import org.tron.protos.Contract.{TransferAssetContract, TransferContract, TriggerSmartContract}
import org.tron.protos.Tron.Transaction
import org.tron.protos.Tron.Transaction.Contract.ContractType
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.{Int256, Uint256}
import tron4s.Implicits._
import tron4s.domain.PrivateKey
import tron4s.infrastructure.client.grpc.GrpcWalletClient
import tron4s.models.{TransactionException, TransactionResult}
import tron4s.utils.FutureUtils._

import scala.async.Async._
import scala.concurrent.Future
import scala.concurrent.duration._

case class TransactionRetries(retries: Int = 0, confirmations: Int = 0) {
  def useRetry = copy(retries - 1)
  def hasRetries = retries > 0
}

class TransactionFacade @Inject() (
  transactionBuilder: TransactionBuilder,
  walletClient: GrpcWalletClient) {

  def confirmTransaction(wallet: WalletGrpc.Wallet, transaction: Transaction, confirmations: Int = 1)(implicit executionContext: Scheduler): Future[Either[String, Boolean]] = async {
    await(wallet.getTransactionInfoById(BytesMessage(ByteString.copyFrom(transaction.hashBytes)))) match {
      case res if !res.result.isSucess =>
        Left("Could not confirm transaction")
      case res if res.result.isSucess && confirmations > 0 =>
        await(delay(3.seconds))
        await(confirmTransaction(wallet, transaction, confirmations - 1))
      case res if res.result.isSucess =>
        Right(true)
      case _ =>
        Left("Unconfirmed")
    }
  }

  def broadcastWithRetries(wallet: WalletGrpc.Wallet, transaction: Transaction, retryStrategy: TransactionRetries)(implicit executionContext: Scheduler): Future[Either[TransactionException, TransactionResult]] = async {

    println("broadcasting..")

    await(wallet.broadcastTransaction(transaction)) match {
      case result if result.result =>
        println("success", result)
        // Broadcast was a success
        await(confirmTransaction(wallet, transaction, retryStrategy.confirmations)) match {
          case Right(true) =>
            // Broadcast and confirmations were a success
            Right(TransactionResult(transaction, result.code, result.message.decodeString))
          case Left(confirm) if retryStrategy.hasRetries =>
            await(delay(3.seconds))
            println("confirming", result, confirm)
            // Could not confirm, try again to broadcast
            await(broadcastWithRetries(wallet, transaction, retryStrategy.useRetry))
          case Left(_) =>
            // Could not confirm and no retries left
            Left(TransactionException(transaction, result.code, result.message.decodeString))
        }
      case result if !result.result && retryStrategy.hasRetries =>
        println(result)
        await(delay(3.seconds))
        // The broadcast failed but there were retries left
        await(broadcastWithRetries(wallet, transaction, retryStrategy.useRetry))
      case result if !result.result =>
        println(result)
        // Broadcast failed and there are no retries left
        Left(TransactionException(transaction, result.code, result.message.decodeString))
    }
  }

  /**
    * Sends TRX
    */
  def sendTRX(
    privateKey: PrivateKey,
    to: String,
    amount: Long,
    data: Option[String] = None,
    retryStrategy: TransactionRetries = TransactionRetries(),
    retries: Int = 3)(implicit executionContext: Scheduler): Future[Either[TransactionException, TransactionResult]] = async {

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

    await(broadcastWithRetries(wallet, transaction, retryStrategy)) match {
      case Right(result) =>
        Right(TransactionResult(transaction, result.code, result.message))
      case Left(_) if retries > 0 =>
        await(sendTRX(privateKey, to, amount, data, retryStrategy, retries - 1))
      case Left(result) =>
        Left(TransactionException(transaction, result.code, result.message))
    }
  }

  /**
    * Sends TRX
    */
  def sendTRC10(
     privateKey: PrivateKey,
     to: String,
     token: String,
     amount: Long,
     data: Option[String] = None,
     retryStrategy: TransactionRetries = TransactionRetries(),
     retries: Int = 3)(implicit executionContext: Scheduler): Future[Either[TransactionException, TransactionResult]] = async {

    val addressStr = privateKey.address

    val transferContract = TransferAssetContract(
      ownerAddress = addressStr.toByteString,
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

    val wallet = await(walletClient.full)

    await(broadcastWithRetries(wallet, transaction, retryStrategy)) match {
      case Right(result) =>
        Right(TransactionResult(transaction, result.code, result.message))
      case Left(_) if retries > 0 =>
        await(sendTRC10(privateKey, to, token, amount, data, retryStrategy, retries - 1))
      case Left(result) if retries == 0 =>
        Left(TransactionException(transaction, result.code, result.message))
    }
  }

  /**
    * Sends TRX
    */
  def sendTRC20(
     privateKey: PrivateKey,
     to: tron4s.domain.Address,
     contractAddress: tron4s.domain.Address,
     amount: Long,
     data: Option[String] = None,
     retryStrategy: TransactionRetries = TransactionRetries(),
     retries: Int = 3)(implicit executionContext: Scheduler): Future[Either[TransactionException, TransactionResult]] = async {

    val addressStr = privateKey.address

    val encodedFunction = FunctionEncoder.encode(
      new org.web3j.abi.datatypes.Function(
        "transfer",
        util.Arrays.asList(
          new Address("0x" + to.toHex.substring(2)),
          new Uint256(BigInteger.valueOf(amount)),
        ),
        util.Arrays.asList()
      )
    )

    val triggerSmartContract = TriggerSmartContract(
      ownerAddress = addressStr.toByteString,
      contractAddress = contractAddress.toByteString,
      data = ByteString.copyFrom(ByteArray.fromHexString(encodedFunction.substring(2)))
    )

    val contract = Transaction.Contract(
      `type` = ContractType.TriggerSmartContract,
      parameter = Some(Any.pack(triggerSmartContract.asInstanceOf[TriggerSmartContract])))

    var transaction = transactionBuilder
      .buildTransactionWithContract(contract)
      .update(_.rawData.feeLimit := 1000000)

    transaction = await(transactionBuilder.setReference(transaction))

    data.foreach { transactionData =>
      transaction = transaction.update(_.rawData.data := transactionData.toByteString)
    }

    transaction = transactionBuilder.sign(transaction, privateKey)

    val wallet = await(walletClient.full)

    await(broadcastWithRetries(wallet, transaction, retryStrategy)) match {
      case Right(result) =>
        Right(TransactionResult(transaction, result.code, result.message))
      case Left(_) if retries > 0 =>
        await(sendTRC20(privateKey, to, contractAddress, amount, data, retryStrategy, retries - 1))
      case Left(result) if retries == 0 =>
        Left(TransactionException(transaction, result.code, result.message))
    }
  }

  /**
    * Sends TRX
    */
  def buildSendTRC20(
      from: String,
     to: String,
     contractAddress: String,
     amount: Long,
     data: Option[String] = None,
     retryStrategy: TransactionRetries = TransactionRetries(),
     retries: Int = 3)(implicit executionContext: Scheduler) = {

    val addressStr = tron4s.domain.Address(from)

    val encodedFunction = FunctionEncoder.encode(
      new org.web3j.abi.datatypes.Function(
        "transfer",
        util.Arrays.asList(
          new Address(ByteArray.toHexString(to.decode58.toByteArray)),
          new Int256(BigInteger.valueOf(amount)),
        ),
        util.Arrays.asList()
      )
    )

    val triggerSmartContract = TriggerSmartContract(
      ownerAddress = addressStr.toByteString,
      contractAddress = contractAddress.toByteString,
      data = encodedFunction.toByteString
    )

    val contract = Transaction.Contract(
      `type` = ContractType.TriggerSmartContract,
      parameter = Some(Any.pack(triggerSmartContract.asInstanceOf[TriggerSmartContract])))

    transactionBuilder.buildTransactionWithContract(contract)
  }
}
