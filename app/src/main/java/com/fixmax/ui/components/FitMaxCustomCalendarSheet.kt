package com.fixmax.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Modern Custom Calendar Modal Bottom Sheet for FitMax.
 * Replaces the default Android DatePicker dialog with a sleek, One UI 7 inspired interactive calendar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitMaxCustomCalendarSheet(
    initialDate: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.5.dp)
                    .background(Color(0xFFE2E6EA), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Activity Calendar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14181B)
                    )
                    Text(
                        text = "Select any date to view historical health metrics",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF2F4F7), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF4A5568),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Shortcut Chips (Today, Yesterday, 7 Days Ago)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Today" to LocalDate.now(),
                    "Yesterday" to LocalDate.now().minusDays(1),
                    "Last Week" to LocalDate.now().minusDays(7)
                ).forEach { (label, targetDate) ->
                    val isChipSelected = selectedDate == targetDate
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isChipSelected) Color(0xFF005A43) else Color(0xFFF2F5F3),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    selectedDate = targetDate
                                    displayedMonth = YearMonth.from(targetDate)
                                    onDateSelected(targetDate)
                                    onDismiss()
                                }
                            )
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isChipSelected) Color.White else Color(0xFF374151),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Month Navigation Header: < November 2026 >
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFB), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = Color(0xFF14181B)
                    )
                }

                val monthYearStr = displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))
                Text(
                    text = monthYearStr,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14181B)
                )

                IconButton(
                    onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = Color(0xFF14181B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekday Headers: S M T W T F S
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLetter ->
                    Text(
                        text = dayLetter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8C98A4),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid for displayedMonth
            val firstDayOfMonth = displayedMonth.atDay(1)
            // Sunday is 0, Monday is 1, etc.
            val leadingEmptyDays = firstDayOfMonth.dayOfWeek.value % 7
            val daysInMonth = displayedMonth.lengthOfMonth()
            val totalCells = leadingEmptyDays + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (colIndex in 0 until 7) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - leadingEmptyDays + 1

                            if (cellIndex < leadingEmptyDays || dayNumber > daysInMonth) {
                                // Empty spacer cell
                                Box(modifier = Modifier.size(38.dp))
                            } else {
                                val cellDate = displayedMonth.atDay(dayNumber)
                                val isSelected = cellDate == selectedDate
                                val isToday = cellDate == LocalDate.now()
                                val isFuture = cellDate.isAfter(LocalDate.now())

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            color = when {
                                                isSelected -> Color(0xFF005A43)
                                                isToday -> Color(0xFFDCF4E9)
                                                else -> Color.Transparent
                                            },
                                            shape = CircleShape
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                selectedDate = cellDate
                                                onDateSelected(cellDate)
                                                onDismiss()
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = when {
                                                isSelected -> Color.White
                                                isToday -> Color(0xFF005A43)
                                                isFuture -> Color(0xFFB8C2CC)
                                                else -> Color(0xFF14181B)
                                            }
                                        )
                                        if (!isFuture && !isSelected) {
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(3.5.dp)
                                                    .background(
                                                        if (isToday) Color(0xFF005A43) else Color(0xFF43A047),
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Selected Date Indicator Bar
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF7FCF9),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFDDE7E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Selected Date",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B867B)
                        )
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14181B)
                        )
                    }

                    Button(
                        onClick = {
                            onDateSelected(selectedDate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005A43)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text("View Day", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
