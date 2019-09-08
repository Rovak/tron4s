package tron4s.app.cli.commands

import tron4s.app.cli.AppCmd
import scala.async.Async._

case class CurrentRoundCmd() extends Command {

  override def execute(args: AppCmd) = async {
    println("RENT ROUND 4")
    Thread.sleep(1000)
  }
}
