package com.lecture2.ui.components

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lecture2.MainActivity

/**
 * トーチの明るさを調整するスライダー
 * Android 13以降で利用可能
 */
@Composable
fun TorchBrightnessSlider(activity: MainActivity?, modifier: Modifier = Modifier) {
    val maxTorchLevel = remember { activity?.getMaxTorchLevel() ?: 1 }
    var torchLevel by remember { mutableFloatStateOf(maxTorchLevel.toFloat()) }

    Column(modifier = modifier) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && maxTorchLevel > 1) {
            Text(
                text = "トーチの明るさ: ${torchLevel.toInt()} / $maxTorchLevel",
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            Text(
                text = "※スライダーを動かすと自動的にライトがオンになります",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = torchLevel,
                onValueChange = { newValue ->
                    torchLevel = newValue
                    val level = newValue.toInt().coerceIn(1, maxTorchLevel)
                    activity?.setTorchBrightness(level)
                },
                valueRange = 1f..maxTorchLevel.toFloat(),
                steps = if (maxTorchLevel > 2) maxTorchLevel - 2 else 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Text(
                text = "トーチの明るさ調整は Android 13 以降で利用可能です",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
            )
        } else {
            Text(
                text = "お使いのデバイスはトーチの明るさ調整をサポートしていません",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
            )
        }
    }
}
