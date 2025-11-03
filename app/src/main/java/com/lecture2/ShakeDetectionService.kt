package com.lecture2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

/**
 * バックグラウンドで振動を検知し、フラッシュライトを制御するサービス
 */
class ShakeDetectionService : Service(), SensorEventListener {
    
    // センサーとライトの状態管理
    private lateinit var sensorManager: SensorManager
    private var isLightOn = false
    private var lastShakeTime: Long = 0
    
    // 設定値（MainActivityから受け取る）
    private var shakeThreshold = DEFAULT_THRESHOLD
    private var shakeInterval = DEFAULT_INTERVAL
    private var currentTorchLevel = DEFAULT_TORCH_LEVEL

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "shake_detection_channel"
        
        // デフォルト値
        private const val DEFAULT_THRESHOLD = 80.0f
        private const val DEFAULT_INTERVAL = 1000L
        private const val DEFAULT_TORCH_LEVEL = 1
        
        // Intent用のキー
        const val EXTRA_SHAKE_THRESHOLD = "shake_threshold"
        const val EXTRA_SHAKE_INTERVAL = "shake_interval"
        const val EXTRA_TORCH_LEVEL = "torch_level"
    }

    override fun onCreate() {
        super.onCreate()
        initializeSensorManager()
        startForegroundWithNotification()
        registerAccelerometer()
        Log.d(TAG, "Service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        receiveSettingsFromIntent(intent)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        if (isLightOn) {
            toggleLight(false)
        }
        Log.d(TAG, "Service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 精度変更時の処理は不要
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerData(event)
        }
    }

    // ===== 初期化メソッド =====
    
    private fun initializeSensorManager() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun startForegroundWithNotification() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("振動検知サービス")
            .setContentText("振動を検知してフラッシュライトを制御しています")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(createAppPendingIntent())
            .setOngoing(true)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun registerAccelerometer() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    // ===== 設定受信メソッド =====
    
    private fun receiveSettingsFromIntent(intent: Intent?) {
        intent?.let {
            shakeThreshold = it.getFloatExtra(EXTRA_SHAKE_THRESHOLD, DEFAULT_THRESHOLD)
            shakeInterval = it.getLongExtra(EXTRA_SHAKE_INTERVAL, DEFAULT_INTERVAL)
            currentTorchLevel = it.getIntExtra(EXTRA_TORCH_LEVEL, DEFAULT_TORCH_LEVEL)
            
            Log.d(TAG, "Settings: Threshold=$shakeThreshold, Interval=$shakeInterval, TorchLevel=$currentTorchLevel")
        }
    }

    // ===== センサーデータ処理メソッド =====
    
    private fun handleAccelerometerData(event: SensorEvent) {
        val (x, y, z) = event.values
        val acceleration = calculateAcceleration(x, y, z)
        
        if (isShakeDetected(acceleration)) {
            handleShakeDetection()
        }
    }

    private fun calculateAcceleration(x: Float, y: Float, z: Float): Float {
        return kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    private fun isShakeDetected(acceleration: Float): Boolean {
        if (acceleration <= shakeThreshold) return false
        
        val currentTime = System.currentTimeMillis()
        val timeSinceLastShake = currentTime - lastShakeTime
        
        return timeSinceLastShake > shakeInterval
    }

    private fun handleShakeDetection() {
        lastShakeTime = System.currentTimeMillis()
        isLightOn = !isLightOn
        toggleLight(isLightOn)
        Log.d(TAG, "Shake detected! Light: ${if (isLightOn) "ON" else "OFF"}")
    }

    // ===== ライト制御メソッド =====
    
    private fun toggleLight(turnOn: Boolean) {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        
        try {
            cameraManager.cameraIdList.firstOrNull()?.let { cameraId ->
                if (turnOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    setTorchWithLevel(cameraManager, cameraId, currentTorchLevel)
                } else {
                    cameraManager.setTorchMode(cameraId, turnOn)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to control torch: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setTorchWithLevel(cameraManager: CameraManager, cameraId: String, level: Int) {
        try {
            cameraManager.turnOnTorchWithStrengthLevel(cameraId, level)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set torch level: ${e.message}")
            cameraManager.setTorchMode(cameraId, true)
        }
    }

    // ===== 通知関連メソッド =====
    
    private fun createAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "振動検知サービス",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "バックグラウンドで振動を検知します"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

private const val TAG = "ShakeService"