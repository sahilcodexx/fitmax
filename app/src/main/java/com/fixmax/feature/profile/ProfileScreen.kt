package com.fixmax.feature.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.ui.components.m3BounceClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Sahil") }
    var userEmail by remember { mutableStateOf("sahil@gmail.com") }

    // Dialog & Sheet States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showStatDetailDialog by remember { mutableStateOf<String?>(null) }
    var activeSheet by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Settings Toggle States
    var backgroundTrackingEnabled by remember { mutableStateOf(true) }
    var dailyGoalNotifications by remember { mutableStateOf(true) }
    var highAccuracySensors by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FCF9))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Navigation: Back Arrow & Settings Gear Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.m3BounceClick(onClick = onBack)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1A2E26)
                )
            }

            IconButton(
                onClick = { activeSheet = "App Settings" },
                modifier = Modifier.m3BounceClick { activeSheet = "App Settings" }
            ) {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings", tint = Color(0xFF1A2E26))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // User Avatar with Edit Badge (Interactive)
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.m3BounceClick { showEditProfileDialog = true }
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .background(Color(0xFFCCE8DB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF5BAA88),
                    modifier = Modifier.size(54.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(Color(0xFF005A43), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(13.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
        Text(text = userEmail, fontSize = 13.sp, color = Color(0xFF6B867B))

        Spacer(modifier = Modifier.height(24.dp))

        // 3-Stat Summary Row (Interactive)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatColumn(
                    number = "12",
                    label = "Weeks",
                    modifier = Modifier.m3BounceClick { showStatDetailDialog = "Weeks" }
                )
                HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFE2EBE6))
                ProfileStatColumn(
                    number = "48.2k",
                    label = "Total Steps",
                    modifier = Modifier.m3BounceClick { showStatDetailDialog = "Total Steps" }
                )
                HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFE2EBE6))
                ProfileStatColumn(
                    number = "320",
                    label = "Achievements",
                    modifier = Modifier.m3BounceClick { showStatDetailDialog = "Achievements" }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Item List (All Interactive)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SettingsListItem(
                    icon = Icons.Outlined.EmojiEvents,
                    title = "Achievements",
                    onClick = { activeSheet = "Achievements" }
                )
                HorizontalDivider(color = Color(0xFFF0F5F2))
                SettingsListItem(
                    icon = Icons.Outlined.BarChart,
                    title = "My Progress",
                    onClick = { activeSheet = "My Progress" }
                )
                HorizontalDivider(color = Color(0xFFF0F5F2))
                SettingsListItem(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "Health Profile",
                    onClick = { activeSheet = "Health Profile" }
                )
                HorizontalDivider(color = Color(0xFFF0F5F2))
                SettingsListItem(
                    icon = Icons.Outlined.Settings,
                    title = "App Settings",
                    onClick = { activeSheet = "App Settings" }
                )
                HorizontalDivider(color = Color(0xFFF0F5F2))
                SettingsListItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help & Support",
                    onClick = { activeSheet = "Help & Support" }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Log Out Pill Button
        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCF4E9), contentColor = Color(0xFF005A43)),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .m3BounceClick { showLogoutDialog = true }
        ) {
            Text(text = "Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(130.dp))
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempEmail by remember { mutableStateOf(userEmail) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        userName = tempName
                        userEmail = tempEmail
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B867B))
                }
            },
            title = {
                Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // 3-Stat Detail Dialog
    showStatDetailDialog?.let { statName ->
        AlertDialog(
            onDismissRequest = { showStatDetailDialog = null },
            confirmButton = {
                TextButton(onClick = { showStatDetailDialog = null }) {
                    Text("Close", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("$statName Milestone", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (statName) {
                        "Weeks" -> {
                            Text("• Total Active Weeks: 12 weeks", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Joined: August 2026", color = Color(0xFF6B867B), fontSize = 13.sp)
                            Text("• Weekly Goal Consistency: 88%", color = Color(0xFF005A43), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        "Total Steps" -> {
                            Text("• Lifetime Steps: 48,230 steps", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Total Distance: 35.7 km", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Next Rank: 50k Silver Walker (1,770 steps left)", color = Color(0xFF005A43), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        "Achievements" -> {
                            Text("• Total Badges & Medals: 320", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Latest Badge: 3-Day Walking Streak", color = Color(0xFF005A43), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("• Top 5% of active community walkers", color = Color(0xFF6B867B), fontSize = 13.sp)
                        }
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Dedicated Bottom Sheets for Settings Items
    activeSheet?.let { sheetType ->
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sheetType,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2E26)
                    )
                    IconButton(onClick = { activeSheet = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF6B867B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (sheetType) {
                    "Achievements" -> {
                        AchievementBadgeItem("🏆", "3-Day Walking Streak", "Completed yesterday • Unlocked")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F5F2))
                        AchievementBadgeItem("⚡", "10,000 Step Master", "Hit 10k steps in a single calendar day")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F5F2))
                        AchievementBadgeItem("🌅", "Early Bird Walker", "Logged 2,000 steps before 08:00 AM")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F5F2))
                        AchievementBadgeItem("🏃", "Half Marathon Equivalent", "Cumulative 21 km walked in a week")
                    }
                    "My Progress" -> {
                        Text("• Daily Average: 7,842 steps", fontSize = 14.sp, color = Color(0xFF1A2E26))
                        Text("• Weekly Average: 48,230 steps", fontSize = 14.sp, color = Color(0xFF1A2E26))
                        Text("• Monthly Goal Reached: 26 out of 30 days", fontSize = 14.sp, color = Color(0xFF005A43), fontWeight = FontWeight.SemiBold)
                        Text("• Best Performance: 14,210 steps on Saturday", fontSize = 14.sp, color = Color(0xFF6B867B))
                    }
                    "Health Profile" -> {
                        Text("• Height: 178 cm (5'10\")", fontSize = 14.sp, color = Color(0xFF1A2E26))
                        Text("• Weight: 72 kg", fontSize = 14.sp, color = Color(0xFF1A2E26))
                        Text("• Calibrated Stride Length: 0.74 meters", fontSize = 14.sp, color = Color(0xFF005A43), fontWeight = FontWeight.SemiBold)
                        Text("• Daily Caloric Expenditure Baseline: 1,850 kcal", fontSize = 14.sp, color = Color(0xFF6B867B))
                    }
                    "App Settings" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Background Step Tracking", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A2E26))
                                Text("Samsung Sensor Hub continuous counting", fontSize = 12.sp, color = Color(0xFF6B867B))
                            }
                            Switch(
                                checked = backgroundTrackingEnabled,
                                onCheckedChange = { backgroundTrackingEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF005A43))
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F5F2))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Goal Milestone Notifications", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A2E26))
                                Text("Notify when reaching 50%, 80%, and 100%", fontSize = 12.sp, color = Color(0xFF6B867B))
                            }
                            Switch(
                                checked = dailyGoalNotifications,
                                onCheckedChange = { dailyGoalNotifications = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF005A43))
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F5F2))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("High Precision Calibration", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A2E26))
                                Text("Adaptive step filtering for false positives", fontSize = 12.sp, color = Color(0xFF6B867B))
                            }
                            Switch(
                                checked = highAccuracySensors,
                                onCheckedChange = { highAccuracySensors = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF005A43))
                            )
                        }
                    }
                    "Help & Support" -> {
                        Text("Frequently Asked Questions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Q: How does FitMax count steps without GPS?\nFitMax utilizes your phone's hardware step sensor hub and real-time accelerometer vector analysis. Zero battery drain from GPS.", fontSize = 13.sp, color = Color(0xFF6B867B))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Q: What if the count pauses when screen turns off?\nEnsure FitMax is excluded from Samsung Battery Optimization (Unrestricted mode).", fontSize = 13.sp, color = Color(0xFF6B867B))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Log Out Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        Toast.makeText(context, "Logged out of FitMax session", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B867B))
                }
            },
            title = {
                Text("Log Out of FitMax?", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Text(
                    "Your step history is safely cached locally on your Samsung Galaxy device. You can log in anytime.",
                    color = Color(0xFF6B867B),
                    fontSize = 14.sp
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ProfileStatColumn(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = number, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF6B867B))
    }
}

@Composable
fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .m3BounceClick(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF1A2E26), modifier = Modifier.size(20.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A2E26))
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFA0B5AC), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AchievementBadgeItem(emoji: String, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFE8F6EF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF6B867B))
        }
    }
}
