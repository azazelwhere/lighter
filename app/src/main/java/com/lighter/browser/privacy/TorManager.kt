package com.lighter.browser.privacy

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.lighter.browser.privacy.TorManager.OrbotStatus.*

/**
 * Bridges with Orbot (the official Android Tor client).
 *
 * Lighter does NOT ship its own Tor binary - it relies on Orbot being installed.
 * Communication:
 *   1. Status: Orbot broadcasts ACTION_STATUS with extra STATUS_STARTING / STATUS_ON / STATUS_STOPPING / STATUS_OFF
 *   2. Trigger: send ACTION_START with category CATEGORY_TOR(GTK)_CONTROL to launch Orbot
 *
 * When Orbot is connected, a SOCKS5 proxy is available at 127.0.0.1:9050.
 * ProxyManager / WebView should be configured to use it.
 */
class TorManager(private val context: Context) {

    enum class OrbotStatus { UNKNOWN, NOT_INSTALLED, STARTING, ON, STOPPING, OFF, ERROR }

    var status: OrbotStatus = UNKNOWN
        private set

    val socksHost = "127.0.0.1"
    val socksPort = 9050

    fun isOrbotInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(ORBOT_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    /** Launch Orbot app and send the START intent. */
    fun startOrbot(): Boolean {
        if (!isOrbotInstalled()) {
            status = NOT_INSTALLED
            return false
        }
        return try {
            val intent = Intent(ORBOT_START_ACTION).apply {
                setPackage(ORBOT_PACKAGE)
                putExtra("org.torproject.android.intent.extra.PACKAGE_NAME", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            // Also send broadcast
            context.sendBroadcast(Intent(ORBOT_START_ACTION).apply {
                setPackage(ORBOT_PACKAGE)
                putExtra("org.torproject.android.intent.extra.PACKAGE_NAME", context.packageName)
            })
            status = STARTING
            true
        } catch (t: Throwable) {
            status = ERROR
            false
        }
    }

    fun stopOrbot() {
        try {
            val intent = Intent(ORBOT_STOP_ACTION).apply { setPackage(ORBOT_PACKAGE) }
            context.sendBroadcast(intent)
            status = STOPPING
        } catch (_: Throwable) {}
    }

    /** Open Play Store listing for Orbot. */
    fun openInstallPage() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$ORBOT_PACKAGE"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Throwable) {
            // Fallback to browser URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://guardianproject.info/apps/org.torproject.android/"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /** Apply status update from Orbot broadcast intent. */
    fun updateFromBroadcast(intent: Intent) {
        val statusExtra = intent.getStringExtra(ORBOT_EXTRA_STATUS) ?: return
        status = when (statusExtra) {
            ORBOT_STATUS_STARTING -> STARTING
            ORBOT_STATUS_ON -> ON
            ORBOT_STATUS_STOPPING -> STOPPING
            ORBOT_STATUS_OFF -> OFF
            else -> UNKNOWN
        }
    }

    /** Returns true if Tor should be considered usable for new requests. */
    fun isUsable(): Boolean = status == ON || status == STARTING

    companion object {
        const val ORBOT_PACKAGE = "org.torproject.android"
        const val ORBOT_START_ACTION = "org.torproject.android.START_TOR"
        const val ORBOT_STOP_ACTION = "org.torproject.android.STOP_TOR"
        const val ORBOT_EXTRA_STATUS = "org.torproject.android.intent.extra.STATUS"
        const val ORBOT_STATUS_STARTING = "STARTING"
        const val ORBOT_STATUS_ON = "ON"
        const val ORBOT_STATUS_STOPPING = "STOPPING"
        const val ORBOT_STATUS_OFF = "OFF"
    }
}
