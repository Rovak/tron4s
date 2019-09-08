package tron4s.app.cli.commands

import org.tron.protos.Tron.Account
import tron4s.app.cli.AppCmd
import tron4s.infrastructure.client.grpc.WalletClient
import tron4s.Implicits._
import tron4s.infrastructure.exporter.RecordFormatter
import tron4s.models.AccountModel

import scala.async.Async._

case class ShowAccountCmd(app: tron4s.app.App, account: String, node: String = "full") extends Command {


  def toCsv[A](record: A)(implicit recordFormatter: RecordFormatter[A]) = {
    recordFormatter.format(record).toCsv
  }

  def execute(args: AppCmd) = async {

    val client = app.injector.getInstance(classOf[WalletClient])

    node.toLowerCase match {
      case "full" =>
        await(for {
          fullClient <- client.full
          account <- fullClient.getAccount(Account(address = account.decode58))
        } yield {
          write(toCsv(AccountModel.fromProto(account)))
        })

      case "solidity" =>
        await(for {
          fullClient <- client.solidity
          account <- fullClient.getAccount(Account(address = account.decode58))
        } yield {
          write(toCsv(AccountModel.fromProto(account)))
        })
    }
  }
}
