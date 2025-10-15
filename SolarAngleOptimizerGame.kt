package com.google.ai.edge.gallery.customtasks.interactivelearning



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

// ===== MAIN ACTIVITY =====
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SolarAngleOptimizerGame()
            }
        }
    }
}

// ===== GAME DATA =====
data class Level(
    val number: Int,
    val sunStartAngle: Float,   // 0-360 degrees
    val sunEndAngle: Float,     // Sun movement
    val season: String,         // Affects optimal angle
    val obstacles: List<Obstacle> = emptyList(),
    val targetEfficiency: Float = 70f
)

data class Obstacle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: String
)

// ===== GAME STATE =====
class GameState {
    var currentLevel by mutableStateOf(1)
    var panelAngle by mutableStateOf(45f)  // Solar panel angle
    var sunAngle by mutableStateOf(90f)    // Current sun position
    var efficiency by mutableStateOf(0f)   // Current efficiency %
    var score by mutableStateOf(0)
    var levelComplete by mutableStateOf(false)
    var gameStarted by mutableStateOf(false)
    var timeRemaining by mutableStateOf(30f) // seconds per level

    val levels = listOf(
        Level(1, 60f, 120f, "Summer", targetEfficiency = 60f),
        Level(2, 45f, 135f, "Summer", targetEfficiency = 65f),
        Level(3, 30f, 150f, "Spring", targetEfficiency = 70f),
        Level(4, 20f, 160f, "Winter", targetEfficiency = 70f),
        Level(5, 15f, 165f, "Winter", targetEfficiency = 75f),
    )

    fun getCurrentLevel(): Level {
        return levels.getOrNull(currentLevel - 1) ?: levels.last()
    }

    fun calculateEfficiency(): Float {
        val level = getCurrentLevel()
        val optimalAngle = (level.sunStartAngle + level.sunEndAngle) / 2f
        val angleDiff = abs(panelAngle - optimalAngle)
        val maxDiff = 90f
        return maxOf(0f, 100f - (angleDiff / maxDiff * 100f))
    }

    fun nextLevel() {
        if (currentLevel < levels.size) {
            currentLevel++
            panelAngle = 45f
            levelComplete = false
            timeRemaining = 30f
            gameStarted = false
        }
    }

    fun resetLevel() {
        panelAngle = 45f
        levelComplete = false
        timeRemaining = 30f
        gameStarted = false
    }
}

// ===== MAIN GAME COMPOSABLE =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolarAngleOptimizerGame() {
    val gameState = remember { GameState() }
    val level = gameState.getCurrentLevel()

    // Game timer
    LaunchedEffect(gameState.gameStarted, gameState.currentLevel) {
        if (gameState.gameStarted) {
            while (gameState.timeRemaining > 0 && !gameState.levelComplete) {
                delay(100)
                gameState.timeRemaining -= 0.1f
                gameState.efficiency = gameState.calculateEfficiency()

                // Check win condition
                if (gameState.efficiency >= level.targetEfficiency) {
                    gameState.levelComplete = true
                    gameState.score += (gameState.timeRemaining * 10).toInt()
                }
            }
        }
    }

    // Sun animation
    val sunPosition by animateFloatAsState(
        targetValue = if (gameState.gameStarted) level.sunEndAngle else level.sunStartAngle,
        animationSpec = tween(durationMillis = if (gameState.gameStarted) 30000 else 0),
        label = "sun_animation"
    )

    gameState.sunAngle = sunPosition

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB)) // Sky blue
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌞 Solar Angle Optimizer",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Level: ${gameState.currentLevel}")
                    Text("Score: ${gameState.score}")
                    Text("Time: ${String.format("%.1f", gameState.timeRemaining)}s")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            GameCanvas(
                gameState = gameState,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Efficiency: ${String.format("%.1f", gameState.efficiency)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gameState.efficiency >= level.targetEfficiency) Color.Green else Color.Red
                )

                LinearProgressIndicator(
                    progress = gameState.efficiency / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = if (gameState.efficiency >= level.targetEfficiency) Color.Green else Color.Red
                )

                Text(
                    text = "Target: ${level.targetEfficiency}% | Season: ${level.season}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Panel Angle: ${String.format("%.1f", gameState.panelAngle)}°",
                    fontSize = 14.sp,
                    color = Color.Blue
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!gameState.gameStarted && !gameState.levelComplete) {
                Button(
                    onClick = { gameState.gameStarted = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("▶ Start")
                }
            }

            if (gameState.levelComplete) {
                Button(
                    onClick = { gameState.nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Next Level →")
                }
            }

            Button(
                onClick = { gameState.resetLevel() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E))
            ) {
                Text("🔄 Reset")
            }
        }

        // Win/Lose Message
        if (gameState.levelComplete) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(
                    text = "🎉 Level Complete! Bonus: +${(gameState.timeRemaining * 10).toInt()}",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (gameState.timeRemaining <= 0 && gameState.gameStarted) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336))
            ) {
                Text(
                    text = "⏰ Time's Up! Try Again!",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ===== GAME CANVAS =====
@Composable
fun GameCanvas(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, _ ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val vector = change.position - center
                val angle = (atan2(vector.y, vector.x) * 180f / PI + 90f).toFloat()
                gameState.panelAngle = ((angle % 360f + 360f) % 360f)
            }
        }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val groundY = size.height * 0.8f

        // Draw ground
        drawRect(
            color = Color(0xFF8BC34A),
            topLeft = Offset(0f, groundY),
            size = androidx.compose.ui.geometry.Size(size.width, size.height - groundY)
        )

        // Draw sun
        val sunRadius = 30f
        val sunDistance = 120f
        val sunRadians = (gameState.sunAngle - 90f) * PI / 180f
        val sunX = centerX + cos(sunRadians).toFloat() * sunDistance
        val sunY = groundY - 50f + sin(sunRadians).toFloat() * sunDistance

        drawCircle(
            color = Color(0xFFFFEB3B),
            radius = sunRadius,
            center = Offset(sunX, sunY)
        )

        // Draw sun rays
        for (i in 0 until 8) {
            val rayAngle = i * 45f
            val rayStart = Offset(
                sunX + cos(rayAngle * PI / 180).toFloat() * (sunRadius + 5),
                sunY + sin(rayAngle * PI / 180).toFloat() * (sunRadius + 5)
            )
            val rayEnd = Offset(
                sunX + cos(rayAngle * PI / 180).toFloat() * (sunRadius + 15),
                sunY + sin(rayAngle * PI / 180).toFloat() * (sunRadius + 15)
            )
            drawLine(
                color = Color(0xFFFFEB3B),
                start = rayStart,
                end = rayEnd,
                strokeWidth = 3f
            )
        }

        // Draw solar panel base
        drawRect(
            color = Color(0xFF424242),
            topLeft = Offset(centerX - 5f, groundY - 20f),
            size = androidx.compose.ui.geometry.Size(10f, 20f)
        )

        // Draw solar panel (rotatable)
        rotate(gameState.panelAngle, Offset(centerX, groundY - 20f)) {
            drawRect(
                color = Color(0xFF1976D2),
                topLeft = Offset(centerX - 40f, groundY - 25f),
                size = androidx.compose.ui.geometry.Size(80f, 10f)
            )

            // Panel grid lines
            for (i in 1..3) {
                drawLine(
                    color = Color.White,
                    start = Offset(centerX - 40f + i * 20f, groundY - 25f),
                    end = Offset(centerX - 40f + i * 20f, groundY - 15f),
                    strokeWidth = 1f
                )
            }
        }

        // Draw efficiency visualization (energy beam)
        if (gameState.gameStarted && gameState.efficiency > 20f) {
            val beamAlpha = (gameState.efficiency / 100f).coerceIn(0f, 1f)
            val beamColor = Color(0xFFFFEB3B).copy(alpha = beamAlpha * 0.7f)

            val panelCenterX = centerX + cos((gameState.panelAngle + 90f) * PI / 180).toFloat() * 5f
            val panelCenterY = groundY - 20f + sin((gameState.panelAngle + 90f) * PI / 180).toFloat() * 5f

            drawLine(
                color = beamColor,
                start = Offset(sunX, sunY),
                end = Offset(panelCenterX, panelCenterY),
                strokeWidth = 8f * beamAlpha
            )
        }
    }
}
