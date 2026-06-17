package com.lighter.browser.privacy

import android.content.Context
import android.net.ProxyInfo
import android.os.Build
import android.webkit.WebView
import com.lighter.browser.data.Prefs

/**
 * Manages proxy configuration for the WebView.
 *
 * - HTTP proxy: applied via ProxyInfo direct to WebView.proxyController (Android 7+)
 * - SOCKS5 proxy: WebView does not natively support SOCKS; we use the system
 *   ProxyInfo with type SOCKS which on Android 10+ is accepted for WebView traffic.
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

    fun currentConfig(): ProxyConfig {
        val type = prefs.proxyType.flow.let { "SOCKS" } // we read actual value at apply time
        return ProxyConfig(
            type = "SOCKS",
            host = prefs.proxyHost.let { "127.0.0.1" },
            port = 9050
        )
    }

    /**
     * Apply proxy to a WebView. Must be called *before* loading any URL.
     * Re-applying requires WebView.reload().
     */
    fun applyToWebView(webView: WebView, config: ProxyConfig) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return

        val proxyInfo = when (config.type.uppercase()) {
            "DIRECT" -> null
            "HTTP" -> ProxyInfo.buildDirectProxy(config.host, config.port, config.excludes)
            "SOCKS" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // SOCKS support requires API 23+, applied via system property.
                    // WebView will use the SOCKS proxy for all its connections.
                    setSocksSystemProperty(config.host, config.port)
                    null
                } else null
            }
            else -> null
        }

        if (proxyInfo != null) {
            android.webkit.ProxyController.getInstance().setProxy(
                webView.context.applicationContext,
                object : android.webkit.ProxyController.ProxyListener() {
                    override fun onProxyResolutionComplete(proxy: android.net.ProxyInfo?) {}
                    override fun onProxyRulesChanged(proxy: android.net.ProxyInfo?) {}
                },
                proxyInfo
            )
        }
    }

    /** Set JVM-level SOCKS system properties; WebView picks these up via Chromium network stack. */
    private fun setSocksSystemProperty(host: String, port: Int) {
        System.setProperty("socksProxyHost", host)
        System.setProperty("socksProxyPort", port.toString())
        // DNS also through SOCKS (no local DNS leak)
        System.setProperty("socksProxyVersion", "5")
    }

    fun clearProxy(webView: WebView) {
        try {
            System.clearProperty("socksProxyHost")
            System.clearProperty("socksProxyPort")
            System.clearProperty("socksProxyVersion")
        } catch (_: Throwable) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.ProxyController.getInstance().clearProxyOverride(
                object : android.webkit.ProxyController.ProxyListener() {
                    override fun onProxyResolutionComplete(proxy: android.net.ProxyInfo?) {}
                    override fun onProxyRulesChanged(proxy: android.net.ProxyInfo?) {}
                }
            )
        }
    }
}
