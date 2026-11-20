package com.example.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RamStats
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidRamCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SunsetPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    showHiddenFiles: Boolean,
    showStorageBar: Boolean,
    ramStats: RamStats,
    isPinConfigured: Boolean,
    isVaultPasswordEnabled: Boolean,
    isBiometricEnabled: Boolean,
    isPrivacyOptionsRequired: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onToggleHiddenFiles: (Boolean) -> Unit,
    onToggleStorageBar: (Boolean) -> Unit,
    onToggleVaultPassword: (Boolean) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onChangeVaultPin: (String) -> Unit,
    onUpdateRecoverySecurity: (question: String, answer: String) -> Unit,
    onOpenAdPrivacyOptions: () -> Unit,
    onBoostRam: () -> Unit,
    onClearAppCache: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showRecoveryConfigDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var recQuestionInput by remember { mutableStateOf("Memorable recovery phrase") }
    var recAnswerInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("btn_settings_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) TextPrimary else LightTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Settings",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Preferences, Safe Vault & Privacy",
                    color = if (isDark) TextSecondary else LightTextSecondary,
                    fontSize = 11.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SectionHeader("Device RAM Memory") }

            item {
                LiquidRamCard(
                    ramStats = ramStats,
                    onBoostRam = onBoostRam
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Safe Vault Security")
            }

            item {
                SettingSwitchRow(
                    icon = if (isVaultPasswordEnabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    iconColor = if (isVaultPasswordEnabled) NeonCyan else SunsetPink,
                    title = "Vault Password Protection",
                    subtitle = if (!isPinConfigured) {
                        "Set up a 4-digit PIN inside Safe Vault"
                    } else if (isVaultPasswordEnabled) {
                        "PBKDF2 PIN required to unlock Vault"
                    } else {
                        "Vault password lock paused"
                    },
                    checked = isVaultPasswordEnabled,
                    onCheckedChange = onToggleVaultPassword
                )
            }

            item {
                SettingSwitchRow(
                    icon = Icons.Filled.Fingerprint,
                    iconColor = ElectricPurple,
                    title = "Biometric (Fingerprint / Face) Unlock",
                    subtitle = if (isBiometricEnabled) "Hardware BiometricPrompt enabled" else "Biometric prompt disabled",
                    checked = isBiometricEnabled,
                    onCheckedChange = onToggleBiometric
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    onClick = { showChangePinDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x2800E5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Key, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isPinConfigured) "Change Vault PIN" else "Set Vault PIN",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Secured via PBKDF2-SHA256 & Android Keystore",
                                    color = if (isDark) TextSecondary else LightTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showChangePinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x2800E5FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isPinConfigured) "Change" else "Set", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    onClick = { showRecoveryConfigDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x287C4DFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Security, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Update Recovery Passphrase",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Configure your personal PIN recovery question",
                                    color = if (isDark) TextSecondary else LightTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showRecoveryConfigDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x287C4DFF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Update", color = ElectricPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Display & Theme Preferences")
            }

            item {
                SettingSwitchRow(
                    icon = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    iconColor = if (isDarkTheme) NeonCyan else SunsetPink,
                    title = if (isDarkTheme) "Theme: Luxury Night Mode" else "Theme: Frosted Day Mode",
                    subtitle = if (isDarkTheme) "Deep obsidian canvas" else "Bright liquid frost",
                    checked = isDarkTheme,
                    onCheckedChange = onToggleTheme
                )
            }

            item {
                SettingSwitchRow(
                    icon = Icons.Filled.PieChart,
                    iconColor = ElectricBlue,
                    title = "Dashboard Storage Overview",
                    subtitle = "Show real-time glowing radial storage progress ring",
                    checked = showStorageBar,
                    onCheckedChange = onToggleStorageBar
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("File System & Cache")
            }

            item {
                SettingSwitchRow(
                    icon = if (showHiddenFiles) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    iconColor = SunsetPink,
                    title = "Show Hidden Files",
                    subtitle = "Display dotfiles (.nomedia, .cache) in Explorer",
                    checked = showHiddenFiles,
                    onCheckedChange = onToggleHiddenFiles
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    onClick = onClearAppCache
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x28FF2A85)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = SunsetPink, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Clear App Cache",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Reclaim thumbnail cache & temporary preview files",
                                    color = if (isDark) TextSecondary else LightTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = onClearAppCache,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x28FF2A85)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear", color = SunsetPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Privacy, Consent & Compliance")
            }

            if (isPrivacyOptionsRequired) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        onClick = onOpenAdPrivacyOptions
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x2800E5FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.PrivacyTip, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Manage Ad Privacy Choices",
                                        color = if (isDark) TextPrimary else LightTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Update Google UMP consent preferences",
                                        color = if (isDark) TextSecondary else LightTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenAdPrivacyOptions,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x2800E5FF)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_ad_privacy_choices")
                            ) {
                                Text("Manage", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    onClick = { showPrivacyPolicyDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x2800E5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Shield, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Privacy Policy & Data Safety",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Local storage access, Vault AES-256 & AdMob disclosure",
                                    color = if (isDark) TextSecondary else LightTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showPrivacyPolicyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x2800E5FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_view_privacy_policy")
                        ) {
                            Text("View", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        if (showChangePinDialog) {
            AlertDialog(
                onDismissRequest = { showChangePinDialog = false },
                containerColor = if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF),
                title = { Text("Update Safe Vault PIN", color = if (isDark) TextPrimary else LightTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Enter a new 4-digit PIN for Safe Vault:",
                            color = if (isDark) TextSecondary else LightTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4) newPinInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("4-digit numeric PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_settings_pin")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPinInput.length == 4) {
                                onChangeVaultPin(newPinInput)
                                showChangePinDialog = false
                                newPinInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("btn_confirm_change_pin")
                    ) {
                        Text("Save PIN", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePinDialog = false }) {
                        Text("Cancel", color = if (isDark) TextSecondary else LightTextSecondary)
                    }
                }
            )
        }

        if (showRecoveryConfigDialog) {
            AlertDialog(
                onDismissRequest = { showRecoveryConfigDialog = false },
                containerColor = if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF),
                title = { Text("Set Recovery Security Question", color = if (isDark) TextPrimary else LightTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = recQuestionInput,
                            onValueChange = { recQuestionInput = it },
                            label = { Text("Security Question") },
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
                            value = recAnswerInput,
                            onValueChange = { recAnswerInput = it },
                            label = { Text("Recovery Answer (Min 2 chars)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorderSubtle,
                                focusedTextColor = if (isDark) TextPrimary else LightTextPrimary,
                                unfocusedTextColor = if (isDark) TextPrimary else LightTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (recAnswerInput.trim().length >= 2) {
                                onUpdateRecoverySecurity(recQuestionInput, recAnswerInput.trim())
                                recAnswerInput = ""
                                showRecoveryConfigDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Save Recovery", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRecoveryConfigDialog = false }) {
                        Text("Cancel", color = if (isDark) TextSecondary else LightTextSecondary)
                    }
                }
            )
        }

        if (showPrivacyPolicyDialog) {
            PrivacyPolicyDialog(
                onDismiss = { showPrivacyPolicyDialog = false },
                onManagePrivacyOptions = if (isPrivacyOptionsRequired) {
                    {
                        showPrivacyPolicyDialog = false
                        onOpenAdPrivacyOptions()
                    }
                } else null
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val isDark = LocalIsDarkTheme.current
    Text(
        text = title,
        color = if (isDark) TextPrimary else LightTextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = if (isDark) TextSecondary else LightTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VoidBlack,
                    checkedTrackColor = NeonCyan
                )
            )
        }
    }
}
