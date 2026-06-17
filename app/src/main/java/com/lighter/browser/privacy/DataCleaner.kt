package com.lighter.browser.privacy

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import com.lighter.browser.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cleans all browsing-related data:
 *  - WebView cache + history
 *  - CookieManager cookies
 *  - WebStorage (localStorage / IndexedDB / WebSQL)
 *  - WebViewDatabase (form data, http auth)
 *  - App database (history)
 *
 * Used on app exit (if clear-on-exit enabled) and from Settings.
 */
class DataCleaner(
    private val context: Context,
    private val database: AppDatabase
) {

    suspend fun clearAll(includeBookmarks: Boolean = false) = withContext(Dispatchers.IO) {
        // WebView
        try {
            android.webkit.WebViewDatabase.getInstance(context)?.apply {
                clearHttpAuthUsernamePassword()
                clearUsernamePassword()
                clearFormData()
            }
        } catch (_: Throwable) {}

        try { WebStorage.getInstance().deleteAllData() } catch (_: Throwable) {}
        try { CookieManager.getInstance().removeAllCookies(null) } catch (_: Throwable) {}
        try { CookieManager.getInstance().flush() } catch (_: Throwable) {}
        try { WebView(context).clearCache(true) } catch (_: Throwable) {}
        try { WebView.clearClientCertPreferences(null) } catch (_: Throwable) {}

        // App database
        database.historyDao().clear()
        if (includeBookmarks) {
            database.bookmarkDao().clear()
        }

        // Cache dir
        try { context.cacheDir.deleteRecursively() } catch (_: Throwable) {}
    }

    /** Clear only the active tab's data (for incognito close). */
    fun clearTabLocal(webView: WebView) {
        try {
            webView.clearCache(true)
            webView.clearHistory()
        } catch (_: Throwable) {}
    }
}
