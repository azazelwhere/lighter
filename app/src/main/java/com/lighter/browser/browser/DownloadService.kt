package com.lighter.browser.browser

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import android.os.Environment

/**
 * Foreground service for big / resumable downloads via OkHttp.
 * Launched by WorkManager when system DownloadManager is unavailable
 * or when spoofed headers must be sent.
 */
class DownloadService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // The actual download work is done via WorkManager (DownloadWorker below).
        // This Service exists purely to satisfy the foregroundServiceType=dataSync
        // declaration in the manifest, allowing long-running downloads.
        return START_NOT_STICKY
    }
}

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val filename = inputData.getString("filename") ?: "download.bin"
        val ua = inputData.getString("ua") ?: "Lighter/1.0"
        val referer = inputData.getString("referer")
        val cookies = inputData.getString("cookies")

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(url)
            .header("User-Agent", ua)
            .apply {
                if (referer != null) header("Referer", referer)
                if (cookies != null) header("Cookie", cookies)
            }
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return Result.retry()

            val target = File(
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                filename
            )
            response.body?.byteStream()?.use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output, bufferSize = 8192)
                }
            } ?: return Result.failure()

            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel("downloads") == null) {
                nm.createNotificationChannel(
                    NotificationChannel("downloads", "Downloads", NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
    }
}
