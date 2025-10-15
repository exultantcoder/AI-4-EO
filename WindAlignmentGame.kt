package com.google.ai.edge.gallery.customtasks.interactivelearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

class WindEnergyGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WindAlignmentGame()
            }
        }
    }
}

data class WindGameLevel(
    val windDirectionStart: Float,  // degrees start of wind direction arc
    val windDirectionEnd: Float,    // degrees end of wind direction arc
    val windSpeed: Float,            // m/s (affects difficulty)
    val targetEfficiency: Float      // % needed to pass
)

class WindGameState {
    var currentLevel by mutableStateOf(1)
    var turbineAngle by mutableStateOf(0f) // degrees 0-360
    var windDirection by mutableStateOf(0f)
    var efficiency by mutableStateOf(0f)
    var score by mutableStateOf(0)
    var levelComplete by mutableStateOf(false)
    var gameRunning by mutableStateOf(false)
    var timeRemaining by mutableStateOf(30f) // seconds per level

    val levels = listOf(
        WindGameLevel(45f, 90f, 5f, 60f),
        WindGameLevel(30f, 100f, 6f, 65f),
        WindGameLevel(0f, 180f, 7f, 70f),
        WindGameLevel(270f, 360f, 8f, 75f),
        WindGameLevel(350f, 10f, 9f, 80f)
    )

    fun currentLevelData() = levels.getOrElse(currentLevel - 1) { levels.last() }

    fun calculateEfficiency(): Float {
        // Efficiency based on how close turbine is aligned with wind direction avg
        val avgWindDir = (currentLevelData().windDirectionStart + currentLevelData().windDirectionEnd) / 2f
        val diff = min(abs(turbineAngle - avgWindDir), 360 - abs(turbineAngle - avgWindDir))
        val maxDiff = 90f
        return max(0f, 100f - (diff / maxDiff) * 100f)
    }

    fun nextLevel() {
        if (currentLevel < levels.size) {
            currentLevel += 1
            resetLevel()
        }
    }

    fun resetLevel() {
        turbineAngle = 0f
        windDirection = currentLevelData().windDirectionStart
        efficiency = 0f
        levelComplete = false
        gameRunning = false
        timeRemaining = 30f
    }
}

@Composable
fun WindAlignmentGame() {
    val state = remember { WindGameState() }
    val level = state.currentLevelData()

    LaunchedEffect(state.gameRunning) {
        if (state.gameRunning) {
            while(state.timeRemaining > 0f && !state.levelComplete) {
                delay(100)
                state.timeRemaining -= 0.1f
                state.efficiency = state.calculateEfficiency()
                // End level if efficiency target met
                if(state.efficiency >= level.targetEfficiency) {
                    state.levelComplete = true
                    state.score += (state.timeRemaining * 10).toInt()
                }
            }
        }
    }

    val windAngleAnimated by animateFloatAsState(
        targetValue = if(state.gameRunning) level.windDirectionEnd else level.windDirectionStart,
        animationSpec = tween(durationMillis = 30000)
    )
    state.windDirection = windAngleAnimated

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFD0E8F2))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Wind Alignment Game",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF0D47A1),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text("Level: ${state.currentLevel}", fontWeight = FontWeight.SemiBold)
        Text("Score: ${state.score}")
        Text(String.format("Time: %.1f sec", state.timeRemaining))
        Spacer(Modifier.height(16.dp))

        // Main game canvas
        GameCanvas(
            turbineAngle = state.turbineAngle,
            windDirection = state.windDirection,
            onAngleChange = { newAngle ->
                if(!state.levelComplete && state.gameRunning) {
                    state.turbineAngle = (newAngle + 360) % 360
                }
            }
        )

        Spacer(Modifier.height(12.dp))
        Text(
            "Efficiency: ${String.format("%.1f", state.efficiency)}%",
            color = if(state.efficiency >= level.targetEfficiency) Color(0xFF388E3C) else Color(0xFFD32F2F),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text("Target Efficiency: ${level.targetEfficiency}%")

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if(!state.gameRunning) Button(onClick = { state.gameRunning = true }) { Text("Start") }
            if(state.gameRunning) Button(onClick = { state.gameRunning = false }) { Text("Pause") }
            if(state.levelComplete) Button(onClick = { state.nextLevel() }) { Text("Next Level") }
            Button(onClick = { state.resetLevel() }) { Text("Reset") }
        }
    }
}

@Composable
fun GameCanvas(turbineAngle: Float, windDirection: Float, onAngleChange: (Float) -> Unit) {
    val size = 300.dp
    Canvas(modifier = Modifier
        .size(size)
        .clip(RoundedCornerShape(16.dp))
        .background(Color.White)
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val center = size.toPx() / 2f
                val pos = change.position
                val vector = Offset(pos.x - center, pos.y - center)
                val angle = (atan2(vector.y, vector.x) * 180f / PI + 90).toFloat()
                onAngleChange(angle)
            }
        }
    ) {
        val center = size.toPx() / 2f
        val radius = center - 40f

        // Draw circle representing the wind turbine base
        drawCircle(color = Color.LightGray, radius = radius, center = Offset(center, center))

        // Draw wind direction arrow
        rotate(degrees = windDirection) {
            drawLine(
                color = Color.Blue,
                start = Offset(center, center),
                end = Offset(center, center - radius),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        // Draw turbine blade (rotatable)
        rotate(degrees = turbineAngle) {
            drawLine(
                color = Color.Green,
                start = Offset(center, center),
                end = Offset(center, center - radius / 2),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.Green,
                start = Offset(center, center),
                end = Offset(center - radius / 4, center + radius / 4),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.Green,
                start = Offset(center, center),
                end = Offset(center + radius / 4, center + radius / 4),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }
    }
}
