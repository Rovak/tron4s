package tron4s.domain

case class Account(
  address: Address,
  name: Option[String] = None,
)
