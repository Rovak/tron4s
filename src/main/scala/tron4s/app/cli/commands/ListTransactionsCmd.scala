package tron4s.app.cli.commands

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.Source
import org.tron.protos.Tron.Transaction
import play.api.Logger
import tron4s.blockchain.TransactionStream
import tron4s.app.cli.AppCmd
import tron4s.infrastructure.client.grpc.WalletClient
import tron4s.domain.Address
import tron4s.services.DataExporter
import tron4s.utils.ModelUtils

import scala.async.Async.{async, await}

case class ListTransactionsCmd(app: tron4s.app.App, format: String = "csv", fromAddress: Option[String] = None, toAddress: Option[String] = None)  extends Command {

  override def execute(args: AppCmd) = async {

    val decider: Supervision.Decider = { exc =>
      Logger.error("SYNC NODE ERROR", exc)
      Supervision.Restart
    }

    implicit val system = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer(
      ActorMaterializerSettings(system).withSupervisionStrategy(decider))(system)

    val dataExporter = new DataExporter

    await(
      dataExporter.exportData(
        buildStream.map(ModelUtils.contractModelFromProto).filter(_.isDefined).map(_.get),
        format
      )
    )
  }

  def buildStream: Source[Transaction, NotUsed] = {
    val wallet = app.injector.getInstance(classOf[WalletClient])

    fromAddress.map { from =>
      new TransactionStream(wallet).streamFromThis(Address(from))
    }.orElse {
      toAddress.map { to =>
        new TransactionStream(wallet).streamToThis(Address(to))
      }
    }.getOrElse(Source.empty)
  }
}
