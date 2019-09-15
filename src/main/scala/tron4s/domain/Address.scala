package tron4s.domain

import com.google.protobuf.ByteString
import tron4s.Implicits._

trait HasAddress {
  def address: Address
}

object Address {
  def EMPTY = Address("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT")
}

case class Address(address: String) {
  require(address.length == 34, "Address must be 34 characters")
  require(address.charAt(0) == 'T', "Address must start with T")

  def toByteString: ByteString = address.decode58
  def toHex: String = toByteString.toHex
}
