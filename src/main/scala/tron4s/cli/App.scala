package tron4s.cli

import tron4s.cli.commands.{CreateTransferCmd, CurrentRoundCmd, VoteRoundCmd}

object App {

  def main(args: Array[String]): Unit = {

    val parser = new scopt.OptionParser[AppCmd]("tron4s") {
      head("TRON 4 Scala", "0.1")

      cmd("round").action((_, c) => c.copy(cmd = Some(CurrentRoundCmd())))
        .text("get current round")

      cmd("votes").action((_, c) => c.copy(cmd = Some(VoteRoundCmd())))
        .text("get votes")

      cmd("new_transaction").action((_, c) => c.copy(cmd = Some(CreateTransferCmd())))
        .text("build a transaction")
    }

    parser.parse(args, AppCmd()).foreach { config =>
      config.cmd.foreach(_.execute(config))
    }
  }

}
