package com.nexorape.safework.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexorape.safework.R

/** Presentation only: callers retain routing, role restrictions and session invalidation. */
data class SafeWorkNavItem(val label: String, val icon: ImageVector)

@Composable
fun SafeWorkShell(items: List<SafeWorkNavItem>, selected: Int, onSelect: (Int) -> Unit,
                  content: @Composable () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(SafeWorkIcons.Shield, null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                }
            }
        }, bottomBar = { SafeWorkBottomBar(items, selected, onSelect) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { content() }
    }
}

/** Equal targets grow with text; no fixed-height labels, ellipsis or icon-only sections. */
@Composable
fun SafeWorkBottomBar(items: List<SafeWorkNavItem>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, tonalElevation = 0.dp) {
        Column {
            HorizontalDivider(color = colors.outlineVariant)
            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp)
                .selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
                items.forEachIndexed { index, item ->
                    val active = selected == index
                    Column(Modifier.weight(1f).clip(MaterialTheme.shapes.medium)
                        .selectable(selected = active, role = Role.Tab, onClick = { onSelect(index) })
                        .heightIn(min = 72.dp).padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.clip(MaterialTheme.shapes.medium)
                            .background(if (active) colors.primaryContainer else Color.Transparent)
                            .padding(horizontal = 18.dp, vertical = 4.dp)) {
                            Icon(item.icon, null, Modifier.size(24.dp),
                                tint = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant)
                        }
                        Text(item.label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center,
                            color = if (active) colors.primary else colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
