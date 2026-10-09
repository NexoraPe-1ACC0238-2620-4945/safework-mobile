package com.nexorape.safework

import android.app.Application
import com.nexorape.safework.core.configuration.AppGraph

class SafeWorkApplication : Application() {
    val graph by lazy { AppGraph(this) }
}
