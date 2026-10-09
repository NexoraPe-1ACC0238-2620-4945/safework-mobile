package com.nexorape.safework.incidentmanagement.infrastructure.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import com.nexorape.safework.incidentmanagement.domain.model.IncidentLocation
import com.nexorape.safework.incidentmanagement.domain.repositories.LocationProvider

/** Called only after runtime permission. One foreground fix; never background tracking. */
class DeviceLocation(context: Context) : LocationProvider {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    @SuppressLint("MissingPermission") // Caller gates ACCESS_COARSE/FINE_LOCATION; SecurityException is also handled.
    override suspend fun capture(): IncidentLocation? = withContext(Dispatchers.Main.immediate) {
        withTimeoutOrNull(20_000) {
            suspendCancellableCoroutine { continuation ->
                val enabled = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                    .filter { manager.isProviderEnabled(it) }
                if (enabled.isEmpty()) { continuation.resume(null); return@suspendCancellableCoroutine }
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(IncidentLocation.of(String.format(Locale.ROOT, "%.6f, %.6f", location.latitude, location.longitude)))
                    }
                    override fun onProviderEnabled(provider: String) { }
                    override fun onProviderDisabled(provider: String) { }
                    @Deprecated("Required on API26") override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
                }
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
                try {
                    enabled.forEach { manager.requestLocationUpdates(it, 0L, 0f, listener, Looper.getMainLooper()) }
                } catch (_: SecurityException) {
                    manager.removeUpdates(listener)
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
    }
}
