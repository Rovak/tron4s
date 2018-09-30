package tron4s.cli.commands

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import tron4s.cli.AppCmd
import tron4s.importer.FullNodeImporter

import scala.async.Async._

case class ImportCmd(app: tron4s.App) extends Command {

  override def execute(args: AppCmd) = async {

    val fullchain = app.injector.getInstance(classOf[FullNodeImporter])
    implicit val actorSystem = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer()

    write("Building Stream")
    val stream = await(fullchain.buildStream(actorSystem))
    write("Reading Stream")

    await(stream.run())
  }
}
