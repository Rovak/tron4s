package tron4s.cli.commands

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.tron.api.api.PaginatedMessage
import tron4s.cli.AppCmd
import tron4s.client.grpc.WalletClient
import tron4s.services.DataExporter
import tron4s.tokens.TokenListStream
import tron4s.utils.{ModelUtils, ProtoUtils}

import scala.async.Async._

case class ListTokensCmd(app: tron4s.App, format: String = "csv") extends Command {

  override def execute(args: AppCmd) = async {

    val wallet = app.injector.getInstance(classOf[WalletClient])
    implicit val system = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer()

    val dataExporter = new DataExporter

    println("starting to read all tokens")

    await(
      dataExporter.exportData(
        new TokenListStream(wallet)
          .buildStream
          .map(ModelUtils.fromProto),
        format
      )
    )
  }
}
