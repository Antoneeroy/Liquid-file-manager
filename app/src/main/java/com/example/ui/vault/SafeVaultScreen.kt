package com.example.ui.vault

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.VaultEntity
import com.example.data.model.FileItem
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidSquircleBadge
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIridescentBorder
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VaultGradient
import com.example.ui.theme.VoidBlack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Safe Vault Screen (Strictly ad-free per AdMob & UX policy).
 * Uses hardware Android Keystore + PBKDF2-SHA256 PIN + authentic BiometricPrompt.
 */
@Composable
fun SafeVaultScreen(
    vaultItems: List<VaultEntity>,
    isUnlocked: Boolean,
    isPinConfigured: Boolean,
    isPasswordProtectionEnabled: Boolean,
    isBiometricEnabled: Boolean,
    failedAttempts: Int,
    lockoutRemainingSeconds: Long,
    hasRecoveryConfigured: Boolean,
    recoveryQuestion: String,
    onSetupVaultPin: (pin: String, question: String, answer: String) -> Unit,
    onUnlockWithPin: (String) -> Boolean,
    onRequestBiometricUnlock: () -> Unit,
    onVerifyRecoveryAnswer: (String) -> Boolean,
    onSetNewPin: (String) -> Unit,
    onLockVault: () -> Unit,
    onRestoreItem: (VaultEntity) -> Unit,
    onAddFileToVaultClick: () -> Unit,
    onPreviewVaultItem: (VaultEntity) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // First-time Vault setup state
    var setupPin by remember { mutableStateOf("") }
    var setupConfirmPin by remember { mutableStateOf("") }
    var setupQuestion by remember { mutableStateOf("City of birth or memorable phrase") }
    var setupAnswer by remember { mutableStateOf("") }
    var setupError by remember { mutableStateOf<String?>(null) }

    // Forgot password recovery dialog
    var showForgotDialog by remember { mutableStateOf(false) }
    var recoveryAnswerInput by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf(false) }

    // Set new PIN dialog after recovery
    var showSetNewPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var newPinConfirmInput by remember { mutableStateOf("") }
    var newPinError by remember { mutableStateOf(false) }

    val shouldShowLock = !isPinConfigured || (isPasswordProtectionEnabled && !isUnlocked)

    Box(modifier = modifier.fillMaxSize()) {
        if (!isPinConfigured) {
            // First-Time Safe Vault PIN & Recovery Setup
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) TextPrimary else LightTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Configure Safe Vault",
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    accentGlowColor = NeonCyan
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create Your Private 4-Digit PIN",
                                color = if (isDark) TextPrimary else LightTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Keys are protected by Android Keystore & PBKDF2-SHA256. Set a personal recovery passphrase in case you forget your PIN.",
                            color = if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        OutlinedTextField(
                            value = setupPin,
                            onValueChange = {
                                if (it.length <= 4) setupPin = it.filter { ch -> ch.isDigit() }
                                setupError = null
                            },
                            label = { Text("New 4-Digit PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_setup_vault_pin")
                        )

                        OutlinedTextField(
                            value = setupConfirmPin,
                            onValueChange = {
                                if (it.length <= 4) setupConfirmPin = it.filter { ch -> ch.isDigit() }
                                setupError = null
                            },
                            label = { Text("Confirm 4-Digit PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_setup_vault_pin_confirm")
                        )

                        OutlinedTextField(
                            value = setupQuestion,
                            onValueChange = { setupQuestion = it },
                            label = { Text("Recovery Question (Optional)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = setupAnswer,
                            onValueChange = {
                                setupAnswer = it
                                setupError = null
                            },
                            label = { Text("Personal Recovery Answer") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_setup_recovery_answer")
                        )

                        setupError?.let { err ->
                            Text(text = err, color = SunsetPink, fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = {
                                when {
                                    setupPin.length != 4 -> setupError = "PIN must be exactly 4 digits."
                                    setupPin != setupConfirmPin -> setupError = "PINs do not match."
                                    setupAnswer.trim().length < 2 -> setupError = "Enter a recovery answer (min 2 characters)."
                                    else -> {
                                        onSetupVaultPin(setupPin, setupQuestion, setupAnswer.trim())
                                        setupPin = ""
                                        setupConfirmPin = ""
                                        setupAnswer = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_save_initial_vault_pin")
                        ) {
                            Text("Enable AES-256 Safe Vault", color = VoidBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        } else if (shouldShowLock) {
            // Glass Keypad PIN Lock Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) TextPrimary else LightTextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0x3300E5FF), Color(0x107C4DFF), Color.Transparent)))
                            .border(1.5.dp, GlassIridescentBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "Safe Vault",
                            tint = NeonCyan,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Safe Vault",
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AES-256-GCM Hardware-Backed Vault",
                        color = if (isDark) TextMuted else LightTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4-Digit PIN Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pinError || lockoutRemainingSeconds > 0L) SunsetPink
                                        else if (isFilled) NeonCyan
                                        else if (isDark) Color(0x20FFFFFF) else Color(0x20000000)
                                    )
                                    .border(
                                        1.dp,
                                        if (pinError || lockoutRemainingSeconds > 0L) SunsetPink
                                        else if (isFilled) NeonCyan
                                        else GlassBorderSubtle,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    when {
                        lockoutRemainingSeconds > 0L -> {
                            Text(
                                text = "Too many attempts. Try again in ${lockoutRemainingSeconds}s or use Biometrics.",
                                color = SunsetPink,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                        pinError -> {
                            Text(
                                text = "Incorrect PIN (Failed attempts: $failedAttempts)",
                                color = SunsetPink,
                                fontSize = 11.5.sp
                            )
                        }
                        else -> {
                            Text(
                                text = "Enter your 4-digit Vault PIN",
                                color = if (isDark) TextMuted else LightTextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isBiometricEnabled) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0x2200E5FF))
                                    .border(1.dp, Color(0x4400E5FF), RoundedCornerShape(20.dp))
                                    .clickable { onRequestBiometricUnlock() }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("btn_vault_biometric_action")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Fingerprint,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Biometric Unlock",
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (hasRecoveryConfigured) {
                            Text(
                                text = "Forgot PIN?",
                                color = if (isDark) TextSecondary else LightTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        recoveryAnswerInput = ""
                                        recoveryError = false
                                        showForgotDialog = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("btn_forgot_password")
                            )
                        }
                    }
                }

                // Glass Numeric Keypad
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("BIO", "0", "DEL")
                    )

                    for (row in rows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(22.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (key in row) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    when (key) {
                                        "BIO" -> {
                                            if (isBiometricEnabled) {
                                                KeypadCircleButton(
                                                    content = {
                                                        Icon(Icons.Filled.Fingerprint, contentDescription = "Biometric Unlock", tint = NeonCyan, modifier = Modifier.size(28.dp))
                                                    },
                                                    onClick = { onRequestBiometricUnlock() }
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.size(66.dp))
                                            }
                                        }
                                        "DEL" -> {
                                            KeypadCircleButton(
                                                content = {
                                                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete Digit", tint = if (isDark) TextSecondary else LightTextSecondary, modifier = Modifier.size(22.dp))
                                                },
                                                onClick = {
                                                    if (enteredPin.isNotEmpty()) {
                                                        enteredPin = enteredPin.dropLast(1)
                                                        pinError = false
                                                    }
                                                }
                                            )
                                        }
                                        else -> {
                                            KeypadCircleButton(
                                                content = {
                                                    Text(text = key, color = if (isDark) TextPrimary else LightTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                                                },
                                                onClick = {
                                                    if (lockoutRemainingSeconds > 0L) return@KeypadCircleButton
                                                    if (enteredPin.length < 4) {
                                                        val next = enteredPin + key
                                                        enteredPin = next
                                                        pinError = false
                                                        if (next.length == 4) {
                                                            val success = onUnlockWithPin(next)
                                                            if (!success) {
                                                                pinError = true
                                                                enteredPin = ""
                                                            } else {
                                                                pinError = false
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Unlocked Vault Contents Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) TextPrimary else LightTextPrimary)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Safe Vault",
                            color = if (isDark) TextPrimary else LightTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddFileToVaultClick,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = VoidBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add File", color = VoidBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (isPasswordProtectionEnabled) {
                            Button(
                                onClick = onLockVault,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF2A85)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = SunsetPink, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock", color = SunsetPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp),
                    cornerRadius = 22.dp,
                    backgroundColor = Color(0x2800E5FF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x3300E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Vault Protected",
                                color = if (isDark) TextPrimary else LightTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${vaultItems.size} file(s) encrypted with AES-256-GCM",
                                color = if (isDark) TextSecondary else LightTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Encrypted Files (Tap to Preview or Restore)",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (vaultItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.LockOpen,
                                contentDescription = null,
                                tint = if (isDark) TextMuted.copy(alpha = 0.5f) else LightTextMuted,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No encrypted files yet",
                                color = if (isDark) TextSecondary else LightTextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use the options menu on any file in Explorer to encrypt & lock",
                                color = if (isDark) TextMuted else LightTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(vaultItems, key = { it.id }) { item ->
                            VaultItemRow(
                                item = item,
                                onClick = { onPreviewVaultItem(item) },
                                onRestore = { onRestoreItem(item) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(96.dp))
                        }
                    }
                }
            }
        }

        // Dialog 1: Personal Recovery Answer Dialog
        if (showForgotDialog) {
            AlertDialog(
                onDismissRequest = { showForgotDialog = false },
                containerColor = if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF),
                title = {
                    Text(
                        text = "Recover Safe Vault PIN",
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Security Question: $recoveryQuestion",
                            color = if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = recoveryAnswerInput,
                            onValueChange = {
                                recoveryAnswerInput = it
                                recoveryError = false
                            },
                            label = { Text("Enter your recovery answer") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_recovery_answer")
                        )
                        if (recoveryError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Incorrect recovery answer.", color = SunsetPink, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (onVerifyRecoveryAnswer(recoveryAnswerInput)) {
                                showForgotDialog = false
                                newPinInput = ""
                                newPinConfirmInput = ""
                                newPinError = false
                                showSetNewPinDialog = true
                            } else {
                                recoveryError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("btn_verify_recovery")
                    ) {
                        Text("Verify & Reset", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotDialog = false }) {
                        Text("Cancel", color = if (isDark) TextSecondary else LightTextSecondary)
                    }
                }
            )
        }

        // Dialog 2: Set New PIN Dialog
        if (showSetNewPinDialog) {
            AlertDialog(
                onDismissRequest = { showSetNewPinDialog = false },
                containerColor = if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF),
                title = {
                    Text(
                        text = "Set New Safe Vault PIN",
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter a new 4-digit numeric PIN for your Safe Vault:",
                            color = if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 12.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4) newPinInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("New 4-digit PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_new_pin")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPinConfirmInput,
                            onValueChange = { if (it.length <= 4) newPinConfirmInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Confirm PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_confirm_pin")
                        )
                        if (newPinError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("PIN must be 4 digits and match confirm field", color = SunsetPink, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPinInput.length == 4 && newPinInput == newPinConfirmInput) {
                                onSetNewPin(newPinInput)
                                pinError = false
                                showSetNewPinDialog = false
                            } else {
                                newPinError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("btn_save_new_pin")
                    ) {
                        Text("Save & Open Vault", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSetNewPinDialog = false }) {
                        Text("Cancel", color = if (isDark) TextSecondary else LightTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun KeypadCircleButton(
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "keypad_press_scale"
    )

    Box(
        modifier = Modifier
            .size(68.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = CircleShape,
                spotColor = NeonCyan.copy(alpha = 0.20f)
            )
            .clip(CircleShape)
            .background(
                if (isDark) {
                    if (isPressed) Color(0x3500E5FF) else Color(0x1E142436)
                } else {
                    if (isPressed) Color(0x3000E5FF) else Color(0x14000000)
                }
            )
            .border(
                1.2.dp,
                if (isPressed) SolidColor(NeonCyan) else GlassIridescentBorder,
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun VaultItemRow(
    item: VaultEntity,
    onClick: () -> Unit,
    onRestore: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val dateStr = remember(item.vaultedAt) { sdf.format(Date(item.vaultedAt)) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        cornerRadius = 20.dp,
        accentGlowColor = NeonCyan,
        backgroundColor = if (isDark) Color(0x2500E5FF) else Color(0xEAFFFFFF),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                LiquidSquircleBadge(
                    icon = Icons.Filled.Lock,
                    gradient = VaultGradient,
                    contentDescription = null,
                    size = 44.dp,
                    iconSize = 22.dp,
                    cornerRadius = 14.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.originalName,
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${FileItem.formatFileSize(item.originalSize)} • Vaulted $dateStr",
                        color = if (isDark) TextSecondary else LightTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onRestore,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E5FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Restore, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restore", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
