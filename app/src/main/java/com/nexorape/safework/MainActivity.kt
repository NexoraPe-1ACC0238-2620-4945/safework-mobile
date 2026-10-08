package com.nexorape.safework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nexorape.safework.core.designsystem.theme.SafeWorkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeWorkTheme {
                SafeWorkScreen()
            }
        }
    }
}

@Composable
private fun SafeWorkScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.welcome_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "English - light", showBackground = true, locale = "en-rUS")
@Preview(name = "Latin American Spanish - light", showBackground = true, locale = "es-rPE")
@Composable
private fun SafeWorkScreenPreview() {
    SafeWorkTheme(darkTheme = false) {
        SafeWorkScreen()
    }
}

@Preview(name = "English - dark", showBackground = true)
@Composable
private fun SafeWorkScreenDarkPreview() {
    SafeWorkTheme(darkTheme = true) {
        SafeWorkScreen()
    }
}
