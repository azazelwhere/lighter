package com.lighter.browser.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Permissions {
    const val NOTIFICATION = 1001
    const val STORAGE = 1002

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION
            )
        }
    }
}

object Urls {
    fun isValidUrl(s: String): Boolean {
        val t = s.trim()
        if (t.isEmpty()) return false
        return t.startsWith("http://") || t.startsWith("https://") || (t.contains('.') && !t.contains(' '))
    }

    fun toUrlOrSearch(input: String, searchBaseUrl: String = "https://duckduckgo.com/?q="): String {
        val t = input.trim()
        if (t.startsWith("http://") || t.startsWith("https://") || t.startsWith("about:")) return t
        if (t.contains('.') && !t.contains(' ') && !t.contains('\n')) return "https://$t"
        return searchBaseUrl + java.net.URLEncoder.encode(t, "UTF-8")
    }

    fun host(url: String): String? = try {
        val noProto = url.substringAfter("://")
        noProto.substringBefore('/').substringBefore(':')
    } catch (t: Throwable) { null }
}
