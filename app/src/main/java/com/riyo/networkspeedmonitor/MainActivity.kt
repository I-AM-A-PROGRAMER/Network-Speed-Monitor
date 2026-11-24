package com.riyo.networkspeedmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.riyo.networkspeedmonitor.ui.SpeedMonitorScreen
import com.riyo.networkspeedmonitor.ui.theme.NetworkSpeedMonitorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetworkSpeedMonitorTheme {
                SpeedMonitorScreen()
            }
        }
    }
}
