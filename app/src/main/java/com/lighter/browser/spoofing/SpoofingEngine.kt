package com.lighter.browser.spoofing

import android.content.Context
import android.webkit.WebView
import com.lighter.browser.data.Prefs

/**
 * Orchestrates spoofing: takes the active profile from ProfileManager,
 * builds the JS override script via JsInjector, and applies both
 * WebSettings + JS injection to a given WebView.
 */
class SpoofingEngine(
    private val context: Context,
    private val profileManager: ProfileManager,
    private val prefs: Prefs
) {

    /** Apply spoofing settings + inject the override script on a freshly-created WebView. */
    fun applyInitial(webView: WebView) {
        val profile = profileManager.resolveForRequest()
        profileManager.applyToWebSettings(webView.settings, profile)
    }

    /** Called on every page navigation (onPageStarted). Re-injects overrides. */
    fun injectOnPageStarted(webView: WebView) {
        val profile = profileManager.resolveForRequest()
        val script = JsInjector.buildScript(profile)
        // Evaluate before page scripts run
        webView.evaluateJavascript(script, null)
    }

    /** Returns the resolved UA for the active profile (used for okhttp/download manager). */
    fun currentUserAgent(): String =
        profileManager.resolveForRequest().userAgent

    /** Returns the resolved profile (used for logging / UI display). */
    fun currentProfile(): SpoofProfile =
        profileManager.resolveForRequest()
}
