package tron4s.cli.commands

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import play.api.Logger
import tron4s.cli.AppCmd
import tron4s.client.grpc.WalletClient
import tron4s.importer.BlockChainStreamBuilder

import scala.async.Async.{async, await}

case class TailBlocksCmd(app: tron4s.App)  extends Command {

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

    await(
      blockChainStreamBuilder
        .readFullNodeBlocksContinously(fullWallet)
        .runWith(Sink.foreach { block =>
          println("block", block.getBlockHeader.getRawData.number)
        })
    )
  }
}
