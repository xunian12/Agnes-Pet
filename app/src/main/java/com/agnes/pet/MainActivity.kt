package com.agnes.pet

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.tv_status)

        findViewById<Button>(R.id.btn_start).setOnClickListener { startPet() }
        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            status.text = "Agnes已下线"
        }

        // 打开应用时自动召回桌宠；没有权限时引导到系统设置。
        if (Settings.canDrawOverlays(this)) {
            startPet()
        } else {
            status.text = "请先开启悬浮窗权限"
        }
    }

    private fun startPet() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            status.text = "请开启悬浮窗权限后再启动 Agnes"
            return
        }
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        status.text = "Agnes已上线！"
    }
}
