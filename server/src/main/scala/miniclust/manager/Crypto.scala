//package miniclust.manager
//
//import javax.crypto.{Cipher, SecretKeyFactory}
//import javax.crypto.spec.{SecretKeySpec, IvParameterSpec, PBEKeySpec}
//import java.security.SecureRandom
//import java.util.Base64
//
///*
// * Copyright (C) 2025 Romain Reuillon
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//object Crypto:
//  object Secret:
//    given Conversion[Secret, String] = identity
//    def apply(s: String): Secret = s
//
//  opaque type Secret = String
//
//  private val algorithm = "AES/CBC/PKCS5Padding"
//  private val keyLength = 256 // bits
//  private val saltLength = 16 // bytes
//  private val ivLength = 16 // bytes
//  private val iterations = 65536
//
//  def deriveKey(secret: String, salt: Array[Byte]): SecretKeySpec =
//    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//    val spec = new PBEKeySpec(secret.toCharArray, salt, iterations, keyLength)
//    val keyBytes = factory.generateSecret(spec).getEncoded
//    new SecretKeySpec(keyBytes, "AES")
//
//  def encrypt(plainText: String)(using secret: Secret): String =
//    val salt = new Array[Byte](saltLength)
//    val iv = new Array[Byte](ivLength)
//    val random = new SecureRandom()
//    random.nextBytes(salt)
//    random.nextBytes(iv)
//
//    val key = deriveKey(secret, salt)
//    val cipher = Cipher.getInstance(algorithm)
//    cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv))
//    val cipherText = cipher.doFinal(plainText.getBytes("UTF-8"))
//
//    val combined = salt ++ iv ++ cipherText
//    Base64.getEncoder.encodeToString(combined)
//
//  def decrypt(encrypted: String)(using secret: Secret): String =
//    val decoded = Base64.getDecoder.decode(encrypted)
//    val salt = decoded.slice(0, saltLength)
//    val iv = decoded.slice(saltLength, saltLength + ivLength)
//    val cipherText = decoded.drop(saltLength + ivLength)
//
//    val key = deriveKey(secret, salt)
//    val cipher = Cipher.getInstance(algorithm)
//    cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv))
//    val decryptedBytes = cipher.doFinal(cipherText)
//    String(decryptedBytes, "UTF-8")