package com.videoasistan.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Edit uygulamasının arayüzünü okur ve butonlara dokunur.
 * Not: Her edit uygulamasının buton adları/ID'leri farklıdır ve güncellemelerle değişir;
 * aşağıdaki etiket listelerini gerçek cihazda kendi uygulamanıza göre ayarlayın.
 */
class EditorAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile var instance: EditorAccessibilityService? = null
    }

    override fun onServiceConnected() { instance = this }
    override fun onUnbind(intent: android.content.Intent?): Boolean { instance = null; return super.onUnbind(intent) }
    override fun onInterrupt() {}
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    /** Ekranda verilen metinlerden birini içeren tıklanabilir öğeye dokunur. */
    fun clickByText(vararg labels: String): Boolean {
        val root = rootInActiveWindow ?: return false
        for (l in labels) {
            for (n in root.findAccessibilityNodeInfosByText(l)) {
                var t: AccessibilityNodeInfo? = n
                while (t != null && !t.isClickable) t = t.parent
                if (t != null && t.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            }
        }
        return false
    }

    fun back() = performGlobalAction(GLOBAL_ACTION_BACK)
}
