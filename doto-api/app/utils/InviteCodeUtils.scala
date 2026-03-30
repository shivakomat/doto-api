package utils

import scala.util.Random

object InviteCodeUtils:
  private val Alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

  def generate(): String =
    (1 to 6).map(_ => Alphabet(Random.nextInt(Alphabet.length))).mkString
