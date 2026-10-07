package com.agnes.pet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            startService(Intent(this, OverlayService::class.java))
            findViewById<TextView>(R.id.tv_status).text = "Agnes已上线！"
        }
        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            findViewById<TextView>(R.id.tv_status).text = "Agnes已下线"
        }
    }
}
