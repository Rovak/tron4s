package tron4s.cli.commands

import tron4s.cli.AppCmd

case class CreateTransferCmd() extends Command {

  override def execute(args: AppCmd): Unit = {

    for {
      from <- ask("from")
      to <- ask("to")
      amount <- askNumber("How much")
    } {
      println(from.trim, to.trim, amount)
    }

  }
}
