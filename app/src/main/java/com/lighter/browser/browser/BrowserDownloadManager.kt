package com.lighter.browser.browser

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import androidx.core.net.toUri
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.lighter.browser.spoofing.SpoofingEngine
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Handles download requests originating from WebView or user action.
 *
 * Two paths:
 *  - System DownloadManager: for direct URLs (no auth, no special headers)
 *  - Custom OkHttp downloader (via WorkManager): for headers like spoofed UA,
 *    Referer, cookies — system DownloadManager does not let us set arbitrary headers.
 */
class BrowserDownloadManager(
    private val context: Context,
    private val spoofingEngine: SpoofingEngine
) {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    fun enqueue(
        url: String,
        contentDisposition: String? = null,
        mimeType: String? = null,
        userAgent: String = spoofingEngine.currentUserAgent(),
        referer: String? = null,
        cookies: String? = null
    ): Long {
        val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
            .takeIf { it.isNotBlank() && it != "download.bin" }
            ?: "download_${System.currentTimeMillis()}"

        // Use system DownloadManager for simple cases (no special headers needed)
        val request = DownloadManager.Request(url.toUri()).apply {
            setTitle(filename)
            setDescription("Lighter Browser download")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            allowScanningByMediaScanner()
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            addRequestHeader("User-Agent", userAgent)
            if (referer != null) addRequestHeader("Referer", referer)
            if (cookies != null) addRequestHeader("Cookie", cookies)
            setDestinationUri(Uri.fromFile(File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)))
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return try {
            dm.enqueue(request)
        } catch (t: Throwable) {
            // Fallback: queue a WorkManager job using OkHttp
            queueCustomDownload(url, filename, userAgent, referer, cookies)
            -1L
        }
    }

    private fun queueCustomDownload(
        url: String,
        filename: String,
        userAgent: String,
        referer: String?,
        cookies: String?
    ) {
        val req = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                workDataOf(
                    "url" to url,
                    "filename" to filename,
                    "ua" to userAgent,
                    "referer" to referer,
                    "cookies" to cookies
                )
            )
        }.build()
        WorkManager.getInstance(context).enqueue(req)
    }

    /** For non-WebView downloads (e.g. long-press link) — uses spoofed UA by default. */
    fun startDownload(url: String, referer: String? = null) {
        enqueue(url = url, referer = referer)
    }
}
