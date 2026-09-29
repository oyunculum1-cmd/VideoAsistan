package com.videoasistan.app

import android.content.Context

/** Proje durumunu kalıcı saklar (uygulama kapansa da kalınan yerden devam). */
data class JobState(
    val editorPkg: String,
    val prompt: String,
    val step: Int,
    val status: String,   // IDLE, RUNNING, DONE, ERROR
    val message: String
)

object StateStore {
    private fun sp(c: Context) = c.getSharedPreferences("job_state", Context.MODE_PRIVATE)

    fun save(c: Context, s: JobState) = sp(c).edit()
        .putString("pkg", s.editorPkg).putString("prompt", s.prompt)
        .putInt("step", s.step).putString("status", s.status)
        .putString("msg", s.message).apply()

    fun load(c: Context): JobState {
        val p = sp(c)
        return JobState(
            p.getString("pkg", "") ?: "", p.getString("prompt", "") ?: "",
            p.getInt("step", 0), p.getString("status", "IDLE") ?: "IDLE",
            p.getString("msg", "") ?: ""
        )
    }
}
