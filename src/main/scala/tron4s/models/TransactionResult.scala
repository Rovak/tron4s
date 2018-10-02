package tron4s.models

import org.tron.api.api.Return.response_code
import org.tron.protos.Tron.Transaction

case class TransactionResult(transaction: Transaction, code: response_code, message: String)
