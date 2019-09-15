package tron4s.app.cli.commands

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import org.tron.protos.Tron.Block
import play.api.Logger
import tron4s.app.cli.AppCmd
import tron4s.infrastructure.client.grpc.GrpcWalletClient
import tron4s.Implicits._
import tron4s.importer.BlockChainStreamBuilder

import scala.async.Async.{async, await}

case class TailBlocksCmd(app: tron4s.app.App, producer: Option[String] = None)  extends Command {

  override def execute(args: AppCmd) = async {

    val decider: Supervision.Decider = { exc =>
      Logger.error("SYNC NODE ERROR", exc)
      Supervision.Restart
    }

    val blockChainStreamBuilder = app.injector.getInstance(classOf[BlockChainStreamBuilder])
    val wallet = app.injector.getInstance(classOf[GrpcWalletClient])
    implicit val system = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer(
      ActorMaterializerSettings(system).withSupervisionStrategy(decider))(system)

    val fullWallet = await(wallet.full)

    var stream = blockChainStreamBuilder
      .readFullNodeBlocksContinously(fullWallet)

    producer.foreach { producerAddress =>
      stream = stream.filter(_.witness == producerAddress)
    }

    await(
      stream
        .runWith(Sink.foreach[Block] { block =>
          println("block", block.getBlockHeader.getRawData.number)
        })
    )
  }
}
