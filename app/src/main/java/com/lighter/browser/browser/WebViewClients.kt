package com.lighter.browser.browser

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.*
import android.webkit.WebView.HitTestResult
import androidx.compose.runtime.mutableStateOf
import com.lighter.browser.privacy.AdBlocker
import com.lighter.browser.spoofing.SpoofingEngine

/**
 * Bundles all the WebView callback objects + the WebView itself,
 * and exposes a small Compose-friendly state object (TabState).
 *
 * The actual WebView instance is owned by TabManager and tracked per-tab.
 * This file only provides the client classes.
 */
class LighterWebViewClient(
    private val spoofingEngine: SpoofingEngine,
    private val adBlocker: AdBlocker,
    private val onPageStarted: (String) -> Unit,
    private val onPageFinished: (String) -> Unit,
    private val onProgress: (Int) -> Unit,
    private val onTitleReceived: (String) -> Unit,
    private val onUrlChanged: (String) -> Unit,
    private val onSslError: (String) -> Unit
) : WebViewClient() {

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        if (adBlocker.isBlocked(url)) {
            return WebResourceResponse("text/plain", "utf-8", java.io.ByteArrayInputStream(ByteArray(0)))
        }
        return null
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        url?.let {
            // Inject spoofing overrides ASAP, before page scripts run.
            view?.let { spoofingEngine.injectOnPageStarted(it) }
            onPageStarted(it)
            onUrlChanged(it)
        }
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        // Re-inject after page load too, in case SPA navigation reset things.
        if (view != null && url != null) {
            spoofingEngine.injectOnPageStarted(view)
            onPageFinished(url)
        }
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        // For an anonymous browser, fail closed by default but let user decide later.
        handler?.cancel()
        onSslError(error?.url ?: "")
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        // Allow http/https inside the WebView; let external intents handle mailto/tel/etc.
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false
        }
        // Don't auto-launch external intents (anti-leak); user can tap to open.
        return true
    }
}

class LighterWebChromeClient(
    private val onProgress: (Int) -> Unit,
    private val onTitleReceived: (String) -> Unit,
    private val onFavicon: (Bitmap?) -> Unit
) : WebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        onProgress(newProgress)
    }
    override fun onReceivedTitle(view: WebView?, title: String?) {
        title?.let { if (it.isNotBlank()) onTitleReceived(it) }
    }
    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        onFavicon(icon)
    }
    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        // Block alert() dialogs (often used for fingerprinting / nag screens)
        result?.cancel()
        return true
    }
    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        result?.cancel()
        return true
    }
}

class LighterDownloadListener(
    private val onDownload: (String, String?, String?) -> Unit
) : DownloadListener {
    override fun onDownloadStart(
        url: String?, userAgent: String?, contentDisposition: String?, mimetype: String?, contentLength: Long
    ) {
        url?.let { onDownload(it, contentDisposition, mimetype) }
    }
}

/** Compose-observable tab state. */
data class TabState(
    val id: String,
    val url: String,
    val title: String = "",
    val favicon: Bitmap? = null,
    val progress: Int = 0,
    val loading: Boolean = false,
    val incognito: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val sslError: Boolean = false
)

/** Hit-test result for long-press menu. */
data class HitResult(
    val type: Int = HitTestResult.UNKNOWN_TYPE,
    val extra: String? = null
)
