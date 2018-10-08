package tron4s.models

case class TransferModel(
  ownerAddress: String = "",
  toAddress: String = "",
  amount: Long
)
