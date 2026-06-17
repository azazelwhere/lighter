package com.lighter.browser.browser

import android.content.Context
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.collection.LruCache
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.lighter.browser.data.AppDatabase
import com.lighter.browser.data.Prefs
import com.lighter.browser.data.HistoryEntity
import com.lighter.browser.privacy.AdBlocker
import com.lighter.browser.privacy.ProxyManager
import com.lighter.browser.privacy.TorManager
import com.lighter.browser.spoofing.SpoofingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Owns all open tabs (WebViews). Each tab has:
 *   - a WebView instance (held in memory)
 *   - a Compose-observable TabState
 *
 * Tabs are created via newTab() / newIncognitoTab(), switched via switchTo(),
 * closed via closeTab(). The currently active tab's WebView is the only one
 * attached to the screen.
 */
class TabManager(
    private val context: Context,
    private val spoofingEngine: SpoofingEngine,
    private val adBlocker: AdBlocker,
    private val proxyManager: ProxyManager,
    private val torManager: TorManager,
    private val prefs: Prefs,
    private val database: AppDatabase,
    private val downloadManager: BrowserDownloadManager
) {
    data class Tab(val state: TabState, val webView: WebView)

    private val _tabs = mutableStateListOf<Tab>()
    val tabs: List<Tab> get() = _tabs

    private val _activeIndex = mutableStateOf(-1)
    val activeIndex: Int get() = _activeIndex.value

    private val _activeState = mutableStateOf<TabState?>(null)
    val activeState: TabState? get() = _activeState.value

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val faviconCache = LruCache<String, Bitmap>(64)

    fun newTab(url: String? = null, incognito: Boolean = false): TabState {
        val id = UUID.randomUUID().toString()
        val startUrl = url ?: "about:blank"
        val state = TabState(id = id, url = startUrl, incognito = incognito)
        val webView = createWebView(state, incognito)
        _tabs.add(Tab(state, webView))
        _activeIndex.value = _tabs.size - 1
        _activeState.value = state
        if (url != null) webView.loadUrl(url)
        return state
    }

    fun newIncognitoTab(url: String? = null): TabState = newTab(url, incognito = true)

    fun switchTo(index: Int) {
        if (index in _tabs.indices) {
            _activeIndex.value = index
            _activeState.value = _tabs[index].state
        }
    }

    fun closeTab(index: Int) {
        if (index !in _tabs.indices) return
        val tab = _tabs[index]
        try {
            tab.webView.apply {
                stopLoading()
                loadUrl("about:blank")
                clearCache(true)
                clearHistory()
                removeAllViews()
                destroy()
            }
        } catch (_: Throwable) {}
        _tabs.removeAt(index)
        if (_tabs.isEmpty()) {
            _activeIndex.value = -1
            _activeState.value = null
        } else {
            _activeIndex.value = (_activeIndex.value - 1).coerceAtLeast(0)
            _activeState.value = _tabs[_activeIndex.value].state
        }
    }

    fun closeAll() {
        for (i in _tabs.indices.reversed()) closeTab(i)
    }

    fun activeWebView(): WebView? = _tabs.getOrNull(_activeIndex.value)?.webView

    fun loadUrl(url: String) {
        val tab = _tabs.getOrNull(_activeIndex.value) ?: return
        val resolved = resolveUrl(url)
        tab.webView.loadUrl(resolved)
        updateState(tab) { it.copy(url = resolved, loading = true, progress = 0) }
    }

    fun reload() {
        activeWebView()?.reload()
    }

    fun goBack() {
        val v = activeWebView()
        if (v?.canGoBack() == true) v.goBack()
    }

    fun goForward() {
        val v = activeWebView()
        if (v?.canGoForward() == true) v.goForward()
    }

    fun stopLoading() {
        activeWebView()?.stopLoading()
    }

    fun canGoBack(): Boolean = activeWebView()?.canGoBack() == true
    fun canGoForward(): Boolean = activeWebView()?.canGoForward() == true

    fun evaluateJs(script: String, callback: ((String?) -> Unit)? = null) {
        activeWebView()?.evaluateJavascript(script, callback)
    }

    fun takeScreenshot(callback: (Bitmap?) -> Unit) {
        val v = activeWebView() ?: return callback(null)
        try {
            v.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(v.width, android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
            )
            val bmp = Bitmap.createBitmap(v.width, v.measuredHeight.coerceAtLeast(v.height), Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bmp)
            v.draw(canvas)
            callback(bmp)
        } catch (t: Throwable) {
            callback(null)
        }
    }

    private fun createWebView(state: TabState, incognito: Boolean): WebView {
        val webView = WebView(context)
        // Apply proxy if Tor enabled
        scope.launch {
            if (prefs.torEnabled.flow.first() && torManager.isUsable()) {
                proxyManager.applyToWebView(
                    webView,
                    ProxyManager.ProxyConfig("SOCKS", torManager.socksHost, torManager.socksPort)
                )
            }
        }

        // Settings
        webView.settings.apply {
            scope.launch {
                javaScriptEnabled = prefs.javascriptEnabled.flow.first()
                domStorageEnabled = prefs.domStorageEnabled.flow.first()
                allowFileAccess = false
                allowContentAccess = false
                blockNetworkLoads = false
                cacheMode = WebSettings.LOAD_DEFAULT
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                mediaPlaybackRequiresUserGesture = true
                loadsImagesAutomatically = prefs.imagesEnabled.flow.first()
                // Block mixed content (active+passive)
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                // Disable geolocation by default
                setGeolocationEnabled(false)
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                saveFormData = false
                savePassword = false
            }
            // Spoof UA + WebSettings
            spoofingEngine.applyInitial(webView)

            if (incognito) {
                // Incognito: disable DOM storage, cache, cookies for this WebView
                domStorageEnabled = false
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
        }

        // Cookie acceptance policy
        scope.launch {
            val accept = prefs.cookiesEnabled.flow.first()
            CookieManager.getInstance().setAcceptCookie(accept && !incognito)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false)
        }

        // Clients
        webView.webViewClient = LighterWebViewClient(
            spoofingEngine = spoofingEngine,
            adBlocker = adBlocker,
            onPageStarted = { url ->
                val tab = findTabByWebView(webView) ?: return@LighterWebViewClient
                updateState(tab) { it.copy(loading = true, url = url, sslError = false) }
            },
            onPageFinished = { url ->
                val tab = findTabByWebView(webView) ?: return@LighterWebViewClient
                updateState(tab) { it.copy(loading = false, progress = 100) }
                // Save history (only non-incognito)
                if (!tab.state.incognito) {
                    scope.launch {
                        if (prefs.saveHistory.flow.first()) {
                            val title = tab.state.title.ifBlank { url }
                            database.historyDao().insert(HistoryEntity(title = title, url = url))
                        }
                    }
                }
            },
            onProgress = { p ->
                val tab = findTabByWebView(webView) ?: return@LighterWebViewClient
                updateState(tab) { it.copy(progress = p, loading = p < 100) }
            },
            onTitleReceived = { title ->
                val tab = findTabByWebView(webView) ?: return@LighterWebViewClient
                updateState(tab) { it.copy(title = title) }
            },
            onUrlChanged = { newUrl ->
                val tab = findTabByWebView(webView) ?: return@LighterWebViewClient
                updateState(tab) { it.copy(url = newUrl) }
            },
            onSslError = { _ ->
                val tab = findTabByWebView(webView) ?: return@LighterWebViewClient
                updateState(tab) { it.copy(sslError = true) }
            }
        )

        webView.webChromeClient = LighterWebChromeClient(
            onProgress = { p ->
                val tab = findTabByWebView(webView) ?: return@LighterWebChromeClient
                updateState(tab) { it.copy(progress = p, loading = p < 100) }
            },
            onTitleReceived = { title ->
                val tab = findTabByWebView(webView) ?: return@LighterWebChromeClient
                updateState(tab) { it.copy(title = title) }
            },
            onFavicon = { bmp ->
                val tab = findTabByWebView(webView) ?: return@LighterWebChromeClient
                if (bmp != null) faviconCache.put(tab.state.url, bmp)
                updateState(tab) { it.copy(favicon = bmp) }
            }
        )

        webView.setDownloadListener(
            LighterDownloadListener { url, contentDisposition, mimetype ->
                downloadManager.enqueue(
                    url = url,
                    contentDisposition = contentDisposition,
                    mimeType = mimetype,
                    userAgent = spoofingEngine.currentUserAgent(),
                    referer = state.url
                )
            }
        )

        // Apply DNT header
        webView.settings.userAgentString = spoofingEngine.currentUserAgent()

        return webView
    }

    private fun findTabByWebView(webView: WebView): Tab? = _tabs.firstOrNull { it.webView === webView }

    private fun updateState(tab: Tab, transform: (TabState) -> TabState) {
        val idx = _tabs.indexOf(tab)
        if (idx < 0) return
        val newState = transform(_tabs[idx].state)
        _tabs[idx] = _tabs[idx].copy(state = newState)
        if (idx == _activeIndex.value) {
            _activeState.value = newState
        }
    }

    private fun resolveUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "about:blank"
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("about:")) return trimmed
        if (trimmed.startsWith("javascript:") || trimmed.startsWith("data:")) return trimmed
        if (trimmed.contains(' ') || !trimmed.contains('.')) {
            // Treat as search query
            return "https://duckduckgo.com/?q=" + java.net.URLEncoder.encode(trimmed, "UTF-8")
        }
        return "https://$trimmed"
    }
}
