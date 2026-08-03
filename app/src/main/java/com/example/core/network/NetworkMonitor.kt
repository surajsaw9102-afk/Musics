package com.example.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConnectionType(val label: String) {
    WIFI("Wi-Fi (High Speed)"),
    CELLULAR_5G("Cellular 5G/4G"),
    OFFLINE("Offline (No Connection)")
}

data class NetworkStatus(
    val isOnline: Boolean = true,
    val isWifi: Boolean = true,
    val connectionType: ConnectionType = ConnectionType.WIFI,
    val isForceOfflineMode: Boolean = false
)

object NetworkMonitor {

    private val _status = MutableStateFlow(NetworkStatus())
    val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    fun setForceOffline(enabled: Boolean) {
        val current = _status.value
        if (enabled) {
            _status.value = current.copy(
                isForceOfflineMode = true,
                isOnline = false,
                connectionType = ConnectionType.OFFLINE
            )
        } else {
            _status.value = current.copy(
                isForceOfflineMode = false,
                isOnline = true,
                connectionType = if (current.isWifi) ConnectionType.WIFI else ConnectionType.CELLULAR_5G
            )
        }
    }

    fun setConnectionType(type: ConnectionType) {
        val forceOffline = _status.value.isForceOfflineMode
        if (forceOffline) return

        when (type) {
            ConnectionType.OFFLINE -> {
                _status.value = _status.value.copy(
                    isOnline = false,
                    isWifi = false,
                    connectionType = ConnectionType.OFFLINE
                )
            }
            ConnectionType.WIFI -> {
                _status.value = _status.value.copy(
                    isOnline = true,
                    isWifi = true,
                    connectionType = ConnectionType.WIFI
                )
            }
            ConnectionType.CELLULAR_5G -> {
                _status.value = _status.value.copy(
                    isOnline = true,
                    isWifi = false,
                    connectionType = ConnectionType.CELLULAR_5G
                )
            }
        }
    }
}
