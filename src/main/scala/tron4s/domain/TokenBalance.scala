package tron4s.domain

/**
  * Amount for a specific token
  * @param name name of the token
  * @param balance balance of the token
  */
case class TokenBalance(name: String, balance: Long)
