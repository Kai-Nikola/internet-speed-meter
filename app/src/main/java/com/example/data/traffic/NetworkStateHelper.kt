package com.example.data.traffic

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build

enum class NetworkType(val label: String) {
    WIFI("Wi-Fi"),
    MOBILE("Mobile Data"),
    ETHERNET("Ethernet"),
    OTHER("Connected"),
    DISCONNECTED("Offline")
}

data class NetworkInfoState(
    val type: NetworkType = NetworkType.DISCONNECTED,
    val detail: String = "Not Connected",
    val isConnected: Boolean = false
)

object NetworkStateHelper {
    fun getNetworkInfo(context: Context): NetworkInfoState {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return NetworkInfoState(NetworkType.DISCONNECTED, "No Connectivity Service", false)

        val activeNetwork = connectivityManager.activeNetwork
            ?: return NetworkInfoState(NetworkType.DISCONNECTED, "Disconnected", false)

        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkInfoState(NetworkType.DISCONNECTED, "No Capabilities", false)

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                var wifiDetail = "Wi-Fi Connected"
                try {
                    val wifiManager =
                        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val info = wifiManager?.connectionInfo
                    if (info != null && info.ssid != null && info.ssid != "<unknown ssid>") {
                        val cleanSsid = info.ssid.replace("\"", "")
                        val linkSpeed = info.linkSpeed
                        wifiDetail = if (linkSpeed > 0) "$cleanSsid (${linkSpeed} Mbps)" else cleanSsid
                    }
                } catch (_: Exception) {
                    // Fallback to default label
                }
                NetworkInfoState(NetworkType.WIFI, wifiDetail, true)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                NetworkInfoState(NetworkType.MOBILE, "Cellular (4G/5G)", true)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                NetworkInfoState(NetworkType.ETHERNET, "Ethernet (LAN)", true)
            }
            else -> {
                NetworkInfoState(NetworkType.OTHER, "Connected", true)
            }
        }
    }
}
