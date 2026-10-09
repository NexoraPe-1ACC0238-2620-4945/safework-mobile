package com.nexorape.safework.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun localTimestamp(instant: Instant): String {
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).withZone(zone).format(instant) + " (${zone.id})"
}
