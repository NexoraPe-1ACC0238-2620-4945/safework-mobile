package com.nexorape.safework.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nexorape.safework.R
import com.nexorape.safework.core.designsystem.theme.SafeWorkBrandViolet

/** Scrollable, bounded-width content; controls grow with font size and avoid the IME. */
@Composable
fun SafeWorkScreen(centerContent: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Surface supplies LocalContentColor as well as the background, including unauthenticated routes.
    Surface(Modifier.fillMaxSize(), color = colors.background, contentColor = colors.onBackground) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
            val viewportHeight = maxHeight
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight).padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp,
                    if (centerContent) Alignment.CenterVertically else Alignment.Top), content = content)
        }
    }
}

@Composable
fun SafeWorkBrand() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = SafeWorkBrandViolet, contentColor = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.medium) {
            // Dark ink contrasts with the original violet in both system themes.
            Icon(SafeWorkIcons.Shield, null, Modifier.padding(10.dp).size(24.dp),
                tint = androidx.compose.ui.graphics.Color(0xFF0D0C22))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.design_brand_caption), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SafeWorkHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() })
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SafeWorkCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
fun SafeWorkMessage(text: String, error: Boolean = false, success: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Surface(color = if (error) colors.errorContainer else colors.secondaryContainer,
        contentColor = if (error) colors.onErrorContainer else colors.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(if (success) SafeWorkIcons.Check else SafeWorkIcons.Info, null, Modifier.size(20.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SafeWorkLoading() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.design_loading), style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Composable
fun SafeWorkPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean, icon: ImageVector = SafeWorkIcons.Arrow) {
    Button(onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(12.dp))
        Icon(icon, null, Modifier.size(20.dp))
    }
}

@Composable
fun SafeWorkSecondaryButton(text: String, onClick: () -> Unit, enabled: Boolean, icon: ImageVector) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(16.dp)) {
        Icon(icon, null, Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text)
    }
}

@Composable
fun SafeWorkField(value: String, onValueChange: (String) -> Unit, label: String, enabled: Boolean,
                  icon: ImageVector, type: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, enabled = enabled, singleLine = true,
        leadingIcon = { Icon(icon, null, Modifier.size(20.dp)) },
        shape = MaterialTheme.shapes.small, keyboardOptions = KeyboardOptions(keyboardType = type),
        modifier = Modifier.fillMaxWidth())
}

/** Visibility is transient UI state, reset on clearing and backgrounding; never saveable. */
@Composable
fun SafeWorkSecretField(value: String, onValueChange: (String) -> Unit, label: String,
                        enabled: Boolean, allowReveal: Boolean = true) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(value.isEmpty()) { if (value.isEmpty()) visible = false }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) visible = false
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val description = stringResource(if (visible) R.string.design_hide_password else R.string.design_show_password)
    val visibilityState = stringResource(if (visible) R.string.design_password_visible else R.string.design_password_hidden)
    OutlinedTextField(value, onValueChange, label = { Text(label) }, enabled = enabled, singleLine = true,
        leadingIcon = { Icon(SafeWorkIcons.Lock, null, Modifier.size(20.dp)) },
        trailingIcon = if (allowReveal) {{
            IconToggleButton(checked = visible, onCheckedChange = { visible = it }, enabled = enabled,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { stateDescription = visibilityState }) {
                Icon(if (visible) SafeWorkIcons.EyeOff else SafeWorkIcons.Eye, description, Modifier.size(22.dp))
            }
        }} else null,
        shape = MaterialTheme.shapes.small,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
        modifier = Modifier.fillMaxWidth())
}

@Composable
fun SafeWorkDetail(icon: ImageVector, label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.padding(top = 3.dp).size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Visual tab selection only; routing and role checks stay with the caller. */
@Composable
fun SafeWorkTabs(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surfaceContainer, shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()) {
        Row(Modifier.padding(4.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            labels.forEachIndexed { index, label ->
                val active = index == selectedIndex
                Box(Modifier.weight(1f).clip(MaterialTheme.shapes.small)
                    .background(if (active) colors.primaryContainer else Color.Transparent)
                    .selectable(selected = active, role = Role.Tab, onClick = { onSelect(index) })
                    .heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center,
                        color = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant)
                }
            }
        }
    }
}
