package com.lighter.browser.spoofing

import java.util.TimeZone

/**
 * Built-in device profiles that mimic popular real devices.
 * Each profile is a coherent bundle: UA + platform + screen + GPU + tz + locale.
 */
object ProfilePresets {

    val all: List<SpoofProfile> = listOf(
        SpoofProfile(
            id = SpoofProfile.DEFAULT_ANDROID,
            name = "Android 13 / Pixel 7",
            userAgent = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            platform = "Linux armv8l",
            vendor = "Google Inc.",
            language = "en-US",
            languages = listOf("en-US", "en"),
            timezone = "America/Los_Angeles",
            timezoneOffsetMinutes = TimeZone.getTimeZone("America/Los_Angeles").getOffset(System.currentTimeMillis()) / -60000,
            screenWidth = 1080,
            screenHeight = 2400,
            devicePixelRatio = 2.625f,
            colorDepth = 24,
            hardwareConcurrency = 8,
            deviceMemory = 8,
            webglVendor = "Qualcomm",
            webglRenderer = "Adreno (TM) 730",
            canvasNoise = 0.0001f,
            batteryLevel = 0.78f,
            batteryCharging = false,
            blockBattery = false,
            blockSensors = false,
            fontsList = listOf("Roboto", "Noto Sans", "Noto Serif", "Droid Sans Mono"),
            pluginsList = listOf("PDF Viewer", "Chrome PDF Viewer", "Chromium PDF Viewer")
        ),
        SpoofProfile(
            id = SpoofProfile.DEFAULT_WINDOWS,
            name = "Windows 11 / Chrome 120",
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            platform = "Win32",
            vendor = "Google Inc.",
            language = "en-US",
            languages = listOf("en-US", "en"),
            timezone = "America/New_York",
            timezoneOffsetMinutes = TimeZone.getTimeZone("America/New_York").getOffset(System.currentTimeMillis()) / -60000,
            screenWidth = 1920,
            screenHeight = 1080,
            devicePixelRatio = 1.0f,
            colorDepth = 24,
            hardwareConcurrency = 12,
            deviceMemory = 16,
            webglVendor = "Google Inc. (NVIDIA)",
            webglRenderer = "ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            canvasNoise = 0.0001f,
            batteryLevel = 0.85f,
            batteryCharging = true,
            blockBattery = false,
            blockSensors = true,
            fontsList = listOf("Arial", "Calibri", "Cambria", "Candara", "Consolas", "Constantia", "Corbel", "Segoe UI", "Tahoma", "Times New Roman", "Verdana"),
            pluginsList = listOf("PDF Viewer", "Chrome PDF Viewer", "Chromium PDF Viewer", "Microsoft Edge PDF Viewer", "WebKit built-in PDF")
        ),
        SpoofProfile(
            id = SpoofProfile.DEFAULT_MAC,
            name = "macOS 14 / Safari 17",
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",
            platform = "MacIntel",
            vendor = "Apple Computer, Inc.",
            language = "en-US",
            languages = listOf("en-US", "en"),
            timezone = "America/Los_Angeles",
            timezoneOffsetMinutes = TimeZone.getTimeZone("America/Los_Angeles").getOffset(System.currentTimeMillis()) / -60000,
            screenWidth = 2560,
            screenHeight = 1440,
            devicePixelRatio = 2.0f,
            colorDepth = 30,
            hardwareConcurrency = 10,
            deviceMemory = 16,
            webglVendor = "Apple Inc.",
            webglRenderer = "Apple M1 Pro",
            canvasNoise = 0.0001f,
            batteryLevel = 0.62f,
            batteryCharging = false,
            blockBattery = false,
            blockSensors = true,
            fontsList = listOf("Helvetica", "Helvetica Neue", "Arial", "Times", "Courier", "Georgia", "Palatino", "Verdana", "Avenir Next", "Menlo"),
            pluginsList = listOf("WebKit built-in PDF")
        ),
        SpoofProfile(
            id = SpoofProfile.DEFAULT_IPHONE,
            name = "iOS 17 / iPhone 15 Safari",
            userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1",
            platform = "iPhone",
            vendor = "Apple Computer, Inc.",
            language = "en-US",
            languages = listOf("en-US", "en"),
            timezone = "America/New_York",
            timezoneOffsetMinutes = TimeZone.getTimeZone("America/New_York").getOffset(System.currentTimeMillis()) / -60000,
            screenWidth = 1179,
            screenHeight = 2556,
            devicePixelRatio = 3.0f,
            colorDepth = 24,
            hardwareConcurrency = 6,
            deviceMemory = 6,
            webglVendor = "Apple Inc.",
            webglRenderer = "Apple GPU",
            canvasNoise = 0.0001f,
            batteryLevel = 0.55f,
            batteryCharging = false,
            blockBattery = true,
            blockSensors = false,
            fontsList = listOf("Helvetica", "Helvetica Neue", "Arial", "Times New Roman", "Courier", "Georgia", "SF Pro", "Avenir Next"),
            pluginsList = listOf()
        ),
        SpoofProfile(
            id = SpoofProfile.DEFAULT_LINUX,
            name = "Linux / Firefox 121",
            userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:121.0) Gecko/20100101 Firefox/121.0",
            platform = "Linux x86_64",
            vendor = "",
            language = "en-US",
            languages = listOf("en-US", "en"),
            timezone = "Europe/Berlin",
            timezoneOffsetMinutes = TimeZone.getTimeZone("Europe/Berlin").getOffset(System.currentTimeMillis()) / -60000,
            screenWidth = 1920,
            screenHeight = 1080,
            devicePixelRatio = 1.0f,
            colorDepth = 24,
            hardwareConcurrency = 8,
            deviceMemory = 16,
            webglVendor = "Mesa",
            webglRenderer = "Mesa Intel(R) UHD Graphics 630 (CFL GT2)",
            canvasNoise = 0.0001f,
            batteryLevel = 0.5f,
            batteryCharging = false,
            blockBattery = true,
            blockSensors = true,
            fontsList = listOf("DejaVu Sans", "DejaVu Serif", "DejaVu Sans Mono", "Liberation Sans", "Liberation Serif", "Liberation Mono", "Ubuntu", "Ubuntu Mono"),
            pluginsList = listOf()
        ),
        // Randomized rotating profile (regenerated per request)
        SpoofProfile(
            id = "random",
            name = "Acak (rotasi per request)",
            userAgent = "RANDOM",
            platform = "RANDOM",
            vendor = "RANDOM",
            language = "en-US",
            languages = listOf("en-US", "en"),
            timezone = "RANDOM",
            timezoneOffsetMinutes = 0,
            screenWidth = 1920,
            screenHeight = 1080,
            devicePixelRatio = 1.0f,
            colorDepth = 24,
            hardwareConcurrency = 8,
            deviceMemory = 8,
            webglVendor = "RANDOM",
            webglRenderer = "RANDOM",
            canvasNoise = 0.0005f,
            batteryLevel = 0.5f,
            batteryCharging = false,
            blockBattery = false,
            blockSensors = false,
            fontsList = listOf(),
            pluginsList = listOf()
        )
    )

    fun byId(id: String): SpoofProfile =
        all.firstOrNull { it.id == id } ?: all.first()

    fun randomized(): SpoofProfile {
        val base = all.filter { it.id != "random" }.random()
        val tzPool = listOf("America/New_York", "America/Los_Angeles", "Europe/London", "Europe/Berlin", "Asia/Tokyo", "Asia/Singapore", "Australia/Sydney")
        val tz = tzPool.random()
        return base.copy(
            id = "random_instance",
            timezone = tz,
            timezoneOffsetMinutes = TimeZone.getTimeZone(tz).getOffset(System.currentTimeMillis()) / -60000,
            canvasNoise = (0.0001f..0.001f).random(),
            hardwareConcurrency = listOf(4, 6, 8, 12, 16).random(),
            deviceMemory = listOf(4, 8, 16).random(),
            batteryLevel = (0.1f..1.0f).random(),
            batteryCharging = listOf(true, false).random()
        )
    }
}
