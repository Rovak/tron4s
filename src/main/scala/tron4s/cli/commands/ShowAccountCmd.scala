package tron4s.cli.commands

import org.tron.protos.Tron.Account
import tron4s.cli.AppCmd
import tron4s.client.grpc.WalletClient
import tron4s.Implicits._
import tron4s.models.AccountModel

import scala.async.Async._

case class ShowAccountCmd(app: tron4s.App, account: String, node: String = "full") extends Command {

  def execute(args: AppCmd) = async {

    val client = app.injector.getInstance(classOf[WalletClient])

    node.toLowerCase match {
      case "full" =>
        await(for {
          fullClient <- client.full
          account <- fullClient.getAccount(Account(address = account.decode58))
        } yield {
          write(AccountModel.fromProto(account).toRecord.toCsv)
        })
      case "solidity" =>
        await(for {
          fullClient <- client.solidity
          account <- fullClient.getAccount(Account(address = account.decode58))
        } yield {
          write(AccountModel.fromProto(account).toRecord.toCsv)
        })
    }
  }
}
