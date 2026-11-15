package com.fixmax.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val teal = Color(0xFF007467)
private val card = Color(0xFFEEF5F2)
private val ink = Color(0xFF202825)

@Composable
fun IntroScreen(onFinished: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxSize().background(Color(0xFFF5FBF8)).statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth().height(38.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("FitAir", color = teal, fontSize = 15.sp)
            if (page < 2) TextButton(onClick = onFinished, contentPadding = PaddingValues(0.dp)) { Text("Skip", color = teal, fontSize = 14.sp) }
        }

        when (page) {
            0 -> WelcomePage(Modifier.weight(1f))
            1 -> PrivacyPage(Modifier.weight(1f))
            2 -> ConnectPage(Modifier.weight(1f))
            else -> HeartRatePage(Modifier.weight(1f))
        }

        PageDots(page)
        Spacer(Modifier.height(16.dp))
        when (page) {
            0, 1 -> Button(onClick = { page++ }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = teal)) {
                Text("Continue", fontSize = 14.sp, fontWeight = FontWeight.Normal)
            }
            2 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { page = 3 }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = teal)) {
                    Icon(Icons.Default.HealthAndSafety, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(8.dp)); Text("Connect")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { page = 3 }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) {
                    FlaskIcon(); Spacer(Modifier.width(8.dp)); Text("Try with demo data", color = teal)
                }
                TextButton(onClick = onFinished) { Text("Not now", color = teal) }
            }
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onFinished, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = teal)) {
                    Text("↗  Open Google Health")
                }
                TextButton(onClick = onFinished) { Text("Not now", color = teal) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ColumnScope.WelcomePage(modifier: Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1.2f))
        RingArtwork(Modifier.size(260.dp), 0)
        Spacer(Modifier.height(22.dp))
        Text("Welcome to FitAir", color = teal, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text("Know how ready you are", color = ink, fontSize = 27.sp, lineHeight = 32.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text("Your Fitbit Air data becomes three simple scores each morning: Recovery, Sleep and Strain.", color = TextSecondary, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(.8f))
    }
}

@Composable
private fun ColumnScope.PrivacyPage(modifier: Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        RingArtwork(Modifier.size(220.dp), 1)
        Spacer(Modifier.height(18.dp))
        Text("Private by design", color = teal, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text("Your data stays yours", color = ink, fontSize = 27.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        InfoCard(Icons.Default.WifiOff, Color(0xFFE2E2FF), Color(0xFF30346D), "No sign-up", "No account, no email, no password. Just open the app.")
        Spacer(Modifier.height(8.dp))
        InfoCard(Icons.Default.PhoneAndroid, Color(0xFFB5F38E), Color(0xFF174321), "Stays on this phone", "Every score is worked out and stored on your device, and deleted when you uninstall FitAir.")
        Spacer(Modifier.height(8.dp))
        InfoCard(Icons.Default.WifiOff, Color(0xFFFFE0CF), Color(0xFF482715), "Never shared", "FitAir has no internet access, so nothing can leave your phone.")
    }
}

@Composable
private fun InfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, iconColor: Color, title: String, body: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(card).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = ink, fontSize = 15.sp, lineHeight = 19.sp)
            Text(body, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun ColumnScope.ConnectPage(modifier: Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        RingArtwork(Modifier.size(260.dp), 2)
        Spacer(Modifier.height(16.dp))
        Text("Connect your data", color = teal, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text("Connect Health Connect", color = ink, fontSize = 27.sp, lineHeight = 32.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("FitAir reads the sleep, heart and activity data your Fitbit Air saves there, and saves workouts you record in FitAir back to it. You choose exactly what it can see.", color = TextSecondary, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ColumnScope.HeartRatePage(modifier: Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        RingArtwork(Modifier.size(260.dp), 3)
        Spacer(Modifier.height(12.dp))
        Text("Optional", color = teal, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text("See your heart rate live", color = ink, fontSize = 27.sp, lineHeight = 32.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text("Health Connect updates about once an hour. Your band can also send your heart rate to FitAir every second.", color = TextSecondary, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        StatusCard(true, "Bluetooth on and allowed", "")
        Spacer(Modifier.height(8.dp))
        StatusCard(false, "Fitbit Air paired", "Set up in the Google Health app")
    }
}

@Composable
private fun StatusCard(done: Boolean, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(if (done) card else Color(0xFFE5EBE8)).padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(if (done) teal else Color(0xFFFFD9E1)), contentAlignment = Alignment.Center) {
            Text(if (done) "✓" else "2", color = if (done) Color.White else Color(0xFF794454), fontSize = 14.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = ink, fontSize = 14.sp)
            if (subtitle.isNotEmpty()) Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PageDots(page: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { i -> Box(Modifier.width(if (i == page) 24.dp else 8.dp).height(8.dp).clip(CircleShape).background(if (i == page) teal else Color(0xFFBCCBC5))) }
    }
}

@Composable
private fun FlaskIcon() {
    Canvas(Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * .34f, h * .08f)
            lineTo(w * .66f, h * .08f)
            lineTo(w * .62f, h * .19f)
            lineTo(w * .57f, h * .19f)
            lineTo(w * .57f, h * .48f)
            lineTo(w * .88f, h * .84f)
            lineTo(w * .86f, h * .94f)
            lineTo(w * .76f, h * .95f)
            lineTo(w * .22f, h * .95f)
            lineTo(w * .14f, h * .94f)
            lineTo(w * .12f, h * .84f)
            lineTo(w * .43f, h * .48f)
            lineTo(w * .43f, h * .19f)
            lineTo(w * .38f, h * .19f)
            close()
        }
        drawPath(path, teal)
    }
}

@Composable
private fun RingArtwork(modifier: Modifier, style: Int) {
    val colors = when (style) {
        0 -> listOf(Color(0xFFB6F08B), Color(0xFFFFD9C2), Color(0xFFDCE2FF), Color(0xFF397332))
        1 -> listOf(Color(0xFF9EEEDB), Color(0xFFE0EFEB), Color(0xFFD1E8E3), Color(0xFF72AAA0))
        2 -> listOf(Color(0xFFC7E4FF), Color(0xFFD7EBEE), Color(0xFFCDE3E8), Color(0xFF83B3BC))
        else -> listOf(Color(0xFFFFD9E2), Color(0xFFF2E5E6), Color(0xFFEADCDC), Color(0xFFEADCDC))
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val c = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
            if (style == 0) {
                listOf(55f, 68f, 81f, 94f).forEachIndexed { i, r ->
                    drawArc(colors[i], -225f, 295f, false, androidx.compose.ui.geometry.Offset(c.x-r.dp.toPx(), c.y-r.dp.toPx()), androidx.compose.ui.geometry.Size(2*r.dp.toPx(), 2*r.dp.toPx()), style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round))
                }
            } else {
                for (layer in 7 downTo 1) {
                    val path = Path()
                    val radius = (36 + layer * 7).dp.toPx()
                    repeat(121) { step ->
                        val angle = step * 2 * PI / 120
                        val organic = radius * (1f + if (style == 2) 0f else .065f * cos(10 * angle).toFloat())
                        val x = c.x + organic * cos(angle).toFloat()
                        val y = c.y + organic * sin(angle).toFloat()
                        if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, colors[if (style == 3) 3 else layer % colors.size].copy(alpha = if (layer == 1) .8f else .2f), style = Stroke(width = if (layer == 1) 1.8.dp.toPx() else 1.2.dp.toPx()))
                }
                if (style == 2) drawCircle(colors[0], radius = 58.dp.toPx(), center = c)
                else {
                    val centerShape = Path()
                    repeat(121) { step ->
                        val angle = step * 2 * PI / 120
                        val radius = 55.dp.toPx() * (1f + .065f * cos(10 * angle).toFloat())
                        val x = c.x + radius * cos(angle).toFloat()
                        val y = c.y + radius * sin(angle).toFloat()
                        if (step == 0) centerShape.moveTo(x, y) else centerShape.lineTo(x, y)
                    }
                    centerShape.close()
                    drawPath(centerShape, colors[0])
                    if (style == 1) {
                        drawAccent(Offset(c.x - 82.dp.toPx(), c.y - 58.dp.toPx()), 11.dp.toPx(), Color(0xFFDCD9FF))
                        drawAccent(Offset(c.x + 82.dp.toPx(), c.y - 35.dp.toPx()), 9.dp.toPx(), Color(0xFFFFD9C2))
                        drawCircle(Color(0xFFFFD7E0), 8.dp.toPx(), Offset(c.x - 78.dp.toPx(), c.y + 68.dp.toPx()))
                        drawAccent(Offset(c.x + 75.dp.toPx(), c.y + 78.dp.toPx()), 12.dp.toPx(), Color(0xFFB6F08B))
                    }
                }
            }
        }
        if (style != 0) {
            val symbol = when (style) { 1 -> Icons.Default.Lock; 2 -> Icons.Default.HealthAndSafety; else -> Icons.Default.Favorite }
            Icon(symbol, null, tint = if (style == 3) Color(0xFF59001F) else Color(0xFF003C37), modifier = Modifier.size(if (style == 2) 56.dp else 54.dp))
        }
    }
}

private fun DrawScope.drawAccent(center: Offset, radius: Float, color: Color) {
    val path = Path()
    repeat(12) { index ->
        val angle = index * 2 * PI / 12 - PI / 2
        val r = radius * if (index % 3 == 0) 1f else .72f
        val point = Offset(center.x + r * cos(angle).toFloat(), center.y + r * sin(angle).toFloat())
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    drawPath(path, color)
}
