package com.videoasistan.app

data class Step(val title: String, val labels: Array<String>)

/**
 * Talimattan adım listesi üretir. Şimdilik anahtar kelime tabanlı basit bir planlayıcı;
 * ileride buraya bir yapay zeka API'si (senaryo/sahne analizi) bağlanabilir.
 */
object EditPlanner {
    fun plan(prompt: String): List<Step> {
        val p = prompt.lowercase()
        val steps = mutableListOf(Step("Yeni proje", arrayOf("New project", "Yeni proje", "Start editing", "Create")))
        if ("kes" in p || "kırp" in p || "trim" in p || "cut" in p)
            steps += Step("Kırpma", arrayOf("Split", "Böl", "Trim", "Kırp"))
        if ("geçiş" in p || "transition" in p)
            steps += Step("Geçiş", arrayOf("Transitions", "Geçişler"))
        if ("efekt" in p || "effect" in p)
            steps += Step("Efekt", arrayOf("Effects", "Efektler"))
        steps += Step("Dışa aktar", arrayOf("Export", "Dışa aktar", "Save", "Kaydet"))
        return steps
    }
}
