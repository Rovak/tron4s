package tron4s.cli.commands

import com.google.protobuf.ByteString
import com.google.protobuf.any.Any
import org.tron.api.api.WalletGrpc
import org.tron.common.crypto.ECKey
import org.tron.common.utils.ByteArray
import org.tron.protos.Contract.TransferContract
import org.tron.protos.Tron.Transaction
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.Implicits._
import tron4s.cli.AppCmd
import tron4s.services.{TransactionBuilder, TransactionService}

import scala.async.Async._

case class CreateTransferCmd(app: tron4s.App) extends Command {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def execute(args: AppCmd): Unit = {

    for {
      privateKey <- ask("Key")
      to <- ask("to")
      amount <- askLong("How much")
    } {
      runSync(async {

        val ecKey = ECKey.fromPrivate(ByteArray.fromHexString(privateKey))
        val address = ecKey.getAddress
        val addressStr = ByteString.copyFrom(address).encode58

        val transactionBuilder = app.injector.getInstance(classOf[TransactionBuilder])
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

        ask("Data").foreach { data =>
          transaction = transaction.withRawData(transaction.getRawData.withData(data.toByteString))
        }

        transaction = transactionBuilder.sign(transaction, ByteArray.fromHexString(privateKey))

        val walletClient = app.injector.getInstance(classOf[WalletGrpc.Wallet])

        if (askBoolean(s"Sending $amount TRX from $addressStr to $to. Confirm?").contains(true)) {

          await(walletClient.broadcastTransaction(transaction))
          write(s"Successfully sent $amount TRX from $addressStr to $to\nHash: ${transaction.hash}")

          val transactionService = app.injector.getInstance(classOf[TransactionService])
          write(s"Waiting for confirmation...")

          await(transactionService.confirmHash(transaction.hash))
          write(s"Transaction confirmed!\nhttps://tronscan.org/#/transaction/${transaction.hash}")
        }
      })
    }
  }
}
