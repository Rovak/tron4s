package tron4s.transaction

import io.github.novacrypto.bip32.ExtendedPrivateKey
import io.github.novacrypto.bip32.networks.Bitcoin
import io.github.novacrypto.bip39.wordlists.English
import io.github.novacrypto.bip39.{MnemonicValidator, SeedCalculator}
import org.specs2.mutable._
import org.tron.common.crypto.ECKey
import org.tron.common.utils.{Base58, ByteArray, Sha256Hash}

import scala.collection.JavaConverters._

object SendTransactionSpec extends Specification {
  "transaction" should {

    "tries retries" in {

      ok
    }
  }
}