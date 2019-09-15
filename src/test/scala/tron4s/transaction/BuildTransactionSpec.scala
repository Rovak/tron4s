package tron4s.transaction

import org.specs2.mutable._
import tron4s.domain.Address
import tron4s.domain.blockchain.Transaction
import tron4s.domain.blockchain.contracts.TransferTrxContract
import tron4s.infrastructure.protobuf.TransactionFactory

object BuildTransactionSpec extends Specification {

  "transaction" should {

    "build transfer contract" in {

      val transaction = Transaction(
        contract = TransferTrxContract(
          amount = 1,
          from = Address.EMPTY,
          to = Address.EMPTY,
        )
      )

      TransactionFactory.toTransaction(transaction.contract)

      ok
    }
  }
}