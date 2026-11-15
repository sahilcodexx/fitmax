package com.fixmax.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.core.common.ActivityState
import com.fixmax.data.database.FixMaxDatabase
import com.fixmax.data.repository.StepRepository
import com.fixmax.tracking.StepTrackingService
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { StepRepository(FixMaxDatabase.getInstance(context)) }
    val today = remember { LocalDate.now().toString() }
    val summary by repo.observeToday(today).collectAsState(initial = null)

    val isRunning by StepTrackingService.isServiceRunning.collectAsState()
    val liveActivity by StepTrackingService.liveActivityState.collectAsState()
    val liveCadence by StepTrackingService.liveCadence.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Header
        Text("FIX MAX", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Samsung Galaxy M35 5G Motion Intelligence", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // Live Service Status Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) Color(0xFF4CAF50) else Color(0xFFF44336))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRunning) "ENGINE ACTIVE (${liveActivity.name})" else "ENGINE STOPPED",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Metric Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${summary?.totalSteps ?: 0}",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("STEPS TODAY", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Distance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.2f km", (summary?.distanceMeters ?: 0f) / 1000f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cadence", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${liveCadence.toInt()} spm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Goal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("10,000", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Service Controls
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { StepTrackingService.start(context) },
                enabled = !isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text("Start Tracking")
            }
            OutlinedButton(
                onClick = { StepTrackingService.stop(context) },
                enabled = isRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text("Stop")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Instant Verification Button (Allows testing step count increment and Room DB persistence without walking)
        OutlinedButton(
            onClick = {
                scope.launch {
                    repo.addSteps(today, 5)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Step (+5 Steps Simulator)")
        }
    }
}
