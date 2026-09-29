package com.videoasistan.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread

/** Arka planda çalışan edit görevi (Foreground Service). */
class EditJobService : Service() {

    companion object {
        const val CH = "edit_jobs"
        fun start(c: Context, pkg: String, prompt: String) {
            val i = Intent(c, EditJobService::class.java).putExtra("pkg", pkg).putExtra("prompt", prompt)
            c.startForegroundService(i)
        }
    }

    @Volatile private var running = false

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        // Sistem servisi öldürüp yeniden başlatırsa intent null gelir: kayıtlı durumdan devam et.
        val saved = StateStore.load(this)
        val pkg = intent?.getStringExtra("pkg") ?: saved.editorPkg
        val prompt = intent?.getStringExtra("prompt") ?: saved.prompt
        val startStep = if (intent == null) saved.step else 0
        if (pkg.isEmpty() || running) return START_STICKY

        val n = NotificationCompat.Builder(this, CH)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Video düzenleniyor…")
            .setContentText("İşlem arka planda devam ediyor")
            .setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 29)
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        else startForeground(1, n)

        running = true
        thread { runJob(pkg, prompt, startStep) }
        return START_STICKY
    }

    private fun runJob(pkg: String, prompt: String, from: Int) {
        val steps = EditPlanner.plan(prompt)
        try {
            packageManager.getLaunchIntentForPackage(pkg)?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(it)
            }
            Thread.sleep(4000)
            for (i in from until steps.size) {
                StateStore.save(this, JobState(pkg, prompt, i, "RUNNING", steps[i].title))
                val a11y = EditorAccessibilityService.instance
                    ?: throw IllegalStateException("Erişilebilirlik izni kapalı")
                if (!a11y.clickByText(*steps[i].labels)) {
                    // Bulunamazsa kısa bekleyip bir kez daha dene
                    Thread.sleep(2000)
                    a11y.clickByText(*steps[i].labels)
                }
                Thread.sleep(2500)
            }
            StateStore.save(this, JobState(pkg, prompt, steps.size, "DONE", "Tamamlandı"))
            notifyDone("Videolarınız hazır!")
        } catch (e: Exception) {
            StateStore.save(this, JobState(pkg, prompt, from, "ERROR", e.message ?: "Hata"))
            notifyDone("Düzenleme durdu: ${e.message}")
        } finally {
            running = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun notifyDone(text: String) {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(this, CH)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Video Asistan").setContentText(text)
            .setContentIntent(pi).setAutoCancel(true).build()
        getSystemService(NotificationManager::class.java).notify(2, n)
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH, "Düzenleme işleri", NotificationManager.IMPORTANCE_DEFAULT))
    }
}
