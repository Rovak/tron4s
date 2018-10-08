package tron4s.cli.commands

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.tron.protos.Tron.Transaction
import tron4s.blockchain.{TokenListStream, TransactionStream}
import tron4s.cli.AppCmd
import tron4s.client.grpc.WalletClient
import tron4s.domain.Address
import tron4s.services.DataExporter
import tron4s.utils.ModelUtils

import scala.async.Async.{async, await}

case class ListTransactionsCmd(app: tron4s.App, format: String = "csv", fromAddress: Option[String] = None, toAddress: Option[String] = None)  extends Command {

  override def execute(args: AppCmd) = async {

    implicit val system = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer()

    val dataExporter = new DataExporter

    println("starting to read all tokens")

    await(
      dataExporter.exportData(
        buildStream
          .map(ModelUtils.fromProto),
        format
      )
    )
  }

  def buildStream: Source[Transaction, NotUsed] = {
    val wallet = app.injector.getInstance(classOf[WalletClient])

    fromAddress.map { from =>
      new TransactionStream(wallet)
        .streamFromThis(Address(from))
        .map(x => ModelUtils.contractToModel(x.getRawData.contract.head))
    }.orElse {
      toAddress.map { to =>
        new TransactionStream(wallet)
          .streamToThis(Address(to))
      }
    }.getOrElse(Source.empty)
  }
}
