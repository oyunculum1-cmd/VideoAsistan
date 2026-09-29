package com.videoasistan.app

import android.content.Context

data class EditorApp(val name: String, val pkg: String)

object Editors {
    private val known = listOf(
        EditorApp("CapCut", "com.lemon.lvoverseas"),
        EditorApp("CapCut (alt)", "com.lemon.lvoverseas.lite"),
        EditorApp("InShot", "com.camerasideas.instashot"),
        EditorApp("VN", "com.frontrow.vlog"),
        EditorApp("KineMaster", "com.nexstreaming.app.kinemasterfree"),
        EditorApp("YouCut", "com.camerasideas.trimmer")
    )

    fun installed(c: Context): List<EditorApp> = known.filter {
        try { c.packageManager.getPackageInfo(it.pkg, 0); true } catch (e: Exception) { false }
    }
}
