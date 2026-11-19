package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPurpleGradient
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VoidBlack

enum class NavigationTab(val label: String) {
    DASHBOARD("Storage"),
    EXPLORER("Files"),
    VAULT("Vault"),
    TOOLS("Optimizer"),
    WIRELESS("Share"),
    SETTINGS("Settings")
}

@Composable
fun FloatingBottomDock(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pill Glass Dock
        Box(
            modifier = Modifier
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp), spotColor = NeonCyan.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xCC0D121D))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x3500E5FF),
                            Color(0x20FFFFFF),
                            Color(0x207C4DFF)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Storage / Dashboard
                DockItem(
                    selected = currentTab == NavigationTab.DASHBOARD,
                    label = "Storage",
                    activeIcon = Icons.Filled.PieChart,
                    inactiveIcon = Icons.Outlined.PieChart,
                    testTag = "tab_dashboard",
                    onClick = { onTabSelected(NavigationTab.DASHBOARD) }
                )

                // Tab 2: Explorer
                DockItem(
                    selected = currentTab == NavigationTab.EXPLORER,
                    label = "Files",
                    activeIcon = Icons.Filled.Folder,
                    inactiveIcon = Icons.Outlined.Folder,
                    testTag = "tab_explorer",
                    onClick = { onTabSelected(NavigationTab.EXPLORER) }
                )

                // Center Action Button (+)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CyanPurpleGradient)
                        .border(1.5.dp, Color(0x80FFFFFF), CircleShape)
                        .clickable(onClick = onAddClicked)
                        .testTag("action_center_add"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Quick Actions",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Tab 3: Safe Vault
                DockItem(
                    selected = currentTab == NavigationTab.VAULT,
                    label = "Vault",
                    activeIcon = Icons.Filled.Lock,
                    inactiveIcon = Icons.Outlined.Lock,
                    testTag = "tab_vault",
                    onClick = { onTabSelected(NavigationTab.VAULT) }
                )

                // Tab 4: Tools / Optimizer
                DockItem(
                    selected = currentTab == NavigationTab.TOOLS,
                    label = "Clean",
                    activeIcon = Icons.Filled.AutoAwesome,
                    inactiveIcon = Icons.Outlined.AutoAwesome,
                    testTag = "tab_tools",
                    onClick = { onTabSelected(NavigationTab.TOOLS) }
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    selected: Boolean,
    label: String,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "icon_scale"
    )
    val tintColor by animateColorAsState(
        targetValue = if (selected) NeonCyan else TextMuted,
        label = "icon_tint"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier
                .size(22.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextMuted
        )
    }
}
