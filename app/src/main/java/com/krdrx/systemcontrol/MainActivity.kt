package com.krdrx.systemcontrol

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import rikka.shizuku.Shizuku

class MainActivity : Activity() {
    private val bg = Color.rgb(13, 18, 24)
    private val card = Color.rgb(25, 34, 42)
    private val accent = Color.rgb(0, 210, 180)
    private lateinit var status: TextView
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener { _, grantResult ->
            runOnUiThread { status.text = if (grantResult == PackageManager.PERMISSION_GRANTED) "Shizuku: Izin diberikan" else "Shizuku: Izin ditolak" }
        }
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(20, 28, 20, 20) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = TextView(this).apply { text = "SYSTEM CONTROL X"; textSize = 25f; setTextColor(accent); setPadding(0, 8, 0, 4) }
        content.addView(title)
        content.addView(label("Kontrol sistem Android melalui Shizuku"))
        status = label(shizukuStatus())
        content.addView(panel(status))
        content.addView(button("Hubungkan / minta izin Shizuku") {
            if (!Shizuku.pingBinder()) status.text = "Shizuku belum berjalan. Buka aplikasi Shizuku dan mulai layanan."
            else if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) Shizuku.requestPermission(1001)
            else status.text = "Shizuku: Terhubung dan diizinkan"
        })
        content.addView(section("SYSTEM TWEAKS"))
        content.addView(button("Animasi 0.5x") { runShell("settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5") })
        content.addView(button("Animasi normal (1x)") { runShell("settings put global window_animation_scale 1; settings put global transition_animation_scale 1; settings put global animator_duration_scale 1") })
        content.addView(button("Matikan seluruh animasi") { runShell("settings put global window_animation_scale 0; settings put global transition_animation_scale 0; settings put global animator_duration_scale 0") })
        content.addView(section("DISPLAY & SYSTEM"))
        content.addView(button("Tampilkan informasi perangkat") {
            output.text = "Model: ${android.os.Build.MODEL}\nAndroid: ${android.os.Build.VERSION.RELEASE}\nSDK: ${android.os.Build.VERSION.SDK_INT}\nMIUI: ${android.os.Build.DISPLAY}"
        })
        content.addView(button("Buka pengaturan aplikasi") { startActivity(android.content.Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)) })
        content.addView(button("Buka pengaturan layar") { startActivity(android.content.Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)) })
        content.addView(section("APPLICATION MANAGER"))
        content.addView(button("Daftar aplikasi terpasang") { showApps(content) })
        content.addView(section("HASIL PERINTAH"))
        output = label("Belum ada perintah dijalankan.")
        content.addView(panel(output))
        content.addView(label("Catatan: hanya perintah yang ditampilkan dan dibatasi aplikasi. Perubahan animasi dapat dipulihkan dengan Animasi normal."))
        scroll.addView(content); root.addView(scroll); setContentView(root)
    }

    private fun showApps(parent: LinearLayout) {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0).sortedBy { pm.getApplicationLabel(it).toString().lowercase() }
        val names = apps.map { "${pm.getApplicationLabel(it)}  •  ${it.packageName}" }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Aplikasi terpasang (${apps.size})").setItems(names) { _, which ->
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.parse("package:${apps[which].packageName}"))
            startActivity(intent)
        }.setPositiveButton("Tutup", null).show()
    }

    private fun runShell(command: String) {
        if (!Shizuku.pingBinder()) { output.text = "Gagal: Shizuku tidak aktif."; return }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) { output.text = "Gagal: izin Shizuku belum diberikan."; return }
        Thread {
            try {
                val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
                val result = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                val code = process.waitFor()
                runOnUiThread { output.text = "Exit code: $code\n${result.ifBlank { error.ifBlank { "Perintah selesai." } }}" }
            } catch (e: Exception) { runOnUiThread { output.text = "Kesalahan: ${e.message}" } }
        }.start()
    }

    private fun shizukuStatus(): String = when {
        !Shizuku.pingBinder() -> "Shizuku: Tidak terhubung"
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> "Shizuku: Terhubung"
        else -> "Shizuku: Menunggu izin"
    }
    private fun label(textValue: String) = TextView(this).apply { text = textValue; textSize = 14f; setTextColor(Color.rgb(220, 230, 235)); setPadding(4, 10, 4, 10) }
    private fun section(textValue: String) = TextView(this).apply { text = textValue; textSize = 12f; setTextColor(accent); setPadding(4, 22, 4, 8) }
    private fun panel(view: View) = FrameLayout(this).apply { setBackgroundColor(card); setPadding(14, 8, 14, 8); addView(view) }
    private fun button(textValue: String, action: () -> Unit) = Button(this).apply {
        text = textValue; isAllCaps = false; setTextColor(Color.WHITE); setBackgroundColor(card)
        gravity = Gravity.CENTER_VERTICAL; setPadding(16, 8, 16, 8)
        setOnClickListener { action() }
        val lp = LinearLayout.LayoutParams(-1, -2); lp.setMargins(0, 5, 0, 5); layoutParams = lp
    }
}
