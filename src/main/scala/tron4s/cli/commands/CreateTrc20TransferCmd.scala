package tron4s.cli.commands

import monix.execution.Scheduler.Implicits.global
import tron4s.Implicits._
import tron4s.cli.AppCmd
import tron4s.domain.{Address, PrivateKey}
import tron4s.facades.TransactionFacade
import tron4s.models.TransactionResult
import tron4s.services.TransactionService

import scala.async.Async._

case class CreateTrc20TransferCmd(app: tron4s.App) extends Command {

  override def execute(args: AppCmd) = async {

//    for {
//      privateKeyStr <- ask("Key")
//      to <- ask("to").map(Address(_))
//      contractAddress <- ask("address").map(Address(_))
//      amount <- askLong("How much")
//    } {
      runSync(async {

        val privateKeyStr = "6aa006b3bce25dbefb76f3fd7d604cc9395d409cdc558f8f5b42ddb5d7880846"
        val to = Address("TAspoCMsaXAK7cbAxhQiXmNWvc9qMg1kCz")
        val contractAddress = Address("TG37mUxRUaH1E8DWSrrmoQ79BnZn1yHztb")
        val amount = 1

        val privateKey = PrivateKey(privateKeyStr)
        val addressStr = privateKey.address

        val transactionFacade = app.injector.getInstance(classOf[TransactionFacade])

        if (askBoolean(s"Sending $amount TRX from $addressStr to $to. Confirm?").contains(true)) {

          await(transactionFacade.sendTRC20(
            privateKey,
            to,
            contractAddress,
            amount,
          )) match {
            case Right( TransactionResult(transaction, _, _)) =>
              write(s"Successfully sent $amount TRX from $addressStr to $to\nHash: ${transaction.hash}")

              val transactionService = app.injector.getInstance(classOf[TransactionService])
              write(s"Waiting for confirmation...")

              await(transactionService.confirmHash(transaction.hash))
              write(s"Transaction confirmed!")
              println(s"https://tronscan.org/#/transaction/${transaction.hash}")
              println(s"https://api.trongrid.io/wallet/gettransactioninfobyid?value=${transaction.hash}")
            case Left(exc) =>
              println("Transaction Exception", exc)
              println("hash: " + exc.transaction.hash)
              println("code: " + exc.code)
          }
        }
      })
//    }
  }
}
