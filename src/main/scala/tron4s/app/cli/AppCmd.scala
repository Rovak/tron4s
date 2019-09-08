package tron4s.app.cli

import tron4s.app.cli.commands.Command

case class AppCmd(
  cmd: Option[Command] = None)