package com.agnes.pet

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.util.Log
import androidx.core.app.NotificationCompat

class OverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: WebView? = null
    private var params: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val CHANNEL_ID = "agnes_pet_channel"
        private const val NOTIFICATION_ID = 8888
        private const val PET_SIZE_DP = 180
        private const val PET_HEIGHT_DP = 260
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("喵~ Agnes在这里哦"))
        setupOverlay()
        startNotificationWhisper()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams(
            dpToPx(PET_SIZE_DP),
            dpToPx(PET_HEIGHT_DP),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 120
        }
        overlayView = WebView(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            alpha = 1f
            visibility = View.VISIBLE
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    Log.d("AgnesPet", "pet.html loaded: $url, size=${view.width}x${view.height}")
                }
            }
            loadUrl("file:///android_asset/pet.html")
            setOnTouchListener(createTouchLister())
        }
        windowManager?.addView(overlayView, params)
        Log.d("AgnesPet", "overlay added: ${params?.width}x${params?.height} at ${params?.x},${params?.y}")
    }

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var lastTapTime = 0L
    private var touchStartTime = 0L
    private var hasMoved = false
    private var tapCount = 0

    private fun createTouchLister() = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params?.x ?: 0; initialY = params?.y ?: 0
                initialTouchX = event.rawX; initialTouchY = event.rawY
                touchStartTime = System.currentTimeMillis(); hasMoved = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - initialTouchX).toInt()
                val dy = (event.rawY - initialTouchY).toInt()
                if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                    hasMoved = true
                    params?.x = initialX + dx; params?.y = initialY + dy
                    windowManager?.updateViewLayout(overlayView, params)
                }
            }
            MotionEvent.ACTION_UP -> {
                val elapsed = System.currentTimeMillis() - touchStartTime
                if (!hasMoved) {
                    val now = System.currentTimeMillis()
                    if (now - lastTapTime < 300) {
                        tapCount++
                        if (tapCount >= 3) onTripleTap() else onDoubleTap()
                    } else {
                        tapCount = 0
                        if (elapsed > 600) onLongPress() else onTap()
                    }
                    lastTapTime = now
                }
            }
        }
        true
    }

    private fun onTap() = overlayView?.evaluateJavascript("window.agnesPet?.onTap()", null)
    private fun onDoubleTap() = overlayView?.evaluateJavascript("window.agnesPet?.onDoubleTap()", null)
    private fun onTripleTap() = overlayView?.evaluateJavascript("window.agnesPet?.onTripleTap()", null)
    private fun onLongPress() = overlayView?.evaluateJavascript("window.agnesPet?.onLongPress()", null)

    private val whispers = listOf("喵~", "主人~", "Agnes在看你", "可爱吗？", "摸我呀")
    private fun startNotificationWhisper() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIFICATION_ID, buildNotification(whispers.random()))
                handler.postDelayed(this, 3600_000L)
            }
        }, 3600_000L)
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("🐱 Agnes").setContentText(text)
        .setSmallIcon(android.R.drawable.ic_menu_compass)
        .setContentIntent(PendingIntent.getActivity(this, 0, packageManager.getLaunchIntentForPackage(packageName), PendingIntent.FLAG_IMMUTABLE))
        .setOngoing(true).setSilent(true).build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Agnes Pet", NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) }
            )
        }
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        overlayView?.let { windowManager?.removeView(it); it.destroy() }
        overlayView = null; handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
