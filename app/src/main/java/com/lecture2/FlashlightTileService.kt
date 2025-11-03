package com.lecture2

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * クイック設定タイルからアプリを開く
 */
@RequiresApi(Build.VERSION_CODES.N)
class FlashlightTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening called")
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "onClick called - attempting to open app")
        
        // Android 14以降は別の方法を使用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            openAppWithPendingIntent()
        } else {
            openApp()
        }
    }
    
    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "Tile added")
    }
    
    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "Tile removed")
    }
    
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun openAppWithPendingIntent() {
        try {
            Log.d(TAG, "Using PendingIntent method (Android 14+)")
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
            Log.d(TAG, "Successfully opened app with PendingIntent")
        } catch (e: Exception) {
            Log.e(TAG, "Error with PendingIntent, trying fallback: ${e.message}", e)
            openApp()
        }
    }
    
    private fun openApp() {
        try {
            Log.d(TAG, "Using Intent method")
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            Log.d(TAG, "Starting activity")
            startActivityAndCollapse(intent)
            Log.d(TAG, "Successfully opened app")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app: ${e.message}", e)
        }
    }
}

private const val TAG = "FlashlightTile"