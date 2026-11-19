package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xF0101624) else Color(0xF0FFFFFF),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.PrivacyTip,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Privacy Policy & Compliance",
                    color = if (isDark) TextPrimary else LightTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Liquid Files - Data Safety & Privacy Disclosure",
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                PolicySection(
                    title = "1. Zero Data Collection & Zero Telemetry",
                    body = "Liquid Files operates 100% on-device. We do not track, collect, store, transmit, or sell your personal data, files, browsing logs, or hardware telemetry to any external servers or third parties."
                )

                PolicySection(
                    title = "2. Storage Access & Permissions",
                    body = "Storage access permissions (MANAGE_EXTERNAL_STORAGE / Scoped Storage) are strictly utilized to provide core file browsing, renaming, organization, compression, and trash restoration requested by the user."
                )

                PolicySection(
                    title = "3. Safe Vault AES-256 Encryption",
                    body = "Files moved to the Safe Vault are encrypted directly on your device using military-grade AES-256-GCM. Encryption keys are derived on-device and never leave your hardware. Vault files are excluded from system backups."
                )

                PolicySection(
                    title = "4. Local Wi-Fi Sharing",
                    body = "The Wireless File Sharing feature operates exclusively on your local Wi-Fi network (LAN). No cloud proxy or intermediate servers are used. File transfers remain within your own network."
                )

                PolicySection(
                    title = "5. Biometric Authentication",
                    body = "Biometric unlock relies on Android BiometricPrompt hardware security. Biometric fingerprint or facial data is processed solely by the Android OS Trusted Execution Environment (TEE) and is never accessible to this application."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_close_privacy_policy")
            ) {
                Text("Got It", color = VoidBlack, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun PolicySection(title: String, body: String) {
    val isDark = LocalIsDarkTheme.current
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) TextPrimary else LightTextPrimary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            color = if (isDark) TextSecondary else LightTextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
