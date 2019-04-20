package tron4s.cli.commands

import com.google.protobuf.any.Any
import monix.execution.Scheduler.Implicits.global
import org.tron.api.api.WalletGrpc
import org.tron.protos.Contract.TransferContract
import org.tron.protos.Tron.Transaction
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.Implicits._
import tron4s.cli.AppCmd
import tron4s.domain.{Address, PrivateKey}
import tron4s.facades.{TransactionFacade, TransactionRetries}
import tron4s.services.{TransactionBuilder, TransactionService}

import scala.async.Async._

case class CreateTransferCmd(app: tron4s.App) extends Command {

  override def execute(args: AppCmd) = async {

    for {
      privateKeyStr <- ask("Key")
      to <- ask("to").map(Address(_))
      amount <- askLong("How much")
    } {
      runSync(async {

        val privateKey = PrivateKey(privateKeyStr)
        val addressStr = privateKey.address

        val transactionBuilder = app.injector.getInstance(classOf[TransactionBuilder])
        val transferContract = TransferContract(
          ownerAddress = addressStr.toByteString,
          toAddress = to.toByteString,
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

        transaction = transactionBuilder.sign(transaction, privateKey)

        val walletClient = app.injector.getInstance(classOf[WalletGrpc.Wallet])

        val transactionFacade = app.injector.getInstance(classOf[TransactionFacade])

        if (askBoolean(s"Sending $amount TRX from $addressStr to $to. Confirm?").contains(true)) {

          await(transactionFacade.broadcastWithRetries(walletClient, transaction, TransactionRetries(6, 6)))
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
