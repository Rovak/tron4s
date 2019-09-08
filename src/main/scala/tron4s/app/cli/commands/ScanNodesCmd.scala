package tron4s.app.cli.commands

import tron4s.app.cli.AppCmd
import tron4s.infrastructure.network.NetworkScanner

import scala.async.Async._

case class ScanNodesCmd(app: tron4s.app.App) extends Command {

  override def execute(args: AppCmd) = async {
    val networkScanner = app.injector.getInstance(classOf[NetworkScanner])
    networkScanner.start()
  }
}
