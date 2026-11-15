package com.fixmax.feature.activity

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.ui.components.m3BounceClick

/**
 * Activity & Workout Screen.
 * Recreated exactly matching user provided Image 3:
 * 1. Top header: Avatar, "Hello, Sophia", circular floating notification bell with red dot
 * 2. Month navigator capsule: "Feb 2025" with left/right arrows
 * 3. Peach week strip: SUN MON TUE WED THR FRI SAT with vertical capsule on selected WED 4
 * 4. 2-Column Pastel Metric Cards:
 *    - Time (Pastel Sky Blue, 1:30 Hours, clock icon)
 *    - Exercises (Pastel Lime Green, 12/8 Tasks, target icon)
 * 5. Weekly Points Card:
 *    - Circular progress gauge (64%)
 *    - "1544" Weekly points with green ▲ 6% badge
 *    - Inset container with dashed grid lines and smooth ocean blue cubic spline curve
 */
@Composable
fun ActivityScreen(
    onBack: () -> Unit = {}
) {
    var selectedDayIndex by remember { mutableStateOf(3) } // WED 4 default
    val weekDays = listOf(
        Pair("SUN", "1"),
        Pair("MON", "2"),
        Pair("TUE", "3"),
        Pair("WED", "4"),
        Pair("THR", "5"),
        Pair("FRI", "6"),
        Pair("SAT", "7")
    )

    var currentMonth by remember { mutableStateOf("Feb 2025") }
    var showNotifDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // 1. Top Header: Avatar + "Hello, Sophia" + Circular Floating Notification Button (Image 3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circular Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFFFFD4C4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👩‍🦰",
                        fontSize = 24.sp
                    )
                }

                Text(
                    text = "Hello, Sophia",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14181F)
                )
            }

            // Circular White Notification Button with Red Badge Dot
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .m3BounceClick { showNotifDialog = true },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF14181F),
                        modifier = Modifier.size(22.dp)
                    )

                    // Red Badge Dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFFE53935), CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-9).dp, y = 9.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 2. Month Navigator Capsule: ← [ Feb 2025 ] →
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { currentMonth = "Jan 2025" },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = Color(0xFF14181F),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFC7B2),
                modifier = Modifier.height(38.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 22.dp)
                ) {
                    Text(
                        text = currentMonth,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14181F)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { currentMonth = "Mar 2025" },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = Color(0xFF14181F),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Peach Week Strip (SUN 1, MON 2, TUE 3, WED 4 pill, THR 5, FRI 6, SAT 7)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekDays.forEachIndexed { index, (dayName, dateNum) ->
                val isSelected = index == selectedDayIndex
                val isPastActive = index == 0 || index == 1 // Days 1 & 2 marked active

                if (isSelected) {
                    // Tall Vertical Salmon Pill for WED 4
                    Surface(
                        shape = RoundedCornerShape(19.dp),
                        color = Color(0xFFFFC7B2),
                        modifier = Modifier
                            .width(42.dp)
                            .height(64.dp)
                            .m3BounceClick { selectedDayIndex = index }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14181F)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = dateNum,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF14181F)
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .width(36.dp)
                            .m3BounceClick { selectedDayIndex = index }
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8A939D)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isPastActive) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFFFFD5C4), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dateNum,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14181F)
                                )
                            }
                        } else {
                            Text(
                                text = dateNum,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14181F)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. 2-Column Pastel Metric Cards (Time & Exercises)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Time Card (Pastel Sky Blue)
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFFD6EEF8),
                modifier = Modifier
                    .weight(1f)
                    .height(148.dp)
                    .m3BounceClick { }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14181F)
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFC3E3F2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF2979FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "1:30",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF14181F)
                        )
                        Text(
                            text = "Hours",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5A7382)
                        )
                    }
                }
            }

            // Exercises Card (Pastel Lime Green)
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFFE2F4C5),
                modifier = Modifier
                    .weight(1f)
                    .height(148.dp)
                    .m3BounceClick { }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exercises",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14181F)
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFD2EBAF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = Color(0xFF689F38),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "12/8",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF14181F)
                        )
                        Text(
                            text = "Tasks",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF61774A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Large Weekly Points Card with Circular Gauge & Cubic Spline Wave Graph
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(0.6.dp, Color(0xFFE8ECEF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Circular Progress Gauge Arc (64%)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeW = 6.dp.toPx()
                                // Background track
                                drawArc(
                                    color = Color(0xFFEEEEEE),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(strokeW, cap = StrokeCap.Round)
                                )
                                // Lime progress arc
                                drawArc(
                                    color = Color(0xFF8BC34A),
                                    startAngle = -90f,
                                    sweepAngle = 360f * 0.64f,
                                    useCenter = false,
                                    style = Stroke(strokeW, cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = "64%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF14181F)
                            )
                        }

                        // Weekly points text & trend
                        Column {
                            Text(
                                text = "Weekly points",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "1544",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF14181F)
                                )
                                // Green ▲ 6% Pill Badge
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "▲ 6%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }

                    // Three dots options
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Inset Container: Dashed grid lines + Ocean blue cubic spline graph
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFF6F8F9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Canvas for dashed lines & spline wave curve
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val colCount = 7
                            val colWidth = w / (colCount - 1)

                            // 1. Draw 7 Vertical Dashed Guidelines
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            for (i in 0 until colCount) {
                                val x = i * colWidth
                                drawLine(
                                    color = Color(0xFFE0E3E6),
                                    start = Offset(x, 0f),
                                    end = Offset(x, h),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                            }

                            // 2. Smooth Cubic Spline Curve in Ocean Blue
                            val points = listOf(
                                Offset(0f * colWidth, h * 0.78f),
                                Offset(1f * colWidth, h * 0.84f),
                                Offset(2f * colWidth, h * 0.72f),
                                Offset(3f * colWidth, h * 0.76f),
                                Offset(4f * colWidth, h * 0.88f),
                                Offset(5f * colWidth, h * 0.32f), // Peak on Friday
                                Offset(6f * colWidth, h * 0.50f)
                            )

                            val splinePath = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val midX = (p0.x + p1.x) / 2
                                    cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                }
                            }

                            drawPath(
                                path = splinePath,
                                color = Color(0xFF3586A7),
                                style = Stroke(
                                    width = 3.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // 3. Highlight Indicator Point on Peak (Friday)
                            val peakPoint = points[5]
                            // Outer white circle with shadow
                            drawCircle(
                                color = Color.White,
                                radius = 7.dp.toPx(),
                                center = peakPoint
                            )
                            drawCircle(
                                color = Color(0xFF14181F),
                                radius = 3.5.dp.toPx(),
                                center = peakPoint
                            )
                        }

                        // Day Labels Row: S M T W T F S
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.width(20.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF8A939D)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }

    // Notification Dialog
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            confirmButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("OK", color = Color(0xFF14181F), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(text = "Daily Workout Goals", fontWeight = FontWeight.Bold, color = Color(0xFF14181F))
            },
            text = {
                Text(
                    text = "You're 2 tasks away from hitting today's exercise target. Keep going!",
                    fontSize = 14.sp,
                    color = Color(0xFF5A7382)
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
