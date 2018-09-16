package tronweb4s.utils

object StaticAddressFormatter {
  var formatter: AddressFormatter = new MainNetFormatter
}

trait AddressFormatter {
  def prefixByte: Byte
  def prefixString: String
}


class TestNetFormatter extends AddressFormatter {
  val prefixByte = 0xa0.toByte
  val prefixString = "a0"
}

class MainNetFormatter extends AddressFormatter {
  val prefixByte = 0x41.toByte
  val prefixString = "41"
}