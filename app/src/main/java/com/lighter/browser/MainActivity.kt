package com.lighter.browser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lighter.browser.privacy.TorManager
import com.lighter.browser.spoofing.ProfileManager
import com.lighter.browser.ui.screens.*
import com.lighter.browser.ui.theme.LighterTheme
import com.lighter.browser.util.Permissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var app: LighterApp
    private lateinit var torManager: TorManager
    private lateinit var profileManager: ProfileManager

    private val orbotReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { torManager.updateFromBroadcast(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = LighterApp.get(application)
        torManager = app.torManager
        profileManager = app.profileManager

        Permissions.requestNotificationPermission(this)

        if (app.tabManager.tabs.isEmpty()) {
            app.tabManager.newTab("https://duckduckgo.com")
        }

        intent?.data?.let { uri ->
            app.tabManager.loadUrl(uri.toString())
        }

        setContent {
            val navController = rememberNavController()
            val activeState by remember { derivedStateOf { app.tabManager.activeState } }
            val incognito = activeState?.incognito == true

            LighterTheme(incognito = incognito) {
                NavHost(
                    navController = navController,
                    startDestination = "browser"
                ) {
                    composable("browser") {
                        BrowserScreen(
                            tabManager = app.tabManager,
                            onOpenTabs = { navController.navigate("tabs") },
                            onOpenBookmarks = { navController.navigate("bookmarks") },
                            onOpenHistory = { navController.navigate("history") },
                            onOpenSettings = { navController.navigate("settings") },
                            onOpenSpoofing = { navController.navigate("spoofing") },
                            onOpenDownloads = { },
                            onOpenFind = { },
                            onScreenshot = {
                                app.tabManager.takeScreenshot { bmp ->
                                    if (bmp != null) { }
                                }
                            },
                            onReader = {
                                app.tabManager.evaluateJs(
                                    """(function(){
                                        var b=document.createElement('style');
                                        b.innerHTML='*{background:#fff !important;color:#000 !important;}img,iframe,video{display:none !important;}';
                                        document.head.appendChild(b);
                                    })();"""
                                )
                            },
                            onShare = {
                                val url = app.tabManager.activeState?.url ?: return@BrowserScreen
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, url)
                                }
                                startActivity(Intent.createChooser(share, "Bagikan URL"))
                            },
                            onExit = { finishAffinity() }
                        )
                    }
                    composable("tabs") {
                        TabsScreen(
                            tabManager = app.tabManager,
                            onOpenTab = { idx ->
                                app.tabManager.switchTo(idx)
                                navController.popBackStack()
                            },
                            onClose = { navController.popBackStack() }
                        )
                    }
                    composable("bookmarks") {
                        BookmarksScreen(
                            database = app.database,
                            onOpen = { url ->
                                app.tabManager.loadUrl(url)
                                navController.popBackStack()
                            },
                            onClose = { navController.popBackStack() }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            database = app.database,
                            onOpen = { url ->
                                app.tabManager.loadUrl(url)
                                navController.popBackStack()
                            },
                            onClose = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            prefs = app.prefs,
                            torManager = torManager,
                            onClose = { navController.popBackStack() }
                        )
                    }
                    composable("spoofing") {
                        SpoofingScreen(
                            profileManager = profileManager,
                            onApply = {
                                app.tabManager.tabs.forEach { it.webView.reload() }
                                navController.popBackStack()
                            },
                            onClose = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(TorManager.ORBOT_EXTRA_STATUS)
            addAction("org.torproject.android.intent.action.STATUS")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(orbotReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(orbotReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        try { unregisterReceiver(orbotReceiver) } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch(Dispatchers.IO) {
            if (app.prefs.clearOnExit.flow.first()) {
                app.dataCleaner.clearAll(includeBookmarks = false)
            }
            app.tabManager.closeAll()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (app.tabManager.canGoBack()) {
            app.tabManager.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
