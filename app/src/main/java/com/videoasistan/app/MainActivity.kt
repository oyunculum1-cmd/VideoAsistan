package com.videoasistan.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var editors: List<EditorApp>

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_main)
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)

        editors = Editors.installed(this)
        val sp = findViewById<Spinner>(R.id.spEditor)
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            if (editors.isEmpty()) listOf("Kurulu edit uygulaması bulunamadı") else editors.map { it.name })

        findViewById<Button>(R.id.btnA11y).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val prompt = findViewById<EditText>(R.id.etPrompt).text.toString()
            if (editors.isEmpty() || prompt.isBlank()) {
                Toast.makeText(this, "Edit uygulaması ve talimat gerekli", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            if (!a11yEnabled()) {
                Toast.makeText(this, "Önce erişilebilirlik iznini açın", Toast.LENGTH_LONG).show(); return@setOnClickListener
            }
            EditJobService.start(this, editors[sp.selectedItemPosition].pkg, prompt)
            Toast.makeText(this, "Başladı. Başka uygulamaya geçebilirsiniz.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Tek tıkla devam: son kaydedilen durumu göster
        val s = StateStore.load(this)
        findViewById<TextView>(R.id.tvA11y).text =
            if (a11yEnabled()) "Erişilebilirlik: açık ✓" else "Erişilebilirlik: kapalı ✗"
        findViewById<TextView>(R.id.tvStatus).text = when (s.status) {
            "RUNNING" -> "Devam ediyor: ${s.message} (adım ${s.step + 1})"
            "DONE" -> "Son proje tamamlandı ✓"
            "ERROR" -> "Durdu: ${s.message} (adım ${s.step + 1})"
            else -> "Hazır"
        }
        if (s.prompt.isNotEmpty()) findViewById<EditText>(R.id.etPrompt).setText(s.prompt)
    }

    private fun a11yEnabled(): Boolean {
        val v = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return v.contains("$packageName/")
    }
}
