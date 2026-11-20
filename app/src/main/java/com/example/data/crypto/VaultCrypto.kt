package com.example.data.crypto

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object VaultCrypto {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val SALT_LENGTH_BYTE = 16
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH_BIT = 256
    private const val BUFFER_SIZE = 64 * 1024 // 64 KB streaming buffer

    private fun deriveKey(pin: String, salt: ByteArray): SecretKeySpec {
        val chars = pin.toCharArray()
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(chars, salt, ITERATION_COUNT, KEY_LENGTH_BIT)
        return try {
            val tmp = factory.generateSecret(spec)
            val encoded = tmp.encoded
            try {
                SecretKeySpec(encoded, "AES")
            } finally {
                encoded?.fill(0)
            }
        } finally {
            spec.clearPassword()
            chars.fill('\u0000')
        }
    }

    /**
     * Encrypts inputFile to outputFile using streaming AES-256-GCM.
     * Preserves exact binary format (16-byte salt + 12-byte IV + GCM ciphertext) for update compatibility.
     */
    fun encryptFile(inputFile: File, outputFile: File, pin: String): Boolean {
        if (!inputFile.exists() || !inputFile.canRead() || pin.isEmpty()) return false
        outputFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
        val tempOutput = File(outputFile.parentFile, "${outputFile.name}.tmp")
        return try {
            val random = SecureRandom()
            val salt = ByteArray(SALT_LENGTH_BYTE).also { random.nextBytes(it) }
            val iv = ByteArray(IV_LENGTH_BYTE).also { random.nextBytes(it) }
            val secretKey = deriveKey(pin, salt)

            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

            FileOutputStream(tempOutput).use { fos ->
                fos.write(salt)
                fos.write(iv)
                CipherOutputStream(fos, cipher).use { cos ->
                    FileInputStream(inputFile).use { fis ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            cos.write(buffer, 0, bytesRead)
                        }
                    }
                    cos.flush()
                }
                fos.flush()
            }

            if (tempOutput.exists()) {
                if (outputFile.exists()) outputFile.delete()
                if (tempOutput.renameTo(outputFile)) {
                    true
                } else {
                    tempOutput.delete()
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            if (tempOutput.exists()) tempOutput.delete()
            false
        }
    }

    /**
     * Decrypts inputFile to outputFile using streaming AES-256-GCM.
     * Validates GCM authentication tag on stream completion; deletes temp file on auth failure.
     */
    fun decryptFile(inputFile: File, outputFile: File, pin: String): Boolean {
        if (!inputFile.exists() || !inputFile.canRead() || pin.isEmpty()) return false
        outputFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
        val tempOutput = File(outputFile.parentFile, "${outputFile.name}.tmp")
        return try {
            var success = false
            FileInputStream(inputFile).use { fis ->
                val salt = ByteArray(SALT_LENGTH_BYTE)
                val iv = ByteArray(IV_LENGTH_BYTE)

                if (fis.read(salt) != SALT_LENGTH_BYTE) return false
                if (fis.read(iv) != IV_LENGTH_BYTE) return false

                val secretKey = deriveKey(pin, salt)
                val cipher = Cipher.getInstance(ALGORITHM)
                val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

                CipherInputStream(fis, cipher).use { cis ->
                    FileOutputStream(tempOutput).use { fos ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (cis.read(buffer).also { bytesRead = it } != -1) {
                            fos.write(buffer, 0, bytesRead)
                        }
                        fos.flush()
                    }
                }
                success = true
            }

            if (success && tempOutput.exists()) {
                if (outputFile.exists()) outputFile.delete()
                if (tempOutput.renameTo(outputFile)) {
                    true
                } else {
                    tempOutput.delete()
                    false
                }
            } else {
                tempOutput.delete()
                false
            }
        } catch (e: Exception) {
            if (tempOutput.exists()) tempOutput.delete()
            false
        }
    }
}
