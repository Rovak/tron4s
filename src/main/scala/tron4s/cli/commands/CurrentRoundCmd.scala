package tron4s.cli.commands
import tron4s.cli.AppCmd

case class CurrentRoundCmd() extends Command {

  override def execute(args: AppCmd): Unit = {
    println("RENT ROUND 4")
    Thread.sleep(1000)
  }
}
