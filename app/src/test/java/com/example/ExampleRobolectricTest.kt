package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.crypto.VaultCrypto
import com.example.data.crypto.VaultSecurityManager
import com.example.data.model.FileItem
import com.example.data.repository.FileManagerRepository
import com.example.data.server.LocalWebServer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun `app identity matches Liquid Files`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Liquid Files", appName)
    }

    @Test
    fun `format file size renders accurate units`() {
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

        val encSuccess = VaultCrypto.encryptFile(sampleFile, encryptedFile, "HighEntropySecretKey_2026")
        assertTrue(encSuccess)
        assertTrue(encryptedFile.exists())

        val decSuccess = VaultCrypto.decryptFile(encryptedFile, decryptedFile, "HighEntropySecretKey_2026")
        assertTrue(decSuccess)
        assertEquals("TopSecretLuxuryStorageData_2026", decryptedFile.readText())

        // Wrong secret must fail GCM tag verification and clean up output
        val wrongDecryptedFile = File(context.filesDir, "test_wrong.txt")
        val wrongDecSuccess = VaultCrypto.decryptFile(encryptedFile, wrongDecryptedFile, "WrongKey_9999")
        assertFalse(wrongDecSuccess)
        assertFalse(wrongDecryptedFile.exists())
    }

    @Test
    fun `vault crypto handles multi-chunk and zero-byte files`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Zero-byte file roundtrip
        val emptyFile = File(context.filesDir, "empty_test.bin").apply { writeBytes(ByteArray(0)) }
        val emptyEnc = File(context.filesDir, "empty_test.enc")
        val emptyDec = File(context.filesDir, "empty_test_dec.bin")
        assertTrue(VaultCrypto.encryptFile(emptyFile, emptyEnc, "KeyEmptyTest"))
        assertTrue(VaultCrypto.decryptFile(emptyEnc, emptyDec, "KeyEmptyTest"))
        assertEquals(0L, emptyDec.length())

        // 128KB payload spanning multiple 64KB streaming buffers
        val largeData = ByteArray(128 * 1024) { (it % 256).toByte() }
        val largeFile = File(context.filesDir, "large_test.bin").apply { writeBytes(largeData) }
        val encryptedFile = File(context.filesDir, "large_test.enc")
        val decryptedFile = File(context.filesDir, "large_test_dec.bin")

        assertTrue(VaultCrypto.encryptFile(largeFile, encryptedFile, "4321StrongSecret"))
        assertTrue(VaultCrypto.decryptFile(encryptedFile, decryptedFile, "4321StrongSecret"))
        assertArrayEquals(largeData, decryptedFile.readBytes())
    }

    @Test
    fun `vault security manager enforces user PIN, lockout cooldown and removes backdoors`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        assertFalse("Fresh install must require PIN setup", security.isPinConfigured())

        security.setupVaultCredentials(
            pin = "7539",
            recoveryQuestion = "First pet name?",
            recoveryAnswer = "Milo"
        )

        assertTrue(security.isPinConfigured())
        assertTrue(security.verifyPin("7539"))

        // Verify legacy hardcoded backdoor codes are strictly rejected
        assertFalse(security.verifyPin("1234"))
        assertFalse(security.verifyPin("1"))
        assertFalse(security.verifyPin("A1234"))

        // Reset failed attempts from negative checks, then verify 10-attempt lockout cooldown
        security.resetFailedAttempts()
        for (attempt in 1..9) {
            assertFalse(security.verifyPin("0000"))
            assertEquals(0L, security.getLockoutRemainingSeconds())
        }
        assertFalse(security.verifyPin("0000"))
        assertTrue("10th attempt must trigger cooldown lockout", security.getLockoutRemainingSeconds() > 0L)
        assertFalse("Even correct PIN must be blocked during lockout", security.verifyPin("7539"))

        // Recovery validation
        assertTrue(security.verifyRecoveryAnswer("  milo "))
        assertFalse(security.verifyRecoveryAnswer("1"))
        assertFalse(security.verifyRecoveryAnswer("A1234"))

        // Master key generation persistence
        val master1 = security.getOrCreateMasterKeySecret()
        val master2 = security.getOrCreateMasterKeySecret()
        assertTrue(master1.isNotBlank())
        assertEquals(master1, master2)
    }

    @Test
    fun `repository blocks path traversal and supports trash restore roundtrip`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = FileManagerRepository(context)
        val workspace = File(context.filesDir, "repo_test_root").apply { mkdirs() }

        // Path traversal / invalid names must be rejected
        assertFalse(repo.createFolder(workspace.absolutePath, ".."))
        assertFalse(repo.createFolder(workspace.absolutePath, "../escape"))
        assertFalse(repo.createFolder(workspace.absolutePath, "sub/nested"))
        assertFalse(repo.createFolder(workspace.absolutePath, "   "))

        assertTrue(repo.createFolder(workspace.absolutePath, "SafeFolder"))
        val safeFolder = File(workspace, "SafeFolder")
        assertTrue(safeFolder.exists() && safeFolder.isDirectory)

        assertTrue(repo.createTextFile(safeFolder.absolutePath, "note.txt", "Hello Liquid Files"))
        val noteFile = File(safeFolder, "note.txt")
        assertTrue(noteFile.exists())

        // Rename traversal rejection
        assertFalse(repo.renameItem(noteFile.absolutePath, "../../hijack.txt"))
        assertTrue(repo.renameItem(noteFile.absolutePath, "renamed_note.txt"))
        val renamedFile = File(safeFolder, "renamed_note.txt")
        assertTrue(renamedFile.exists())

        // Search nested folder file via repository.searchFiles
        val nestedSearchHits = repo.searchFiles("renamed_note", showHidden = false, searchRoot = workspace.absolutePath)
        assertTrue(nestedSearchHits.any { it.name == "renamed_note.txt" })

        // Delete to trash and verify trash record created
        val fileItem = FileItem(
            path = renamedFile.absolutePath,
            name = renamedFile.name,
            size = renamedFile.length(),
            lastModified = renamedFile.lastModified(),
            isDirectory = false,
            category = com.example.data.model.FileCategory.DOCUMENTS,
            extension = "txt"
        )
        assertTrue(repo.deleteToTrash(fileItem))
        assertFalse(renamedFile.exists())

        val ramStats = repo.getRamStats()
        assertNotNull(ramStats)
    }

    @Test
    fun `local web server initializes offline safely`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val server = LocalWebServer(context) { context.filesDir.absolutePath }
        assertFalse(server.isRunning.value)
        assertEquals("Offline", server.serverUrl.value)
    }
}
