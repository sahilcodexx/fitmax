package com.fixmax.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.Tune
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
import com.fixmax.ui.components.*
import com.fixmax.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WellnessDashboardScreen(
    onOpenDevSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val repo = remember { StepRepository(FixMaxDatabase.getInstance(context)) }
    val today = remember { LocalDate.now().toString() }
    val formattedDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }
    val summary by repo.observeToday(today).collectAsState(initial = null)

    val isRunning by StepTrackingService.isServiceRunning.collectAsState()
    val liveActivity by StepTrackingService.liveActivityState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MintBackground)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header: Date + Discreet Tuning Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Activity",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = formattedDate,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onOpenDevSettings) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Sensor Settings",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Center Ripple Ring
        ConcentricRippleRing(
            steps = summary?.totalSteps ?: 0L,
            goalSteps = 10_000L
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Activity State Pastel Badge
        PastelBadge(
            text = if (isRunning) {
                when (liveActivity) {
                    ActivityState.WALKING -> "Active • Walking Detected"
                    ActivityState.RUNNING -> "Active • Running Detected"
                    ActivityState.VEHICLE_LIKE_MOTION -> "Moving in Vehicle"
                    else -> "Active • Standing / Resting"
                }
            } else "Tracking Paused",
            style = if (isRunning) BadgeStyle.GREEN else BadgeStyle.PEACH
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Organic Metric Pill Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Distance Metric Card
            MetricPillCard(
                title = "Distance",
                value = String.format("%.2f km", (summary?.distanceMeters ?: 0f) / 1000f),
                badgeText = "GPS-free",
                badgeStyle = BadgeStyle.PURPLE,
                modifier = Modifier.weight(1f)
            )

            // Active Time Metric Card
            MetricPillCard(
                title = "Active Time",
                value = "${summary?.activeMinutes ?: 0} min",
                badgeText = "Today",
                badgeStyle = BadgeStyle.GREEN,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Deep Forest Emerald Green Primary Action Button
        Button(
            onClick = {
                if (isRunning) {
                    StepTrackingService.stop(context)
                } else {
                    StepTrackingService.start(context)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreenPrimary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRunning) "Pause Tracking" else "Resume Daily Walk",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricPillCard(
    title: String,
    value: String,
    badgeText: String,
    badgeStyle: BadgeStyle,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = TextSecondary)
                PastelBadge(text = badgeText, style = badgeStyle)
            }
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
