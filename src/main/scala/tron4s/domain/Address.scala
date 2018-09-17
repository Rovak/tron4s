package tron4s.domain

import tron4s.AddressStr

trait HasAddress {
  def address: AddressStr
}

case class Address(address: String) {
  require(address.length == 34, "Address must be 34 characters")
}
