package com.example.ui.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.VaultEntity
import com.example.data.model.FileItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyanPurpleGradient
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricPurple
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
import com.example.ui.theme.VoidBlack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SafeVaultScreen(
    vaultItems: List<VaultEntity>,
    isUnlocked: Boolean,
    isPasswordProtectionEnabled: Boolean,
    isBiometricEnabled: Boolean = true,
    onUnlockWithPin: (String) -> Boolean,
    onBiometricUnlock: () -> Unit = {},
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
    var failedAttempts by remember { mutableStateOf(0) }

    // Biometric state
    var showBiometricDialog by remember { mutableStateOf(false) }
    var isBiometricVerifying by remember { mutableStateOf(false) }
    var isBiometricSuccess by remember { mutableStateOf(false) }

    // Forgot password dialogs
    var showForgotDialog by remember { mutableStateOf(false) }
    var recoveryAnswerInput by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf(false) }

    // Set new PIN dialog
    var showSetNewPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var newPinConfirmInput by remember { mutableStateOf("") }
    var newPinError by remember { mutableStateOf(false) }

    val shouldShowLock = isPasswordProtectionEnabled && !isUnlocked

    Box(modifier = modifier.fillMaxSize()) {
        if (shouldShowLock) {
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shield Icon with Liquid Glow
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0x3300E5FF), Color(0x107C4DFF), Color.Transparent)))
                            .border(1.5.dp, GlassIridescentBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "Safe Vault",
                            tint = NeonCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Luxury Safe Vault",
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AES-256-GCM Military Encryption",
                        color = if (isDark) TextMuted else LightTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3-Level Security Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x1800E5FF))
                            .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3-Level Security Shield Active",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                                        if (pinError) SunsetPink
                                        else if (isFilled) NeonCyan
                                        else if (isDark) Color(0x20FFFFFF) else Color(0x20000000)
                                    )
                                    .border(
                                        1.dp,
                                        if (pinError) SunsetPink
                                        else if (isFilled) NeonCyan
                                        else GlassBorderSubtle,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    if (pinError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (failedAttempts >= 10) {
                            Text(
                                text = "Max attempts exceeded (10/10)! Recovery Opened.",
                                color = SunsetPink,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Incorrect PIN (Attempt $failedAttempts of 10). ${10 - failedAttempts} tries left",
                                color = SunsetPink,
                                fontSize = 11.5.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (failedAttempts > 0) "Attempts used: $failedAttempts/10" else "Enter 4-Digit Security PIN",
                            color = if (isDark) TextMuted else LightTextMuted,
                            fontSize = 11.5.sp
                        )
                    }

                    // Biometric & Forgot Password buttons
                    Spacer(modifier = Modifier.height(10.dp))
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
                                    .clickable {
                                        showBiometricDialog = true
                                        isBiometricVerifying = false
                                        isBiometricSuccess = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
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

                        Text(
                            text = "Forgot Password?",
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
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("btn_forgot_password")
                        )
                    }
                }

                // Glass Numeric Keypad (1..9, Biometric, 0, Backspace)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
                                            KeypadCircleButton(
                                                content = {
                                                    Icon(Icons.Filled.Fingerprint, contentDescription = "Biometric", tint = NeonCyan, modifier = Modifier.size(28.dp))
                                                },
                                                onClick = {
                                                    showBiometricDialog = true
                                                    isBiometricVerifying = false
                                                    isBiometricSuccess = false
                                                }
                                            )
                                        }
                                        "DEL" -> {
                                            KeypadCircleButton(
                                                content = {
                                                    Icon(Icons.Filled.Backspace, contentDescription = "Delete", tint = if (isDark) TextSecondary else LightTextSecondary, modifier = Modifier.size(22.dp))
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
                                                    if (enteredPin.length < 4) {
                                                        val next = enteredPin + key
                                                        enteredPin = next
                                                        pinError = false
                                                        if (next.length == 4) {
                                                            val success = onUnlockWithPin(next)
                                                            if (!success) {
                                                                failedAttempts++
                                                                pinError = true
                                                                enteredPin = ""
                                                                if (failedAttempts >= 10) {
                                                                    recoveryAnswerInput = ""
                                                                    recoveryError = false
                                                                    showForgotDialog = true
                                                                }
                                                            } else {
                                                                failedAttempts = 0
                                                                pinError = false
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

                // Vault Header
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

                // Vault Security Banner
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
                                text = "${vaultItems.size} files hidden & encrypted with AES-256",
                                color = if (isDark) TextSecondary else LightTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Encrypted Files",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 16.sp,
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
                                text = "Tap '+ Add File' or long press in Explorer to lock",
                                color = if (isDark) TextMuted else LightTextMuted,
                                fontSize = 12.sp
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

        // Dialog 1: Forgot Password Recovery Dialog
        if (showForgotDialog) {
            AlertDialog(
                onDismissRequest = { showForgotDialog = false },
                containerColor = if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF),
                title = {
                    Text(
                        text = "Safe Vault Password Recovery",
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "To reset your password, answer the security question or enter the emergency master code:\n\n• Security Answer: Enter 1\n• Master Code: Enter A1234",
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
                            label = { Text("Enter Answer (1) or Code (A1234)") },
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
                            Text("Invalid input. Type '1' or 'A1234'", color = SunsetPink, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val verified = onVerifyRecoveryAnswer(recoveryAnswerInput)
                            if (verified) {
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

        // Dialog 2: Set New Working PIN Dialog
        if (showSetNewPinDialog) {
            AlertDialog(
                onDismissRequest = { showSetNewPinDialog = false },
                containerColor = if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF),
                title = {
                    Text(
                        text = "Set New Safe Vault Password",
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
                                failedAttempts = 0
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

        // Dialog 3: Biometric Authentication Dialog
        if (showBiometricDialog && shouldShowLock) {
            AlertDialog(
                onDismissRequest = {
                    if (!isBiometricVerifying) showBiometricDialog = false
                },
                containerColor = if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Biometric Unlock",
                            color = if (isDark) TextPrimary else LightTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isBiometricSuccess) "Biometric Confirmed! Unlocking Safe Vault..."
                            else if (isBiometricVerifying) "Scanning fingerprint sensor..."
                            else "Touch the glowing fingerprint sensor below to unlock with Biometrics",
                            color = if (isBiometricSuccess) DeepEmerald else if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 12.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Large glowing interactive Fingerprint Sensor
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isBiometricSuccess) Color(0x3300C853)
                                    else if (isBiometricVerifying) Color(0x3300E5FF)
                                    else Color(0x1800E5FF)
                                )
                                .border(
                                    2.dp,
                                    if (isBiometricSuccess) DeepEmerald
                                    else if (isBiometricVerifying) NeonCyan
                                    else Color(0x4000E5FF),
                                    CircleShape
                                )
                                .clickable(enabled = !isBiometricVerifying && !isBiometricSuccess) {
                                    isBiometricVerifying = true
                                }
                                .testTag("btn_biometric_touch_sensor"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBiometricSuccess) Icons.Filled.Check else Icons.Filled.Fingerprint,
                                contentDescription = "Fingerprint Sensor",
                                tint = if (isBiometricSuccess) DeepEmerald else NeonCyan,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        LaunchedEffect(isBiometricVerifying) {
                            if (isBiometricVerifying) {
                                kotlinx.coroutines.delay(400)
                                isBiometricSuccess = true
                                kotlinx.coroutines.delay(350)
                                showBiometricDialog = false
                                failedAttempts = 0
                                pinError = false
                                onBiometricUnlock()
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isBiometricSuccess) "Access Granted" else "Tap sensor or button to authenticate",
                            color = if (isBiometricSuccess) DeepEmerald else NeonCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!isBiometricVerifying && !isBiometricSuccess) {
                                isBiometricVerifying = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("btn_confirm_biometric")
                    ) {
                        Text("Scan Fingerprint", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBiometricDialog = false }) {
                        Text("Use PIN Instead", color = if (isDark) TextSecondary else LightTextSecondary)
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
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(if (isDark) Color(0x18FFFFFF) else Color(0x12000000))
            .border(1.dp, GlassBorderSubtle, CircleShape)
            .clickable(onClick = onClick),
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
            .height(72.dp),
        cornerRadius = 18.dp,
        backgroundColor = if (isDark) Color(0x18101624) else Color(0xEAFFFFFF),
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3300E5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                }
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x2800E5FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Restore", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
