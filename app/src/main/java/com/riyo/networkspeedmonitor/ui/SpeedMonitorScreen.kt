package com.riyo.networkspeedmonitor.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.riyo.networkspeedmonitor.R
import com.riyo.networkspeedmonitor.model.SpeedData
import com.riyo.networkspeedmonitor.repository.SpeedMonitorRepository
import com.riyo.networkspeedmonitor.service.NetworkSpeedMonitorService
import com.riyo.networkspeedmonitor.ui.theme.AccentDownload
import com.riyo.networkspeedmonitor.ui.theme.AccentDownloadContainer
import com.riyo.networkspeedmonitor.ui.theme.AccentUpload
import com.riyo.networkspeedmonitor.ui.theme.AccentUploadContainer
import com.riyo.networkspeedmonitor.ui.theme.ButtonStart
import com.riyo.networkspeedmonitor.ui.theme.ButtonStop
import com.riyo.networkspeedmonitor.ui.theme.DarkBackground
import com.riyo.networkspeedmonitor.ui.theme.DarkCardBorder
import com.riyo.networkspeedmonitor.ui.theme.DarkSurface
import com.riyo.networkspeedmonitor.ui.theme.DarkSurfaceVariant
import com.riyo.networkspeedmonitor.ui.theme.StatusActive
import com.riyo.networkspeedmonitor.ui.theme.StatusInactive
import com.riyo.networkspeedmonitor.ui.theme.TextMuted
import com.riyo.networkspeedmonitor.ui.theme.TextPrimary
import com.riyo.networkspeedmonitor.ui.theme.TextSecondary
import com.riyo.networkspeedmonitor.util.LiveUpdateHelper
import com.riyo.networkspeedmonitor.util.LiveUpdateStatus

@Composable
fun SpeedMonitorScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isMonitoringActive by SpeedMonitorRepository.isMonitoringActive.collectAsState()
    val speedData by SpeedMonitorRepository.currentSpeed.collectAsState()

    var liveUpdateStatus by remember { mutableStateOf(LiveUpdateHelper.getPromotionStatus(context)) }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Refresh permission & live update status when resuming the activity
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                liveUpdateStatus = LiveUpdateHelper.getPromotionStatus(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted) {
            NetworkSpeedMonitorService.startService(context)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            HeaderSection(isMonitoringActive = isMonitoringActive)

            // Notification Permission Warning (if needed)
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                NotificationPermissionWarning(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onOpenSettings = {
                        openAppNotificationSettings(context)
                    }
                )
            }

            // Speed Metric Cards (Download & Upload)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SpeedMetricCard(
                    title = "DOWNLOAD",
                    speedValue = speedData.formatDownload(),
                    unit = "MB/s",
                    iconRes = R.drawable.ic_download,
                    accentColor = AccentDownload,
                    containerColor = AccentDownloadContainer,
                    modifier = Modifier.weight(1f)
                )

                SpeedMetricCard(
                    title = "UPLOAD",
                    speedValue = speedData.formatUpload(),
                    unit = "MB/s",
                    iconRes = R.drawable.ic_upload,
                    accentColor = AccentUpload,
                    containerColor = AccentUploadContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // Unsupported TrafficStats Warning
            if (!speedData.isSupported) {
                UnsupportedCountersWarning()
            }

            // Main Control Button (Start / Stop)
            MainControlButton(
                isMonitoringActive = isMonitoringActive,
                onToggleMonitoring = {
                    if (isMonitoringActive) {
                        NetworkSpeedMonitorService.stopService(context)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            NetworkSpeedMonitorService.startService(context)
                        }
                    }
                }
            )

            // Additional Status Card
            StatusDetailsCard(
                isMonitoringActive = isMonitoringActive,
                liveUpdateStatus = liveUpdateStatus,
                isSamsung = LiveUpdateHelper.isSamsungDevice,
                onOpenLiveUpdateSettings = {
                    LiveUpdateHelper.openLiveUpdateSettings(context)
                },
                onOpenSamsungNowBar = {
                    LiveUpdateHelper.openSamsungNowBarSettings(context)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Made by Supriyo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.6.sp,
                    color = TextMuted
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/I-AM-A-PROGRAMER")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) { }
                        }
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_github),
                        contentDescription = "GitHub",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "GitHub",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(isMonitoringActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "App Logo",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column {
                Text(
                    text = "Network Speed Monitor",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Real-time device throughput",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Active / Stopped Status Badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isMonitoringActive) Color(0xFF143823) else Color(0xFF262A33))
                .border(
                    width = 1.dp,
                    color = if (isMonitoringActive) StatusActive.copy(alpha = 0.5f) else DarkCardBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isMonitoringActive) StatusActive else StatusInactive)
            )
            Text(
                text = if (isMonitoringActive) "ACTIVE" else "STOPPED",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isMonitoringActive) StatusActive else TextMuted
            )
        }
    }
}

@Composable
private fun SpeedMetricCard(
    title: String,
    speedValue: String,
    unit: String,
    iconRes: Int,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = speedValue,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = unit,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )
        }
    }
}

@Composable
private fun MainControlButton(
    isMonitoringActive: Boolean,
    onToggleMonitoring: () -> Unit
) {
    Button(
        onClick = onToggleMonitoring,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isMonitoringActive) ButtonStop else ButtonStart,
            contentColor = Color.White
        )
    ) {
        Icon(
            painter = painterResource(
                id = if (isMonitoringActive) R.drawable.ic_stop else R.drawable.ic_speed_notification
            ),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (isMonitoringActive) "Stop Monitoring" else "Start Monitoring",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusDetailsCard(
    isMonitoringActive: Boolean,
    liveUpdateStatus: LiveUpdateStatus,
    isSamsung: Boolean,
    onOpenLiveUpdateSettings: () -> Unit,
    onOpenSamsungNowBar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkCardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "SYSTEM STATUS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextSecondary
            )

            // Monitoring Status Row
            StatusRow(
                label = "Foreground Monitoring",
                value = if (isMonitoringActive) "Active (Updating ~1s)" else "Stopped",
                valueColor = if (isMonitoringActive) StatusActive else TextMuted
            )

            HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)

            // Live Update Status Row
            val (statusText, statusColor) = when (liveUpdateStatus) {
                LiveUpdateStatus.ALLOWED -> "Allowed by System" to StatusActive
                LiveUpdateStatus.DISABLED -> "Disabled in System Settings" to Color(0xFFFBBF24)
                LiveUpdateStatus.NOT_SUPPORTED_OS -> "Unavailable (< Android 16/17)" to TextMuted
            }

            StatusRow(
                label = "Live Update Promotion",
                value = statusText,
                valueColor = statusColor
            )

            // Samsung Galaxy M17 / One UI 9 Note
            if (isSamsung) {
                HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)

                Text(
                    text = "Samsung One UI 9 Now Bar Setup:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentDownload
                )
                Text(
                    text = "1. Go to Settings > Lock screen and AOD > Now bar.\n2. Tap 'Live notifications' and toggle Network Speed Monitor ON.\n3. Ensure Lock screen notification display is set to 'Show content'.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = TextSecondary
                )

                Button(
                    onClick = onOpenSamsungNowBar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                ) {
                    Text(
                        text = "Open Samsung Now Bar Settings",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = "Android and Samsung One UI determine whether promoted notifications appear in the status bar chip or Now Bar. Live Update promotion is requested automatically on supported systems.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = TextMuted
                )
            }

            // Button to open Live Update / Promoted Notification settings
            OutlinedButton(
                onClick = onOpenLiveUpdateSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Enable Live Update",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun NotificationPermissionWarning(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2214)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF854D0E)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Notification Permission Required",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFDE047)
            )
            Text(
                text = "To display real-time speed in the persistent notification and enable Live Update promotion in Samsung Now Bar, notification permission must be granted.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color(0xFFFEF08A)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCA8A04))
                ) {
                    Text("Grant Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("App Settings", fontSize = 12.sp, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun UnsupportedCountersWarning() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1515)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF991B1B)))
    ) {
        Text(
            text = "Device traffic counters (TrafficStats) are currently reporting unavailable.",
            fontSize = 12.sp,
            color = Color(0xFFFCA5A5),
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun openAppNotificationSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { }
    }
}
