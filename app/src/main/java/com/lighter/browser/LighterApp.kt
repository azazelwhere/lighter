package com.lighter.browser

import android.app.Application
import com.lighter.browser.data.AppDatabase
import com.lighter.browser.data.Prefs
import com.lighter.browser.browser.BrowserDownloadManager
import com.lighter.browser.browser.TabManager
import com.lighter.browser.privacy.AdBlocker
import com.lighter.browser.privacy.DataCleaner
import com.lighter.browser.privacy.DnsOverHttpsResolver
import com.lighter.browser.privacy.ProxyManager
import com.lighter.browser.privacy.TorManager
import com.lighter.browser.spoofing.ProfileManager
import com.lighter.browser.spoofing.SpoofingEngine

/**
 * Manual DI container — no Hilt to keep the build lean and the APK small.
 */
class LighterApp : Application() {

    lateinit var prefs: Prefs
        private set

    lateinit var database: AppDatabase
        private set

    lateinit var profileManager: ProfileManager
        private set

    lateinit var spoofingEngine: SpoofingEngine
        private set

    lateinit var adBlocker: AdBlocker
        private set

    lateinit var dnsResolver: DnsOverHttpsResolver
        private set

    lateinit var proxyManager: ProxyManager
        private set

    lateinit var torManager: TorManager
        private set

    lateinit var dataCleaner: DataCleaner
        private set

    lateinit var downloadManager: BrowserDownloadManager
        private set

    lateinit var tabManager: TabManager
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        database = AppDatabase.get(this)
        profileManager = ProfileManager(this).also {
            it.setActiveById(it.loadActiveId())
        }
        spoofingEngine = SpoofingEngine(this, profileManager, prefs)
        adBlocker = AdBlocker(this).also { it.loadBundledFallback() }
        dnsResolver = DnsOverHttpsResolver()
        proxyManager = ProxyManager(this, prefs)
        torManager = TorManager(this)
        dataCleaner = DataCleaner(this, database)
        downloadManager = BrowserDownloadManager(this, spoofingEngine)
        tabManager = TabManager(
            context = this,
            spoofingEngine = spoofingEngine,
            adBlocker = adBlocker,
            proxyManager = proxyManager,
            torManager = torManager,
            prefs = prefs,
            database = database,
            downloadManager = downloadManager
        )
    }

    companion object {
        fun get(app: Application): LighterApp = app as LighterApp
    }
}
