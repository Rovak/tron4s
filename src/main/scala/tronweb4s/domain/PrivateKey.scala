package tronweb4s.domain

import org.tron.common.crypto.ECKey
import org.tron.common.utils.{Base58, ByteArray}

/**
  * Private Key
  */
case class PrivateKey(keyBytes: Array[Byte]) {
  require(keyBytes.length == 64,  "Key must be 64 bytes")

  /**
    * Private key as hex string
    */
  def this(key: String) = {
    this(ByteArray.fromHexString(key))
  }

  /**
    * Compute the address from the private key
    */
  lazy val address = {
    val key = ECKey.fromPrivate(keyBytes)
    Base58.encode58Check(key.getAddress)
  }

}
