package tron4s.cli.commands

import tron4s.Implicits._
import tron4s.cli.AppCmd
import tron4s.network.NetworkScanner

case class ScanNodesCmd(app: tron4s.App) extends Command {

  override def execute(args: AppCmd): Unit = {
    val networkScanner = app.injector.getInstance(classOf[NetworkScanner])
    runSync(networkScanner.start())
  }
}
