package com.example.cst438project1

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// PBKDF2 from the standard library, so no dependency is needed. The iteration
// count is what makes a stolen database expensive to attack; it is slow on
// purpose, so callers must stay off the main thread.
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16

    private val random = SecureRandom()
    private val encoder: Base64.Encoder = Base64.getEncoder()
    private val decoder: Base64.Decoder = Base64.getDecoder()

    // A fresh salt per user, so two people with the same password still get
    // different hashes and one cracked password does not reveal the other.
    fun newSalt(): String = ByteArray(SALT_BYTES)
        .also(random::nextBytes)
        .let(encoder::encodeToString)

    // The password arrives as a String from the text field, so there is nothing
    // to zero out that the UI is not already holding.
    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), decoder.decode(salt), ITERATIONS, KEY_BITS)
        try {
            return encoder.encodeToString(
                SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
            )
        } finally {
            spec.clearPassword()
        }
    }

    // Compared in constant time so a wrong guess cannot be narrowed down by
    // how long the answer took.
    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(
            decoder.decode(hash(password, salt)),
            decoder.decode(expectedHash)
        )
}
