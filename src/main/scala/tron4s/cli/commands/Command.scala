package tron4s.cli.commands

import tron4s.cli.AppCmd

import scala.io.StdIn
import scala.util.{Success, Try}

trait Command {

  def execute(args: AppCmd): Unit

  def clear() = print("\033[2J")

  def write(msg: String) = println(msg)
  def update(msg: String) = print(msg + "\r")


  def ask(question: String) = {
    print(question + ": ")
    Some(StdIn.readLine())
  }

  def askBoolean(question: String) = {
    print(question + " [y/n]: ")
    StdIn.readLine().toLowerCase match {
      case "y" | "yes" => Some(true)
      case "n" | "no" => Some(false)
      case _ => None
    }
  }

  def askNumber(question: String) = {
    print(question + ": ")
    Try(StdIn.readLine().toDouble) match {
      case Success(amount) =>
        Some(amount)
      case _ =>
        None
    }
  }

  def askLong(question: String) = {
    print(question + ": ")
    Try(StdIn.readLine().toLong) match {
      case Success(amount) =>
        Some(amount)
      case _ =>
        None
    }
  }


}
