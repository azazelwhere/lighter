package com.lighter.browser.privacy

import android.content.Context
import android.net.ProxyInfo
import android.webkit.WebView
import com.lighter.browser.data.Prefs

class ProxyManager(private val context: Context, private val prefs: Prefs) {

    data class ProxyConfig(
        val type: String,
        val host: String,
        val port: Int,
        val excludes: List<String> = emptyList()
    )

    fun applyToWebView(webView: WebView, config: ProxyConfig) {
        when (config.type.uppercase()) {
            "DIRECT" -> clearProxy(webView)
            "HTTP" -> {
                val proxyInfo = ProxyInfo.buildDirectProxy(config.host, config.port, config.excludes)
                applyProxyInfo(webView, proxyInfo)
            }
            "SOCKS" -> {
                setSocksSystemProperty(config.host, config.port)
            }
        }
    }

    private fun setSocksSystemProperty(host: String, port: Int) {
        System.setProperty("socksProxyHost", host)
        System.setProperty("socksProxyPort", port.toString())
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
