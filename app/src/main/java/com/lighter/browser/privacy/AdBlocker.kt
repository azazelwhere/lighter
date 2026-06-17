package com.lighter.browser.privacy

import android.content.Context
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Lightweight ad & tracker blocker.
 *
 * Uses a local list of blocked hosts (EasyList / EasyPrivacy / StevenBlack hosts format)
 * cached under /files/adblock/hosts.txt.
 *
 * Lookup is O(1) via ConcurrentHashMap key set.
 *
 * Used by WebViewClient.shouldInterceptRequest to short-circuit blocked requests.
 */
class AdBlocker(private val context: Context) {

    private val blockedHosts = ConcurrentHashMap.newKeySet<String>()
    private val blocklistFile: File by lazy {
        File(context.filesDir, "adblock").apply { mkdirs() }
            .let { File(it, "hosts.txt") }
    }

    var enabled: Boolean = true

    fun isBlocked(url: String): Boolean {
        if (!enabled) return false
        val host = extractHost(url) ?: return false
        // Exact match
        if (blockedHosts.contains(host)) return true
        // Suffix match for subdomains
        val parts = host.split('.')
        for (i in 1 until parts.size - 1) {
            val sub = parts.drop(i).joinToString(".")
            if (blockedHosts.contains(sub)) return true
        }
        return false
    }

    /**
     * Load a hosts-format file: lines like `0.0.0.0 example.com` or `127.0.0.1 example.com`
     * or simply `example.com`. Empty / comment lines (#) are skipped.
     */
    fun loadFromText(text: String) {
        blockedHosts.clear()
        text.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
            // Format: "IP host" or just "host"
            val tokens = trimmed.split(Regex("\\s+"))
            val host = if (tokens.size >= 2) tokens[1] else tokens[0]
            // Strip www. prefix for normalization
            val normalized = host.removePrefix("www.").lowercase()
            if (normalized.isNotEmpty() && normalized.contains('.')) {
                blockedHosts.add(normalized)
            }
        }
        blocklistFile.writeText(text)
    }

    fun loadCached(): Boolean {
        if (!blocklistFile.exists()) return false
        loadFromText(blocklistFile.readText())
        return true
    }

    fun blockedCount(): Int = blockedHosts.size

    /**
     * Bundled starter blocklist. Used on first launch (offline) before user fetches
     * the full EasyList + StevenBlack list from the Settings screen.
     */
    fun loadBundledFallback() {
        val bundled = """
            # === Starter ad/tracker hosts (subset of StevenBlack) ===
            0.0.0.0 doubleclick.net
            0.0.0.0 googleadservices.com
            0.0.0.0 googlesyndication.com
            0.0.0.0 googletagmanager.com
            0.0.0.0 googletagservices.com
            0.0.0.0 analytics.google.com
            0.0.0.0 www.google-analytics.com
            0.0.0.0 ssl.google-analytics.com
            0.0.0.0 facebook.com
            0.0.0.0 www.facebook.com
            0.0.0.0 connect.facebook.net
            0.0.0.0 graph.facebook.com
            0.0.0.0 pixel.facebook.com
            0.0.0.0 ads.facebook.com
            0.0.0.0 amazon-adsystem.com
            0.0.0.0 aax.amazon-adsystem.com
            0.0.0.0 criteo.com
            0.0.0.0 cas.criteo.com
            0.0.0.0 adserver.yahoo.com
            0.0.0.0 ads.yahoo.com
            0.0.0.0 analytics.yahoo.com
            0.0.0.0 scorecardresearch.com
            0.0.0.0 noscript.net
            0.0.0.0 quantserve.com
            0.0.0.0 pixel.quantserve.com
            0.0.0.0 adnxs.com
            0.0.0.0 ib.adnxs.com
            0.0.0.0 moatads.com
            0.0.0.0 ads.moatads.com
            0.0.0.0 js.moatads.com
            0.0.0.0 adservice.google.com
            0.0.0.0 adservice.google.no
            0.0.0.0 adservice.google.dk
            0.0.0.0 pagead2.googlesyndication.com
            0.0.0.0 adservice.google.co.id
            0.0.0.0 adclick.g.doubleclick.net
            0.0.0.0 stats.g.doubleclick.net
            0.0.0.0 fls.doubleclick.net
            0.0.0.0 ad.doubleclick.net
            0.0.0.0 smartadserver.com
            0.0.0.0 ads.smartadserver.com
            0.0.0.0 openx.net
            0.0.0.0 ads.openx.net
            0.0.0.0 rubiconproject.com
            0.0.0.0 fastlane.rubiconproject.com
            0.0.0.0 pubmatic.com
            0.0.0.0 ads.pubmatic.com
            0.0.0.0 taboola.com
            0.0.0.0 cdn.taboola.com
            0.0.0.0 outbrain.com
            0.0.0.0 widgets.outbrain.com
            0.0.0.0 disqus.com
            0.0.0.0 disquscdn.com
            0.0.0.0 hotjar.com
            0.0.0.0 static.hotjar.com
            0.0.0.0 script.hotjar.com
            0.0.0.0 mixpanel.com
            0.0.0.0 api.mixpanel.com
            0.0.0.0 segment.com
            0.0.0.0 cdn.segment.com
            0.0.0.0 amplitude.com
            0.0.0.0 api.amplitude.com
            0.0.0.0 branch.io
            0.0.0.0 api.branch.io
            0.0.0.0 app.link
            0.0.0.0 adjust.com
            0.0.0.0 app.adjust.com
            0.0.0.0 appsflyer.com
            0.0.0.0 events.appsflyer.com
            0.0.0.0 crashlytics.com
            0.0.0.0 mobile.crashlytics.com
            0.0.0.0 sentry.io
            0.0.0.0 browser.sentry-cdn.com
            0.0.0.0 newrelic.com
            0.0.0.0 bam.nr-data.net
            0.0.0.0 js-agent.newrelic.com
            0.0.0.0 yandex.ru
            0.0.0.0 mc.yandex.ru
            0.0.0.0 mc.yandex.com
            0.0.0.0 metric.yandex.ru
            0.0.0.0 baidu.com
            0.0.0.0 hm.baidu.com
            0.0.0.0 pos.baidu.com
        """.trimIndent()
        loadFromText(bundled)
    }

    private fun extractHost(url: String): String? {
        return try {
            val noProto = url.substringAfter("://")
            val hostPart = noProto.substringBefore('/')
            val host = hostPart.substringBefore(':').lowercase()
            if (host.isBlank()) null else host.removePrefix("www.")
        } catch (t: Throwable) { null }
    }
}
