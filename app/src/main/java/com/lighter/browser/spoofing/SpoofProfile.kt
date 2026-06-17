package com.lighter.browser.spoofing

import kotlinx.serialization.Serializable

/**
 * Represents a complete device identity profile used to spoof the browser fingerprint.
 * All values are injected into the WebView via JS overrides + WebView settings.
 */
@Serializable
data class SpoofProfile(
    val id: String,
    val name: String,
    val userAgent: String,
    val platform: String,                // navigator.platform
    val vendor: String,                  // navigator.vendor
    val language: String,                // navigator.language
    val languages: List<String>,         // navigator.languages
    val timezone: String,                // IANA tz, e.g. "America/New_York"
    val timezoneOffsetMinutes: Int,      // minutes from UTC (sign flipped from getTimezoneOffset)
    val screenWidth: Int,
    val screenHeight: Int,
    val devicePixelRatio: Float,
    val colorDepth: Int,                 // 24 / 30 / 48
    val hardwareConcurrency: Int,
    val deviceMemory: Int,               // navigator.deviceMemory (GB)
    val webglVendor: String,
    val webglRenderer: String,
    val canvasNoise: Float,              // 0..1 amount of noise added to canvas
    val batteryLevel: Float,             // 0..1
    val batteryCharging: Boolean,
    val blockBattery: Boolean,
    val blockSensors: Boolean,
    val fontsList: List<String>,
    val pluginsList: List<String>,
    val extraJs: String = ""             // user-supplied JS to run before page
) {
    companion object {
        const val DEFAULT_ANDROID = "default_android"
        const val DEFAULT_WINDOWS = "default_windows"
        const val DEFAULT_MAC = "default_mac"
        const val DEFAULT_IPHONE = "default_iphone"
        const val DEFAULT_LINUX = "default_linux"
    }
}
