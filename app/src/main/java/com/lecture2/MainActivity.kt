package com.lecture2

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lecture2.ui.components.BackgroundServiceControl
import com.lecture2.ui.components.FlashLightButtons
import com.lecture2.ui.components.TorchBrightnessSlider


/**
 * フラッシュライトと振動検知を制御するメインアクティビティ
 */
class MainActivity : ComponentActivity(), SensorEventListener {
    
    // センサー管理
    private lateinit var sensorManager: SensorManager
    private var isLightOn = false
    private var lastShakeTime: Long = 0
    
    // 設定値
    val shakeThreshold = 10.0f// TODO: No9.自分好みの閾値に変更する
    val shakeInterval = 1000L
    var currentTorchLevel = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeSensorManager()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        registerAccelerometer()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 精度変更時の処理は不要
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerData(event)
        }
    }

    // ===== 初期化メソッド =====
    
    private fun initializeSensorManager() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    private fun setupUI() {
        enableEdgeToEdge()
        setContent {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
        }
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

    // ===== センサーデータ処理メソッド =====
    
    private fun handleAccelerometerData(event: SensorEvent) {

        val (x, y, z) = event.values
        Log.d("SensorValue", "x=$x,y=$y,z=$z")//TODO: No8.Logcatでセンサーの生データを観測する




            // TODO: No1. 3軸の加速度から合成加速度を計算しよう
            // ヒント1: 3次元空間のベクトルの大きさは √(x² + y² + z²) で求められます
            // ヒント2: kotlin.math.sqrt() 関数で平方根を計算できます
            // ヒント3: sqrt()はDouble型を受け取るので、計算結果を.toDouble()してから渡します
            // ヒント4: 最終的にFloat型に戻すため、.toFloat()で変換します
            // 例: kotlin.math.sqrt((数値).toDouble()).toFloat()

            val acceleration =___

            // TODO: No2. 加速度が閾値を超えたかチェックしよう
            // ヒント: if文で acceleration と shakeThreshold を比較します
            if (___) {

                // TODO: No3. 現在の時刻を取得しよう
                // ヒント: System.currentTimeMillis() でミリ秒単位の現在時刻を取得できます
                val currentTime =___

                // TODO: No4. 前回の振動検知から十分な時間が経過したかチェックしよう
                // ヒント1: (現在時刻 - 前回の時刻) で経過時間を計算します
                // ヒント2: 経過時間が shakeInterval より大きければ、振動として認識します
                if (___) {

                    // TODO: No5. 前回の振動時刻を更新しよう
                    // ヒント: lastShakeTime に現在時刻を代入します
                    lastShakeTime = ___

                    // TODO: No6. ライトの状態を反転させよう
                    // ヒント1: !演算子でboolean値を反転できます
                    // ヒント2: isLightOn が true なら false に、false なら true にします
                    isLightOn = ___

                    // TODO: No7. ライトをオン/オフしよう
                    // ヒント: toggleLight(boolean) 関数に新しいライトの状態を渡します
                   ___
                }
            }
        }
    }

    // ===== ライト制御メソッド =====
    
    fun toggleLight(turnOn: Boolean) {
        val cameraManager = getSystemService(CameraManager::class.java) ?: return
        
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
    
    fun setTorchBrightness(level: Int) {
        currentTorchLevel = level
        
        // 明るさを変更するときに自動的にライトをオンにする
        if (!isLightOn) {
            isLightOn = true
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val cameraManager = getSystemService(CameraManager::class.java) ?: return
            try {
                cameraManager.cameraIdList.firstOrNull()?.let { cameraId ->
                    setTorchWithLevel(cameraManager, cameraId, level)
                    Log.d(TAG, "Torch brightness set to level: $level")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update torch brightness: ${e.message}")
            }
        } else {
            // Android 13未満の場合は通常のオン/オフのみ
            toggleLight(true)
            Log.d(TAG, "Torch brightness control not available (Android < 13), turning light on")
        }
    }
    
    fun getMaxTorchLevel(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val cameraManager = getSystemService(CameraManager::class.java) ?: return 1
            try {
                cameraManager.cameraIdList.firstOrNull()?.let { cameraId ->
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val maxLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                    if (maxLevel > 1) return maxLevel
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get max torch level: ${e.message}")
            }
        }
        return 1
    }
    
    // ===== サービス制御メソッド =====
    
    fun startShakeService() {
        val intent = Intent(this, ShakeDetectionService::class.java).apply {
            putExtra(ShakeDetectionService.EXTRA_SHAKE_THRESHOLD, shakeThreshold)
            putExtra(ShakeDetectionService.EXTRA_SHAKE_INTERVAL, shakeInterval)
            putExtra(ShakeDetectionService.EXTRA_TORCH_LEVEL, currentTorchLevel)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        Log.d(TAG, "Service started with settings - Threshold: $shakeThreshold, Interval: $shakeInterval, TorchLevel: $currentTorchLevel")
    }
    
    fun stopShakeService() {
        val intent = Intent(this, ShakeDetectionService::class.java)
        stopService(intent)
        Log.d(TAG, "Service stopped")
    }
}

private const val TAG = "MainActivity"

// ===== Compose UI =====

/**
 * メイン画面のレイアウト
 * 各コンポーネントを組み合わせて表示
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val activity = LocalContext.current as? MainActivity

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // TODO: No12.上からバックグラウンド処理ボタン　フラッシュライトのボタン　トーチの明るさのスライダーの順にUIを変更しよう

        TorchBrightnessSlider(activity = activity)

        FlashLightButtons(activity = activity)

        BackgroundServiceControl(activity = activity)

    }
}
