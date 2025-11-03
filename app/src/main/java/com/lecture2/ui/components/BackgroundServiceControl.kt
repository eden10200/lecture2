package com.lecture2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lecture2.MainActivity

/**
 * バックグラウンドサービスの開始/停止を制御するコンポーネント
 */
@Composable
fun BackgroundServiceControl(activity: MainActivity?, modifier: Modifier = Modifier) {
    var isServiceRunning by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.padding(bottom = 16.dp)) {
        Text(
            text = "バックグラウンド振動検知",
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (isServiceRunning) "サービス実行中" else "サービス停止中",
            color = if (isServiceRunning) Color.Green else Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
            onClick = { 
                isServiceRunning = if (isServiceRunning) {
                    activity?.stopShakeService()
                    false
                } else {
                    activity?.startShakeService()
                    true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceRunning) Color.Red else Color.Green
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = if (isServiceRunning) "停止" else "開始", color = Color.White)
        }
    }
}
