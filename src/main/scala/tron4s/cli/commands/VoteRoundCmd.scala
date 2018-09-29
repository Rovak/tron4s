package tron4s.cli.commands

import tron4s.cli.AppCmd

import scala.io.StdIn

case class VoteRoundCmd() extends Command {

  override def execute(args: AppCmd): Unit = {
    println("got: " + ask("What name"))
  }
}
