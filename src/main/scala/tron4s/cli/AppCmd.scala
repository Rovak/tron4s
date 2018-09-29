package tron4s.cli

import tron4s.cli.commands.Command

case class AppCmd(
  cmd: Option[Command] = None)