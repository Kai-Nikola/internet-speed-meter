package com.example.data.traffic

data class LiveSpeedSnapshot(
    val downloadBps: Long = 0L,
    val uploadBps: Long = 0L,
    val peakDownloadToday: Long = 0L,
    val peakUploadToday: Long = 0L,
    val todayWifiBytes: Long = 0L,
    val todayMobileBytes: Long = 0L,
    val networkInfo: NetworkInfoState = NetworkInfoState(),
    val isServiceActive: Boolean = true,
    val isScreenOn: Boolean = true,
    val historySamples: List<SpeedPoint> = emptyList()
) {
    val totalTodayBytes: Long get() = todayWifiBytes + todayMobileBytes
}

data class SpeedPoint(
    val timestamp: Long,
    val downBps: Long,
    val upBps: Long
)
