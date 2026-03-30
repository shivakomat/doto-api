package utils

import org.mindrot.jbcrypt.BCrypt

object PasswordUtils:
  private val CostFactor = 12

  def hash(password: String): String =
    BCrypt.hashpw(password, BCrypt.gensalt(CostFactor))

  def verify(password: String, hash: String): Boolean =
    BCrypt.checkpw(password, hash)
