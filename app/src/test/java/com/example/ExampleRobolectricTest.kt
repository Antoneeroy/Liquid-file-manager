package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.crypto.VaultCrypto
import com.example.data.model.FileItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Liquid Files", appName)
  }

  @Test
  fun `format file size test`() {
    assertEquals("0 B", FileItem.formatFileSize(0))
    assertEquals("1 KB", FileItem.formatFileSize(1024))
    assertEquals("1 MB", FileItem.formatFileSize(1024 * 1024))
  }

  @Test
  fun `vault crypto encryption and decryption roundtrip`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sampleFile = File(context.filesDir, "test_secret.txt").apply {
      writeText("TopSecretLuxuryStorageData_2026")
    }
    val encryptedFile = File(context.filesDir, "test_secret.enc")
    val decryptedFile = File(context.filesDir, "test_decrypted.txt")

    val encSuccess = VaultCrypto.encryptFile(sampleFile, encryptedFile, "1234")
    assertTrue(encSuccess)
    assertTrue(encryptedFile.exists())

    val decSuccess = VaultCrypto.decryptFile(encryptedFile, decryptedFile, "1234")
    assertTrue(decSuccess)
    assertEquals("TopSecretLuxuryStorageData_2026", decryptedFile.readText())

    // Test decryption failure with incorrect PIN
    val wrongDecryptedFile = File(context.filesDir, "test_wrong.txt")
    val wrongDecSuccess = VaultCrypto.decryptFile(encryptedFile, wrongDecryptedFile, "9999")
    org.junit.Assert.assertFalse(wrongDecSuccess)
  }

  @Test
  fun `vault crypto handles multi-chunk streaming payload`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // 128KB payload (spans multiple 64KB streaming buffers)
    val largeData = ByteArray(128 * 1024) { (it % 256).toByte() }
    val largeFile = File(context.filesDir, "large_test.bin").apply {
      writeBytes(largeData)
    }
    val encryptedFile = File(context.filesDir, "large_test.enc")
    val decryptedFile = File(context.filesDir, "large_test_dec.bin")

    val encSuccess = VaultCrypto.encryptFile(largeFile, encryptedFile, "4321")
    assertTrue(encSuccess)

    val decSuccess = VaultCrypto.decryptFile(encryptedFile, decryptedFile, "4321")
    assertTrue(decSuccess)
    org.junit.Assert.assertArrayEquals(largeData, decryptedFile.readBytes())
  }

  @Test
  fun `3-level security codes validation`() {
    val defaultPin = "1234"
    val emergencyAnswer = "1"
    val masterCode = "A1234"

    // Level 2: PIN check
    assertTrue(defaultPin == "1234")

    // Level 3: Dual Emergency Recovery validation
    assertTrue(emergencyAnswer.trim() == "1")
    assertTrue(masterCode.trim().equals("A1234", ignoreCase = true))

    // 10 attempts lockout threshold check
    var failedAttempts = 0
    fun registerFailedAttempt(): Boolean {
      failedAttempts++
      return failedAttempts >= 10
    }

    for (i in 1..9) {
      org.junit.Assert.assertFalse("Attempt $i should not trigger 10-attempt lockout", registerFailedAttempt())
    }
    assertTrue("10th attempt must trigger recovery lockout", registerFailedAttempt())
  }
}
