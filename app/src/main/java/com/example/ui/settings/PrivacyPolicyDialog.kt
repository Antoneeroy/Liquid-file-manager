package com.example.ui.settings

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
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit,
    onManagePrivacyOptions: (() -> Unit)? = null
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
                    text = "Privacy Policy & Data Safety",
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
                    text = "Liquid Files - Privacy & SDK Disclosure",
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                PolicySection(
                    title = "1. Local File Processing",
                    body = "Your personal files, folders, document contents, media playback history, and Safe Vault contents are processed strictly on your device and are never uploaded to external servers by Liquid Files."
                )

                PolicySection(
                    title = "2. Storage Access Permissions",
                    body = "Storage permissions (All Files Access / Scoped Media / Storage Access Framework) are used solely to let you browse, copy, move, rename, inspect, delete, and organize files you choose on your device and removable SD card."
                )

                PolicySection(
                    title = "3. Safe Vault AES-256 Encryption & Biometrics",
                    body = "Vault items are encrypted locally with AES-256-GCM using hardware-backed keys wrapped by Android Keystore. PIN verification uses salted PBKDF2-SHA256 hashes. Biometrics run through Android BiometricPrompt hardware security and biometric templates never reach app memory. Vault directories are excluded from device backups."
                )

                PolicySection(
                    title = "4. Advertising & Consent (Google AdMob + UMP)",
                    body = "Liquid Files displays non-intrusive Google AdMob banner and native ads outside Safe Vault and file operation dialogs. Where required by law (EEA/UK/US states), Google User Messaging Platform (UMP) collects your ad consent choices. Google Mobile Ads SDK may collect Advertising ID (AD_ID), approximate IP-based region, diagnostics, and ad interaction telemetry in accordance with Google's Privacy Policy."
                )

                PolicySection(
                    title = "5. Local Wi-Fi File Portal",
                    body = "Wi-Fi sharing starts a temporary HTTP server confined strictly to your local Wi-Fi network (LAN) and blocked from accessing private Vault or system directories."
                )
            }
        },
        dismissButton = if (onManagePrivacyOptions != null) {
            {
                TextButton(onClick = onManagePrivacyOptions) {
                    Text("Ad Choices", color = NeonCyan, fontWeight = FontWeight.SemiBold)
                }
            }
        } else null,
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
