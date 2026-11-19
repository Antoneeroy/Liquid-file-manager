package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.SdCard
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

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    showHiddenFiles: Boolean,
    showStorageBar: Boolean,
    ramStats: RamStats,
    isVaultPasswordEnabled: Boolean,
    isBiometricEnabled: Boolean = true,
    onToggleTheme: (Boolean) -> Unit,
    onToggleHiddenFiles: (Boolean) -> Unit,
    onToggleStorageBar: (Boolean) -> Unit,
    onToggleVaultPassword: (Boolean) -> Unit,
    onToggleBiometric: (Boolean) -> Unit = {},
    onChangeVaultPin: (String) -> Unit,
    onBoostRam: () -> Unit,
    onClearAppCache: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

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
                    text = "Preferences, Safe Vault & System RAM",
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
            // Section 1: Real RAM Memory
            item {
                SectionHeader("Device RAM Memory")
            }

            item {
                LiquidRamCard(
                    ramStats = ramStats,
                    onBoostRam = onBoostRam
                )
            }

            // Section 2: Safe Vault Security (Password Protection On / Off)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Safe Vault Security")
            }

            item {
                SettingSwitchRow(
                    icon = if (isVaultPasswordEnabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    iconColor = if (isVaultPasswordEnabled) NeonCyan else SunsetPink,
                    title = "Vault Password Protection",
                    subtitle = if (isVaultPasswordEnabled) "PIN required to access Vault (Default: 1234)" else "Vault password is OFF (Opens directly)",
                    checked = isVaultPasswordEnabled,
                    onCheckedChange = onToggleVaultPassword
                )
            }

            item {
                SettingSwitchRow(
                    icon = Icons.Filled.Fingerprint,
                    iconColor = ElectricPurple,
                    title = "Biometric (Fingerprint) Unlock",
                    subtitle = if (isBiometricEnabled) "Quick biometric authentication enabled" else "Biometric prompt disabled",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    text = "Change Vault PIN",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Emergency code: A1234 | Recovery: 1",
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
                            Text("Change", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 3: Display & Theme Preferences
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Display & Theme Preferences")
            }

            // Day / Night Theme Toggle
            item {
                SettingSwitchRow(
                    icon = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    iconColor = if (isDarkTheme) NeonCyan else SunsetPink,
                    title = if (isDarkTheme) "Theme: Luxury Night Mode" else "Theme: Frosted Day Mode",
                    subtitle = if (isDarkTheme) "Deep obsidian void black (#07090E)" else "Bright liquid frost pearl white",
                    checked = isDarkTheme,
                    onCheckedChange = onToggleTheme
                )
            }

            // Show / Hide Storage Bar
            item {
                SettingSwitchRow(
                    icon = Icons.Filled.PieChart,
                    iconColor = ElectricBlue,
                    title = "Dashboard Storage Bar & Ring",
                    subtitle = "Show real-time glowing radial storage progress on dashboard",
                    checked = showStorageBar,
                    onCheckedChange = onToggleStorageBar
                )
            }

            // Section 4: File Explorer & Privacy
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("File System & Privacy")
            }

            // Show Hidden Files (.nomedia, .hidden)
            item {
                SettingSwitchRow(
                    icon = if (showHiddenFiles) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    iconColor = SunsetPink,
                    title = "Show Hidden Files",
                    subtitle = "Display system dotfiles (.nomedia, .cache, .tmp) in explorer",
                    checked = showHiddenFiles,
                    onCheckedChange = onToggleHiddenFiles
                )
            }

            // Clear App Cache action
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    text = "Clear Explorer Cache",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Reclaim thumbnail cache & temp indexes",
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

            // Section 5: Legal & Play Store Compliance
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader("Legal & Play Store Compliance")
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
                                    text = "Privacy Policy & Compliance",
                                    color = if (isDark) TextPrimary else LightTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "100% on-device, zero telemetry & offline safe",
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

        // Change PIN Dialog in Settings
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

        // Privacy Policy Dialog
        if (showPrivacyPolicyDialog) {
            PrivacyPolicyDialog(
                onDismiss = { showPrivacyPolicyDialog = false }
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
