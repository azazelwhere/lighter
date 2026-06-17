package com.lighter.browser.privacy

import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress

/**
 * DNS-over-HTTPS resolver built on OkHttp.
 *
 * Used both for ad-blocker filter downloads and (optionally) as the system DNS
 * by routing WebView traffic through the OkHttp stack inside WebViewClient.
 */
class DnsOverHttpsResolver(
    private val bootstrapClient: OkHttpClient = OkHttpClient.Builder().build()
) {
    fun resolve(dohUrl: String): Dns {
        val url = HttpUrl.get(dohUrl)
        return DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url(url)
            .includeIPv6(true)
            .build()
    }

    /** Resolve a hostname via DoH, returns first A record or null. */
    fun lookup(dohUrl: String, host: String): InetAddress? {
        return try {
            resolve(dohUrl).lookup(host).firstOrNull()
        } catch (t: Throwable) { null }
    }
}

object DohProviders {
    val presets = mapOf(
        "Cloudflare" to "https://cloudflare-dns.com/dns-query",
        "Google" to "https://dns.google/dns-query",
        "Quad9" to "https://dns.quad9.net/dns-query",
        "AdGuard DNS" to "https://dns.adguard.com/dns-query",
        "AdGuard Family" to "https://family.adguard-dns.com/dns-query",
        "NextDNS" to "https://dns.nextdns.io",
        "Mullvad" to "https://doh.mullvad.net/dns-query"
    )
}
