package com.agnes.pet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return
        if (!Settings.canDrawOverlays(context)) {
            Log.d("AgnesPet", "boot skipped: overlay permission is disabled")
            return
        }
        val serviceIntent = Intent(context, OverlayService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d("AgnesPet", "overlay started after boot")
        } catch (e: Exception) {
            Log.e("AgnesPet", "boot start failed", e)
        }
    }
}
