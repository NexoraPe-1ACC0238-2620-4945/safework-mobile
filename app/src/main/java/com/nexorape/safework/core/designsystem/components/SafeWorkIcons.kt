package com.nexorape.safework.core.designsystem.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Original, simple outline symbols. No third-party artwork or icon dependency. */
object SafeWorkIcons {
    private fun icon(name: String, draw: PathBuilder.() -> Unit) = ImageVector.Builder(
        name, 24.dp, 24.dp, 24f, 24f,
    ).apply {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = draw)
    }.build()

    val Shield = icon("Shield") {
        moveTo(12f, 3f); lineTo(20f, 6f); lineTo(20f, 12f)
        curveTo(20f, 16f, 16f, 20f, 12f, 22f)
        curveTo(8f, 20f, 4f, 16f, 4f, 12f); lineTo(4f, 6f); close()
        moveTo(8f, 12f); lineTo(11f, 15f); lineTo(16f, 9f)
    }
    val Person = icon("Person") {
        moveTo(16f, 7f); curveTo(16f, 12.3f, 8f, 12.3f, 8f, 7f)
        curveTo(8f, 1.7f, 16f, 1.7f, 16f, 7f); close()
        moveTo(4f, 21f); curveTo(4f, 12f, 20f, 12f, 20f, 21f)
    }
    val Email = icon("Email") {
        moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(3f, 19f); close()
        moveTo(3f, 6f); lineTo(12f, 13f); lineTo(21f, 6f)
    }
    val Lock = icon("Lock") {
        moveTo(7f, 10f); lineTo(7f, 7f); curveTo(7f, 0.5f, 17f, 0.5f, 17f, 7f); lineTo(17f, 10f)
        moveTo(5f, 10f); lineTo(19f, 10f); lineTo(19f, 21f); lineTo(5f, 21f); close()
        moveTo(12f, 14f); lineTo(12f, 17f)
    }
    val Eye = icon("Eye") {
        moveTo(2f, 12f); curveTo(7f, 4f, 17f, 4f, 22f, 12f)
        curveTo(17f, 20f, 7f, 20f, 2f, 12f); close()
        moveTo(15f, 12f); curveTo(15f, 16f, 9f, 16f, 9f, 12f)
        curveTo(9f, 8f, 15f, 8f, 15f, 12f); close()
    }
    val EyeOff = icon("EyeOff") {
        moveTo(3f, 3f); lineTo(21f, 21f)
        moveTo(8f, 7f); curveTo(13f, 4f, 19f, 7f, 22f, 12f); lineTo(19f, 15f)
        moveTo(5f, 9f); lineTo(2f, 12f); curveTo(6f, 18f, 11f, 20f, 16f, 17f)
    }
    val Company = icon("Company") {
        moveTo(5f, 21f); lineTo(5f, 3f); lineTo(19f, 3f); lineTo(19f, 21f); close()
        moveTo(9f, 21f); lineTo(9f, 16f); lineTo(15f, 16f); lineTo(15f, 21f)
        moveTo(9f, 7f); lineTo(10f, 7f); moveTo(14f, 7f); lineTo(15f, 7f)
        moveTo(9f, 11f); lineTo(10f, 11f); moveTo(14f, 11f); lineTo(15f, 11f)
    }
    val Phone = icon("Phone") {
        moveTo(7f, 3f); lineTo(17f, 3f); lineTo(17f, 21f); lineTo(7f, 21f); close()
        moveTo(10f, 6f); lineTo(14f, 6f); moveTo(11f, 18f); lineTo(13f, 18f)
    }
    val Edit = icon("Edit") {
        moveTo(4f, 16f); lineTo(16f, 4f); lineTo(20f, 8f); lineTo(8f, 20f); lineTo(3f, 21f); close()
        moveTo(13f, 7f); lineTo(17f, 11f)
    }
    val Arrow = icon("Arrow") {
        moveTo(4f, 12f); lineTo(20f, 12f); moveTo(14f, 6f); lineTo(20f, 12f); lineTo(14f, 18f)
    }
    val Refresh = icon("Refresh") {
        moveTo(20f, 10f); curveTo(18f, 2f, 6f, 2f, 4f, 10f)
        moveTo(4f, 6f); lineTo(4f, 10f); lineTo(8f, 10f)
        moveTo(4f, 14f); curveTo(6f, 22f, 18f, 22f, 20f, 14f)
        moveTo(16f, 14f); lineTo(20f, 14f); lineTo(20f, 18f)
    }
    val Logout = icon("Logout") {
        moveTo(10f, 4f); lineTo(4f, 4f); lineTo(4f, 20f); lineTo(10f, 20f)
        moveTo(9f, 12f); lineTo(21f, 12f); moveTo(17f, 8f); lineTo(21f, 12f); lineTo(17f, 16f)
    }
    val Info = icon("Info") {
        moveTo(21f, 12f); curveTo(21f, 24f, 3f, 24f, 3f, 12f)
        curveTo(3f, 0f, 21f, 0f, 21f, 12f); close()
        moveTo(12f, 11f); lineTo(12f, 17f); moveTo(12f, 7f); lineTo(12f, 7.2f)
    }
    val Bell = icon("Bell") {
        moveTo(5f, 17f); lineTo(19f, 17f); lineTo(17f, 14f); lineTo(17f, 9f)
        curveTo(17f, 2f, 7f, 2f, 7f, 9f); lineTo(7f, 14f); close()
        moveTo(10f, 21f); lineTo(14f, 21f)
    }
    val Incidents = icon("Incidents") {
        moveTo(8f, 3f); lineTo(16f, 3f); lineTo(16f, 7f); lineTo(8f, 7f); close()
        moveTo(8f, 5f); lineTo(4f, 5f); lineTo(4f, 21f); lineTo(20f, 21f); lineTo(20f, 5f); lineTo(16f, 5f)
        moveTo(8f, 12f); lineTo(16f, 12f); moveTo(8f, 16f); lineTo(13f, 16f)
    }
    val Clock = icon("Clock") {
        moveTo(21f, 12f); curveTo(21f, 24f, 3f, 24f, 3f, 12f)
        curveTo(3f, 0f, 21f, 0f, 21f, 12f); close()
        moveTo(12f, 7f); lineTo(12f, 12f); lineTo(16f, 14f)
    }
    val Pin = icon("Pin") {
        moveTo(12f, 22f); curveTo(9f, 18f, 5f, 13f, 5f, 9f)
        curveTo(5f, 0f, 19f, 0f, 19f, 9f); curveTo(19f, 13f, 15f, 18f, 12f, 22f); close()
        moveTo(15f, 9f); curveTo(15f, 13f, 9f, 13f, 9f, 9f); curveTo(9f, 5f, 15f, 5f, 15f, 9f); close()
    }
    val Check = icon("Check") { moveTo(5f, 12f); lineTo(10f, 17f); lineTo(20f, 6f) }
}
