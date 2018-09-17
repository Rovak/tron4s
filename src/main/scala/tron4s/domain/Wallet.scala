package tron4s.domain

/**
  * Wallet
  */
case class Wallet(privateKey: PrivateKey) extends HasAddress {
  def address = privateKey.address
}
