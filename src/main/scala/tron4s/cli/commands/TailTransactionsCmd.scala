package tron4s.cli.commands

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import play.api.Logger
import tron4s.cli.AppCmd
import tron4s.client.grpc.WalletClient
import tron4s.importer.BlockChainStreamBuilder
import tron4s.utils.ModelUtils
import tron4s.Implicits._
import tron4s.models.TransferContractModel

import scala.async.Async.{async, await}

case class TailTransactionsCmd(app: tron4s.App, address: Option[Seq[String]] = None, token: Option[String] = None)  extends Command {

  override def execute(args: AppCmd) = async {

    val decider: Supervision.Decider = { exc =>
      Logger.error("SYNC NODE ERROR", exc)
      Supervision.Restart
    }

    val blockChainStreamBuilder = app.injector.getInstance(classOf[BlockChainStreamBuilder])
    val wallet = app.injector.getInstance(classOf[WalletClient])
    implicit val system = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer(
      ActorMaterializerSettings(system).withSupervisionStrategy(decider))(system)

    val fullWallet = await(wallet.full)

//    val dataExporter = new DataExporter

    var stream =  blockChainStreamBuilder
      .readFullNodeBlocksContinously(fullWallet)
      .mapConcat(_.transactions.toList)

    // Filter by address
    address.foreach { from =>
      stream = stream.filter(_.getRawData.contract.head.addresses.exists(x => from.contains(x)))
    }

    var stream2 = stream
      .map(ModelUtils.contractModelFromProto).filter(_.isDefined).map(_.get)

    // Filter by token
    token.foreach { t =>
      stream2 = stream2.filter {
        case x: TransferContractModel if x.token == t =>
          true
        case _ =>
          false
      }
    }

    await {
      stream2
        .runWith(Sink.foreach { transaction =>
          println("transaction", transaction.toRecord.toCsv)
        })
    }
  }
}
