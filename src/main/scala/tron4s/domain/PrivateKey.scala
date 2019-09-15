package tron4s.domain

import org.tron.common.crypto.ECKey
import org.tron.common.utils.{Base58, ByteArray}

object PrivateKey {
  def apply(key: String): PrivateKey = {
    PrivateKey(ByteArray.fromHexString(key))
  }

  def create = {
    PrivateKey(new ECKey().getPrivKeyBytes)
  }
}

/**
  * Private Key
  */
case class PrivateKey(keyBytes: Array[Byte]) {

  /**
    * Private key
    */
  lazy val key = ECKey.fromPrivate(keyBytes)

  /**
    * Get Key as hexdecimal
    */
  lazy val toHex = ByteArray.toHexString(keyBytes)

  /**
    * Compute the address from the private key
    */
  lazy val address = Address(Base58.encode58Check(key.getAddress))

}
