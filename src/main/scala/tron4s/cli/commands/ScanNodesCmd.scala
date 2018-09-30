package tron4s.cli.commands

import tron4s.cli.AppCmd
import tron4s.network.NetworkScanner

import scala.async.Async._

case class ScanNodesCmd(app: tron4s.App) extends Command {

  override def execute(args: AppCmd) = async {
    val networkScanner = app.injector.getInstance(classOf[NetworkScanner])
    networkScanner.start()
  }
}
