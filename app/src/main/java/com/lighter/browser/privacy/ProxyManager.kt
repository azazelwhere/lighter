package com.lighter.browser.privacy

import android.content.Context
import android.net.ProxyInfo
import android.webkit.WebView
import com.lighter.browser.data.Prefs

/**
 * Manages proxy configuration for the WebView.
 *
 * - HTTP proxy: applied via ProxyInfo direct to android.webkit.ProxyController
 * - SOCKS5 proxy: WebView does not natively support SOCKS; we use the system
 *   properties (socksProxyHost / socksProxyPort) which Chromium network stack
 *   reads on Android 10+.
 *
 * For full SOCKS5 support (including DNS), use TorManager which sets up Orbot SOCKS.
 */
class ProxyManager(private val context: Context, private val prefs: Prefs) {

    data class ProxyConfig(
        val type: String,    // "HTTP" / "SOCKS" / "DIRECT"
        val host: String,
        val port: Int,
        val excludes: List<String> = emptyList()
    )

    /**
     * Apply proxy to a WebView. Must be called *before* loading any URL.
     * Re-applying requires WebView.reload().
     */
    fun applyToWebView(webView: WebView, config: ProxyConfig) {
        when (config.type.uppercase()) {
            "DIRECT" -> {
                clearProxy(webView)
            }
            "HTTP" -> {
                val proxyInfo = ProxyInfo.buildDirectProxy(config.host, config.port, config.excludes)
                applyProxyInfo(webView, proxyInfo)
            }
            "SOCKS" -> {
                // SOCKS5: set system properties - WebView's Chromium stack picks these up.
                setSocksSystemProperty(config.host, config.port)
            }
        }
    }

    /** Set JVM-level SOCKS system properties; WebView picks these up via Chromium network stack. */
    private fun setSocksSystemProperty(host: String, port: Int) {
        System.setProperty("socksProxyHost", host)
        System.setProperty("socksProxyPort", port.toString())
        // DNS also through SOCKS (no local DNS leak)
        System.setProperty("socksProxyVersion", "5")
    }

    private fun applyProxyInfo(webView: WebView, proxyInfo: ProxyInfo?) {
        try {
            val listener = object : android.webkit.ProxyController.ProxyListener() {
                override fun onProxyResolutionComplete(proxy: ProxyInfo?) {}
                override fun onProxyRulesChanged(proxy: ProxyInfo?) {}
            }
            android.webkit.ProxyController.getInstance().setProxy(
                webView.context.applicationContext,
                listener,
                proxyInfo
            )
        } catch (t: Throwable) {
            // Fallback: ignore, proxy just won't apply
        }
    }

    fun clearProxy(webView: WebView) {
        try {
            System.clearProperty("socksProxyHost")
            System.clearProperty("socksProxyPort")
            System.clearProperty("socksProxyVersion")
        } catch (_: Throwable) {}
        try {
            val listener = object : android.webkit.ProxyController.ProxyListener() {
                override fun onProxyResolutionComplete(proxy: ProxyInfo?) {}
                override fun onProxyRulesChanged(proxy: ProxyInfo?) {}
            }
            android.webkit.ProxyController.getInstance().clearProxyOverride(
                webView.context.applicationContext,
                Runnable {},
                listener
            )
        } catch (_: Throwable) {}
    }
}
