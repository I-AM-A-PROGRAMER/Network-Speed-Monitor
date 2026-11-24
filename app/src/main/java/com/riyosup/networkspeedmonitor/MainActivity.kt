package com.riyosup.networkspeedmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.riyosup.networkspeedmonitor.ui.SpeedMonitorScreen
import com.riyosup.networkspeedmonitor.ui.theme.NetworkSpeedMonitorTheme

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
