package tron4s.app.cli.commands

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import tron4s.blockchain.TokenListStream
import tron4s.app.cli.AppCmd
import tron4s.exporter.DataExporter
import tron4s.infrastructure.client.grpc.GrpcWalletClient
import tron4s.utils.ModelUtils

import scala.async.Async._

case class ListTokensCmd(app: tron4s.app.App, format: String = "csv") extends Command {

  override def execute(args: AppCmd) = async {

    val wallet = app.injector.getInstance(classOf[GrpcWalletClient])
    implicit val system = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer()

    val dataExporter = new DataExporter

    println("starting to read all tokens")

    await(
      dataExporter.exportData(
        new TokenListStream(wallet).buildStream.map(ModelUtils.fromProto),
        format
      )
    )
  }
}
