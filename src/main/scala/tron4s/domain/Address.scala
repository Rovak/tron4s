package tron4s.domain

import tron4s.Implicits._

trait HasAddress {
  def address: Address
}

object Address {
  def EMPTY = Address("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT")
}

case class Address(address: String) {
  require(address.length == 34, "Address must be 34 characters")

  def toByteString = address.decode58
}
