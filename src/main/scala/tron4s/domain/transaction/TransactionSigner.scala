package tron4s.domain.transaction

import com.google.protobuf.ByteString
import org.tron.common.utils.Sha256Hash
import org.tron.protos.Tron.Transaction
import tron4s.domain.PrivateKey

class TransactionSigner {

  /**
    * Add signature to the transaction
    */
  def sign(transaction: Transaction, pk: PrivateKey): Transaction = {
    val signature = pk.key.sign(Sha256Hash.hash(transaction.getRawData.toByteArray))
    val sig = ByteString.copyFrom(signature.toByteArray)
    transaction.addSignature(sig)
  }
}
